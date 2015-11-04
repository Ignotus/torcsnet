
import cicontest.torcs.genome.IGenome;

public class DefaultDriverGenome implements IGenome {
    private static final long serialVersionUID = 6534186543165341653L;
    private NeuralNetwork myNN = new NeuralNetwork(10,8,2);
    public NeuralNetwork getMyNN() {
        return myNN;
    }
}

