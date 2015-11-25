import java.io.File;
import java.io.IOException;

import cicontest.algorithm.abstracts.AbstractAlgorithm;
import cicontest.algorithm.abstracts.AbstractRace;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.controller.Driver;
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

            // create data recorder
            DataRecorder dataRecorder = null;
            if (Configuration.RECORD_DATA) {
                try {
                    dataRecorder = new DataRecorder(Configuration.OUTPUT_FILE);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

            // init NN
            DefaultDriverGenome genome = new  DefaultDriverGenome(dataRecorder);
            drivers[0] = genome;

            // start a race
            DefaultRace race = new DefaultRace();
            race.setTrack( AbstractRace.DefaultTracks.getTrack(0));
            race.laps = 1;

            // for speedup set withGUI to false
            results = race.runRace(drivers, false);

            // close the data recorder
            if (dataRecorder != null) {
                try {
                    dataRecorder.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // save genome/nn
            DriversUtils.storeGenome(drivers[0]);
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