import java.io.*;

import cicontest.torcs.client.SensorModel;
import storage.MLPSetup;
import storage.Normalization;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class SingleMLPController implements NeuralNetworkController {

    private MLP mNN;
    private Normalization mNorm;
    private RealVector mPrediction;

    public static SingleMLPController initializeController(String weightsFile) throws IOException, ClassNotFoundException {
        MLPSetup setup = MLPSetup.readSetup(weightsFile);
        SingleMLPController controller = new SingleMLPController();
        controller.mNN = new MLP(setup);
        controller.mNN.setActivationFunction(new ActivationFunctions.Sigmoid());
        controller.mNN.setWeights(setup.W1, setup.W2);
        controller.mNorm = setup.norm;
        controller.mPrediction = new ArrayRealVector(setup.getNumOutputs());
        return controller;
    }

    @Override
    public void updatePredictions(SensorModel model) {
        RealVector vector = sensorsToVector(model);
        RealVector prediction = mNN.predict(vector);
        // Vector contains [ACTION_ACCELERATION, ACTION_STEERING, ACTION_BRAKING]
        System.out.println("Pred: " + prediction);

        mNorm.denormalizeOutput(prediction, 0, 1);
        System.out.println("Denormalized: " + prediction);
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

    private RealVector sensorsToVector(SensorModel sensors) {
        RealVector vector = new ArrayRealVector(mNN.getInputLayerSize());
        vector.setEntry(0, sensors.getSpeed());
        vector.setEntry(1, sensors.getAngleToTrackAxis());

        double []trackEdgeSensors = sensors.getTrackEdgeSensors();

        for (int i = 0; i < trackEdgeSensors.length; i++) {
            vector.setEntry(i + 2, trackEdgeSensors[i]);
        }

        mNorm.normalizeInputVector(vector, 0, 1);
        return vector;
    }

}