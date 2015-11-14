
import cicontest.torcs.genome.IGenome;

public class DefaultDriverGenome implements IGenome {

    private static final long serialVersionUID = 6534186543165341653L;
    private NeuralNetwork myNN = new NeuralNetwork(10,8,2);
    private DataRecorder mDataRecorder = null;

    public DefaultDriverGenome() {}

    public DefaultDriverGenome(DataRecorder dataRecorder) {
        mDataRecorder = dataRecorder;
    }

    public NeuralNetwork getMyNN() {
        return myNN;
    }

    public DataRecorder getDataRecorder() {
        return mDataRecorder;
    }

}

