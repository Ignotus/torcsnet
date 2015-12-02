import org.ini4j.Ini;

import java.io.File;

public class Configuration {
    private static Ini ini = openIni("config.ini");

    private static Ini openIni(String filePath) {
        try {
            return new Ini(new File(filePath));
        } catch (java.io.IOException e) {
            return null;
        }
    }

    private static final String value(final String name, final String defaultValue) {
        if (ini == null) {
            return defaultValue;
        }

        final String str = ini.get("Configuration", name);
        if (str == null || str.isEmpty()) {
            return defaultValue;
        }
        return str;
    }

    public static final boolean RECORD_DATA = Boolean.parseBoolean(Configuration.value("record_data", "false"));
    public static final String PROP_FILE = Configuration.value("properties",
            "/Users/sander/GitHub/torcsnet/torcs.properties");
    public static final String OUTPUT_FILE = Configuration.value("output_file",
            "/Users/sander/GitHub/torcsnet/out.txt");

    // For MLP training
    // Should contain *.csv files for NN training
    public static final String CSV_DIRECTORY = Configuration.value("csv_directory",
            "/Users/sander/Downloads/without_noise");
    public static final int LINE_VALUES = Integer.parseInt(Configuration.value("line_values", "27"));
    public static final String WEIGHTS_FILE = Configuration.value("weights_file",
            "/Users/sander/GitHub/torcsnet/src/main/resources/memory/weights.dump");

    // For training with 3 separate NNs
    public static final String WEIGHTS_FILE_ACCEL = Configuration.value("weights_accel_file",
            "/Users/sander/GitHub/torcsnet/src/main/resources/memory/weights_accel.dump");

    public static final String WEIGHTS_FILE_STEERING = Configuration.value("weights_steering_file",
            "/Users/sander/GitHub/torcsnet/src/main/resources/memory/weights_steering.dump");

    public static final String WEIGHTS_FILE_BRAKING = Configuration.value("weights_braking_file",
            "/Users/sander/GitHub/torcsnet/src/main/resources/memory/weights_braking.dump");

}