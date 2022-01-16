package myeighthours;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.util.Duration;
import myeighthours.database.dao.MehDb;
import myeighthours.database.dao.MehDbJavafx;
import myeighthours.database.dao.MehDbState;
import myeighthours.scraper.ScraperState;
import myeighthours.scraper.SimulatedWebscraperTask;
import myeighthours.scraper.WebscraperTask;
import myeighthours.scraper.WebscraperTaskAbstract;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

import static myeighthours.helper.HelperDigest.SHA1String;

public class MehService extends ScheduledService<MehServiceState> {

    private static final Logger LOG = LoggerFactory.getLogger(MehService.class);

    private final MehProperties mehProperties;

    private final SimulationProperties simulationProperties;

    private long webscrapIntervalSeconds = 60 * 60L;

    private MehOptions options;

    private List<Fichaje> ultimosFichajesDetectados;

    private ScraperState currentScraperState = ScraperState.UNKNOWN;

    private WorkState currentWorkState = WorkState.UNKNOWN;

    private boolean webcrawlerDisabled = false;

    private boolean userHasTriggeredWebscraper = false;

    private MehDb mehDb;

    private boolean webscraperTaskStarted = false;

    public long getWebscraperTaskFinishTime() {
        return currentScraperState.getTimeFinish();
    }

    public MehService(MehOptions options) {
        mehProperties = ConfigFactory.create(MehProperties.class);
        simulationProperties = ConfigFactory.create(SimulationProperties.class);
        webscrapIntervalSeconds = mehProperties.webscrapInterval();
        this.options = options;
        setPeriod(Duration.seconds(mehProperties.mehserviceInterval()));
        final Executor executor = Executors.newSingleThreadExecutor( runnable -> {
            Thread t = new Thread(runnable);
            t.setName("MehService");
            t.setDaemon(true);
            return t ;
        });
        setExecutor(executor);
    }

    private MehService() {
        throw new RuntimeException("Constructor por defecto no permitido");
    }

    private boolean shouldTriggerWebscraper() {
        boolean shouldTrigger;
        boolean mustNotTrigger = isWebscraperTaskStarted() || webcrawlerDisabled;
        if (mustNotTrigger) {
            shouldTrigger = false;
        } else {
            if (userHasTriggeredWebscraper || !webscraperWasTriggeredRecently()) {
                shouldTrigger = true;
            } else {
                shouldTrigger = false;
            }
        }
        return shouldTrigger;
    }

    private boolean webscraperWasTriggeredRecently() {
        long timeMillisSinceLastScrap = System.currentTimeMillis() - getWebscraperTaskFinishTime();
        boolean triggeredRecently = (timeMillisSinceLastScrap < (webscrapIntervalSeconds * 1000L));
        return triggeredRecently;
    }

    private void triggerWebscraperTask() {
        if (userHasTriggeredWebscraper) {
            LOG.debug("Webscraper triggered by user");
        }else {
            LOG.debug("Webscraper triggered by service");
        }
        final WebscraperTaskAbstract webscraperTask;
        if (simulationProperties.isActive()) {
            webscraperTask = new SimulatedWebscraperTask(options);
        } else {
            webscraperTask = new WebscraperTask(options);
        }
        webscraperTask.setOnCancelled(event -> onWebscraperTaskCancelled(event, webscraperTask));
        webscraperTask.setOnFailed(event -> onWebscraperTaskFailed(event, webscraperTask));
        webscraperTask.setOnSucceeded(event -> onWebscraperTaskSucceeded(event, webscraperTask));
        webscraperTask.setOnRunning(event -> onWebscraperTaskRunning(event, webscraperTask));
        webscraperTask.setOnScheduled(event -> onWebscraperTaskScheduled(event, webscraperTask));
        Thread webscraperTaskThread = new Thread(webscraperTask);
        webscraperTaskThread.setName("Webscraper");
        webscraperTaskThread.setDaemon(true);
        webscraperTaskThread.start();
    }

