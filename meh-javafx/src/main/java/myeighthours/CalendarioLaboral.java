package myeighthours;


import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import static org.joda.time.DateTimeConstants.SATURDAY;
import static org.joda.time.DateTimeConstants.SUNDAY;

public class CalendarioLaboral {


    public enum TIPO_JORNADA {JORNADA_ESTANDAR, JORNADA_REDUCIDA, JORNADA_RECUPERAR, JORNADA_FESTIVA}

    public static final DateTimeZone DATETIMEZONE_LOCAL = DateTimeZone.getDefault();

    public final static long ONE_MINUTE_MILLIS = 60 * 1000L;

    public final static long ONE_HOUR_MILLIS = ONE_MINUTE_MILLIS * 60;

    public final static long EIGHT_HOUR_MILLIS = ONE_HOUR_MILLIS * 8;

    public final static long DAY_MILLIS = ONE_HOUR_MILLIS * 24;

    public final static long WEEK_MILLIS = DAY_MILLIS * 7;

    public final static long SCHEDULE_UPDATE_SECONDS = 5;

    public final static long MAX_DESCANSO_COMIDA_MILLIS = 45 * 60 * 1000L;

    private static final long JORNADA_REDUCIDA_MILLIS = ( (6 * 60) + 15) * 60 * 1000L; // 6h 15min

    private static final long JORNADA_ESTANDAR_MILLIS = ( (8 * 60) + 15) * 60 * 1000L; // 8h 15m fichados de trabajo; (9h menos los 45min comer)

    private static final long JORNADA_RECUPERAR_MILLIS = JORNADA_ESTANDAR_MILLIS + (15 * 60 * 1000L); // ESTANDAR + 15min

    private static final long JORNADA_FESTIVA_MILLIS = 0;

    private static final long[] JORNADA_REDUCIDA_EPOCH_DAYS = {
            yearMonthDay(2017, 6, 9),
            yearMonthDay(2017, 6, 16),
            yearMonthDay(2017, 6, 23),
            yearMonthDay(2017, 6, 30),
            yearMonthDay(2017, 7, 7),
            yearMonthDay(2017, 7, 14),
            yearMonthDay(2017, 7, 21),
            yearMonthDay(2017, 7, 28),
            yearMonthDay(2017, 8, 4),
            yearMonthDay(2017, 8, 11),
            yearMonthDay(2017, 8, 18),
            yearMonthDay(2017, 8, 25),
            yearMonthDay(2017, 9, 1),
            yearMonthDay(2017, 9, 8),
            yearMonthDay(2017, 9, 15),
            yearMonthDay(2018, 6, 8),
            yearMonthDay(2018, 6, 15),
            yearMonthDay(2018, 6, 22),
            yearMonthDay(2018, 6, 29),
            yearMonthDay(2018, 7, 6),
            yearMonthDay(2018, 7, 13),
            yearMonthDay(2018, 7, 20),
            yearMonthDay(2018, 7, 27),
            yearMonthDay(2018, 8, 3),
            yearMonthDay(2018, 8, 10),
            yearMonthDay(2018, 8, 17),
            yearMonthDay(2018, 8, 24),
            yearMonthDay(2018, 8, 31),
            yearMonthDay(2018, 9, 7),
            yearMonthDay(2018, 9, 14),
            yearMonthDay(2019, 6, 7),
            yearMonthDay(2019, 6, 14),
            yearMonthDay(2019, 6, 21),
            yearMonthDay(2019, 6, 28),
            yearMonthDay(2019, 7, 5),
            yearMonthDay(2019, 7, 12),
            yearMonthDay(2019, 7, 19),
            yearMonthDay(2019, 7, 26),
            yearMonthDay(2019, 8, 2),
            yearMonthDay(2019, 8, 9),
            yearMonthDay(2019, 8, 16),
            yearMonthDay(2019, 8, 23),
            yearMonthDay(2019, 8, 30),
            yearMonthDay(2019, 9, 6),
            yearMonthDay(2019, 9, 13),
            yearMonthDay(2020, 6, 12),
            yearMonthDay(2020, 6, 19),
            yearMonthDay(2020, 6, 26),
            yearMonthDay(2020, 7, 3),
            yearMonthDay(2020, 7, 10),
            yearMonthDay(2020, 7, 17),
            yearMonthDay(2020, 7, 24),
            yearMonthDay(2020, 7, 31),
            yearMonthDay(2020, 8, 7),
            yearMonthDay(2020, 8, 14),
            yearMonthDay(2020, 8, 21),
            yearMonthDay(2020, 8, 28),
            yearMonthDay(2020, 9, 4),
            yearMonthDay(2020, 12, 24),
            yearMonthDay(2020, 12, 31),
    };

