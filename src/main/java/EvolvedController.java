import cicontest.torcs.client.SensorModel;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import storage.Normalization;


/**
 * Created by sander on 03/12/15.
 */
public class EvolvedController implements NeuralNetworkController {
    private MLRegression mNN;
    private Normalization mNorm;
    private RealVector mPredictions;

    public EvolvedController(MLRegression network) {
        this.mNN = network;
        this.mNorm = createDefaultNormalization();
        System.out.println("EvolvedController initialized");
    }

    @Override
    public void updatePredictions(SensorModel model) {
        RealVector vec = sensorsToVector(model, mNorm);
        MLData prediction = mNN.compute(new BasicMLData(vec.toArray()));

        mPredictions = new ArrayRealVector(prediction.getData());
        mNorm.denormalizeOutput(mPredictions, 0, 1);
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
        return 0;
    }

    private RealVector sensorsToVector(SensorModel sensors, Normalization norm) {
        RealVector vector = new ArrayRealVector(mNN.getInputCount());
        vector.setEntry(0, sensors.getSpeed());
        vector.setEntry(1, sensors.getAngleToTrackAxis());

        double[] trackEdgeSensors = sensors.getTrackEdgeSensors();
        for (int i = 0; i < trackEdgeSensors.length; i++) {
            vector.setEntry(i + 2, trackEdgeSensors[i]);
        }

        norm.normalizeInputVector(vector, 0, 1);
        return vector;
    }

    public static Normalization createDefaultNormalization() {

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
        norm.targetMin.setEntry(0, -1);
        norm.targetMax.setEntry(0, 1);
        // steering
        norm.targetMin.setEntry(1, -1);
        norm.targetMax.setEntry(1, 1);
        // braking
        norm.targetMin.setEntry(2, -1);
        norm.targetMax.setEntry(2, 1);

        return norm;
    }
}
