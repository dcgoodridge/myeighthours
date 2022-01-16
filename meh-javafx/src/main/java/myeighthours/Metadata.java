package myeighthours;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Metadata {

    private static final Logger LOG = LoggerFactory.getLogger(Metadata.class);

    private static final String BUILD_DATE_STRING;

    private static final String APP_NAME_SHORT = "MEH";

    private static final String APP_NAME_LONG = "My Eight Hours";

    static {
        BUILD_DATE_STRING = computeBuildDateString();
    }

    private static String computeBuildDateString() {
        DateTimeFormatter FORMATO_FECHA_FICHAJE_WEB = DateTimeFormat.forPattern("d MMM yyyy HH:mm");
        String reportDate = FORMATO_FECHA_FICHAJE_WEB.print(BuildConfig.BUILD_UNIXTIME);
        return reportDate;
    }

    public static String appName() {
        return APP_NAME_SHORT;
    }

    public static String appNameAndVersion() {
        return APP_NAME_SHORT + " " + versionName();
    }

    public static String appLongNameAndVersion() {
        return APP_NAME_LONG + " " + versionName();
    }

    public static String versionName() {
        return BuildConfig.METADATA_VERSION;
    }

    public static long buildDate() {
        return BuildConfig.BUILD_UNIXTIME;
    }

    public static String buildDateString() {
        return BUILD_DATE_STRING;
    }

}
