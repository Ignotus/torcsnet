import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.torcs.client.Action;
import cicontest.torcs.client.SensorModel;
import cicontest.torcs.genome.IGenome;

public class DefaultDriver extends AbstractDriver {

    private NeuralNetwork MyNN;
    private DataRecorder mDataRecorder;

    public void loadGenome(IGenome genome) {
        if (genome instanceof DefaultDriverGenome) {
            DefaultDriverGenome myGenome = (DefaultDriverGenome) genome;
            MyNN = myGenome.getMyNN();
            mDataRecorder = myGenome.getDataRecorder();
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
        return "Cocaine";
    }

    public void controlQualification(Action action, SensorModel sensors) {
        action.clutch = 1;
        action.steering =  Math.random() * (1 - -1)  -1;
        action.accelerate = 1;
        action.brake = 0;
        //super.controlQualification(action, sensors);
    }

    public void controlRace(Action action, SensorModel sensors) {
        super.controlWarmUp(action, sensors);

        double desiredSpeed;
        double alpha = 0.5;
        double beta = 2.0;
        double maxSpeed = 180.0;
        double t9 = sensors.getTrackEdgeSensors()[9];
        double minTrackEdge = Math.max(sensors.getTrackEdgeSensors()[1], sensors.getTrackEdgeSensors()[17]);
        double maxTrackEdge = Math.min(sensors.getTrackEdgeSensors()[1], sensors.getTrackEdgeSensors()[17]);

        if (t9 > 150.0) {
            desiredSpeed = maxSpeed;
        } else if (t9 > 70.0) {
            desiredSpeed = (alpha * (maxSpeed - sensors.getSpeed()) * Math.abs(maxTrackEdge - t9)) /
                    Math.abs(minTrackEdge - t9);
        } else {
            desiredSpeed = beta * t9 * Math.abs(maxTrackEdge - t9) / Math.abs(minTrackEdge - t9);
        }

        action.accelerate = 2.0 / (1.0 + Math.exp(sensors.getSpeed() - desiredSpeed)) - 1.0;
        if (mDataRecorder != null) {
            mDataRecorder.record(action, sensors);
        }

    }

    public void defaultControl(Action action, SensorModel sensors){
        action.clutch = 1;
        action.steering =  Math.random() * (1 - -1)  -1;
        action.accelerate = 1;
        action.brake = 0;
        //super.defaultControl(action, sensors);
    }
}