
import cicontest.torcs.genome.IGenome;

public class DefaultDriverGenome implements IGenome {

    private static final long serialVersionUID = 6534186543165341653L;
    private NNController mNNController;
    private DataRecorder mDataRecorder;

    public DefaultDriverGenome(DataRecorder dataRecorder, NNController nnController) {
        mDataRecorder = dataRecorder;
        mNNController = nnController;
    }

    public NNController getNNController() {
        return mNNController;
    }

    public DataRecorder getDataRecorder() {
        return mDataRecorder;
    }

}

