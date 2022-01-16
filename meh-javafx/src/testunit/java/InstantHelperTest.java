import myeighthours.helper.InstantHelper;
import org.junit.Test;

import java.time.Instant;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class InstantHelperTest {

    @Test
    public void testIsSameDay_unixepoch() {
        Instant lunes_20170814_1 = Instant.parse("2017-08-14T00:00:01.00Z");
        Instant lunes_20170814_2 = Instant.parse("2017-08-14T23:59:59.00Z");
        Instant martes_20170815_1 = Instant.parse("2017-08-15T00:00:01.00Z");

        assertThat(InstantHelper.isSameDay(lunes_20170814_1.toEpochMilli(), lunes_20170814_2.toEpochMilli()), is(true));
        assertThat(InstantHelper.isSameDay(lunes_20170814_1.toEpochMilli(), martes_20170815_1.toEpochMilli()), is(false));
    }

    @Test
    public void testIsSameDay_instant() {
        Instant lunes_20170814_1 = Instant.parse("2017-08-14T00:00:01.00Z");
        Instant lunes_20170814_2 = Instant.parse("2017-08-14T23:59:59.00Z");
        Instant martes_20170815_1 = Instant.parse("2017-08-15T00:00:01.00Z");

        assertThat(InstantHelper.isSameDay(lunes_20170814_1, lunes_20170814_2), is(true));
        assertThat(InstantHelper.isSameDay(lunes_20170814_1, martes_20170815_1), is(false));
    }

}
