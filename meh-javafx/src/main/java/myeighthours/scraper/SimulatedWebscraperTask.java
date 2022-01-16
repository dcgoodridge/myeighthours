package myeighthours.scraper;

import myeighthours.Fichaje;
import myeighthours.MehOptions;
import myeighthours.SimulationProperties;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class SimulatedWebscraperTask extends WebscraperTaskAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatedWebscraperTask.class);

    private MehOptions options;

    private SimulationProperties simulationProperties;

    private long timestampStart = 0;

    private long timestampFinish = 0;

    public SimulatedWebscraperTask(MehOptions options) {
        this.options = options;
        simulationProperties = ConfigFactory.create( SimulationProperties.class );
    }

    private SimulatedWebscraperTask() {
        throw new UnsupportedOperationException("Private constructor nor supported");
    }

    @Override
    protected List<Fichaje> call() throws Exception {
        timestampStart = System.currentTimeMillis();
        LOG.info("Webscraper task start");
        List<Fichaje> fichajes = escarbarFichajes();
        timestampFinish = System.currentTimeMillis();
        Random random = new Random();
        random.nextDouble();
        if (random.nextDouble()>0.8d) {
            throw new Exception("ERROR SIMULADO DE WEBSCRAPER");
        }
        return fichajes;
    }

    private List<Fichaje> escarbarFichajes() throws Exception {
        List<Fichaje> fichajes = new ArrayList<>();
        try {
            LOG.info("Simulation web scraper long running job started");
            Thread.sleep(simulationProperties.getWebscrapTime() * 1000L);
            LOG.info("Simulation web scraper long running job finished");
            List<Fichaje> fichajesParseados = simularFichajes();
            fichajes.addAll(fichajesParseados);
        } finally {
            LOG.info("Web scraper task finish");
        }
        return fichajes;
    }

    private List<Fichaje> simularFichajes() {
        Calendar cal_0 = Calendar.getInstance();
        cal_0.set(Calendar.HOUR_OF_DAY,8);
        cal_0.set(Calendar.MINUTE,0);
        cal_0.set(Calendar.SECOND,0);
        cal_0.set(Calendar.MILLISECOND,0);
        Calendar cal_1 = Calendar.getInstance();
        cal_1.set(Calendar.HOUR_OF_DAY,13);
        cal_1.set(Calendar.MINUTE,50);
        cal_1.set(Calendar.SECOND,0);
        cal_1.set(Calendar.MILLISECOND,0);
        Calendar cal_2 = Calendar.getInstance();
        cal_2.set(Calendar.HOUR_OF_DAY,14);
        cal_2.set(Calendar.MINUTE,40);
        cal_2.set(Calendar.SECOND,0);
        cal_2.set(Calendar.MILLISECOND,0);
        List<Fichaje> fichajes = new ArrayList<>();
        fichajes.add(new Fichaje.Builder()
                .direccion(Fichaje.DIRECCION.ENTRADA)
                .fechaMarcaje(cal_0.getTimeInMillis())
                .build());
        fichajes.add(new Fichaje.Builder()
                .direccion(Fichaje.DIRECCION.SALIDA)
                .fechaMarcaje(cal_1.getTimeInMillis())
                .build());
        fichajes.add(new Fichaje.Builder()
                .direccion(Fichaje.DIRECCION.ENTRADA)
                .fechaMarcaje(cal_2.getTimeInMillis())
                .build());
        return fichajes;
    }

    @Override
    public long getTimestampFinish() {
        return timestampFinish;
    }

    @Override
    public long getTimestampStart() {
        return timestampStart;
    }

}