    private void onWebscraperTaskScheduled(WorkerStateEvent event, WebscraperTaskAbstract task) {
        LOG.info("Webscraper scheduled");
        ScraperState.Builder crawlerstateBuilder = new ScraperState.Builder(currentScraperState);
        crawlerstateBuilder.state(State.SCHEDULED);
        crawlerstateBuilder.timeStart(System.currentTimeMillis());
        currentScraperState = crawlerstateBuilder.build();
        triggerService();
    }

    private void onWebscraperTaskRunning(WorkerStateEvent event, WebscraperTaskAbstract task) {
        LOG.info("Webscraper running");
        ScraperState.Builder crawlerstateBuilder = new ScraperState.Builder(currentScraperState);
        crawlerstateBuilder.state(State.RUNNING);
        crawlerstateBuilder.timeStart(System.currentTimeMillis());
        currentScraperState = crawlerstateBuilder.build();
        triggerService();
    }

    private void onWebscraperTaskCancelled(WorkerStateEvent event, WebscraperTaskAbstract task) {
        LOG.warn("Webscraper cancelled");
        ScraperState.Builder crawlerstateBuilder = new ScraperState.Builder(currentScraperState);
        crawlerstateBuilder.state(State.CANCELLED);
        crawlerstateBuilder.timeFinish(System.currentTimeMillis());
        currentScraperState = crawlerstateBuilder.build();
        onWebscraperTaskFinished();
        triggerService();
    }

    private void onWebscraperTaskFailed(WorkerStateEvent event, WebscraperTaskAbstract task) {
        LOG.error("Webscraper failed");
        ScraperState.Builder crawlerstateBuilder = new ScraperState.Builder(currentScraperState);
        crawlerstateBuilder.state(State.FAILED);
        crawlerstateBuilder.timeFinish(System.currentTimeMillis());
        currentScraperState = crawlerstateBuilder.build();
        logFailedTask(task);
        webcrawlerDisabled = true;
        onWebscraperTaskFinished();
        triggerService();
    }

    private void onWebscraperTaskSucceeded(WorkerStateEvent event, WebscraperTaskAbstract task) {
        LOG.info("Webscraper succeeded");
        ScraperState.Builder crawlerstateBuilder = new ScraperState.Builder(currentScraperState);
        crawlerstateBuilder.state(State.SUCCEEDED);
        crawlerstateBuilder.timeFinish(System.currentTimeMillis());
        currentScraperState = crawlerstateBuilder.build();
        List<Fichaje> fichajeList = task.getValue();
        logScrapResults(fichajeList);
        ultimosFichajesDetectados = fichajeList;
        saveHistoricalValuesOnBackground(fichajeList);
        onWebscraperTaskFinished();
        triggerService();
    }

    private void onWebscraperTaskFinished()
    {
        userHasTriggeredWebscraper = false;
        webscraperTaskStarted = false;
    }

    private void triggerService() {
        restart();
    }

