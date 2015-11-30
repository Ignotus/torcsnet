import storage.DataRecorder;
import storage.Normalization;
import org.apache.commons.math3.linear.*;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Training procedure for MLP with track data
 */
public class MLPNNTrainingTest {
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
            DataRecorder.SENSOR_TRACK_EDGE_19
    };

    // The values that we want to predict
    public static final int[] OUTPUTS = new int[] {
            DataRecorder.ACTION_ACCELERATION,
            DataRecorder.ACTION_STEERING,
            DataRecorder.ACTION_BRAKING
    };

    @Test
    public void trainAndStore() {
        ControllerData data = readData();
        if (data == null) {
            System.out.println("No data read!");
            return;
        }

        /* Normalize and train on the data */
        Normalization norm = Normalization.createNormalization(data.input, data.target);
        norm.normalizeInput(data.input, -1, 1);
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
                MLPNN nn = new MLPNN(INPUTS.length, nhidden, OUTPUTS.length);
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
                        MLPNNSetup setup = new MLPNNSetup(nn.mW1, nn.mW2, norm);
                        writeSetup(Configuration.WEIGHTS_FILE, setup);
                        System.out.println("OK, weights written to file");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class ControllerData {
        public RealMatrix input;
        public RealMatrix target;

        public ControllerData(RealMatrix input, RealMatrix target) {
            this.input = input;
            this.target = target;
        }
    }

    /* Reads all CSV files in the directory,
     * returns input and target matrices for the neural network*/
    private ControllerData readData() {
        File fileDir = new File(Configuration.CSV_DIRECTORY);
        System.out.println(fileDir);
        File[] files = fileDir.listFiles();
        if (files == null) {
            System.out.println("No data files found");
            return null;
        }

        ArrayList<RealVector> inputVectors = new ArrayList<>();
        ArrayList<RealVector> targetVectors = new ArrayList<>();

        /* Go through all files in the directory*/
        for (File file : files) {
            if (file.isDirectory() || !file.getName().endsWith(".csv")) {
                continue;
            }
            System.out.println("Processing file " + file.getName());
            try {
                Scanner scanner = new Scanner(file);
                /* Parse and train line-by-line */
                while (scanner.hasNextLine()) {
                    String[] entries = scanner.nextLine().split(",");
                    if (entries.length != Configuration.LINE_VALUES) {
                        throw new Exception("Line should contain " + Configuration.LINE_VALUES + " values");
                    }

                    inputVectors.add(parseValues(entries, INPUTS));
                    targetVectors.add(parseValues(entries, OUTPUTS));
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                return null;
            }
        }

        if (inputVectors.size() == 0) {
            return null;
        }

        /* Create matrices */
        RealMatrix inputMatrix = new Array2DRowRealMatrix(inputVectors.size(), INPUTS.length);
        RealMatrix targetMatrix = new Array2DRowRealMatrix(targetVectors.size(), OUTPUTS.length);
        for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
            inputMatrix.setRowVector(i, inputVectors.get(i));
        }
        for (int i = 0; i < targetMatrix.getRowDimension(); i++) {
            targetMatrix.setRowVector(i, targetVectors.get(i));
        }

        return new ControllerData(inputMatrix, targetMatrix);
    }

    private MLPNNSetup readSetup(String filename) throws IOException, ClassNotFoundException {
        MLPNNSetup setup = new MLPNNSetup();
        FileInputStream fis = new FileInputStream(filename);
        ObjectInputStream ois = new ObjectInputStream(fis);
        MatrixUtils.deserializeRealMatrix(setup, "W1", ois);
        MatrixUtils.deserializeRealMatrix(setup, "W2", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "inputMin", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "inputMax", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "targetMin", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "targetMax", ois);
        return setup;
    }

    private void writeSetup(String filename, MLPNNSetup setup) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        MatrixUtils.serializeRealMatrix(setup.W1, oos);
        MatrixUtils.serializeRealMatrix(setup.W2, oos);
        MatrixUtils.serializeRealVector(setup.norm.inputMin, oos);
        MatrixUtils.serializeRealVector(setup.norm.inputMax, oos);
        MatrixUtils.serializeRealVector(setup.norm.targetMin, oos);
        MatrixUtils.serializeRealVector(setup.norm.targetMax, oos);
        oos.close();
    }

    private static RealVector parseValues(String[] line, int[] indices) throws Exception {
        RealVector vector = new ArrayRealVector(indices.length);
        for (int i = 0; i < indices.length; i++) {
            double d = Double.parseDouble(line[indices[i]]);
            if (Double.isNaN(d)) {
                // TODO File contains NaN, temp fix?
                d = 0;
            }
            vector.setEntry(i, d);
        }
        return vector;
    }

}
