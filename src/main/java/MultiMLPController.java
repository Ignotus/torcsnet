import cicontest.torcs.client.SensorModel;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import storage.MLPSetup;
import storage.Normalization;
import java.io.IOException;


/**
 * Created by sander on 02/12/15.
 */
public class MultiMLPController implements NeuralNetworkController {
    private Normalization mNormAccel;
    private Normalization mNormSteering;
    private Normalization mNormBraking;

    private MLP mNNAcceleration;
    private MLP mNNSteering;
    private MLP mNNBraking;

    private double mAcceleration;
    private double mSteering;
    private double mBraking;

    public static MultiMLPController initializeController(String weightsAccel, String weightsSteering, String weightsBraking)
            throws IOException, ClassNotFoundException {

        MLPSetup[] setups = MLPSetup.readSetups(new String[]{weightsAccel, weightsSteering, weightsBraking});
        MultiMLPController controller = new MultiMLPController();

        controller.mNNAcceleration = new MLP(setups[0]);
        controller.mNNSteering = new MLP(setups[1]);
        controller.mNNBraking = new MLP(setups[2]);

        controller.mNormAccel = setups[0].norm;
        controller.mNormSteering = setups[1].norm;
        controller.mNormBraking = setups[2].norm;

        return controller;
    }

    @Override
    public void updatePredictions(SensorModel model) {
        RealVector predictionAcc = mNNAcceleration.predict(sensorsToVector(model, mNNAcceleration, mNormAccel));
        RealVector predictionSteering = mNNSteering.predict(sensorsToVector(model, mNNSteering, mNormSteering));
        RealVector predictionBraking = mNNBraking.predict(sensorsToVector(model, mNNBraking, mNormBraking));
        mNormAccel.denormalizeOutput(predictionAcc, 0, 1);
        mNormSteering.denormalizeOutput(predictionSteering, 0, 1);
        mNormBraking.denormalizeOutput(predictionBraking, 0, 1);
        mAcceleration = predictionAcc.getEntry(0);
        mSteering = predictionSteering.getEntry(0);
        mBraking = predictionBraking.getEntry(0);
    }

    @Override
    public double getAcceleration() {
        return mAcceleration;
    }

    @Override
    public double getSteering() {
        return mSteering;
    }

    @Override
    public double getBraking() {
        return mBraking;
    }

    private RealVector sensorsToVector(SensorModel sensors, MLP mlp, Normalization norm) {
        RealVector vector = new ArrayRealVector(mlp.getInputLayerSize());
        vector.setEntry(0, sensors.getSpeed());
        vector.setEntry(1, sensors.getAngleToTrackAxis());

        double []trackEdgeSensors = sensors.getTrackEdgeSensors();

        for (int i = 0; i < trackEdgeSensors.length; i++) {
            vector.setEntry(i + 2, trackEdgeSensors[i]);
        }

        norm.normalizeInputVector(vector, 0, 1);
        return vector;
    }
}
