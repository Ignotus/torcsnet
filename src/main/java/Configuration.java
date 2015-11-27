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
            "/home/ignotus/Development/torcsnet/torcs.properties");
    public static final String OUTPUT_FILE = Configuration.value("output_file",
            "/home/ignotus/Development/torcsnet/out.txt");

    // For MLP training
    // Should contain *.csv files for NN training
    public static final String CSV_DIRECTORY = Configuration.value("csv_directory",
            "/home/ignotus/Development/torcsnet/data/");
    public static final int LINE_VALUES = Integer.parseInt(Configuration.value("line_values", "27"));
    public static final String WEIGHTS_FILE = Configuration.value("weights_file",
            "/home/ignotus/Development/torcsnet/weights.dump");
}