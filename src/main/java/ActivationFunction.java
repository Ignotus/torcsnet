/**
 * Created by sander on 30/11/15.
 */
public interface ActivationFunction {
    double derivative(double y);
    double value(double x);
}