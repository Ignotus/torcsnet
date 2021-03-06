import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.torcs.client.Action;
import cicontest.torcs.client.SensorModel;
import cicontest.torcs.controller.extras.ABS;
import cicontest.torcs.controller.extras.AutomatedClutch;
import cicontest.torcs.controller.extras.AutomatedGearbox;
import cicontest.torcs.controller.extras.AutomatedRecovering;
import cicontest.torcs.genome.IGenome;
import storage.DataRecorder;

public class DefaultDriver extends AbstractDriver {
    private NeuralNetworkController mController;

    private static final String DRIVER_NAME = "Caffeine";
    private static final double SAFE_DISTANCE = 120;

    private static final double BRAKE_DISTANCE_MAX = 150;
    private static final double BRAKE_DISTANCE_MEDIUM = 60;
    private static final double BRAKE_DISTANCE_MIN = 30;

    private static final double HIGH_SPEED = 140;
    private static final double MEDIUM_SPEED = 100;
    private static final double SAFE_SPEED = 80;
    private static final double MINIMUM_SPEED = 50;

    private static final double ACCEL_FULL = 1.0;
    private static final double ACCEL_LOW = 0.5;

    private DataRecorder mDataRecorder;

    public DefaultDriver() {
        enableExtras(new AutomatedClutch());
        enableExtras(new AutomatedGearbox());
        enableExtras(new AutomatedRecovering());
        enableExtras(new ABS());
    }


    public void loadGenome(IGenome genome) {
        //if (genome instanceof DefaultDriverGenome) {
        DefaultDriverGenome myGenome = (DefaultDriverGenome) genome;
        mController = myGenome.getController();
        mDataRecorder = myGenome.getDataRecorder();
        //} else {
        //    System.err.println("Invalid Genome assigned");
        //}
    }

    public String getDriverName() {
        return DRIVER_NAME;
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

    @Override
    public double getSteering(SensorModel sensorModel) {
        /* Only used in super.defaultControl(...)  */
        return 0;
    }

    @Override
    public double getAcceleration(SensorModel sensorModel) {
        /* Only used in super.defaultControl(...)  */
        return 0;
    }

    private void controlImpl(Action action, SensorModel sensors) {
        mController.updatePredictions(sensors);
        action.accelerate =  mController.getAcceleration();

        /* Apply basic speed heuristics
        *  - Prevent car from standing still if driving upwards on a hill
        *  - Use maximum acceleration when possible
        * */
        final double speed = sensors.getSpeed();
        final double[] edgeSensors = sensors.getTrackEdgeSensors();
        final double frontSensor = Math.max(Math.max(edgeSensors[8], edgeSensors[10]), edgeSensors[9]);

        /* Aggressive acceleration policy */
        if (speed < SAFE_SPEED && action.accelerate > 0) {
            action.accelerate *= 1.5;
        }
        if (frontSensor > SAFE_DISTANCE) {
            action.accelerate = ACCEL_FULL;
        } else if (speed < SAFE_SPEED && frontSensor > 0.25 * SAFE_DISTANCE) {
            action.accelerate = Math.max(ACCEL_LOW, action.accelerate);
        } else if (speed < MINIMUM_SPEED) {
            action.accelerate = ACCEL_FULL;
        }

        /* Braking policy */
        if (speed > HIGH_SPEED && frontSensor < BRAKE_DISTANCE_MAX) {
            action.accelerate = 0;
            action.brake = 0.8;
        }

        if (speed > MEDIUM_SPEED && frontSensor < BRAKE_DISTANCE_MEDIUM) {
            action.accelerate = 0;
            action.brake = 0.8;
        }

        if (speed > SAFE_SPEED && frontSensor < BRAKE_DISTANCE_MIN) {
            action.accelerate = 0;
            action.brake = 1.0;
        }

        action.steering = mController.getSteering();
        action.limitValues();
    }

    @Override
    public void controlRace(Action action, SensorModel sensors) {
        controlImpl(action, sensors);
    }

    @Override
    public void controlWarmUp(Action action, SensorModel sensors) {
        controlImpl(action, sensors);
    }

    @Override
    public void controlQualification(Action action, SensorModel sensors) {
        controlImpl(action, sensors);
    }
}