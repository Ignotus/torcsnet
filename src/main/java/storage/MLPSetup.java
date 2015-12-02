package storage;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.*;

public class MLPSetup {
    public RealMatrix W1;
    public RealMatrix W2;
    public Normalization norm = new Normalization();

    public MLPSetup(RealMatrix W1, RealMatrix W2, Normalization norm) {
        this.W1 = W1;
        this.W2 = W2;
        this.norm = norm;
    }

    public int getNumInputs() {
        return W1.getColumnDimension() - 1; // skip bias neuron
    }

    public int getNumHiddens() {
        return W1.getRowDimension() - 1;
    }

    public int getNumOutputs() {
        return W2.getRowDimension() - 1;
    }

    public MLPSetup() {}

    /* I/O methods */
    /* Read NN weights and normalization */
    public static MLPSetup readSetup(String filename) throws IOException, ClassNotFoundException {
        System.out.println("Loading weights " + MLPSetup.class.getResource(filename).getFile());

        MLPSetup setup = new MLPSetup();
        InputStream fis = MLPSetup.class.getResourceAsStream(filename);
        ObjectInputStream ois = new ObjectInputStream(fis);
        MatrixUtils.deserializeRealMatrix(setup, "W1", ois);
        MatrixUtils.deserializeRealMatrix(setup, "W2", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "inputMin", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "inputMax", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "targetMin", ois);
        MatrixUtils.deserializeRealVector(setup.norm, "targetMax", ois);
        return setup;
    }

    public static MLPSetup[] readSetups(String[] filenames) throws IOException, ClassNotFoundException {
        MLPSetup[] setups = new MLPSetup[filenames.length];
        for (int i = 0; i < filenames.length; i++) {
            setups[i] = readSetup(filenames[i]);
        }
        return setups;
    }

    public void writeSetup(String filename, MLPSetup setup) throws IOException {
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

}