    private static final long[] JORNADA_RECUPERAR_EPOCH_DAYS = {
            yearMonthDay(2017, 10, 16),
            yearMonthDay(2017, 10, 17),
            yearMonthDay(2017, 10, 18),
            yearMonthDay(2017, 10, 19),
            yearMonthDay(2017, 10, 20),
            yearMonthDay(2017, 10, 23),
            yearMonthDay(2017, 10, 24),
            yearMonthDay(2017, 10, 25),
            yearMonthDay(2017, 10, 26),
            yearMonthDay(2017, 10, 27),
            yearMonthDay(2017, 10, 30),
            yearMonthDay(2017, 10, 31),
            yearMonthDay(2017, 11, 2),
            yearMonthDay(2017, 11, 3),
            yearMonthDay(2017, 11, 6),
            yearMonthDay(2017, 11, 7),
            yearMonthDay(2017, 11, 8),
            yearMonthDay(2017, 11, 9),
            yearMonthDay(2017, 11, 10),
            yearMonthDay(2017, 11, 13),
            yearMonthDay(2017, 11, 14),
            yearMonthDay(2017, 11, 15),
            yearMonthDay(2017, 11, 16),
            yearMonthDay(2017, 11, 17),
            yearMonthDay(2017, 11, 20),
            yearMonthDay(2017, 11, 21),
            yearMonthDay(2017, 11, 22),
            yearMonthDay(2017, 11, 23),
            yearMonthDay(2017, 11, 24),
            yearMonthDay(2017, 11, 27),
            yearMonthDay(2017, 11, 28),
            yearMonthDay(2017, 11, 29),
            yearMonthDay(2018, 10, 15),
            yearMonthDay(2018, 10, 16),
            yearMonthDay(2018, 10, 17),
            yearMonthDay(2018, 10, 18),
            yearMonthDay(2018, 10, 19),
            yearMonthDay(2018, 10, 22),
            yearMonthDay(2018, 10, 23),
            yearMonthDay(2018, 10, 24),
            yearMonthDay(2018, 10, 25),
            yearMonthDay(2018, 10, 26),
            yearMonthDay(2018, 10, 29),
            yearMonthDay(2018, 10, 30),
            yearMonthDay(2018, 10, 31),
            yearMonthDay(2018, 11, 5),
            yearMonthDay(2018, 11, 6),
            yearMonthDay(2018, 11, 7),
            yearMonthDay(2018, 11, 8),
            yearMonthDay(2018, 11, 9),
            yearMonthDay(2018, 11, 12),
            yearMonthDay(2018, 11, 13),
            yearMonthDay(2018, 11, 14),
            yearMonthDay(2018, 11, 15),
            yearMonthDay(2018, 11, 16),
            yearMonthDay(2018, 11, 19),
            yearMonthDay(2018, 11, 20),
            yearMonthDay(2018, 11, 21),
            yearMonthDay(2018, 11, 22),
            yearMonthDay(2018, 11, 23),
            yearMonthDay(2018, 11, 26),
            yearMonthDay(2018, 11, 27),
            yearMonthDay(2018, 11, 28),
            yearMonthDay(2018, 11, 29),
            yearMonthDay(2019, 10, 14),
            yearMonthDay(2019, 10, 15),
            yearMonthDay(2019, 10, 16),
            yearMonthDay(2019, 10, 17),
            yearMonthDay(2019, 10, 18),
            yearMonthDay(2019, 10, 21),
            yearMonthDay(2019, 10, 22),
            yearMonthDay(2019, 10, 23),
            yearMonthDay(2019, 10, 24),
            yearMonthDay(2019, 10, 25),
            yearMonthDay(2019, 10, 28),
            yearMonthDay(2019, 10, 29),
            yearMonthDay(2019, 10, 30),
            yearMonthDay(2019, 10, 31),
            yearMonthDay(2019, 11, 4),
            yearMonthDay(2019, 11, 5),
            yearMonthDay(2019, 11, 6),
            yearMonthDay(2019, 11, 7),
            yearMonthDay(2019, 11, 8),
            yearMonthDay(2019, 11, 11),
            yearMonthDay(2019, 11, 12),
            yearMonthDay(2019, 11, 13),
            yearMonthDay(2019, 11, 14),
            yearMonthDay(2019, 11, 15),
            yearMonthDay(2019, 11, 18),
            yearMonthDay(2019, 11, 19),
            yearMonthDay(2019, 11, 20),
            yearMonthDay(2019, 11, 21),
            yearMonthDay(2019, 11, 22),
            yearMonthDay(2019, 11, 25),
            yearMonthDay(2019, 11, 26),
            yearMonthDay(2019, 11, 27),
            yearMonthDay(2020, 10, 13),
            yearMonthDay(2020, 10, 14),
            yearMonthDay(2020, 10, 15),
            yearMonthDay(2020, 10, 16),
            yearMonthDay(2020, 10, 19),
            yearMonthDay(2020, 10, 20),
            yearMonthDay(2020, 10, 21),
            yearMonthDay(2020, 10, 22),
            yearMonthDay(2020, 10, 23),
            yearMonthDay(2020, 10, 26),
            yearMonthDay(2020, 10, 27),
            yearMonthDay(2020, 10, 28),
            yearMonthDay(2020, 10, 29),
            yearMonthDay(2020, 10, 30),
            yearMonthDay(2020, 11, 3),
            yearMonthDay(2020, 11, 4),
            yearMonthDay(2020, 11, 5),
            yearMonthDay(2020, 11, 6),
            yearMonthDay(2020, 11, 9),
            yearMonthDay(2020, 11, 10),
            yearMonthDay(2020, 11, 11),
            yearMonthDay(2020, 11, 12),
            yearMonthDay(2020, 11, 13),
            yearMonthDay(2020, 11, 16),
            yearMonthDay(2020, 11, 17),
            yearMonthDay(2020, 11, 18),
            yearMonthDay(2020, 11, 19),
            yearMonthDay(2020, 11, 20),
            yearMonthDay(2020, 11, 23),
            yearMonthDay(2020, 11, 24),
            yearMonthDay(2020, 11, 25),
            yearMonthDay(2020, 11, 26),
            yearMonthDay(2020, 11, 27),
    };

