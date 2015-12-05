import cicontest.algorithm.abstracts.AbstractAlgorithm;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.client.Controller;
import cicontest.torcs.controller.Driver;
import cicontest.torcs.race.Race;
import cicontest.torcs.race.RaceResult;
import cicontest.torcs.race.RaceResults;
import org.neuroph.contrib.neat.gen.*;
import org.neuroph.contrib.neat.gen.impl.SimpleNeatParameters;
import org.neuroph.contrib.neat.gen.operations.FitnessFunction;
import org.neuroph.contrib.neat.gen.operations.OrganismFitnessScore;
import org.neuroph.contrib.neat.gen.persistence.PersistenceException;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.MultiLayerPerceptron;
import race.TorcsConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sander on 04/12/15.
 */
public class EvolutionaryDriverAlgorithm extends AbstractAlgorithm {
    private static final long serialVersionUID = 654963126362653L;

    private static final boolean EVOLVE_DRIVER = true;

    private int mEvolutionStep = 0;

    public Class<? extends Driver> getDriverClass(){
        return DefaultDriver.class;
    }

    public EvolutionaryDriverAlgorithm() {
    }

    public void run(boolean continue_from_checkpoint) {
        if(!continue_from_checkpoint){
            if (EVOLVE_DRIVER) {
                runEvolution();
            } else {
               runNormalRace(Configuration.NEUROPH_TRAINED_FILE);
               //runNormalRace(Configuration.NEUROPH_EVOLVED_FILE);
            }
        }
    }

    private void runNormalRace(String nnFile) {

        // start a race
        Race race = new Race();
        race.setTrack("road", "aalborg");
        race.setTermination(Race.Termination.LAPS, 1);
        race.setStage(Controller.Stage.RACE);

        NeuralNetwork network = NeuralNetwork.load(nnFile);
        NeuralNetworkController nn = new EvolvedController(network);
        DefaultDriverGenome genome = new DefaultDriverGenome(nn);

        DefaultDriver driver = new DefaultDriver();
        driver.loadGenome(genome);
        race.addCompetitor(driver);

        race.runWithGUI();
    }

    private void runEvolution() {
        System.out.println("Initializing evolver...");
        SimpleNeatParameters params = new SimpleNeatParameters();
        params.setFitnessFunction(new DriverFitnessFunction());
        params.setPopulationSize(2);
        //params.setMaximumFitness(10);
        params.setMaximumGenerations(10);
        Evolver e = createEvolver(params);

        try {
            // Evolve the network
            System.out.println("Starting evolution...");
            Organism best = e.evolve();

            // Get the neural network of the best individual
            NeuralNetwork nn = params.getNeuralNetworkBuilder().createNeuralNetwork(best);
            // Store evolved NN
            nn.save(Configuration.NEUROPH_EVOLVED_FILE);

        } catch (PersistenceException e1) {
            e1.printStackTrace();
        }
    }


    private Evolver createEvolver(NeatParameters params) {
        /* Setup initial network layout */
        ArrayList<NeuronGene> inputs = new ArrayList<>(21);
        ArrayList<NeuronGene> outputs = new ArrayList<>(3);
        for (int i = 0; i < 21; i++) {
            inputs.add(new NeuronGene(NeuronType.INPUT, params));
        }
        for (int i = 0; i < 3; i++) {
            outputs.add(new NeuronGene(NeuronType.OUTPUT, params));
        }

        return Evolver.createNew(params, inputs, outputs);
    }

    public static void main(String[] args) {

        //Set path to torcs.properties
        TorcsConfiguration.getInstance().initialize(new File(Configuration.PROP_FILE));

        /*
         *
         * Start without arguments to run the algorithm
         * Start with -continue to continue a previous run
         * Start with -show to show the best found
         * Start with -show-race to show a race with 10 copies of the best found
         * Start with -human to race against the best found
         *
         */
        EvolutionaryDriverAlgorithm algorithm = new EvolutionaryDriverAlgorithm();
        DriversUtils.registerMemory(algorithm.getDriverClass());
        if(args.length > 0 && args[0].equals("-show")){
            new DefaultRace().showBest();
        } else if(args.length > 0 && args[0].equals("-show-race")){
            new DefaultRace().showBestRace();
        } else if(args.length > 0 && args[0].equals("-human")){
            new DefaultRace().raceBest();
        } else if(args.length > 0 && args[0].equals("-continue")){
            if(DriversUtils.hasCheckpoint()){
                DriversUtils.loadCheckpoint().run(true);
            } else {
                algorithm.run();
            }
        } else {
            algorithm.run();
        }

    }

    private class DriverFitnessFunction implements FitnessFunction {
        @Override
        public void evaluate(List<OrganismFitnessScore> list) {
            mEvolutionStep++;

            /* Evaluate performance by racing */
            System.out.println("Evaluate: " + list.size() + " individuals");
            RaceResults raceResults = runRaceWithPopulation(list);
            for (Map.Entry<Driver, RaceResult> res : raceResults.entrySet()) {
                DefaultDriver driver = (DefaultDriver)res.getKey();
                OrganismFitnessScore ofs = list.get(driver.getIndex());
                ofs.setFitness(computeFitness(res));
            }
        }

        private double computeFitness(Map.Entry<Driver, RaceResult> entry) {
            if (entry.getValue().getLaps() == 0) {
                return 0;
            }
            // todo incorporate crashes / damage
            return entry.getValue().getDistance();
        }
    }

    private RaceResults runRaceWithPopulation(List<OrganismFitnessScore> list) {
        Race race = new Race();
        race.setTrack("road", "aalborg");
        race.setTermination(Race.Termination.LAPS, 1);
        race.setStage(Controller.Stage.RACE);

        for(int i = 0; i < list.size(); i++) {
            // Set evolved neural network to racer
            // Add this racer to race

            NeuralNetwork nn = list.get(i).getNeuralNetwork();
            if (mEvolutionStep == 1) {
                // Set initial neural network?

                System.out.println("Setting initial network layer");
                initializeNetwork(nn, Configuration.NEUROPH_TRAINED_FILE);

            }
            NeuralNetworkController networkController = new EvolvedController(nn);
            DefaultDriverGenome genome = new DefaultDriverGenome(networkController);
            DefaultDriver driver = new DefaultDriver();
            driver.setIndex(i);
            driver.loadGenome(genome);
            race.addCompetitor(driver);
        }

        return race.runWithGUI();
    }

    private void initializeNetwork(NeuralNetwork nn, String filename) {
        // Doesn't work?
        NeuralNetwork preTrained = MultiLayerPerceptron.load(filename);
        nn.reset();
        nn.getLayers().clear();

        Iterator<Layer> layers = preTrained.getLayersIterator();
        while (layers.hasNext()) {
            nn.addLayer(layers.next());
        }

    }

}
