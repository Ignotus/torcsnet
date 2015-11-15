import cicontest.torcs.client.Action;
import cicontest.torcs.client.SensorModel;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

    public static final int ACTION_ACCELERATION = sNumColumns++;
    public static final int ACTION_BRAKING = sNumColumns++;
    public static final int ACTION_STEERING = sNumColumns++;
    public static final int ACTION_GEAR = sNumColumns++;

    private static final int NUM_TRACK_EDGE_SENSORS = 19;

    private CSVWriter mDataFileWriter;

    public DataRecorder(String filename) throws IOException {
        System.out.println(filename);
        File dataFile = new File(filename);
        mDataFileWriter = new CSVWriter(new FileWriter(dataFile));
    }

    public void record(Action action, SensorModel sensors) {
        String[] columns = new String[sNumColumns];

        /* Sensors */
        columns[SENSOR_SPEED] = String.valueOf(sensors.getSpeed());
        columns[SENSORS_ANGLE_TO_TRACK_AXIS] = String.valueOf(sensors.getAngleToTrackAxis());
        columns[SENSOR_TRACK_POSITION] = String.valueOf(sensors.getTrackPosition());
        columns[SENSOR_RPM] = String.valueOf(sensors.getRPM());

        /* Track edge sensors */
        for (int i = 0; i < NUM_TRACK_EDGE_SENSORS; i++) {
            columns[SENSOR_TRACK_EDGE_1 + i] = String.valueOf(sensors.getTrackEdgeSensors()[i]);
        }

        /* Actions */
        columns[ACTION_ACCELERATION] = String.valueOf(action.accelerate);
        columns[ACTION_BRAKING] = String.valueOf(action.brake);
        columns[ACTION_STEERING] = String.valueOf(action.steering);
        columns[ACTION_GEAR] = String.valueOf(action.gear);

        mDataFileWriter.writeNext(columns);
    }

//    public void close() throws IOException {
////        mDataFileWriter.close();
//    }
//
//



}
