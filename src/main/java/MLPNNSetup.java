import storage.Normalization;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Created by sander on 25/11/15.
 */
public class MLPNNSetup {
    public RealMatrix W1;
    public RealMatrix W2;
    public Normalization norm = new Normalization();

    public MLPNNSetup(RealMatrix W1, RealMatrix W2, Normalization norm) {
        this.W1 = W1;
        this.W2 = W2;
        this.norm = norm;
    }

    public MLPNNSetup() {}
}
