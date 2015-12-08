import java.io.File;
import java.io.IOException;
import cicontest.algorithm.abstracts.AbstractAlgorithm;
import cicontest.algorithm.abstracts.AbstractRace;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.controller.Driver;
import cicontest.torcs.client.Controller;
import cicontest.torcs.race.Race;
import storage.DataRecorder;
import race.TorcsConfiguration;

public class DefaultDriverAlgorithm extends AbstractAlgorithm {

    private static final long serialVersionUID = 654963126362653L;

    DefaultDriverGenome[] drivers = new DefaultDriverGenome[1];
    int [] results = new int[1];

    public Class<? extends Driver> getDriverClass(){
        return DefaultDriver.class;
    }

    public void run(boolean continue_from_checkpoint) {
        if(!continue_from_checkpoint){
            DataRecorder dataRecorder = null;
            if (Configuration.RECORD_DATA) {
                try {
                    dataRecorder = new DataRecorder(Configuration.OUTPUT_FILE);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

            DefaultDriverGenome genome = new  DefaultDriverGenome();
            genome.setDataRecorder(dataRecorder);
            drivers[0] = genome;

            // save genome/nn
            System.out.println("Storing genome");
            DriversUtils.storeGenome(drivers[0]);

            // start a race
            Race race = new Race();
            race.setTrack("road", "aalborg");
            race.setTermination(Race.Termination.LAPS, 1);
            race.setStage(Controller.Stage.RACE);

            for (int i = 0; i < 3; ++i) {
                DefaultDriver driver = new DefaultDriver();
                driver.loadGenome(genome);
                race.addCompetitor(driver);
            }

            // for speedup set withGUI to false
            race.runWithGUI();
            //race.run();

            // close the data recorder
            if (dataRecorder != null) {
                try {
                    dataRecorder.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
            // create a checkpoint this allows you to continue this run later
            DriversUtils.createCheckpoint(this);
            // driversUtils.clearCheckpoint();
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
        DefaultDriverAlgorithm algorithm = new DefaultDriverAlgorithm();
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

}
