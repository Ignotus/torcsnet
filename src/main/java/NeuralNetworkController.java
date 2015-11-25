import cicontest.torcs.client.SensorModel;

import java.io.IOException;

public interface NeuralNetworkController {
	void initialize(String weightsFile) throws IOException, ClassNotFoundException;
	void updatePredictions(SensorModel model);
	double getAcceleration();
	double getSteering();
	double getBraking();
}
