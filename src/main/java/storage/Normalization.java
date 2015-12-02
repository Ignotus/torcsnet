package storage;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Created by sander on 24/11/15.
 */
public class Normalization {
    public RealVector inputMin;
    public RealVector inputMax;
    public RealVector targetMin;
    public RealVector targetMax;

    public Normalization() {}

    public static Normalization createNormalization(RealMatrix data, RealMatrix target) {
        Normalization norm = new Normalization();
        norm.inputMin = new ArrayRealVector(data.getColumnDimension());
        norm.inputMax = new ArrayRealVector(data.getColumnDimension());
        norm.targetMin = new ArrayRealVector(target.getColumnDimension());
        norm.targetMax = new ArrayRealVector(target.getColumnDimension());

        for (int i = 0; i < data.getColumnDimension(); i++) {
            RealVector vec = data.getColumnVector(i);
            norm.inputMin.setEntry(i, vec.getMinValue());
            norm.inputMax.setEntry(i, vec.getMaxValue());
        }

        for (int i = 0; i < target.getColumnDimension(); i++) {
            RealVector vec = target.getColumnVector(i);
            norm.targetMin.setEntry(i, vec.getMinValue());
            norm.targetMax.setEntry(i, vec.getMaxValue());
        }

        return norm;
    }

    /* Normalize data to bounds given by min and max */
    public void normalizeInput(RealMatrix m, double toMin, double toMax) {
        for (int i = 0; i < m.getColumnDimension(); i++) {
            double fromMin = inputMin.getEntry(i);
            double fromMax = inputMax.getEntry(i);
            for (int j = 0; j < m.getRowDimension(); j++) {
                m.setEntry(j, i, normValue(m.getEntry(j, i), fromMin, fromMax, toMin, toMax));
            }
        }
    }

    /* Normalize data to interval between toMin and toMax */
    public void normalizeTarget(RealMatrix m, double toMin, double toMax) {
        for (int i = 0; i < m.getColumnDimension(); i++) {
            for (int j = 0; j < m.getRowDimension(); j++) {
                m.setEntry(j, i, normValue(m.getEntry(j, i), targetMin.getEntry(i), targetMax.getEntry(i), toMin, toMax));
            }
        }
    }

    public void normalizeInputVector(RealVector v, double toMin, double toMax) {
        for (int i = 0; i < v.getDimension(); i++) {
            v.setEntry(i, normValue(v.getEntry(i), inputMin.getEntry(i), inputMax.getEntry(i), toMin, toMax));
        }
    }

    public void denormalizeOutput(RealVector v, double lower, double upper) {
        for (int i = 0; i < v.getDimension(); i++) {
            v.setEntry(i, denormValue(v.getEntry(i), targetMin.getEntry(i), targetMax.getEntry(i), lower, upper));
        }
    }

    private static double normValue(double v, double dataMin, double dataMax, double toMin, double toMax) {
        return ((v - dataMin) / (dataMax - dataMin)) * (toMax - toMin) + toMin;
    }

    private static double denormValue(double normValue, double dataMin, double dataMax, double imin, double imax) {
        return ((normValue - imin) / (imax - imin)) * (dataMax - dataMin) + dataMin;
    }

}