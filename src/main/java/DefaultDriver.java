import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.algorithm.abstracts.DriversUtils;
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
    private static final double MINIMUM_SPEED = 35;
    private static final double SAFE_ACCEL_DISTANCE = 180;
    private static final double DANGER_SPEED = 70;

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
        double acceleration = mController.getAcceleration();
        double braking = mController.getBraking();

        if (acceleration > braking) {
            // Temporary solution for safety
            action.accelerate = acceleration;
            action.brake = 0;
        } else {
            action.brake = braking;
            action.accelerate = 0;
        }

        if (sensors.getSpeed() < DANGER_SPEED) {
            action.brake = 0;
        }

        /* Apply basic speed heuristics
        *  - Prevent car from standing still if driving upwards on a hill
        *  - Use maximum accelerating when possible
        * */

        double speed = sensors.getSpeed();

        double[] edgeSensors = sensors.getTrackEdgeSensors();
        double frontSensor = Math.max(Math.max(edgeSensors[8], edgeSensors[10]), edgeSensors[9]);

        if (frontSensor > SAFE_ACCEL_DISTANCE) {
            action.accelerate = 1.0;
        } else if (frontSensor > (SAFE_ACCEL_DISTANCE * 0.8)) {
            action.accelerate = Math.max(action.accelerate, 0.5);
        } else if (frontSensor > (SAFE_ACCEL_DISTANCE * 0.5) && sensors.getSpeed() < DANGER_SPEED) {
            action.accelerate = Math.max(action.accelerate, 0.5);
        } else if (speed < MINIMUM_SPEED) {
            action.accelerate = 1.0;
        }

        if (action.accelerate > 0) {
            action.accelerate *= 1.2;
        }

        action.steering = mController.getSteering();

        System.out.println("Track pos:" + sensors.getTrackPosition());
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