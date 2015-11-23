import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.torcs.client.Action;
import cicontest.torcs.client.SensorModel;
import cicontest.torcs.genome.IGenome;
import org.apache.commons.math3.linear.RealVector;

public class DefaultDriver extends AbstractDriver {

    private static final String DRIVER_NAME = "Cocaine";

    private NNController mNNController;
    private DataRecorder mDataRecorder;

    public void loadGenome(IGenome genome) {
        if (genome instanceof DefaultDriverGenome) {
            DefaultDriverGenome myGenome = (DefaultDriverGenome) genome;
            mNNController = myGenome.getNNController();
            mDataRecorder = myGenome.getDataRecorder();
        } else {
            System.err.println("Invalid Genome assigned");
        }
    }

    public String getDriverName() {
        return DRIVER_NAME;
    }

    public void controlQualification(Action action, SensorModel sensors) {
        action.clutch = 1;
        action.steering = Math.random() * (1 - -1) - 1;
        action.accelerate = 1;
        action.brake = 0;
    }

    public void controlRace(Action action, SensorModel sensors) {
        RealVector vec = mNNController.sensorsToVector(sensors);
        double acceleration = mNNController.predictAcceleration(vec);
        double braking = mNNController.predictBraking(vec);

        if (acceleration > braking) {
            action.accelerate = acceleration;
            action.brake = 0;
        } else {
            action.brake = braking;
            action.accelerate = 0;
        }

        action.steering = mNNController.predictSteering(vec);
    }

    private void controlUsingOnlyHeuristics(Action action, SensorModel sensors) {
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

        super.controlWarmUp(action, sensors);
        action.accelerate = 2.0 / (1.0 + Math.exp(sensors.getSpeed() - desiredSpeed)) - 1.0;
        if (mDataRecorder != null) {
            mDataRecorder.record(action, sensors);
        }
    }

    public void defaultControl(Action action, SensorModel sensors) {
        action.clutch = 1;
        action.steering = Math.random() * (1 - -1) - 1;
        action.accelerate = 1;
        action.brake = 0;
    }

    @Override
    public double getSteering(SensorModel sensorModel) {
        /* TODO: is this used? */
        return 0;
    }

    @Override
    public double getAcceleration(SensorModel sensorModel) {
        /* TODO: is this used? */
        return 0;
    }

}