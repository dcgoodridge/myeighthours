package myeighthours;

import myeighthours.helper.InstantHelper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static myeighthours.CalendarioLaboral.MAX_DESCANSO_COMIDA_MILLIS;

public class MehStateLogic {

    private static final Logger LOG = LoggerFactory.getLogger(MehStateLogic.class);

    private static ZoneId ZONE_ID = ZoneId.systemDefault();

    public static WorkState compute(long currentTime, List<Fichaje> fichajes) {

        CalendarioLaboral.TIPO_JORNADA tipoJornada = CalendarioLaboral.calcularTipoJornada(currentTime);
        Duration duracionTrabajarHoy = Duration.ofMillis( CalendarioLaboral.calcularTiempoATrabajar(currentTime) );
        long jornadaStart = 0;
        long jornadaFinish = 0;
        WorkState.STATE mehStateState = WorkState.STATE.UNKNOWN;
        if (fichajes == null) {
            return new WorkState.Builder().state(WorkState.STATE.UNKNOWN).build();
        }

        //Filtro y me quedo los fichajes de hoy
        List<Fichaje> fichajesDeHoy = new ArrayList<>();
        for (Fichaje fichaje : fichajes) {
            if (InstantHelper.isSameDay(fichaje.getFechaMarcaje(), currentTime)) {
                fichajesDeHoy.add(fichaje);
            }
        }

        //Ordeno los fichajes. Primero los mas recientes
        fichajesDeHoy = fichajesDeHoy.stream().sorted(comparingLong(Fichaje::getFechaMarcaje)).collect(Collectors.toList());

        int fichajesCount = fichajesDeHoy.size();
        if (fichajesCount == 0) {
            return new WorkState.Builder().state(WorkState.STATE.UNKNOWN).build();
        }

        Fichaje fichajeInicial = fichajesDeHoy.get(0);
        Fichaje fichajeUltimo = fichajesDeHoy.get(fichajesDeHoy.size() - 1);

        jornadaStart = fichajeInicial.getFechaMarcaje();

        boolean ahoraTrabajando = true; //Para la nueva logica, tengo que dar por hecho que siempre estoy trabajando...

        if (ahoraTrabajando) mehStateState = WorkState.STATE.WORKING;
        else mehStateState = WorkState.STATE.RESTING;

        Duration duracionTrabajo = Duration.ofSeconds( 0 );
        Duration duracionDescanso = Duration.ofSeconds( 0 );
        Duration duracionDescansoRestanteProbable = Duration.ofSeconds( 0 );

        //Calculo tiempoTrabajado y tiempoDescansado, entre intervalos de fichajes
        duracionTrabajo = calculaDuracionTrabajadaHastaAhora(currentTime, fichajesDeHoy);
        duracionDescanso = calculaDuracionDescansadaHastaAhora(currentTime, fichajesDeHoy);
        duracionDescansoRestanteProbable = calculaDuracionDescansadaRestanteProbable(currentTime, fichajesDeHoy);

        boolean jornadaYaCompletada = (duracionTrabajo.toMillis() >= duracionTrabajarHoy.toMillis());
        //Calculo el tiempo de salida
        if (jornadaYaCompletada){
            duracionTrabajo = duracionTrabajarHoy;
            Duration duracionRestateTrabajoFichado = Duration.ZERO;
            jornadaFinish = jornadaStart + duracionDescanso.toMillis() + duracionTrabajo.toMillis() + duracionRestateTrabajoFichado.toMillis();
            jornadaFinish += duracionDescansoRestanteProbable.toMillis();
        }else{
            Duration duracionRestateTrabajoFichado = duracionTrabajarHoy.minus( duracionTrabajo );
            jornadaFinish = jornadaStart + duracionDescanso.toMillis() + duracionTrabajo.toMillis() + duracionRestateTrabajoFichado.toMillis();
            jornadaFinish += duracionDescansoRestanteProbable.toMillis();
        }
        float porcentajeTrabajado = (float) duracionTrabajo.toMillis() / duracionTrabajarHoy.toMillis();
        if (porcentajeTrabajado > 1f) porcentajeTrabajado = 1f;
        WorkState.Builder mehStateBuilder = new WorkState.Builder();
        mehStateBuilder.timeStart(jornadaStart);
        mehStateBuilder.timeFinish(jornadaFinish);
        mehStateBuilder.progress(porcentajeTrabajado);
        mehStateBuilder.state(mehStateState);
        return mehStateBuilder.build();
    }