    private void saveHistoricalValuesOnBackground(final List<Fichaje> fichajeList) {
        Task<Void> dbTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateHistoricalValues(fichajeList);
                return null;
            }
        };
        Thread thread = new Thread(dbTask);
        thread.setName("MehDbTask");
        thread.setDaemon(true);
        thread.start();
    }

    private void databaseCreateIfNeeded(MehDbState state) {
        File dbFile = new File(state.getDbPath());
        if (dbFile.exists()) return;
        mehDb = new MehDbJavafx(state);
        try {
            LOG.debug("Calling db.create()");
            mehDb.create();
        } catch (Exception e) {
            String userMsg = "Error creating database";
            LOG.error(userMsg, e);
        }
    }

    private void databaseOpen(MehDbState state) {
        mehDb = new MehDbJavafx(state);
        try {
            LOG.debug("Calling db.open()");
            mehDb.open();
        } catch (Exception e) {
            String userMsg = "Error opening database";
            LOG.error(userMsg, e);
        }
    }

    private void databaseClose() {
        try {
            LOG.debug("Calling db.close()");
            mehDb.close();
        } catch (Exception e) {
            String userMsg = "Error closing database";
            LOG.error(userMsg, e);
        }
    }

    private void updateHistoricalValues(List<Fichaje> webscraperTaskResult) {
        try {
            String userName = options.getUsername();
            String fileName = SHA1String(userName);
            File dbFile = new File(MehDbJavafx.getFilesDir(), fileName + ".mehdb");
            String dbPath = dbFile.getPath();
            MehDbState mehDbState = new MehDbState.Builder().dbPath(dbPath).build();
            databaseCreateIfNeeded(mehDbState);
            databaseOpen(mehDbState);
            List<Fichaje> fichajeHistorialList = new ArrayList<>();
            fichajeHistorialList.addAll(mehDb.fichaje().selectAll(50, 0));
            for (Fichaje fichajeScrap : webscraperTaskResult) {
                Predicate<Fichaje> predicate = f -> f.getFechaMarcaje() == fichajeScrap.getFechaMarcaje();
                long coincidencias = fichajeHistorialList.stream().filter(predicate).count();
                Instant instant = Instant.ofEpochMilli(fichajeScrap.getFechaMarcaje());
                if (coincidencias == 0) {
                    LOG.debug("Añadiendo nuevo fichaje a la BBDD: " + instant.toString());
                    mehDb.fichaje().insert(fichajeScrap);
                } else {
                    //LOG.debug("El fichaje ya esta en BBDD: " + instant.toString());
                }
            }
        } catch (Exception e) {
            LOG.error("Error running updateHistoricalValues", e);
        } finally {
            databaseClose();
        }
    }

    private void logScrapResults(List<Fichaje> fichajeList) {
        for (Fichaje fichaje : fichajeList) {
            LOG.debug("Fichaje encontrado: " + fichaje);
        }
    }

    public void eventUserTriggeredWebscraper() {
        userHasTriggeredWebscraper = true;
        webcrawlerDisabled = false;
        triggerService();
    }

    @Override
    protected Task<MehServiceState> createTask() {
        final Task<MehServiceState> task = new Task<MehServiceState>() {
            @Override
            protected MehServiceState call() throws Exception {
                return sheduledServiceRun();
            }
        };
        return task;
    }

    private MehServiceState sheduledServiceRun() {
        if (shouldTriggerWebscraper()) {
            webscraperTaskStarted = true;
            triggerWebscraperTask();
        }
        final WorkState workState;
        final ScraperState scraperState;
        final List<Fichaje> lastResults = ultimosFichajesDetectados;
        if (lastResults != null) {
            long currentTime = System.currentTimeMillis();
            workState = MehStateLogic.compute(currentTime, lastResults);
        } else {
            workState = WorkState.UNKNOWN;
        }
        if (currentScraperState != null) {
            scraperState = currentScraperState;
        } else {
            scraperState = ScraperState.UNKNOWN;
        }
        currentScraperState = scraperState;
        currentWorkState = workState;
        return new MehServiceState.Builder().crawlerstate(scraperState).workstate(workState).build();
    }

    public boolean isWebscraperTaskStarted() {
        return webscraperTaskStarted;
    }

    public void waitShutdown(long millis) {
        final int refreshMillis = 200;
        long waited = 0;
        while (waited < millis) {
            if (isWebscraperTaskStarted()) {
                try {
                    Thread.sleep(refreshMillis);
                    waited += refreshMillis;
                } catch (InterruptedException e) {
                    LOG.error("Thread sleep interrumpido", e);
                }
            } else {
                break;
            }
        }
    }

    private void logFailedTask(WebscraperTaskAbstract task) {
        Throwable throwable = task.getException();
        String errorMsg = "Webscraper task failed";
        if (throwable != null) {
            LOG.error(errorMsg, throwable);
        } else {
            LOG.error(errorMsg);
        }
    }

}
