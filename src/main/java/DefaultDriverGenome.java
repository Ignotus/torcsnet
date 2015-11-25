
import cicontest.torcs.genome.IGenome;
import storage.DataRecorder;

import java.io.IOException;

public class DefaultDriverGenome implements IGenome {

    private static final long serialVersionUID = 6534186543165341653L;
    private DataRecorder mDataRecorder = null;
    private NeuralNetworkController mController = null;
    
    public DefaultDriverGenome(DataRecorder dataRecorder, NeuralNetworkController controller) {
        mDataRecorder = dataRecorder;
        mController = controller;
    }

    public NeuralNetworkController getController() {
        return mController;
    }

    public DataRecorder getDataRecorder() {
        return mDataRecorder;
    }

}

