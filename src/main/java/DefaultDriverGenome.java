
import cicontest.torcs.genome.IGenome;
import storage.DataRecorder;

import java.io.IOException;

public class DefaultDriverGenome implements IGenome {

    private static final long serialVersionUID = 6534186543165341653L;
    private DataRecorder mDataRecorder = null;
    private NeuralNetworkController mController = null;
    
    public DefaultDriverGenome() {
        // init NN
        try {
            mController = MLPNNController.initializeController("/memory/weights.dump");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setDataRecorder(DataRecorder recorder) {
        mDataRecorder = recorder;
    }

    public NeuralNetworkController getController() {
        return mController;
    }

    public DataRecorder getDataRecorder() {
        return mDataRecorder;
    }

}

