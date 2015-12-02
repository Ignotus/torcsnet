import storage.*;
import org.apache.commons.math3.linear.*;
import org.junit.Test;

import java.io.*;

/**
 * Training procedure for MLP with track data
 */
public class MultiMLPTrainingTest {
    private static final int TRAIN_ITERATIONS = 1000;

    // The values that we take as input for predictions
    private static final int[] INPUTS = new int[] {
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
    private static final int[] OUTPUTS = new int[] {
            DataRecorder.ACTION_ACCELERATION,
            DataRecorder.ACTION_STEERING,
            DataRecorder.ACTION_BRAKING
    };


    private static final String[] WEIGHT_FILES = new String[]{
            Configuration.WEIGHTS_FILE_ACCEL,
            Configuration.WEIGHTS_FILE_STEERING,
            Configuration.WEIGHTS_FILE_BRAKING
    };

    @Test
    public void trainAndStore() {
        for (int i = 0; i < OUTPUTS.length; i++) {
            /* Train a separate NN for each output */

            /* Normalize and train on the data */
            TrainingData data = TrainingData.readData(Configuration.CSV_DIRECTORY, INPUTS, new int[]{OUTPUTS[i]}, false);
            if (data == null) {
                System.out.println("No data read!");
                return;
            }

            Normalization norm = Normalization.createNormalization(data.input, data.target);
            norm.normalizeInput(data.input, 0, 1);
            norm.normalizeTarget(data.target, 0.2, 0.8);

            MLP nn = new MLP(INPUTS.length, 25, 1);
            nn.train(data.input, data.target, TRAIN_ITERATIONS, 0.2);

            System.out.println("Training completed. Average error: "
                    + (distanceErrorSum(nn, data.input, data.target) / data.input.getRowDimension()));
            distanceErrorSum(nn, data.input, data.target);

            try {
                MLPSetup setup = new MLPSetup(nn.mW1, nn.mW2, norm);
                setup.writeSetup(WEIGHT_FILES[i], setup);
                System.out.println("OK, weights written to file " + WEIGHT_FILES[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private double distanceErrorSum(MLP nn, RealMatrix input, RealMatrix target) {
        double distanceErrorSum = 0.0;
        for (int i = 0; i < input.getRowDimension(); i++) {
            RealVector targetVector = target.getRowVector(i);
            RealVector pred = nn.predict(input.getRowVector(i));
            distanceErrorSum += targetVector.getDistance(pred);
        }
        return distanceErrorSum;
    }

}