    private static Duration calculaDuracionDescansadaRestanteProbable( long currentTime, List<Fichaje> fichajesDeHoy )
    {
        Duration duracionDescansoRestanteProbable = Duration.ofSeconds( 0 );
        boolean numeroFichajesPar = (fichajesDeHoy.size() % 2 == 0);
        if (fichajesDeHoy.size()==0) return Duration.ofMillis( MAX_DESCANSO_COMIDA_MILLIS );
        if (fichajesDeHoy.size()==1) return Duration.ofMillis( MAX_DESCANSO_COMIDA_MILLIS );
        Fichaje ultimoFichajeHoy = fichajesDeHoy.get(fichajesDeHoy.size() - 1);
        long tiempoDesdeUltimoMarcaje = currentTime - ultimoFichajeHoy.getFechaMarcaje();
        if (numeroFichajesPar) {
            if ((tiempoDesdeUltimoMarcaje > MAX_DESCANSO_COMIDA_MILLIS)){
            }else{
                duracionDescansoRestanteProbable = duracionDescansoRestanteProbable.plusMillis( MAX_DESCANSO_COMIDA_MILLIS - tiempoDesdeUltimoMarcaje );
            }
        }
        return duracionDescansoRestanteProbable;
    }

    private static Duration calculaDuracionTrabajadaHastaAhora( long currentTime, List<Fichaje> fichajesDeHoy )
    {
        boolean numeroFichajesPar = (fichajesDeHoy.size() % 2 == 0);
        if (fichajesDeHoy.size()==0) return Duration.ZERO;
        if (fichajesDeHoy.size()==1) {
            Fichaje ultimoFichajeHoy = fichajesDeHoy.get(fichajesDeHoy.size() - 1);
            long tiempoDesdeUltimoMarcaje = currentTime - ultimoFichajeHoy.getFechaMarcaje();
            return Duration.ofMillis(tiempoDesdeUltimoMarcaje);
        }
        Duration duracionTrabajo = Duration.ofSeconds( 0 );
        Fichaje ultimoFichajeHoy = fichajesDeHoy.get(fichajesDeHoy.size() - 1);
        long tiempoDesdeUltimoMarcaje = currentTime - ultimoFichajeHoy.getFechaMarcaje();
        for (int i = 1; i < fichajesDeHoy.size(); i++) {
            boolean indicePar = (i % 2 == 0);
            if (indicePar) {
                //El indice actual con el anterior forman un periodo de TRABAJO
            } else {
                //El indice actual con el anterior forman un periodo de descanso
                long millisTrabajadosPeriodo = fichajesDeHoy.get( i ).getFechaMarcaje() - fichajesDeHoy.get( i - 1 ).getFechaMarcaje();
                duracionTrabajo = duracionTrabajo.plusMillis( millisTrabajadosPeriodo );
            }
        }
        if (numeroFichajesPar){
            if (tiempoDesdeUltimoMarcaje > MAX_DESCANSO_COMIDA_MILLIS){
                duracionTrabajo = duracionTrabajo.plusMillis( tiempoDesdeUltimoMarcaje - MAX_DESCANSO_COMIDA_MILLIS );
            }else{
                duracionTrabajo = duracionTrabajo.plusMillis( tiempoDesdeUltimoMarcaje );
            }
        } else {
            duracionTrabajo = duracionTrabajo.plusMillis( tiempoDesdeUltimoMarcaje );
        }
        return duracionTrabajo;
    }


    private static Duration calculaDuracionDescansadaHastaAhora( long currentTime, List<Fichaje> fichajesDeHoy )
    {
        boolean numeroFichajesPar = (fichajesDeHoy.size() % 2 == 0);
        if (fichajesDeHoy.size()==0) return Duration.ZERO;
        if (fichajesDeHoy.size()==1) return Duration.ZERO;
        Duration duracionDescanso = Duration.ofSeconds( 0 );
        Fichaje ultimoFichajeHoy = fichajesDeHoy.get(fichajesDeHoy.size() - 1);
        long tiempoDesdeUltimoMarcaje = currentTime - ultimoFichajeHoy.getFechaMarcaje();
        for (int i = 1; i < fichajesDeHoy.size(); i++) {
            boolean indicePar = (i % 2 == 0);
            if (indicePar) {
                long millisDescansadosPeriodo = fichajesDeHoy.get( i ).getFechaMarcaje() - fichajesDeHoy.get( i - 1 ).getFechaMarcaje();
                duracionDescanso = duracionDescanso.plusMillis( millisDescansadosPeriodo );
            } else {
            }
        }
        if (numeroFichajesPar) {
            if ((tiempoDesdeUltimoMarcaje > MAX_DESCANSO_COMIDA_MILLIS)){
                duracionDescanso = duracionDescanso.plusMillis( MAX_DESCANSO_COMIDA_MILLIS );
            }else{
                duracionDescanso = duracionDescanso.plusMillis( tiempoDesdeUltimoMarcaje );
            }
        }
        return duracionDescanso;
    }

