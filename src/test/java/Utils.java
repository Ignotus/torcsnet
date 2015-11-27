import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.Chart;

import java.io.IOException;

public class Utils {
    public static void saveChart(Chart chart, String name) {
        try {
            /* TODO: Maybe change output folder location */
            BitmapEncoder.saveBitmapWithDPI(chart, "/tmp/" + name + ".png", BitmapEncoder.BitmapFormat.PNG, 300);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static RealVector linspace(double start, double end, int n) {
        RealVector vector = new ArrayRealVector(n);
        double step = (end - start) / (n - 1);
        vector.setEntry(0, start);
        vector.setEntry(n - 1, end);
        for (int i = 1; i < n - 1; i++) {
            vector.setEntry(i, vector.getEntry(i - 1) + step);
        }
        return vector;
    }
}