    private static final long[] JORNADA_FESTIVA = {
            yearMonthDay(2017, 1, 2),
            yearMonthDay(2017, 1, 6),
            yearMonthDay(2017, 3, 6),
            yearMonthDay(2017, 4, 13),
            yearMonthDay(2017, 4, 14),
            yearMonthDay(2017, 4, 24),
            yearMonthDay(2017, 5, 1),
            yearMonthDay(2017, 8, 14),
            yearMonthDay(2017, 8, 15),
            yearMonthDay(2017, 10, 12),
            yearMonthDay(2017, 10, 13),
            yearMonthDay(2017, 11, 1),
            yearMonthDay(2017, 12, 6),
            yearMonthDay(2017, 12, 7),
            yearMonthDay(2017, 12, 8),
            yearMonthDay(2017, 12, 25),
            yearMonthDay(2018, 1, 1),
            yearMonthDay(2018, 1, 6),
            yearMonthDay(2018, 1, 29),
            yearMonthDay(2018, 3, 5),
            yearMonthDay(2018, 3, 29),
            yearMonthDay(2018, 3, 30),
            yearMonthDay(2018, 4, 23),
            yearMonthDay(2018, 4, 30),
            yearMonthDay(2018, 5, 1),
            yearMonthDay(2018, 8, 15),
            yearMonthDay(2018, 10, 12),
            yearMonthDay(2018, 11, 1),
            yearMonthDay(2018, 11, 2),
            yearMonthDay(2018, 12, 6),
            yearMonthDay(2018, 12, 7),
            yearMonthDay(2018, 12, 8),
            yearMonthDay(2018, 12, 24),
            yearMonthDay(2018, 12, 25),
            yearMonthDay(2018, 12, 31),
            yearMonthDay(2019, 1, 1),
            yearMonthDay(2019, 1, 7),
            yearMonthDay(2019, 1, 28),
            yearMonthDay(2019, 1, 29),
            yearMonthDay(2019, 3, 4),
            yearMonthDay(2019, 3, 5),
            yearMonthDay(2019, 4, 18),
            yearMonthDay(2019, 4, 19),
            yearMonthDay(2019, 4, 22),
            yearMonthDay(2019, 4, 23),
            yearMonthDay(2019, 5, 1),
            yearMonthDay(2019, 8, 15),
            yearMonthDay(2019, 10, 12),
            yearMonthDay(2019, 11, 1),
            yearMonthDay(2019, 12, 6),
            yearMonthDay(2019, 12, 9),
            yearMonthDay(2019, 12, 25),
            yearMonthDay(2020, 1, 1),
            yearMonthDay(2020, 1, 2),
            yearMonthDay(2020, 1, 3),
            yearMonthDay(2020, 1, 6),
            yearMonthDay(2020, 1, 29),
            yearMonthDay(2020, 3, 5),
            yearMonthDay(2020, 3, 6),
            yearMonthDay(2020, 4, 9),
            yearMonthDay(2020, 4, 10),
            yearMonthDay(2020, 4, 23),
            yearMonthDay(2020, 4, 24),
            yearMonthDay(2020, 5, 1),
            yearMonthDay(2020, 10, 12),
            yearMonthDay(2020, 11, 2),
            yearMonthDay(2020, 12, 7),
            yearMonthDay(2020, 12, 8),
            yearMonthDay(2020, 12, 25),
    };