    public static Duration calcularMillisTrabajados(List<Fichaje> fichajesDia) {
        boolean numeroFichajesPar = (fichajesDia.size() % 2 == 0);
        List<Fichaje> fichajesDiaOrdenados = fichajesDia.stream().sorted(comparingLong(Fichaje::getFechaMarcaje)).collect(Collectors.toList());
        if (numeroFichajesPar){
            return calcularMillisTrabajados_fichajesPar(fichajesDiaOrdenados);
        } else {
            return calcularMillisTrabajados_fichajesImpar(fichajesDiaOrdenados);
        }
    }

    public static Duration calcularMillisTrabajados_fichajesPar(List<Fichaje> fichajesDiaOrdenados) {
        if (fichajesDiaOrdenados.size()==0) return Duration.ZERO;
        if (fichajesDiaOrdenados.size()==1) return Duration.ZERO;
        Duration duracionTrabajo = Duration.ofSeconds( 0 );
        for (int i = 1; i < fichajesDiaOrdenados.size(); i++) {
            boolean indicePar = (i % 2 == 0);
            if (indicePar) {
            } else {
                long millisTrabajadosPeriodo = fichajesDiaOrdenados.get( i ).getFechaMarcaje() - fichajesDiaOrdenados.get( i - 1 ).getFechaMarcaje();
                duracionTrabajo = duracionTrabajo.plusMillis( millisTrabajadosPeriodo );
            }
        }
        return duracionTrabajo;
    }

    public static Duration calcularMillisTrabajados_fichajesImpar(List<Fichaje> fichajesDiaOrdenados) {
        Duration duracionTrabajo = Duration.ofSeconds( 0 );
        if (fichajesDiaOrdenados.size()==0) return Duration.ZERO;
        if (fichajesDiaOrdenados.size()==1) return Duration.ZERO;

        //Primero detecto que fichaje habra sido el de la comida
        //Si existe uno que sea de cantina -> ese es. Si no lo existe...el que mas se acerque a las 2pm, con indice impar.
        boolean fichajeComerDetectado = false;
        int indiceFichajeComer = -1;
        for (int i = 1; i < fichajesDiaOrdenados.size(); i++) {
            Fichaje fichaje = fichajesDiaOrdenados.get(i);
            if (fichaje.isCanteenTerminal()) {
                fichajeComerDetectado = true;
                indiceFichajeComer = i;
            }
        }

        if (!fichajeComerDetectado) {
            indiceFichajeComer = calculaIndiceFichajeComer(fichajesDiaOrdenados);
        }

        //Una vez que tenemos el fichaje de comer, vamos a simular que tenemos un fichaje a la vuelta de comer, y lo añadimos.
        //Ahora tenemos numero de fichajes par estandar.. y calculamos con la version estandar
        long fechaMarcajeVueltaComer = fichajesDiaOrdenados.get(indiceFichajeComer).getFechaMarcaje() + MAX_DESCANSO_COMIDA_MILLIS;
        Fichaje fichajeVirtualVueltaDeComer = new Fichaje.Builder(fichajesDiaOrdenados.get(indiceFichajeComer)).fechaMarcaje(fechaMarcajeVueltaComer).build();

        List<Fichaje> fichajesDiaVirtual = new ArrayList<>();
        fichajesDiaVirtual.addAll(fichajesDiaOrdenados);
        fichajesDiaVirtual.add(indiceFichajeComer+1,fichajeVirtualVueltaDeComer);
        duracionTrabajo = calcularMillisTrabajados_fichajesPar(fichajesDiaVirtual);

        return duracionTrabajo;
    }

    private static int calculaIndiceFichajeComer(List<Fichaje> fichajesDia) {
        List<Long> distanciasHastaLasDos = new ArrayList<>();
        for (int i=0; i< fichajesDia.size(); i++) {
            Fichaje fichaje = fichajesDia.get(i);
            boolean indicePar = (i % 2 == 0);
            org.joda.time.Instant  jodaInstant = org.joda.time.Instant.ofEpochMilli( fichaje.getFechaMarcaje() ) ;
            DateTime dateTimeFichaje = new DateTime(jodaInstant);
            DateTime dateTimeHoraComer = dateTimeFichaje.withTime(14,0,0,0);
            long diffInMillis = Math.abs(dateTimeFichaje.getMillis() - dateTimeHoraComer.getMillis());
            if (indicePar) {
                distanciasHastaLasDos.add(Long.MAX_VALUE);
            } else {
                distanciasHastaLasDos.add(diffInMillis);
            }

        }
        int minIndex = distanciasHastaLasDos.indexOf(Collections.min(distanciasHastaLasDos));
        return minIndex;
    }
}
