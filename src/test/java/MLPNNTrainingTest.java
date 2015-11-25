import storage.ControllerData;
import storage.Normalization;
import org.apache.commons.math3.linear.*;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by sander on 24/11/15.
 * Training procedure for MLP with track data
 */
public class MLPNNTrainingTest {
    private static final int HIDDEN_LAYER_SIZE = 10;
    private static final int TRAIN_ITERATIONS = 100;

    @Test
    public void trainAndStore() {
        ControllerData data = readData();
        if (data == null) {
            System.out.println("No data read!");
            return;
        }

        /* Normalize and train on the data */
        Normalization norm = Normalization.createNormalization(data.input, data.target);
        norm.normalizeInput(data.input);
        norm.normalizeTarget(data.target);

        System.out.println("Read " + data.input.getRowDimension() + " input rows, "
                + data.target.getRowDimension() + " target rows");
        System.out.println("Training...");

        MLPNN nn = new MLPNN(MLPNNConfiguration.INPUTS.length, HIDDEN_LAYER_SIZE, MLPNNConfiguration.OUTPUTS.length);

        nn.train(data.input, data.target, TRAIN_ITERATIONS, 0.002);

        try {
            MLPNNSetup setup = new MLPNNSetup(nn.mW1, nn.mW2, norm);
            writeSetup(MLPNNConfiguration.WEIGHTS_FILE, setup);
            System.out.println("OK, weights written to file");

            MLPNNSetup readSetup = readSetup(MLPNNConfiguration.WEIGHTS_FILE);
            System.out.println("OK, weights read from file");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private ControllerData readData() {
        File fileDir = new File(MLPNNConfiguration.FILE_FOLDER);
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
                    if (entries.length != MLPNNConfiguration.LINE_VALUES) {
                        throw new Exception("Line should contain " + MLPNNConfiguration.LINE_VALUES + " values");
                    }

                    inputVectors.add(parseValues(entries, MLPNNConfiguration.INPUTS));
                    targetVectors.add(parseValues(entries, MLPNNConfiguration.OUTPUTS));
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
        RealMatrix inputMatrix = new Array2DRowRealMatrix(inputVectors.size(), MLPNNConfiguration.INPUTS.length);
        RealMatrix targetMatrix = new Array2DRowRealMatrix(targetVectors.size(), MLPNNConfiguration.OUTPUTS.length);
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
        MatrixUtils.deserializeRealVector(setup.norm, "inputDiff", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "targetMin", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "targetDiff", ois);
        return setup;
    }

    private void writeSetup(String filename, MLPNNSetup setup) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        MatrixUtils.serializeRealMatrix(setup.W1, oos);
        MatrixUtils.serializeRealMatrix(setup.W2, oos);
        MatrixUtils.serializeRealVector(setup.norm.inputMin, oos);
        MatrixUtils.serializeRealVector(setup.norm.inputDiff, oos);
        MatrixUtils.serializeRealVector(setup.norm.targetMin, oos);
        MatrixUtils.serializeRealVector(setup.norm.targetDiff, oos);
        oos.close();
    }

    private static RealVector parseValues(String[] line, int[] indices) throws Exception {
        RealVector vector = new ArrayRealVector(indices.length);
        for (int i = 0; i < indices.length; i++) {
            double d = Double.parseDouble(line[indices[i]]);
            if (Double.isNaN(d)) {
                // TODO temp fix?
                d = 0;
            }
            vector.setEntry(i, d);
        }
        return vector;
    }

}
