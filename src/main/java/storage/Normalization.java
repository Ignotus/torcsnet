package storage;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Created by sander on 24/11/15.
 */
public class Normalization {
    public RealVector inputMin;
    public RealVector inputDiff; // max - min
    public RealVector targetMin;
    public RealVector targetDiff;

    public Normalization() {}

    public static Normalization createNormalization(RealMatrix data, RealMatrix target) {
        Normalization norm = new Normalization();
        norm.inputMin = new ArrayRealVector(data.getColumnDimension());
        norm.inputDiff = new ArrayRealVector(data.getColumnDimension());
        norm.targetMin = new ArrayRealVector(target.getColumnDimension());
        norm.targetDiff = new ArrayRealVector(target.getColumnDimension());

        for (int i = 0; i < data.getColumnDimension(); i++) {
            double min = data.getColumnVector(i).getMinValue();
            double max = data.getColumnVector(i).getMaxValue();
            norm.inputMin.setEntry(i, min);
            norm.inputDiff.setEntry(i, max - min);
        }

        for (int i = 0; i < target.getColumnDimension(); i++) {
            double min = target.getColumnVector(i).getMinValue();
            double max = target.getColumnVector(i).getMaxValue();
            norm.targetMin.setEntry(i, min);
            norm.targetDiff.setEntry(i, max - min);
        }

        return norm;
    }

    /* Normalize data to interval between 0 and 1 */
    public void normalizeInput(RealMatrix m) {
        for (int i = 0; i < m.getColumnDimension(); i++) {
            double min = inputMin.getEntry(i);
            double diff = inputDiff.getEntry(i);
            for (int j = 0; j < m.getRowDimension(); j++) {
                m.setEntry(j, i, (m.getEntry(j, i) - min) / diff);
            }
        }
    }

    /* Normalize data to interval between 0 and 1 */
    public void normalizeTarget(RealMatrix m) {
        for (int i = 0; i < m.getColumnDimension(); i++) {
            double min = targetMin.getEntry(i);
            double diff = targetDiff.getEntry(i);
            for (int j = 0; j < m.getRowDimension(); j++) {
                m.setEntry(j, i, (m.getEntry(j, i) - min) / diff);
            }
        }
    }

    public void normalizeInputVector(RealVector v) {
        for (int i = 0; i < v.getDimension(); i++) {
            double min = inputMin.getEntry(i);
            double diff = inputDiff.getEntry(i);
            v.setEntry(i, (v.getEntry(i) - min) / diff);
        }
    }

    public void denormalizeOutput(RealVector v) {
        for (int i = 0; i < targetMin.getDimension(); i++) {
            v.setEntry(i, v.getEntry(i) * targetDiff.getEntry(i) + targetMin.getEntry(i));
        }
    }

}