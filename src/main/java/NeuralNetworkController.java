import cicontest.torcs.client.SensorModel;

public interface NeuralNetworkController {

    void updatePredictions(SensorModel model);

    double getAcceleration();

    double getSteering();

    double getBraking();

}
