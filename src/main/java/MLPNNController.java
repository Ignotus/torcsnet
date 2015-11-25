/**
 * Created by sander on 25/11/15.
 */
import cicontest.torcs.client.SensorModel;
import storage.Normalization;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class MLPNNController {

    private static final int HIDDEN_LAYER_SIZE = 10;
    private Normalization mNorm;
    private MLPNN mNN;

    private MLPNNController() {
        mNN = new MLPNN(MLPNNConfiguration.INPUTS.length, HIDDEN_LAYER_SIZE, MLPNNConfiguration.OUTPUTS.length);
    }

    public RealVector sensorsToVector(SensorModel sensors) {
        RealVector vector = new ArrayRealVector(MLPNNConfiguration.INPUTS.length);
        // TODO could be nicer
        vector.setEntry(0, sensors.getSpeed());
        vector.setEntry(1, sensors.getAngleToTrackAxis());

        double []trackEdgeSensors = sensors.getTrackEdgeSensors();
        for (int i = 0; i < trackEdgeSensors.length; i++) {
            vector.setEntry(i + 2, trackEdgeSensors[i]);
        }
        mNorm.normalizeInputVector(vector);
        return vector;
    }

    public RealVector predictOutputs(SensorModel sensorModel) {
        RealVector vector = sensorsToVector(sensorModel);
        // Returns vector containing [ACTION_ACCELERATION, ACTION_STEERING, ACTION_BRAKING]
        return mNN.predict(vector).getSubVector(1, vector.getDimension());
    }

    public static MLPNNController InitializeController(String weightsFile) throws IOException, ClassNotFoundException {
        MLPNNController controller = new MLPNNController();
        MLPNNSetup setup = readSetup(weightsFile);
        controller.mNorm = setup.norm;
        controller.mNN.setWeights(setup.W1, setup.W2);
        return controller;
    }

    private static MLPNNSetup readSetup(String filename) throws IOException, ClassNotFoundException {
        MLPNNSetup setup = new MLPNNSetup();
        FileInputStream fis = new FileInputStream(filename);
        ObjectInputStream ois = new ObjectInputStream(fis);
        MatrixUtils.deserializeRealMatrix(setup, "W1", ois);
        MatrixUtils.deserializeRealMatrix(setup, "W2", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "inputMin", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "inputDiff", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "targetMin", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "targetDiff", ois);
        return setup;
    }

}