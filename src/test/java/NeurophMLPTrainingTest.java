import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;
import storage.*;
import org.apache.commons.math3.linear.*;
import org.junit.Test;

import java.util.Vector;

/**
 * Training procedure for Neuroph's MultilayerPerceptron with track data
 *
 */
public class NeurophMLPTrainingTest {
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

    @Test
    public void trainAndStore() {
        TrainingData data = TrainingData.readData(Configuration.CSV_DIRECTORY, INPUTS, OUTPUTS, true);
        if (data == null) {
            System.out.println("No data read!");
            return;
        }

        /* Normalize and train on the data */
        Normalization normData = Normalization.createNormalization(data.input, data.target);
        System.out.println("Norm min target: " + normData.targetMin);
        System.out.println("Norm max target: " + normData.targetMax);


        Normalization norm = EvolvedController.createDefaultNormalization();
        norm.normalizeInput(data.input, 0, 1);
        norm.normalizeTarget(data.target, 0, 1);

        TrainingSet trainingSet = new TrainingSet();
        for (int i = 0; i < data.input.getRowDimension(); i++) {
            TrainingElement row = new SupervisedTrainingElement(data.input.getRowVector(i).toArray(), data.target.getRowVector(i).toArray());
            trainingSet.addElement(row);
        }

        // Create network
        MultiLayerPerceptron mlp = new MultiLayerPerceptron(TransferFunctionType.SIGMOID,
                INPUTS.length, 20, OUTPUTS.length);
        BackPropagation backPropagation = new BackPropagation();
        backPropagation.setLearningRate(0.1);
        backPropagation.setMaxIterations(TRAIN_ITERATIONS);
        mlp.setLearningRule(backPropagation);

        // Train
        System.out.println("Training network...");
        mlp.learnInSameThread(trainingSet);
        System.out.println("Training finished");

        // Save to file
        double errorSum = distanceErrorSum(mlp, trainingSet);
        System.out.println("Network distance error sum on training: " + errorSum);
        System.out.println("Average: " + errorSum / trainingSet.size());
        System.out.println("Saving to file...");
        mlp.save(Configuration.NEUROPH_TRAINED_FILE);
    }

    private double distanceErrorSum(MultiLayerPerceptron nn, TrainingSet set) {
        double distanceErrorSum = 0.0;
        for (int i = 0; i < set.size(); i++) {
            SupervisedTrainingElement elem = (SupervisedTrainingElement)set.elementAt(i);
            nn.setInput(elem.getInput());
            nn.calculate();

            RealVector prediction = new ArrayRealVector(nn.getOutputAsArray());
            RealVector target = new ArrayRealVector(vectorToRealVector(elem.getDesiredOutput()));
            distanceErrorSum += prediction.getDistance(target);
        }
        return distanceErrorSum;
    }

    private RealVector vectorToRealVector(Vector<Double> vec) {
        RealVector v = new ArrayRealVector(vec.size());
        for (int i = 0; i < vec.size(); i++) {
            v.setEntry(i, vec.get(i));
        }
        return v;
    }

}
