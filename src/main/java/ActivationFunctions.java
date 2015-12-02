
/**
 * Created by sander on 30/11/15.
 */
public class ActivationFunctions {
    public static class Sigmoid extends org.apache.commons.math3.analysis.function.Sigmoid implements ActivationFunction {
        @Override
        public double derivative(double sigmoidValue) {
            return sigmoidValue * (1.0 - sigmoidValue);
        }
    }

    public static class Tanh extends org.apache.commons.math3.analysis.function.Tanh implements ActivationFunction {
        @Override
        public double derivative(double tanhValue) {
            return 1.0 - (tanhValue * tanhValue);
        }
    }
}
