package myeighthours.helper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class InstantHelper {

    public static boolean isSameDay(long timestamp1, long timestamp2) {
        Instant instant1 = Instant.ofEpochMilli(timestamp1);
        Instant instant2 = Instant.ofEpochMilli(timestamp2);
        return isSameDay(instant1,instant2);
    }

    public static boolean isSameDay(Instant instant1, Instant instant2) {
        if (instant1 == null || instant2 == null) throw new IllegalArgumentException("The dates must not be null");
        int comparationResult = instant1.truncatedTo(ChronoUnit.DAYS).compareTo(instant2.truncatedTo(ChronoUnit.DAYS));
        return comparationResult==0;
    }

}
