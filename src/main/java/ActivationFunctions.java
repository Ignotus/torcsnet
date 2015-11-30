import org.apache.commons.math3.analysis.function.Sigmoid;

/**
 * Created by sander on 30/11/15.
 */
public class ActivationFunctions {
    public interface ActivationFunction {
        double value(double x);
        double derivative(double y);
    }

    public class Sigmoid extends org.apache.commons.math3.analysis.function.Sigmoid implements ActivationFunction {
        @Override
        public double derivative(double y) {
            return 0;
        }
    }

}
