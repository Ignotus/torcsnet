package storage;

import org.apache.commons.math3.linear.RealMatrix;

/**
 * Created by sander on 25/11/15.
 */
public class ControllerData {
    public RealMatrix input;
    public RealMatrix target;

    public ControllerData(RealMatrix input, RealMatrix target) {
        this.input = input;
        this.target = target;
    }
}
