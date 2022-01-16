import myeighthours.CalendarioLaboral;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class CalendarioLaboralTest {


    private static final DateTimeZone DATETIMEZONE_LOCAL = DateTimeZone.getDefault();


    @Test
    public void testEsJornadaReducida_Agosto2017() throws Exception {
        DateTime lunes_20170814_1 = new DateTime(2017, 8, 14, 0, 1, DATETIMEZONE_LOCAL);
        DateTime lunes_20170814_2 = new DateTime(2017, 8, 14, 23, 59, DATETIMEZONE_LOCAL);

        DateTime martes_20170815_1 = new DateTime(2017, 8, 15, 0, 1, DATETIMEZONE_LOCAL);
        DateTime martes_20170815_2 = new DateTime(2017, 8, 15, 23, 59, DATETIMEZONE_LOCAL);

        DateTime miercoles_20170816_1 = new DateTime(2017, 8, 16, 0, 1, DATETIMEZONE_LOCAL);
        DateTime miercoles_20170816_2 = new DateTime(2017, 8, 16, 23, 59, DATETIMEZONE_LOCAL);

        DateTime jueves_20170817_1 = new DateTime(2017, 8, 17, 0, 1, DATETIMEZONE_LOCAL);
        DateTime jueves_20170817_2 = new DateTime(2017, 8, 17, 23, 59, DATETIMEZONE_LOCAL);

        DateTime viernes_20170818_1 = new DateTime(2017, 8, 18, 0, 1, DATETIMEZONE_LOCAL);
        DateTime viernes_20170818_2 = new DateTime(2017, 8, 18, 23, 59, DATETIMEZONE_LOCAL);

        DateTime sabado_20170819_1 = new DateTime(2017, 8, 19, 0, 1, DATETIMEZONE_LOCAL);
        DateTime sabado_20170819_2 = new DateTime(2017, 8, 19, 23, 59, DATETIMEZONE_LOCAL);

        assertThat(CalendarioLaboral.esJornadaReducida(lunes_20170814_1.getMillis()), is(false));
        assertThat(CalendarioLaboral.esJornadaReducida(lunes_20170814_2.getMillis()), is(false));

        assertThat(CalendarioLaboral.esJornadaReducida(martes_20170815_1.getMillis()), is(false));
        assertThat(CalendarioLaboral.esJornadaReducida(martes_20170815_2.getMillis()), is(false));

        assertThat(CalendarioLaboral.esJornadaReducida(miercoles_20170816_1.getMillis()), is(false));
        assertThat(CalendarioLaboral.esJornadaReducida(miercoles_20170816_2.getMillis()), is(false));

        assertThat(CalendarioLaboral.esJornadaReducida(jueves_20170817_1.getMillis()), is(false));
        assertThat(CalendarioLaboral.esJornadaReducida(jueves_20170817_2.getMillis()), is(false));

        assertThat(CalendarioLaboral.esJornadaReducida(viernes_20170818_1.getMillis()), is(true));
        assertThat(CalendarioLaboral.esJornadaReducida(viernes_20170818_2.getMillis()), is(true));

        assertThat(CalendarioLaboral.esJornadaReducida(sabado_20170819_1.getMillis()), is(false));
        assertThat(CalendarioLaboral.esJornadaReducida(sabado_20170819_2.getMillis()), is(false));

    }

}
