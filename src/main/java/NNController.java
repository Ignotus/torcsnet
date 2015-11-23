import cicontest.torcs.client.SensorModel;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class NNController {

    private RealMatrix mMinDiff;
    private MLP mAccelerationNN;
    private MLP mSteeringNN;
    private MLP mBrakingNN;

    private NNController() {
        mAccelerationNN = new MLP();
        mSteeringNN = new MLP();
        mBrakingNN = new MLP();
    }

    public double predictSteering(SensorModel sensorModel) {
        return 0.0;
    }

    public double predictAcceleration(SensorModel sensorModel) {
        return 0.0;
    }

    public double predictBraking(SensorModel sensorModel) {
        return 0.0;
    }

    public static NNController InitializeController(String weightsFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(weightsFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        NNController NNController = new NNController();
        MatrixUtils.deserializeRealMatrix(NNController, "mMinDiff", ois);

        /* Fill the acceleration NN with weights. */
        MatrixUtils.deserializeRealVector(NNController.mAccelerationNN, "w", ois);
        NNController.mAccelerationNN.setB(ois.readDouble());
        MatrixUtils.deserializeRealMatrix(NNController.mAccelerationNN, "v", ois);
        MatrixUtils.deserializeRealVector(NNController.mAccelerationNN, "a", ois);

        /* Fill the steering NN with weights. */
        MatrixUtils.deserializeRealVector(NNController.mSteeringNN, "w", ois);
        NNController.mSteeringNN.setB(ois.readDouble());
        MatrixUtils.deserializeRealMatrix(NNController.mSteeringNN, "v", ois);
        MatrixUtils.deserializeRealVector(NNController.mSteeringNN, "a", ois);

        /* Fill the braking NN with weights. */
        MatrixUtils.deserializeRealVector(NNController.mBrakingNN, "w", ois);
        NNController.mBrakingNN.setB(ois.readDouble());
        MatrixUtils.deserializeRealMatrix(NNController.mBrakingNN, "v", ois);
        MatrixUtils.deserializeRealVector(NNController.mBrakingNN, "a", ois);

        return NNController;
    }

}
