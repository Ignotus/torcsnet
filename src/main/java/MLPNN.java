import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * Created by sander on 23/11/15.
 */
public class MLPNN {
    private double mLearningRate;

    // Layers sizes
    private int mInputLayerSize;
    private int mHiddenLayerSize;
    private int mOutputLayerSize;

    // Layer matrices
    private ArrayRealVector mInputLayer;
    private ArrayRealVector mHiddenLayer;
    private ArrayRealVector mOutputLayer;

    // Weights between input and hidden layer
    private Array2DRowRealMatrix mW1;
    // Weights between hidden and output layer
    private Array2DRowRealMatrix mW2;

    public MLPNN(int inputLayerSize, int hiddenLayerSize, int outputLayerSize) {
        this.mInputLayerSize = inputLayerSize;
        this.mHiddenLayerSize = hiddenLayerSize;
        this.mOutputLayerSize = outputLayerSize;
        initializeWeights();
    }

    private void initializeWeights() {
        // Initialize W1 and W2 with random values
        // Uniformly sampled from the an interval
        double max = 4f * Math.sqrt(6f / (mInputLayerSize + mOutputLayerSize));
        double min = -4f * Math.sqrt(6f / (mInputLayerSize + mOutputLayerSize));;

    }
}
