import cicontest.torcs.client.SensorModel;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class NNController {

    private static final int MIN = 0;
    private static final int DIFF = 1;

    private RealMatrix mMinDiff;
    private MLP mAccelerationNN;
    private MLP mSteeringNN;
    private MLP mBrakingNN;

    private NNController() {
        mAccelerationNN = new MLP();
        mSteeringNN = new MLP();
        mBrakingNN = new MLP();
    }

    private double normalizeValue(double value, int index) {
        return Math.min((value - mMinDiff.getEntry(index, MIN)) / mMinDiff.getEntry(index, DIFF), 1.0);
    }

    public RealVector sensorsToVector(SensorModel sensors) {
        int numSensors = DataRecorder.SENSOR_TRACK_EDGE_19 - DataRecorder.SENSOR_SPEED + 1;
        RealVector vector = new ArrayRealVector(numSensors);
        vector.setEntry(DataRecorder.SENSOR_SPEED, normalizeValue(sensors.getSpeed(), DataRecorder.SENSOR_SPEED));
        vector.setEntry(DataRecorder.SENSORS_ANGLE_TO_TRACK_AXIS, normalizeValue(sensors.getAngleToTrackAxis(),
                DataRecorder.SENSORS_ANGLE_TO_TRACK_AXIS));
        vector.setEntry(DataRecorder.SENSOR_TRACK_POSITION, normalizeValue(sensors.getTrackPosition(),
                DataRecorder.SENSOR_TRACK_POSITION));
        vector.setEntry(DataRecorder.SENSOR_RPM, normalizeValue(sensors.getRPM(), DataRecorder.SENSOR_RPM));

        double []trackEdgeSensors = sensors.getTrackEdgeSensors();
        for (int i = DataRecorder.SENSOR_TRACK_EDGE_1; i <= DataRecorder.SENSOR_TRACK_EDGE_19; i++) {
            vector.setEntry(i, normalizeValue(trackEdgeSensors[i], i));
        }

        return vector;
    }

    public double predictSteering(RealVector sensorVec) {
        return normalizeValue(mSteeringNN.predict(sensorVec), DataRecorder.ACTION_STEERING);
    }

    public double predictAcceleration(RealVector sensorVec) {
        return normalizeValue(mAccelerationNN.predict(sensorVec), DataRecorder.ACTION_ACCELERATION);
    }

    public double predictBraking(RealVector sensorVec) {
        return normalizeValue(mBrakingNN.predict(sensorVec), DataRecorder.ACTION_BRAKING);
    }

    public static NNController InitializeController(String weightsFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(weightsFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        NNController controller = new NNController();
        MatrixUtils.deserializeRealMatrix(controller, "mMinDiff", ois);

        /* Fill the acceleration NN with weights. */
        MatrixUtils.deserializeRealVector(controller.mAccelerationNN, "w", ois);
        controller.mAccelerationNN.setB(ois.readDouble());
        MatrixUtils.deserializeRealMatrix(controller.mAccelerationNN, "v", ois);
        MatrixUtils.deserializeRealVector(controller.mAccelerationNN, "a", ois);

        /* Fill the steering NN with weights. */
        MatrixUtils.deserializeRealVector(controller.mSteeringNN, "w", ois);
        controller.mSteeringNN.setB(ois.readDouble());
        MatrixUtils.deserializeRealMatrix(controller.mSteeringNN, "v", ois);
        MatrixUtils.deserializeRealVector(controller.mSteeringNN, "a", ois);

        /* Fill the braking NN with weights. */
        MatrixUtils.deserializeRealVector(controller.mBrakingNN, "w", ois);
        controller.mBrakingNN.setB(ois.readDouble());
        MatrixUtils.deserializeRealMatrix(controller.mBrakingNN, "v", ois);
        MatrixUtils.deserializeRealVector(controller.mBrakingNN, "a", ois);

        return controller;
    }

}
