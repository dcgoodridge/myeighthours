import static myeighthours.Fichaje.Builder;
import static myeighthours.Fichaje.DIRECCION;
import static myeighthours.Fichaje.TERMINAL_CANTINA;
import static myeighthours.Fichaje.TERMINAL_ID;
import static myeighthours.Fichaje.TERMINAL_PARKING;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import myeighthours.Fichaje;
import myeighthours.MehStateLogic;
import myeighthours.WorkState;
import org.joda.time.DateTime;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class MehStateLogicTest
{

    private static final double PRECISION = 0.01;
    private static ZoneId ZONE_ID = ZoneId.systemDefault();

    @Test
    public void testProgress_estandar_0fichajes() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 5);
        long currentTime = mismoDiaMillis(ref, "08:45");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertThat((double) mehState.getProgress(), closeTo(0.0, PRECISION));
    }

    @Test
    public void testProgress_estandar_1fichajes() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 5);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .fechaMarcaje(mismoDiaMillis(ref, "08:45"))
                .build());
        long currentTime = mismoDiaMillis(ref, "09:55");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertThat((double) mehState.getProgress(), closeTo(0.1458, PRECISION));
    }

    @Test
    public void testProgress_estandar_2fichajes() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 5);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .fechaMarcaje(mismoDiaMillis(ref, "08:00"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.SALIDA)
                .fechaMarcaje(mismoDiaMillis(ref, "13:50"))
                .build());
        long currentTime = mismoDiaMillis(ref, "14:50");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        float expectedProgress = ((float)(5*60)+50+15)/((8*60)+15);
        assertThat((double) mehState.getProgress(), closeTo(expectedProgress, PRECISION));
    }

    @Test
    public void testProgress_estandar_3fichajes() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 5);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .fechaMarcaje(mismoDiaMillis(ref, "08:00"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.SALIDA)
                .fechaMarcaje(mismoDiaMillis(ref, "13:50"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .fechaMarcaje(mismoDiaMillis(ref, "14:35"))
                .build());
        long currentTime = mismoDiaMillis(ref, "17:00");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertThat((double) mehState.getProgress(), closeTo(1.0, PRECISION));
    }

    @Test
    public void testProgress_reducida_0fichajes() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 5);
        long currentTime = mismoDiaMillis(ref, "08:00");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertThat((double) mehState.getProgress(), closeTo(0.0, PRECISION));
    }

    @Test
    public void testProgress_reducida_1fichajes_1() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 23, 12, 0);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .fechaMarcaje(mismoDiaMillis(ref, "08:00"))
                .build());
        long currentTime = mismoDiaMillis(ref, "09:00");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertThat((double) mehState.getProgress(), closeTo(0.154, PRECISION));
    }

    @Test
    public void testProgress_reducida_1fichajes_2() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 23);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .fechaMarcaje(mismoDiaMillis(ref, "08:00"))
                .build());

        long currentTime = mismoDiaMillis(ref, "14:15");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertThat((double) mehState.getProgress(), closeTo(1.0, PRECISION));
    }

    @Test
    public void testProgress_reducida_1fichajes_3() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 23);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .fechaMarcaje(mismoDiaMillis(ref, "08:00"))
                .build());

        long currentTime = mismoDiaMillis(ref, "15:00");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertThat((double) mehState.getProgress(), closeTo(1.0, PRECISION));
    }

    @Test
    public void testTimeFinish_estandar_1fichajes() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 5);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .terminal(TERMINAL_ID)
                .fechaMarcaje(mismoDiaMillis(ref, "08:00"))
                .build());
        long currentTime = mismoDiaMillis(ref, "09:00");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertEqualDates(mehState.getTimeFinish(), (mismoDiaMillis(ref, "17:00")));
    }

    @Test
    public void testTimeFinish_estandar_2fichajes() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 5);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .terminal(TERMINAL_ID)
                .fechaMarcaje(mismoDiaMillis(ref, "08:00"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.SALIDA)
                .terminal(TERMINAL_ID)
                .fechaMarcaje(mismoDiaMillis(ref, "14:00"))
                .build());
        long currentTime = mismoDiaMillis(ref, "14:30");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertEqualDates(mehState.getTimeFinish(), (mismoDiaMillis(ref, "17:30")));
    }

    @Test
    public void testTimeFinish_estandar_2fichajes_especialcomida1() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 5);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .fechaMarcaje(mismoDiaMillis(ref, "08:00"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.SALIDA)
                .terminal(TERMINAL_CANTINA)
                .fechaMarcaje(mismoDiaMillis(ref, "14:00"))
                .build());
        long currentTime = mismoDiaMillis(ref, "14:30");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertEqualDates(mehState.getTimeFinish(), (mismoDiaMillis(ref, "17:00")));
    }

    @Test
    public void testTimeFinish_estandar_2fichajes_especialcomida2() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 5);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .fechaMarcaje(mismoDiaMillis(ref, "08:00"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.SALIDA)
                .terminal(TERMINAL_CANTINA)
                .fechaMarcaje(mismoDiaMillis(ref, "14:00"))
                .build());
        long currentTime = mismoDiaMillis(ref, "15:30");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertEqualDates(mehState.getTimeFinish(), (mismoDiaMillis(ref, "17:00")));
    }

    @Test
    public void testTimeFinish_estandar_3fichajes_comerFuera() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 5, 12, 0);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .terminal(TERMINAL_ID)
                .fechaMarcaje(mismoDiaMillis(ref, "08:00"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.SALIDA)
                .terminal(TERMINAL_ID)
                .fechaMarcaje(mismoDiaMillis(ref, "14:00"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .terminal(TERMINAL_ID)
                .fechaMarcaje(mismoDiaMillis(ref, "15:00"))
                .build());
        long currentTime = mismoDiaMillis(ref, "16:00");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertEqualDates(mismoDiaMillis(ref, "17:15"), mehState.getTimeFinish());
    }

    @Test
    public void testTimeFinish_estandar_3fichajes_tiempoComidaCorto() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 5);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .terminal(TERMINAL_ID)
                .fechaMarcaje(mismoDiaMillis(ref, "08:00"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.SALIDA)
                .terminal(TERMINAL_CANTINA)
                .fechaMarcaje(mismoDiaMillis(ref, "14:00"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .terminal(TERMINAL_ID)
                .fechaMarcaje(mismoDiaMillis(ref, "14:30"))
                .build());
        long currentTime = mismoDiaMillis(ref, "16:00");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertEqualDates(mehState.getTimeFinish(), (mismoDiaMillis(ref, "17:00")));
    }

    @Test
    public void testTimeFinish_estandar_3fichajes_tiempoComidaExacto() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 5);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .terminal(TERMINAL_ID)
                .fechaMarcaje(mismoDiaMillis(ref, "08:00"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.SALIDA)
                .terminal(TERMINAL_CANTINA)
                .fechaMarcaje(mismoDiaMillis(ref, "14:00"))
                .build());
        long currentTime = mismoDiaMillis(ref, "16:00");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertEqualDates(mehState.getTimeFinish(), (mismoDiaMillis(ref, "17:00")));
    }

    @Test
    public void testTimeFinish_estandar_2fichajes_casiJornadaTerminada() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 5);
        fichajes.add(new Builder()
          .direccion( DIRECCION.ENTRADA)
          .terminal(TERMINAL_ID)
          .fechaMarcaje(mismoDiaMillis(ref, "08:00"))
          .build());
        fichajes.add(new Builder()
          .direccion( DIRECCION.SALIDA)
          .terminal(TERMINAL_CANTINA)
          .fechaMarcaje(mismoDiaMillis(ref, "13:50"))
          .build());
        long currentTime = mismoDiaMillis(ref, "16:48");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertEqualDates(mehState.getTimeFinish(), (mismoDiaMillis(ref, "17:00")));
    }

    @Test
    public void testTimeFinish_estandar_2fichajes_casiJornadaTerminada_recuperar() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2019, 10, 17);
        fichajes.add(new Builder()
          .direccion( DIRECCION.ENTRADA)
          .terminal(TERMINAL_ID)
          .fechaMarcaje(mismoDiaMillis(ref, "08:00"))
          .build());
        fichajes.add(new Builder()
          .direccion( DIRECCION.SALIDA)
          .terminal(TERMINAL_CANTINA)
          .fechaMarcaje(mismoDiaMillis(ref, "13:50"))
          .build());
        long currentTime = mismoDiaMillis(ref, "17:00");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertEqualDates(mehState.getTimeFinish(), (mismoDiaMillis(ref, "17:15")));
    }

    @Test
    public void testTimeFinish_estandar_3fichajes_tiempoComidaLargo() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 5);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .terminal(TERMINAL_ID)
                .fechaMarcaje(mismoDiaMillis(ref, "08:00"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.SALIDA)
                .terminal(TERMINAL_CANTINA)
                .fechaMarcaje(mismoDiaMillis(ref, "14:00"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .terminal(TERMINAL_ID)
                .fechaMarcaje(mismoDiaMillis(ref, "15:00"))
                .build());
        long currentTime = mismoDiaMillis(ref, "16:00");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertEqualDates(mehState.getTimeFinish(), (mismoDiaMillis(ref, "17:00")));
    }


    @Test
    public void testTimeFinish_reducida_1fichajes() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 23);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .fechaMarcaje(mismoDiaMillis(ref, "08:00"))
                .build());

        long currentTime = mismoDiaMillis(ref, "09:00");
        WorkState mehState = MehStateLogic.compute(currentTime, fichajes);
        assertEqualDates((mismoDiaMillis(ref, "14:15")), mehState.getTimeFinish());
    }

    @Test
    public void testTimeFinish_incidencia_2fichajes_sin_scraper_aumenta() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 5);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .fechaMarcaje(mismoDiaMillis(ref, "08:00"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.SALIDA)
                .terminal(TERMINAL_CANTINA)
                .fechaMarcaje(mismoDiaMillis(ref, "13:50"))
                .build());
        WorkState mehState;
        long currentTime;
        currentTime = mismoDiaMillis(ref, "15:00");
        mehState = MehStateLogic.compute(currentTime, fichajes);
        assertEqualDates(mehState.getTimeFinish(), mismoDiaMillis(ref, "17:00"));
        currentTime = mismoDiaMillis(ref, "15:15");
        mehState = MehStateLogic.compute(currentTime, fichajes);
        assertEqualDates(mismoDiaMillis(ref, "17:00"), mehState.getTimeFinish());
    }

    @Test
    public void testTimeFinish_incidencia_2fichajes_aguilar_1() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 6, 15);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .fechaMarcaje(mismoDiaMillis(ref, "08:23"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.SALIDA)
                .terminal(TERMINAL_CANTINA)
                .fechaMarcaje(mismoDiaMillis(ref, "13:55"))
                .build());
        WorkState mehState;
        long currentTime;
        currentTime = mismoDiaMillis(ref, "15:54");
        mehState = MehStateLogic.compute(currentTime, fichajes);
        assertEqualDates(mismoDiaMillis(ref, "17:23"), mehState.getTimeFinish());
    }

    @Test
    public void testTimeFinish_incidencia_5fichajes_dgoodridge_1() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2020, 11, 23);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .terminal(TERMINAL_ID)
                .fechaMarcaje(mismoDiaMillis(ref, "09:27"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.SALIDA)
                .terminal(TERMINAL_ID)
                .fechaMarcaje(mismoDiaMillis(ref, "11:16"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .terminal(TERMINAL_ID)
                .fechaMarcaje(mismoDiaMillis(ref, "12:10"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.SALIDA)
                .terminal(TERMINAL_CANTINA)
                .fechaMarcaje(mismoDiaMillis(ref, "13:53"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .terminal(TERMINAL_PARKING)
                .fechaMarcaje(mismoDiaMillis(ref, "14:17"))
                .build());
        WorkState mehState;
        long currentTime;
        currentTime = mismoDiaMillis(ref, "18:30");
        mehState = MehStateLogic.compute(currentTime, fichajes);
        DateTime expected = mismoDia(ref, "19:15");
        DateTime actual = new DateTime(mehState.getTimeFinish());
        assertEqualDates(expected, actual);
    }

    @Test
    public void testTotalWorkedTime_incidencia_2fichajes_daguilar() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        DateTime ref = getDayRef(2018, 2, 5);
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .terminal(TERMINAL_ID)
                .fechaMarcaje(mismoDiaMillis(ref, "12:31"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.SALIDA)
                .terminal(TERMINAL_CANTINA)
                .fechaMarcaje(mismoDiaMillis(ref, "13:53"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.ENTRADA)
                .terminal(TERMINAL_ID)
                .fechaMarcaje(mismoDiaMillis(ref, "14:51"))
                .build());
        fichajes.add(new Builder()
                .direccion( DIRECCION.SALIDA)
                .terminal(TERMINAL_ID)
                .fechaMarcaje(mismoDiaMillis(ref, "17:42"))
                .build());
        long millisTrabajadosActual = MehStateLogic.calcularMillisTrabajados(fichajes).toMillis();
        long millisTrabajadosExpected = 253L * 60L * 1000L;
        assertThat(millisTrabajadosActual,is(millisTrabajadosExpected));
    }

    private long mismoDiaMillis(DateTime referencia, String hora) {
        return mismoDia(referencia, hora).getMillis();
    }

    private DateTime mismoDia(DateTime referencia, String hora) {
        String[] parts = hora.split(":");
        String hourString = parts[0];
        String minuteString = parts[1];
        int hour = Integer.parseInt(hourString);
        int minute = Integer.parseInt(minuteString);
        DateTime result = referencia.withTime(hour, minute, 0, 0);
        return result;
    }

    private Instant mismoDia(Instant referencia, String hora) {
        LocalDateTime ldt1 = LocalDateTime.ofInstant(referencia, ZONE_ID);
        String[] parts = hora.split(":");
        String hourString = parts[0];
        String minuteString = parts[1];
        int year = ldt1.getYear();
        int monthValue = ldt1.getMonthValue();
        int dayOfMonth = ldt1.getDayOfMonth();
        int hour = Integer.parseInt(hourString);
        int minute = Integer.parseInt(minuteString);
        LocalDateTime ldt2 = LocalDateTime.of(year, monthValue, dayOfMonth, hour, minute);
        return ldt2.atZone(ZONE_ID).toInstant();
    }

    private long mismoDiaMillis(Instant referencia, String hora) {
        return mismoDia(referencia, hora).toEpochMilli();
    }

    private static void assertEqualDates(long expected, long actual) {
        Instant i1 = Instant.ofEpochMilli(expected);
        Instant i2 = Instant.ofEpochMilli(actual);
        assertEqualDates(i1, i2);
    }

    private static void assertEqualDates(Instant expected, Instant actual) {
        LocalDateTime ldt1 = LocalDateTime.ofInstant(expected, ZONE_ID);
        LocalDateTime ldt2 = LocalDateTime.ofInstant(actual, ZONE_ID);
        assertEquals(ldt1, ldt2);
    }

    private static void assertEqualDates(DateTime expected, DateTime actual) {
        assertEqualDates(expected.getMillis(), actual.getMillis());
    }

    private static DateTime getDayRef(int year, int month, int day){
        return new DateTime(year, month, day, 0, 0);
    }

    private static DateTime getDayRef(int year, int month, int day, int hour, int minute){
        return new DateTime(year, month, day, hour, minute);
    }

}
