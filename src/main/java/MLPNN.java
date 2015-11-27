import org.apache.commons.math3.analysis.function.Sigmoid;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.concurrent.ThreadLocalRandom;

/**
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
    public RealMatrix mW1 = null;
    // Weights between hidden and output layer
    public RealMatrix mW2 = null;

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

        this.mOutputLayerGradients = new ArrayRealVector(outputLayerSize + 1);
        this.mHiddenLayerGradients = new ArrayRealVector(hiddenLayerSize + 1);
    }

    public final int getInputLayerSize() {
        return mInputLayerSize;
    }

    public final int getHiddenLayerSize() {
        return mHiddenLayerSize;
    }

    public final int getOutputLayerSize() {
        return mOutputLayerSize;
    }

    public void setWeights(RealMatrix W1, RealMatrix W2) {
        this.mW1 = W1;
        this.mW2 = W2;
    }

    public RealVector predict(RealVector input) {
        pass(input);
        return mOutputLayer.getSubVector(1, mOutputLayer.getDimension() - 1);
    }

    // data: matrix of dim  N X mInputLayerSize
    // target: matrix of dim N X mOutputLayerSize
    public void train(RealMatrix data, RealMatrix target, int numIterations, double learningRate) {
        if (this.mW1 == null) {
            this.mW1 = Randomizer.newMatrix(mHiddenLayerSize + 1, mInputLayerSize + 1);
        }

        if (this.mW2 == null) {
            this.mW2 = Randomizer.newMatrix(mOutputLayerSize + 1, mHiddenLayerSize + 1);
        }

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
