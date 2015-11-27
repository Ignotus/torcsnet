import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.MathUtils;
import org.junit.Test;
import org.knowm.xchart.Chart;
import org.knowm.xchart.ChartBuilder;
import org.knowm.xchart.StyleManager;

public class MLPNNTest {
    @Test
    public void testTrainingAndPrediction() {
        System.out.println("MLPNN Test started");

        /* Generate sinusoidal input data */
        RealVector x = Utils.linspace(0, MathUtils.TWO_PI, 100);
        RealMatrix X = new Array2DRowRealMatrix(x.getDimension(), 1);
        RealVector y = x.map(v -> Math.sin(v) / 2 + 0.5);
        RealMatrix Y = new Array2DRowRealMatrix(y.getDimension(), 1);

        X.setColumnVector(0, x);
        Y.setColumnVector(0, y);

        MLPNN nn = new MLPNN(1, 30, 1);

        long start = System.currentTimeMillis();
        // Train for 100 iterations
        nn.train(X, Y, 10000, 0.2);
        System.out.println("MLPNNTest: Training took " + (System.currentTimeMillis() - start) + "ms");

        /* Get predictions */
        double squaredError = 0.0;
        double[] yPredicted = new double[y.getDimension()];
        start = System.currentTimeMillis();
        for (int i = 0; i < x.getDimension(); i++) {
            RealVector pred = nn.predict(X.getRowVector(i));
            yPredicted[i] = pred.getEntry(1);
            squaredError += Math.pow((yPredicted[i] - y.getEntry(i)), 2);
            System.out.println("yValue: " + y.getEntry(i));
            System.out.println("yPred: " + yPredicted[i]);

        }
        System.out.println("MLPNNTest: squared error: " + squaredError);
        System.out.println("MLPNNTest: Predicting took " + (System.currentTimeMillis() - start) + "ms");

        /* Save results to chart */
        double[] xplot = x.toArray();
        Chart chart = new ChartBuilder().width(800).height(600).theme(StyleManager.ChartTheme.Matlab).build();

        chart.addSeries("sin", xplot, y.toArray());
        chart.addSeries("pred", xplot, yPredicted);
        Utils.saveChart(chart, "mlpnn");
    }
}
