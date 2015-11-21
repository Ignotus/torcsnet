import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.util.MathUtils;
import org.junit.Test;
import org.knowm.xchart.Chart;
import org.knowm.xchart.ChartBuilder;
import org.knowm.xchart.StyleManager;
import scala.Tuple2;

/**
 * Created by sander on 17/11/15.
 */
public class MLPTest {

    @Test
    public void testTrainingAndPrediction() {
        System.out.println("MLP Test started");

        /* Generate sinusoidal input data */
        RealVector x = Utils.linspace(0, MathUtils.TWO_PI, 100);
        RealMatrix X = new Array2DRowRealMatrix(x.getDimension(), 1);
        RealVector y = x.map(v -> Math.sin(v) / 2 + 0.5);
        for (int i = 0; i < x.getDimension(); ++i) {
            X.setRow(i, new double[]{x.getEntry(i)});
        }

        MLPSetup setup = new MLPSetup(1, 10);
        MLP nn = new MLP(setup);

        nn.train(X, y, 100, 0.1);

        /* Get predictions */
        double[] yPredicted = new double[y.getDimension()];
        for (int i = 0; i < x.getDimension(); ++i) {
            yPredicted[i] = nn.predict(X.getRowVector(i));
        }

        /* Save results to chart */
        double[] xplot = x.toArray();
        Chart chart = new ChartBuilder().width(800).height(600).theme(StyleManager.ChartTheme.Matlab).build();
        
        chart.addSeries("sin", xplot, y.toArray());
        chart.addSeries("pred", xplot, yPredicted);
        Utils.saveChart(chart, "mlp");
    }

}