    public static long yearMonthDay(int Y, int M, int d) {
        DateTime dt = yearMonthDayDateTime(Y,M,d);
        long dayMillis = dt.getMillis();
        return dayMillis;
    }

    public static DateTime yearMonthDayDateTime(int Y, int M, int d) {
        DateTime dt = new DateTime(Y, M, d, 0, 0, DATETIMEZONE_LOCAL);
        return dt;
    }

    public static long calcularTiempoATrabajar(long epochMillis) {
        TIPO_JORNADA tipoJornada = calcularTipoJornada(epochMillis);
        return calcularTiempoATrabajar(tipoJornada);
    }

    private static long calcularTiempoATrabajar(TIPO_JORNADA tipoJornada) {
        long jornadaMillis = 0;
        switch (tipoJornada) {
            case JORNADA_ESTANDAR:
                jornadaMillis = JORNADA_ESTANDAR_MILLIS;
                break;
            case JORNADA_REDUCIDA:
                jornadaMillis = JORNADA_REDUCIDA_MILLIS;
                break;
            case JORNADA_RECUPERAR:
                jornadaMillis = JORNADA_RECUPERAR_MILLIS;
                break;
            case JORNADA_FESTIVA:
                jornadaMillis = JORNADA_FESTIVA_MILLIS;
                break;
        }
        return jornadaMillis;
    }

    public static TIPO_JORNADA calcularTipoJornada(long currentTime) {
        if (CalendarioLaboral.esJornadaReducida(currentTime)) {
            return TIPO_JORNADA.JORNADA_REDUCIDA;
        } else if (CalendarioLaboral.esJornadaRecuperar(currentTime))
            return TIPO_JORNADA.JORNADA_RECUPERAR;
        else if (CalendarioLaboral.esJornadaFestiva(currentTime))
            return TIPO_JORNADA.JORNADA_FESTIVA;
        else
            return TIPO_JORNADA.JORNADA_ESTANDAR;
    }

    private static boolean esJornadaFestiva(long currentTime) {
        boolean esJornadaFestiva = false;
        LocalDate localDate = new LocalDate(currentTime, DATETIMEZONE_LOCAL);
        int dayOfWeek = localDate.getDayOfWeek();
        if (dayOfWeek == SATURDAY || dayOfWeek == SUNDAY) {
            esJornadaFestiva = true;
        } else {
            for (long t : JORNADA_FESTIVA) {
                if (t<=currentTime && currentTime<(t+DAY_MILLIS)) {
                    esJornadaFestiva = true;
                    break;
                }
            }
        }
        return esJornadaFestiva;
    }

    public static boolean esJornadaReducida(long timestamp) {
        for (long t : JORNADA_REDUCIDA_EPOCH_DAYS) {
            if (t<=timestamp && timestamp<(t+DAY_MILLIS)) return true;
        }
        return false;
    }

    public static boolean esJornadaRecuperar(long timestamp) {
        for (long t : JORNADA_RECUPERAR_EPOCH_DAYS) {
            if (t<=timestamp && timestamp<(t+DAY_MILLIS)) return true;
        }
        return false;
    }


}
