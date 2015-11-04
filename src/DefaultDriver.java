import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.torcs.client.Action;
import cicontest.torcs.client.SensorModel;
import cicontest.torcs.genome.IGenome;

public class DefaultDriver extends AbstractDriver {

    private NeuralNetwork MyNN;

    public void loadGenome(IGenome genome) {
        if (genome instanceof DefaultDriverGenome) {
            DefaultDriverGenome MyGenome = (DefaultDriverGenome) genome;
            MyNN = MyGenome.getMyNN();
        } else {
            System.err.println("Invalid Genome assigned");
        }
    }

    public double getAcceleration(SensorModel sensors) {
        double[] sensorArray = new double[4];
        double output = MyNN.getOutput(sensors);
    return 1;
    }

    public double getSteering(SensorModel sensors){
        Double output = MyNN.getOutput(sensors);
        return 0.5;
    }

    public String getDriverName() {
        return "simple example";
    }

    public void controlQualification(Action action, SensorModel sensors) {
        action.clutch = 1;
        action.steering =  Math.random() * (1 - -1)  -1;
        action.accelerate = 1;
        action.brake = 0;
        //super.controlQualification(action, sensors);
    }

    public void controlRace(Action action, SensorModel sensors) {
        action.clutch = 1;
        action.steering =  Math.random() * (1 - -1)  -1;
        action.accelerate = 1;
        action.brake = 0;
        //super.ControlRace(action, sensors);
    }

    public void defaultControl(Action action, SensorModel sensors){
        action.clutch = 1;
        action.steering =  Math.random() * (1 - -1)  -1;
        action.accelerate = 1;
        action.brake = 0;
        //super.defaultControl(action, sensors);
    }
}