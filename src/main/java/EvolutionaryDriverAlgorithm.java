import cicontest.algorithm.abstracts.AbstractAlgorithm;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.client.Controller;
import cicontest.torcs.controller.Driver;
import cicontest.torcs.race.Race;
import cicontest.torcs.race.RaceResult;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
import org.encog.neural.neat.PersistNEATPopulation;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.PersistBasicNetwork;
import org.encog.neural.networks.training.TrainingSetScore;

import race.TorcsConfiguration;
import storage.DataRecorder;
import storage.Normalization;
import storage.TrainingData;

import java.io.*;

/**
 * Created by sander on 04/12/15.
 */
public class EvolutionaryDriverAlgorithm extends AbstractAlgorithm {
    private static final long serialVersionUID = 654963126362653L;
    private static final boolean EVOLVE_DRIVER = false;

    public Class<? extends Driver> getDriverClass(){
        return DefaultDriver.class;
    }

    public EvolutionaryDriverAlgorithm() {
    }

    public void run(boolean continue_from_checkpoint) {
        if(!continue_from_checkpoint){
            if (EVOLVE_DRIVER) {
                try {
                    //BasicMLDataSet data = loadTrainingData();
                    //runEvolution(data, 1000);

                    runEvolution(10);
                    System.out.println("Evolution finished");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Choose evolved or normally trained driver
                //runEvolvedRace(Configuration.ENCOG_EVOLVED_FILE);
                runMLPRace(Configuration.ENCOG_TRAINED_FILE);
            }
        }
    }

    private BasicMLDataSet loadTrainingData() throws Exception {
        TrainingData data = TrainingData.readData(Configuration.CSV_DIRECTORY, INPUTS, OUTPUTS, true);
        if (data == null) {
            throw new Exception("No data read");
        }
        /* Normalize and train on the data */
        Normalization normData = Normalization.createNormalization(data.input, data.target);
        System.out.println("Norm min target: " + normData.targetMin);
        System.out.println("Norm max target: " + normData.targetMax);

        Normalization norm = EvolvedController.createDefaultNormalization();
        norm.normalizeInput(data.input, 0, 1);
        norm.normalizeTarget(data.target, 0, 1);

        return new BasicMLDataSet(data.input.getData(), data.target.getData());
    }

    private void runMLPRace(String mlpFile) {
        PersistBasicNetwork persistence = new PersistBasicNetwork();
        try {
            FileInputStream fis = new FileInputStream(mlpFile);
            BasicNetwork nn = (BasicNetwork)persistence.read(fis);
            runRace(new EvolvedController(nn));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void runEvolvedRace(String nnFile) {
        NEATPopulation population = loadNEAT(nnFile);
        TrainEA trainer = NEATUtil.constructNEATTrainer(population, new DriverFitnessScore());

        NEATNetwork network = (NEATNetwork) trainer.getCODEC().decode(population.getBestGenome());
        NeuralNetworkController nn = new EvolvedController(network);
        DefaultDriverGenome genome = new DefaultDriverGenome(nn);
        DefaultDriver driver = new DefaultDriver();
        driver.loadGenome(genome);

        // start a race
        Race race = new Race();
        race.setTrack("road", "aalborg");
        race.setTermination(Race.Termination.LAPS, 1);
        race.setStage(Controller.Stage.RACE);
        race.addCompetitor(driver);

        race.runWithGUI();
    }

    private void runEvolution(final int iterations) throws IOException {
        runEvolution(null, iterations);
    }

    private void runEvolution(BasicMLDataSet trainingData, final int iterations) throws IOException {
        System.out.println("Initializing evolution...");

        NEATPopulation pop = loadNEAT(Configuration.ENCOG_EVOLVED_FILE);
        if (pop == null) {
            System.out.println("Creating new NEAT population");
            //input count, output count, population size
            pop = new NEATPopulation(INPUTS.length, OUTPUTS.length, 5);
            pop.setInitialConnectionDensity(1.0);// not required, but speeds training
            pop.reset();
        } else {
            System.out.println("NEAT population loaded from file");
        }

        EvolutionaryAlgorithm trainer;
        if (trainingData != null) {
            // Just use training data
            trainer = NEATUtil.constructNEATTrainer(pop, new TrainingSetScore(trainingData));
        } else {
            // Use racing performance
            trainer = NEATUtil.constructNEATTrainer(pop, new DriverFitnessScore());
        }

        // Evolve the network
        for (int i = 0; i < iterations; i++) {
            trainer.iteration();
            System.out.println("Epoch #" + trainer.getIteration()
                     + ", Error:" + trainer.getError()
                     + ", Species:" + pop.getSpecies().size()
                     + ", Pop: " + pop.getPopulationSize());
        }
        System.out.println("Training finished");
        storeNEAT(pop);
    }

    private static NEATPopulation loadNEAT(String file) {
        PersistNEATPopulation persistPop = new PersistNEATPopulation();
        try {
            FileInputStream inputStream = new FileInputStream(file);
            NEATPopulation population = (NEATPopulation)persistPop.read(inputStream);
            inputStream.close();
            return population;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void storeNEAT(NEATPopulation population) {
        PersistNEATPopulation persistPop = new PersistNEATPopulation();
        try {
            System.out.println("Storing NEAT results in " + Configuration.ENCOG_EVOLVED_FILE);
            FileOutputStream fos = new FileOutputStream(Configuration.ENCOG_EVOLVED_FILE);
            persistPop.save(fos, population);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class DriverFitnessScore implements CalculateScore {

        @Override
        public double calculateScore(MLMethod mlMethod) {
            NEATNetwork network = (NEATNetwork)mlMethod;
            RaceResult result = runRace(new EvolvedController(network));
            return computeFitness(result);
        }

        @Override
        public boolean shouldMinimize() {
            return false;
        }

        @Override
        public boolean requireSingleThreaded() {
            return false;
        }
    }

    private static double computeFitness(RaceResult result) {
        if (result.getLaps() == 0) {
            return 0;
        }
        // todo incorporate crashes / damage
        return result.getDistance();
    }

    private RaceResult runRace(EvolvedController controller) {
        Race race = new Race();
        race.setTrack("road", "aalborg");
        race.setTermination(Race.Termination.LAPS, 1);
        race.setStage(Controller.Stage.RACE);

        // Set evolved neural network to racer
        // Add this racer to race
        DefaultDriverGenome genome = new DefaultDriverGenome(controller);
        DefaultDriver driver = new DefaultDriver();
        driver.loadGenome(genome);
        race.addCompetitor(driver);
        System.out.println("Starting race...");
        return race.runWithGUI().get(0);
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

    // The values that we take as input for predictions
    private static final int[] INPUTS = new int[] {
            DataRecorder.SENSOR_SPEED,
            DataRecorder.SENSORS_ANGLE_TO_TRACK_AXIS,
            DataRecorder.SENSOR_TRACK_POSITION,
            DataRecorder.SENSOR_TRACK_EDGE_1,
            DataRecorder.SENSOR_TRACK_EDGE_2,
            DataRecorder.SENSOR_TRACK_EDGE_3,
            DataRecorder.SENSOR_TRACK_EDGE_4,
            DataRecorder.SENSOR_TRACK_EDGE_5,
            DataRecorder.SENSOR_TRACK_EDGE_6,
            DataRecorder.SENSOR_TRACK_EDGE_7,
            DataRecorder.SENSOR_TRACK_EDGE_8,
            DataRecorder.SENSOR_TRACK_EDGE_9,
            DataRecorder.SENSOR_TRACK_EDGE_10,
            DataRecorder.SENSOR_TRACK_EDGE_11,
            DataRecorder.SENSOR_TRACK_EDGE_12,
            DataRecorder.SENSOR_TRACK_EDGE_13,
            DataRecorder.SENSOR_TRACK_EDGE_14,
            DataRecorder.SENSOR_TRACK_EDGE_15,
            DataRecorder.SENSOR_TRACK_EDGE_16,
            DataRecorder.SENSOR_TRACK_EDGE_17,
            DataRecorder.SENSOR_TRACK_EDGE_18,
            DataRecorder.SENSOR_TRACK_EDGE_19,
    };

    // The values that we want to predict
    private static final int[] OUTPUTS = new int[] {
            DataRecorder.ACTION_ACCELERATION,
            DataRecorder.ACTION_STEERING,
    };


}
