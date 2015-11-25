import org.apache.commons.math3.analysis.function.Sigmoid;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by sander on 23/11/15.
 * Multilayer Perceptron implementation with one hidden layer
 */
public class MLPNN {
    private static final Sigmoid sigmoid = new Sigmoid();

    // Layers sizes
    private int mInputLayerSize;
    private int mHiddenLayerSize;
    private int mOutputLayerSize;

    // Layer matrices
    private RealVector mInputLayer;
    private RealVector mHiddenLayer;
    private RealVector mOutputLayer;

    // Weights between input and hidden layer
    public RealMatrix mW1;
    // Weights between hidden and output layer
    public RealMatrix mW2;

    // Stored vectors, used in backpropagation, to minimize allocations
    private RealVector mOutputLayerGradients;
    private RealVector mHiddenLayerGradients;

    public MLPNN(int inputLayerSize, int hiddenLayerSize, int outputLayerSize) {
        this.mInputLayerSize = inputLayerSize;
        this.mHiddenLayerSize = hiddenLayerSize;
        this.mOutputLayerSize = outputLayerSize;

        // +1 for bias weight
        this.mInputLayer = new ArrayRealVector(inputLayerSize + 1);
        this.mHiddenLayer = new ArrayRealVector(hiddenLayerSize + 1);
        this.mOutputLayer = new ArrayRealVector(outputLayerSize + 1);

        this.mW1 = new Array2DRowRealMatrix(hiddenLayerSize + 1, inputLayerSize + 1);
        this.mW2 = new Array2DRowRealMatrix(outputLayerSize + 1, hiddenLayerSize + 1);

        this.mOutputLayerGradients = new ArrayRealVector(outputLayerSize + 1);
        this.mHiddenLayerGradients = new ArrayRealVector(hiddenLayerSize + 1);

        initializeWeights();
    }

    private void initializeWeights() {
        // Initialize W1 and W2 with values uniformly sampled from an interval
        double max = 0.5;
        double min = -0.5;

        ThreadLocalRandom rand = ThreadLocalRandom.current();
        for (int i = 1; i <= mInputLayerSize; i++) {
            for (int j = 0; j <=  mHiddenLayerSize; j++) {
                mW1.setEntry(j, i, rand.nextDouble(min, max));
            }
        }

        for (int i = 1; i <= mHiddenLayerSize; i++) {
            for (int j = 0; j <= mOutputLayerSize; j++) {
                mW2.setEntry(j, i, rand.nextDouble(min, max));
            }
        }
    }

    public int getInputLayerSize() {
        return mInputLayerSize;
    }

    public int getHiddenLayerSize() {
        return mHiddenLayerSize;
    }

    public int getOutputLayerSize() {
        return mOutputLayerSize;
    }

    public void setWeights(RealMatrix W1, RealMatrix W2) {
        this.mW1 = W1;
        this.mW2 = W2;
    }

    public RealVector predict(RealVector input) {
        pass(input);
        return mOutputLayer;
    }

    // data: matrix of dim  N X mInputLayerSize
    // target: matrix of dim N X mOutputLayerSize
    public void train(RealMatrix data, RealMatrix target, int numIterations, double learningRate) {
        for (int i = 0; i < numIterations; i++) {
            for (int row = 0; row < data.getRowDimension(); row++) {
                train(data.getRowVector(row), target.getRowVector(row), learningRate);
            }
        }
    }

    // data: vector of length mInputLayerSize
    // target: vector of length mOutputLayerSize
    public void train(RealVector data, RealVector target, double learningRate) {
        pass(data);
        propagateError(target, learningRate);
    }

    public void pass(RealVector input) {
        // Set bias
        mInputLayer.setEntry(0, 1.0);
        mHiddenLayer.setEntry(0, 1.0);

        // Fill input layer
        mInputLayer.setSubVector(1, input);

        // Pass data through the network
        double sum;
        for (int i = 1; i <= mHiddenLayerSize; i++) {
            // Input to hidden layer
            sum = 0.0;
            for (int j = 0; j <= mInputLayerSize; j++) {
                sum += mW1.getEntry(i, j) * mInputLayer.getEntry(j);
            }
            mHiddenLayer.setEntry(i, sigmoid.value(sum));
        }

        for (int i = 1; i <= mOutputLayerSize; i++) {
            // Hidden to output layer
            sum = 0.0;
            for (int j = 0; j <= mHiddenLayerSize; j++) {
                sum += mW2.getEntry(i, j) * mHiddenLayer.getEntry(j);
            }
            mOutputLayer.setEntry(i, sigmoid.value(sum));
        }
    }

    // https://en.wikipedia.org/wiki/Multilayer_perceptron
    // Updates the weights through back propagation
    // Uses gradient descent
    private void propagateError(RealVector target, final double learningRate) {
        // Re-use vectors to minimize allocations
        mOutputLayerGradients.set(0);
        mHiddenLayerGradients.set(0);

        /* Compute output layer gradients */
        for (int i = 1; i <= mOutputLayerSize; i++) {
            double output = mOutputLayer.getEntry(i);
            double error = target.getEntry(i - 1) - output;
            mOutputLayerGradients.setEntry(i, error * output * (1.0 - output));
        }

        /* Compute hidden layer gradients */
        for (int i = 0; i <= mHiddenLayerSize; i++) {
            double errorSum = 0.0;
            for (int j = 1; j <= mOutputLayerSize; j++) {
                errorSum += mW2.getEntry(j, i) * mOutputLayerGradients.getEntry(j);
            }
            double output = mHiddenLayer.getEntry(i);
            mHiddenLayerGradients.setEntry(i, errorSum * output * (1.0 - output));
        }

        /* Update output layer weights (gradient descent) */
        for (int i = 1; i <= mOutputLayerSize; i++) {
            double grad = mOutputLayerGradients.getEntry(i);
            for (int j = 0; j <= mHiddenLayerSize; j++) {
                mW2.addToEntry(i, j, learningRate * grad * mHiddenLayer.getEntry(j));
            }
        }

        /* Update hidden layer weights (gradient descent) */
        for (int i = 1; i <= mHiddenLayerSize; i++) {
            double grad = mHiddenLayerGradients.getEntry(i);
            for (int j = 0; j <= mInputLayerSize; j++) {
                mW1.addToEntry(i, j, learningRate * grad * mInputLayer.getEntry(j));
            }
        }
    }

}
