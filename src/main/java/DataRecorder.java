import cicontest.torcs.client.Action;
import cicontest.torcs.client.SensorModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class DataRecorder {

    private static final String FORMAT = "speed angleToTrackAxis trackPosition rpm trackEdgeSensors(x19) accelerate " +
            "brake steering gear";
    private static final String COMMENT = "# ";
    private static final String SEPARATOR = " ";
    private static final String LINE_SEPARATOR = "\n";

    private PrintWriter mDataFileWriter;

    public DataRecorder(String filename) throws FileNotFoundException {
        File dataFile = new File(filename);
        mDataFileWriter = new PrintWriter(dataFile);
        mDataFileWriter.write(COMMENT + FORMAT + LINE_SEPARATOR);
    }

    public void record(Action action, SensorModel sensors) {
        StringBuilder builder = new StringBuilder();

        /* Sensors */
        builder.append(sensors.getSpeed()).append(SEPARATOR);
        builder.append(sensors.getAngleToTrackAxis()).append(SEPARATOR);
        builder.append(sensors.getTrackPosition()).append(SEPARATOR);
        builder.append(sensors.getRPM()).append(SEPARATOR);

        /* Track edge sensors */
        for (int i = 0; i < sensors.getTrackEdgeSensors().length; i++) {
            builder.append(sensors.getTrackEdgeSensors()[i]).append(SEPARATOR);
        }

        /* Actions */
        builder.append(action.accelerate).append(SEPARATOR);
        builder.append(action.brake).append(SEPARATOR);
        builder.append(action.steering).append(SEPARATOR);
        builder.append(action.gear).append(SEPARATOR);

        builder.append(LINE_SEPARATOR);
        mDataFileWriter.write(builder.toString());
    }


}
