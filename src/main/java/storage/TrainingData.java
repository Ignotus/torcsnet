package storage;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * Created by sander on 02/12/15.
 */
public class TrainingData {
    public RealMatrix input;
    public RealMatrix target;

    public TrainingData(RealMatrix input, RealMatrix target) {
        this.input = input;
        this.target = target;
    }

    /* Reads all CSV files in the directory,
     * returns input and target matrices for the neural network*/
    public static TrainingData readData(String directory, int[] inputIndices, int[] targetIndices, boolean shuffle) {
        File fileDir = new File(directory);
        System.out.println(fileDir);
        File[] files = fileDir.listFiles();
        if (files == null) {
            System.out.println("No data files found in directory " + directory);
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
                    if (entries.length != 27) {
                        System.out.println("Unexpected CSV entry count!");
                    }

                    inputVectors.add(parseValues(entries, inputIndices));
                    targetVectors.add(parseValues(entries, targetIndices));
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

        /* Construct matrices */
        int numRows = inputVectors.size();
        RealMatrix inputMatrix = new Array2DRowRealMatrix(numRows, inputIndices.length);
        RealMatrix targetMatrix = new Array2DRowRealMatrix(numRows, targetIndices.length);

        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < numRows; i++) {
            indices.add(i);
        }
        if (shuffle) {
            Collections.shuffle(indices);
        }

        for (int i = 0; i < numRows; i++) {
            inputMatrix.setRowVector(i, inputVectors.get(indices.get(i)));
        }
        for (int i = 0; i < numRows; i++) {
            targetMatrix.setRowVector(i, targetVectors.get(indices.get(i)));
        }

        return new TrainingData(inputMatrix, targetMatrix);
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