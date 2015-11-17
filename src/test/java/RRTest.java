import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.MathUtils;
import org.junit.Test;
import org.knowm.xchart.Chart;
import org.knowm.xchart.ChartBuilder;
import org.knowm.xchart.StyleManager;
import scala.Tuple2;

/**
 * Created by sander on 17/11/15.
 */
public class RRTest {

    @Test
    public void testTrainingAndPrediction() {
        System.out.println("RR Test started");

        /* Generate sinusoidal input data */
        RealVector x = Utils.linspace(0, MathUtils.TWO_PI, 1000);
        RealMatrix X = new Array2DRowRealMatrix(x.getDimension(), 1);
        RealVector Y1 = x.map(v -> Math.sin(v));
        RealVector Y2 = x.map(v -> Math.sin(v) + 1);
        for (int i = 0; i < x.getDimension(); i++) {
            X.setRow(i, new double[]{x.getEntry(i)});
        }

        /* Train */
        ReservoirRegression nn = new ReservoirRegression(X.getColumnDimension(), 20);
        nn.train(X, Y1, Y2, 100, 0.001);

        /* Get predictions */
        double[] Y1Predicted = new double[Y1.getDimension()];
        double[] Y2Predicted = new double[Y2.getDimension()];
        for (int i = 0; i < x.getDimension(); i++) {
            Tuple2<Object, Object> pred = nn.predict(X.getRowVector(i));
            Y1Predicted[i] = (double) pred._1();
            Y2Predicted[i] = (double) pred._2();
        }

        /* Save results to chart */
        double[] xplot = x.toArray();
        Chart chart = new ChartBuilder().width(800).height(600).theme(StyleManager.ChartTheme.Matlab).build();
        chart.addSeries("sin(x)", xplot, Y1.toArray());
        chart.addSeries("sin(x) + 1", xplot, Y2.toArray());
        chart.addSeries("pred sin(x)", xplot, Y1Predicted);
        chart.addSeries("pred sin(x) + 1", xplot, Y2Predicted);
        Utils.saveChart(chart, "sinus_reservoir");
    }

}
