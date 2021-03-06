
import cicontest.torcs.genome.IGenome;
import storage.DataRecorder;

import java.io.IOException;

public class DefaultDriverGenome implements IGenome {

    private static final long serialVersionUID = 6534186543165341653L;
    private DataRecorder mDataRecorder = null;
    private NeuralNetworkController mController = null;

    public DefaultDriverGenome(NeuralNetworkController controller) {
        this.mController = controller;
    }

    public DefaultDriverGenome() {
        // init NN
        try {
            //mController = SingleMLPController.initializeController("/memory/weights.dump");
            mController = MultiMLPController.initializeController("/memory/weights_accel.dump",
                    "/memory/weights_steering.dump", "/memory/weights_braking.dump");
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

