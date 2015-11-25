import cicontest.torcs.client.SensorModel;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import storage.DataRecorder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class NNController implements NeuralNetworkController {

    private static final int MIN = 0;
    private static final int DIFF = 1;

    private RealMatrix mMinDiff;
    private RealVector mCurX;
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

    private double denormalizeValue(double value, int index) {
        return (mMinDiff.getEntry(index, DIFF) * value) + mMinDiff.getEntry(index, MIN);
    }

    private RealVector sensorsToVector(SensorModel sensors) {
        int numSensors = DataRecorder.SENSOR_TRACK_EDGE_19 - DataRecorder.SENSOR_SPEED + 1;
        RealVector vector = new ArrayRealVector(numSensors);
        vector.setEntry(DataRecorder.SENSOR_SPEED, normalizeValue(sensors.getSpeed(), DataRecorder.SENSOR_SPEED));
        vector.setEntry(DataRecorder.SENSORS_ANGLE_TO_TRACK_AXIS, normalizeValue(sensors.getAngleToTrackAxis(),
                DataRecorder.SENSORS_ANGLE_TO_TRACK_AXIS));
        vector.setEntry(DataRecorder.SENSOR_TRACK_POSITION, normalizeValue(sensors.getTrackPosition(),
                DataRecorder.SENSOR_TRACK_POSITION));
        vector.setEntry(DataRecorder.SENSOR_RPM, normalizeValue(sensors.getRPM(), DataRecorder.SENSOR_RPM));

        double []trackEdgeSensors = sensors.getTrackEdgeSensors();
        for (int i = DataRecorder.SENSOR_TRACK_EDGE_1, j = 0; i <= DataRecorder.SENSOR_TRACK_EDGE_19; i++, j++) {
            vector.setEntry(i, normalizeValue(trackEdgeSensors[j], i));
        }

        return vector;
    }

    @Override
    public void updatePredictions(SensorModel model) {
        mCurX = sensorsToVector(model);
    }

    @Override
    public double getSteering() {
        return denormalizeValue(mSteeringNN.predict(mCurX), DataRecorder.ACTION_STEERING);
    }

    @Override
    public double getAcceleration() {
        return denormalizeValue(mAccelerationNN.predict(mCurX), DataRecorder.ACTION_ACCELERATION);
    }

    @Override
    public double getBraking() {
        return denormalizeValue(mBrakingNN.predict(mCurX), DataRecorder.ACTION_BRAKING);
    }

    public static NNController initializeController(String weightsFile) throws IOException, ClassNotFoundException {
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
