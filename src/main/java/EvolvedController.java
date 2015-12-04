import cicontest.torcs.client.SensorModel;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.MultiLayerPerceptron;
import storage.Normalization;

import java.util.Vector;

/**
 * Created by sander on 03/12/15.
 */
public class EvolvedController implements NeuralNetworkController {
    private NeuralNetwork mNN;
    private Normalization mNorm;
    private RealVector mPredictions;

    public EvolvedController(NeuralNetwork network) {
        this.mNN = network;
        this.mNorm = createDefaultNormalization();
        System.out.println("EvolvedController initialized");
    }

    public EvolvedController(String neurophWeightsFile) {
        this.mNN = MultiLayerPerceptron.load(neurophWeightsFile);
        this.mNorm = createDefaultNormalization();
    }

    @Override
    public void updatePredictions(SensorModel model) {
        RealVector vec = sensorsToVector(model, mNorm);
        mNN.setInput(vec.toArray());
        mNN.calculate();
        mPredictions = vectorToRealVector(mNN.getOutput());
        mNorm.denormalizeOutput(mPredictions, 0, 1);
        System.out.println("Predictions: " + mPredictions);
    }

    @Override
    public double getAcceleration() {
        return mPredictions.getEntry(0);
    }

    @Override
    public double getSteering() {
        return mPredictions.getEntry(1);
    }

    @Override
    public double getBraking() {
        return mPredictions.getEntry(2);
    }

    private RealVector sensorsToVector(SensorModel sensors, Normalization norm) {
        RealVector vector = new ArrayRealVector(mNN.getInputNeurons().size());
        vector.setEntry(0, sensors.getSpeed());
        vector.setEntry(1, sensors.getAngleToTrackAxis());

        double[] trackEdgeSensors = sensors.getTrackEdgeSensors();
        for (int i = 0; i < trackEdgeSensors.length; i++) {
            vector.setEntry(i + 2, trackEdgeSensors[i]);
        }

        norm.normalizeInputVector(vector, 0, 1);
        return vector;
    }

    private Normalization createDefaultNormalization() {

        // Set data normalization based on expected range of values
        Normalization norm = new Normalization();
        norm.inputMin = new ArrayRealVector(21);
        norm.inputMax = new ArrayRealVector(21);
        // speed
        norm.inputMin.setEntry(0, 0);
        norm.inputMax.setEntry(0, 200);
        // track angle
        norm.inputMin.setEntry(1, -Math.PI);
        norm.inputMax.setEntry(1, Math.PI);
        // distance sensors
        for (int i = 0; i < 19; i++) {
            norm.inputMin.setEntry(i + 2, 0);
            norm.inputMax.setEntry(i + 2, 200);
        }

        norm.targetMin = new ArrayRealVector(3);
        norm.targetMax = new ArrayRealVector(3);
        // acceleration
        norm.targetMin.setEntry(0, 0);
        norm.targetMax.setEntry(0, 1);
        // steering
        norm.targetMin.setEntry(1, -1);
        norm.targetMax.setEntry(1, 1);
        // braking
        norm.targetMin.setEntry(2, 0);
        norm.targetMax.setEntry(2, 1);

        return norm;
    }

    private RealVector vectorToRealVector(Vector<Double> vec) {
        RealVector v = new ArrayRealVector(vec.size());
        for (int i = 0; i < vec.size(); i++) {
            v.setEntry(i, vec.get(i));
        }
        return v;
    }

}
