package storage;

import cicontest.torcs.client.Action;
import cicontest.torcs.client.SensorModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class DataRecorder {

    private static int sNumColumns = 0;

    public static final int SENSOR_SPEED = sNumColumns++;
    public static final int SENSORS_ANGLE_TO_TRACK_AXIS = sNumColumns++;
    public static final int SENSOR_TRACK_POSITION = sNumColumns++;
    public static final int SENSOR_RPM = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_1 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_2 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_3 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_4 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_5 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_6 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_7 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_8 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_9 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_10 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_11 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_12 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_13 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_14 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_15 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_16 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_17 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_18 = sNumColumns++;
    public static final int SENSOR_TRACK_EDGE_19 = sNumColumns++;
    public static final int NUM_SENSORS = SENSOR_TRACK_EDGE_19 - SENSOR_SPEED + 1;

    public static final int ACTION_ACCELERATION = sNumColumns++;
    public static final int ACTION_BRAKING = sNumColumns++;
    public static final int ACTION_STEERING = sNumColumns++;
    public static final int ACTION_GEAR = sNumColumns++;

    public static final String SEPARATOR = ", ";
    public static final String LINE_SEPARATOR = "\n";

    private static final int NUM_TRACK_EDGE_SENSORS = 19;

    private PrintWriter mDataFileWriter;

    public DataRecorder(String filename) throws IOException {
        File dataFile = new File(filename);
        mDataFileWriter = new PrintWriter(new FileWriter(dataFile));
    }

    public void record(Action action, SensorModel sensors) {
        StringBuilder builder = new StringBuilder();

        /* Sensors */
        builder.append(sensors.getSpeed()).append(SEPARATOR);
        builder.append(sensors.getAngleToTrackAxis()).append(SEPARATOR);
        builder.append(sensors.getTrackPosition()).append(SEPARATOR);
        builder.append(sensors.getRPM()).append(SEPARATOR);

        /* Track edge sensors */
        for (int i = 0; i < NUM_TRACK_EDGE_SENSORS; i++) {
            builder.append(sensors.getTrackEdgeSensors()[i]).append(SEPARATOR);
        }

        /* Actions */
        builder.append(action.accelerate).append(SEPARATOR);
        builder.append(action.brake).append(SEPARATOR);
        builder.append(action.steering).append(SEPARATOR);
        builder.append(action.gear);

        builder.append(LINE_SEPARATOR);
        mDataFileWriter.write(builder.toString());
    }

    public void close() throws IOException {
        mDataFileWriter.close();
    }

}
