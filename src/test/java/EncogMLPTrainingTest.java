import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.PersistBasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import storage.*;

import org.junit.Test;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Training procedure for Encog MLP with track data
 *
 */
public class EncogMLPTrainingTest {
    private static final int TRAIN_ITERATIONS = 10000;

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
        BasicMLDataSet dataSet = getData();

        // Create network
        BasicNetwork network = getNetwork();

        // Train
        System.out.println("Training network...");
        Train train = new ResilientPropagation(network, dataSet);
        for (int i = 0; i < TRAIN_ITERATIONS; i++) {
            train.iteration();
        }
        System.out.println("Training finished, error: " + train.getError());

        // Save to file
        System.out.println("Saving to file...");
        saveToFile(network);
        System.out.println("Done");
    }

    private void saveToFile(BasicNetwork network) {
        PersistBasicNetwork ps = new PersistBasicNetwork();
        try {
            FileOutputStream os = new FileOutputStream(Configuration.ENCOG_TRAINED_FILE);
            ps.save(os, network);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BasicNetwork getNetwork() {
        BasicNetwork network = new BasicNetwork();
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, INPUTS.length));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 25));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, OUTPUTS.length));
        network.getStructure().finalizeStructure();
        network.reset();
        return network;
    }

    private BasicMLDataSet getData() {
        TrainingData data = TrainingData.readData(Configuration.CSV_DIRECTORY, INPUTS, OUTPUTS, true);
        if (data == null) {
            System.out.println("No data read!");
            return null;
        }

        /* Prepare data */
        Normalization normData = Normalization.createNormalization(data.input, data.target);
        System.out.println("Norm min target: " + normData.targetMin);
        System.out.println("Norm max target: " + normData.targetMax);
        Normalization norm = EvolvedController.createDefaultNormalization();
        norm.normalizeInput(data.input, 0, 1);
        norm.normalizeTarget(data.target, 0, 1);
        return new BasicMLDataSet(data.input.getData(), data.target.getData());
    }

}
