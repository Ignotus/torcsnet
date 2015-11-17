import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.MathUtils;
import org.junit.Test;
import org.knowm.xchart.Chart;
import org.knowm.xchart.ChartBuilder;
import org.knowm.xchart.StyleManager;

public class LRTest {
    @Test
    public void testTrainingAndPrediction() {
        System.out.println("LR Test started");

        /* Generate sinusoidal input data */
        RealVector x = Utils.linspace(0, MathUtils.TWO_PI, 1000);
        RealMatrix X = new Array2DRowRealMatrix(x.getDimension(), 1);
        RealVector y = x.map(v -> 1 / (1 + Math.exp(-6.5 * v + 4)));
        for (int i = 0; i < x.getDimension(); ++i) {
            X.setRow(i, new double[]{x.getEntry(i)});
        }

        LogisticRegressionSetup setup = new LogisticRegressionSetup(1);
        /* Train */
        LogisticRegression nn = new LogisticRegression(setup);
        nn.train(X, y, 100, 1);

        /* Get predictions */
        double[] yPredicted = new double[y.getDimension()];
        for (int i = 0; i < x.getDimension(); ++i) {
            yPredicted[i] = nn.predict(X.getRowVector(i));
        }

        /* Save results to chart */
        double[] xplot = x.toArray();
        Chart chart = new ChartBuilder().width(800).height(600).theme(StyleManager.ChartTheme.Matlab).build();
        
        chart.addSeries("pred sin(x)", xplot, yPredicted);
        Utils.saveChart(chart, "lr");
    }


}
