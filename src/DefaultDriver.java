import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.torcs.client.Action;
import cicontest.torcs.client.SensorModel;
import cicontest.torcs.genome.IGenome;

public class DefaultDriver extends AbstractDriver {

    public void loadGenome(IGenome genome) {
        if (genome instanceof DefaultDriverGenome) {
            DefaultDriverGenome MyGenome = (DefaultDriverGenome) genome;
        } else {
            System.err.println("Invalid Genome assigned");
        }
    }

    public double getAcceleration(SensorModel sensors) {
        return 1;
    }

    public double getSteering(SensorModel sensors){
        return 0;
    }

    public String getDriverName() {
        return "driver";
    }

    /*
     * The following methods are only here as a reminder that you can,
     * and may change all driver methods, including the already implemented
     * ones, such as those beneath.
     */

    public void controlQualification(Action action, SensorModel sensors) {
           super.controlQualification(action, sensors);
    }

    public void controlRace(Action action, SensorModel sensors) {
            super.controlRace(action, sensors);
    }

    public void defaultControl(Action action, SensorModel sensors){
            super.defaultControl(action, sensors);
    }
}