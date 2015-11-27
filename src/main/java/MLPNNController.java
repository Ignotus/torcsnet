import java.io.File;
import cicontest.torcs.client.SensorModel;
import storage.Normalization;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class MLPNNController implements NeuralNetworkController {

    private Normalization mNorm;
    private MLPNN mNN;
    private RealVector mPrediction;

    public static MLPNNController initializeController(String weightsFile) throws IOException, ClassNotFoundException {
        MLPNNSetup setup = readSetup(weightsFile);
        int numInputs = setup.W1.getColumnDimension() - 1; // skip bias neuron
        int numHidden = setup.W1.getRowDimension() - 1;
        int numOutputs = setup.W2.getRowDimension() - 1;
        MLPNNController controller = new MLPNNController();
        controller.mNN = new MLPNN(numInputs, numHidden, numOutputs);
        controller.mNN.setWeights(setup.W1, setup.W2);
        controller.mNorm = setup.norm;
        controller.mPrediction = new ArrayRealVector(numOutputs);
        return controller;
    }

    @Override
    public void updatePredictions(SensorModel model) {
        RealVector vector = sensorsToVector(model);
        // Vector contains [ACTION_ACCELERATION, ACTION_STEERING, ACTION_BRAKING]
        RealVector prediction = mNN.predict(vector);
        mNorm.denormalizeOutput(prediction);
        mPrediction = prediction;
    }

    @Override
    public double getAcceleration() {
        return mPrediction.getEntry(0);
    }

    @Override
    public double getSteering() {
        return mPrediction.getEntry(1);
    }

    @Override
    public double getBraking() {
        return mPrediction.getEntry(2);
    }

    private static MLPNNSetup readSetup(String filename) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = MLPNNController.class.getClassLoader();
        System.out.println("Loading weights");
        File file = new File(classLoader.getResource(filename).getFile());

        MLPNNSetup setup = new MLPNNSetup();
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        MatrixUtils.deserializeRealMatrix(setup, "W1", ois);
        MatrixUtils.deserializeRealMatrix(setup, "W2", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "inputMin", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "inputDiff", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "targetMin", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "targetDiff", ois);
        return setup;
    }

    private RealVector sensorsToVector(SensorModel sensors) {
        RealVector vector = new ArrayRealVector(mNN.getInputLayerSize());
        vector.setEntry(0, sensors.getSpeed());
        vector.setEntry(1, sensors.getAngleToTrackAxis());

        double []trackEdgeSensors = sensors.getTrackEdgeSensors();
        for (int i = 0; i < trackEdgeSensors.length; i++) {
            vector.setEntry(i + 2, trackEdgeSensors[i]);
        }

        mNorm.normalizeInputVector(vector);
        return vector;
    }

}