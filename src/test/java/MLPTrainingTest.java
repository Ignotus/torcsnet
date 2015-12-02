import storage.*;
import org.apache.commons.math3.linear.*;
import org.junit.Test;

import java.io.*;

/**
 * Training procedure for MLP with track data
 */
public class MLPTrainingTest {
    private static final int TRAIN_ITERATIONS = 1000;

    // The values that we take as input for predictions
    public static final int[] INPUTS = new int[] {
            DataRecorder.SENSOR_SPEED,
            DataRecorder.SENSORS_ANGLE_TO_TRACK_AXIS,
            DataRecorder.SENSOR_TRACK_EDGE_1,
            DataRecorder.SENSOR_TRACK_EDGE_2,
            DataRecorder.SENSOR_TRACK_EDGE_3,
            DataRecorder.SENSOR_TRACK_EDGE_4,
            DataRecorder.SENSOR_TRACK_EDGE_5,
            DataRecorder.SENSOR_TRACK_EDGE_6,
            DataRecorder.SENSOR_TRACK_EDGE_7,
            DataRecorder.SENSOR_TRACK_EDGE_8,
            DataRecorder.SENSOR_TRACK_EDGE_9,
            DataRecorder.SENSOR_TRACK_EDGE_10,
            DataRecorder.SENSOR_TRACK_EDGE_11,
            DataRecorder.SENSOR_TRACK_EDGE_12,
            DataRecorder.SENSOR_TRACK_EDGE_13,
            DataRecorder.SENSOR_TRACK_EDGE_14,
            DataRecorder.SENSOR_TRACK_EDGE_15,
            DataRecorder.SENSOR_TRACK_EDGE_16,
            DataRecorder.SENSOR_TRACK_EDGE_17,
            DataRecorder.SENSOR_TRACK_EDGE_18,
            DataRecorder.SENSOR_TRACK_EDGE_19,
    };

    // The values that we want to predict
    public static final int[] OUTPUTS = new int[] {
            DataRecorder.ACTION_ACCELERATION,
            DataRecorder.ACTION_STEERING,
            DataRecorder.ACTION_BRAKING
    };

    @Test
    public void trainAndStore() {
        TrainingData data = TrainingData.readData(Configuration.CSV_DIRECTORY, INPUTS, OUTPUTS, false);
        if (data == null) {
            System.out.println("No data read!");
            return;
        }

        /* Normalize and train on the data */
        Normalization norm = Normalization.createNormalization(data.input, data.target);
        norm.normalizeInput(data.input, 0, 1);
        norm.normalizeTarget(data.target, 0, 1);

        final int numInput = data.input.getRowDimension();
        RealMatrix trainInput = data.input.getSubMatrix(0, numInput * 9 / 10, 0, data.input.getColumnDimension() - 1);
        RealMatrix trainTarget = data.target.getSubMatrix(0, numInput * 9 / 10, 0, data.target.getColumnDimension() - 1);

        RealMatrix testInput = data.input.getSubMatrix(numInput * 9 / 10, numInput - 1, 0, data.input.getColumnDimension() - 1);
        RealMatrix testTarget = data.target.getSubMatrix(numInput * 9 / 10, numInput - 1, 0, data.target.getColumnDimension() - 1);

        System.out.println("Read " + data.input.getRowDimension() + " input rows, "
                + data.target.getRowDimension() + " target rows");
        System.out.println("Training...");

        double minDistanceErrorAvg = Double.MAX_VALUE;
        // The best nhidden was 5 on the test data. LR = 0.16.
        //for (int nhidden = 5; nhidden < 50; nhidden += 5)
        {
            int nhidden = 25;
            //for (double lr = 0.02; lr < 1.0; lr *= 2)
            {
                double lr = 0.32;
                System.out.println("Checking hidden layer size: " + nhidden);
                System.out.println("Learning rate selection: " + lr);
                MLP nn = new MLP(INPUTS.length, nhidden, OUTPUTS.length);
                nn.setActivationFunction(new ActivationFunctions.Tanh());
                nn.train(trainInput, trainTarget, TRAIN_ITERATIONS, lr);

                double distanceErrorSum = 0.0;
                for (int i = 0; i < testInput.getRowDimension(); i++) {
                    RealVector targetVector = testTarget.getRowVector(i);
                    distanceErrorSum += targetVector.getDistance(nn.predict(testInput.getRowVector(i)));
                }

                // It's more important for us to see how it work well in general. Local failures are OK.
                final double distanceErrorAverage = distanceErrorSum / testInput.getRowDimension();
                System.out.println("Average distance error = " + distanceErrorAverage);

                if (distanceErrorAverage < minDistanceErrorAvg) {
                    minDistanceErrorAvg = distanceErrorAverage;
                    System.out.println("Storing new weights...");
                    try {
                        MLPSetup setup = new MLPSetup(nn.mW1, nn.mW2, norm);
                        setup.writeSetup(Configuration.WEIGHTS_FILE, setup);
                        System.out.println("OK, weights written to file " + Configuration.WEIGHTS_FILE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


}
