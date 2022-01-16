package myeighthours.viewcontroller;

import fxmlwindow.FxmlController;
import javafx.collections.FXCollections;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import myeighthours.Fichaje;
import myeighthours.Main;
import myeighthours.scraper.WebscraperHistoricalTask;
import myeighthours.scraper.WebscraperTaskAbstract;
import myeighthours.database.dao.MehDb;
import myeighthours.database.dao.MehDbJavafx;
import myeighthours.database.dao.MehDbState;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static myeighthours.helper.HelperDigest.SHA1String;


public class FichajeSynchronizeController extends FxmlController {

    private static final Logger LOG = LoggerFactory.getLogger(FichajeSynchronizeController.class);

    private static final DateTimeZone DATETIMEZONE_LOCAL = DateTimeZone.getDefault();

    public static String TITLE = "Sincronizar fichajes";

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @FXML
    private Label l_semana;

    @FXML
    private ChoiceBox cb_semana;

    @FXML
    private Button b_cancel;

    @FXML
    private Button b_sync;

    enum MODE {FICHAJE_UPDATE, FICHAJE_INSERT}

    private MODE mode = MODE.FICHAJE_INSERT;

    private Fichaje selectedFichaje;

    private Listener listener;

    private MehDbState mehDbState;

    private MehDb mehDb;

    interface Listener {
        void onEventClickSave(Fichaje fichaje);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void initMehDbState() {
        String userName = Main.options.getUsername();
        String fileName = SHA1String(userName);

        File dbFile = new File(MehDbJavafx.getFilesDir(), fileName + ".mehdb");
        String dbPath = dbFile.getPath();
        MehDbState mehDbState = new MehDbState.Builder().dbPath(dbPath).build();
        this.mehDbState = mehDbState;
    }

    @Override
    public void onWindowInitialize(URL location, ResourceBundle resources) {

        initMehDbState();

        b_sync.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onEventClickSync();
            }
        });

        b_cancel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onEventClickCancel();
            }
        });
    }

    private void onEventClickSync() {
        int selectedIndex = cb_semana.getSelectionModel().getSelectedIndex();

        LocalDate now = new LocalDate(DATETIMEZONE_LOCAL);
        LocalDate selectedWeek = now.withFieldAdded(DurationFieldType.weeks(), -selectedIndex);

        DateTime week_ini = selectedWeek.withDayOfWeek(DateTimeConstants.MONDAY).toDateTime(LocalTime.MIDNIGHT, DATETIMEZONE_LOCAL);
        DateTime week_end = selectedWeek.withDayOfWeek(DateTimeConstants.SUNDAY).toDateTime(new LocalTime(23,59, 59, 999), DATETIMEZONE_LOCAL);

        triggerWebscraperHistoricalTask(week_ini, week_end);
    }

    public void setState(Fichaje fichaje) {
        this.selectedFichaje = fichaje;
        if (fichaje == null) {
            mode = MODE.FICHAJE_INSERT;
        } else {
            mode = MODE.FICHAJE_UPDATE;
        }
    }

    private void onEventClickCancel() {
        close();
    }

    private void notifyOnEventClickSave(Fichaje fichaje) {
        if (listener != null) {
            listener.onEventClickSave(fichaje);
        }
    }

    @Override
    public void onWindowOpen(Stage stage) {
        LOG.debug("onWindowOpen");
        setCustomTitle();
        loadData();
    }

    private void loadData() {
        List<String> opcionesList = new ArrayList<>();
        LocalDate now = new LocalDate(DATETIMEZONE_LOCAL);
        LocalDate week0 = now.withFieldAdded(DurationFieldType.weeks(), -0);
        LocalDate week1 = now.withFieldAdded(DurationFieldType.weeks(), -1);
        LocalDate week2 = now.withFieldAdded(DurationFieldType.weeks(), -2);
        LocalDate week3 = now.withFieldAdded(DurationFieldType.weeks(), -3);
        DateTime dt = new DateTime();
        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MMMM");
        DateTime week0_ini = week0.withDayOfWeek(DateTimeConstants.MONDAY).toDateTime(LocalTime.MIDNIGHT, DATETIMEZONE_LOCAL);
        DateTime week0_end = week0.withDayOfWeek(DateTimeConstants.SUNDAY).toDateTime(new LocalTime(23,59, 59, 999), DATETIMEZONE_LOCAL);
        String week0String = fmt.print(week0_ini)+" ... "+fmt.print(week0_end);
        DateTime week1_ini = week1.withDayOfWeek(DateTimeConstants.MONDAY).toDateTime(LocalTime.MIDNIGHT, DATETIMEZONE_LOCAL);
        DateTime week1_end = week1.withDayOfWeek(DateTimeConstants.SUNDAY).toDateTime(new LocalTime(23,59, 59, 999), DATETIMEZONE_LOCAL);
        String week1String = fmt.print(week1_ini)+" ... "+fmt.print(week1_end);
        DateTime week2_ini = week2.withDayOfWeek(DateTimeConstants.MONDAY).toDateTime(LocalTime.MIDNIGHT, DATETIMEZONE_LOCAL);
        DateTime week2_end = week2.withDayOfWeek(DateTimeConstants.SUNDAY).toDateTime(new LocalTime(23,59, 59, 999), DATETIMEZONE_LOCAL);
        String week2String = fmt.print(week2_ini)+" ... "+fmt.print(week2_end);
        DateTime week3_ini = week3.withDayOfWeek(DateTimeConstants.MONDAY).toDateTime(LocalTime.MIDNIGHT, DATETIMEZONE_LOCAL);
        DateTime week3_end = week3.withDayOfWeek(DateTimeConstants.SUNDAY).toDateTime(new LocalTime(23,59, 59, 999), DATETIMEZONE_LOCAL);
        String week3String = fmt.print(week3_ini)+" ... "+fmt.print(week3_end);
        opcionesList.add("SEMANA -0: "+week0String);
        opcionesList.add("SEMANA -1: "+week1String);
        opcionesList.add("SEMANA -2: "+week2String);
        opcionesList.add("SEMANA -3: "+week3String);
        cb_semana.setItems(FXCollections.observableArrayList(opcionesList));
        cb_semana.getSelectionModel().select(0);
    }

    private void setCustomTitle() {
        getStage().setTitle(TITLE);
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

    private void triggerWebscraperHistoricalTask(DateTime dateTimeStart, DateTime dateTimeEnd) {
        LOG.debug("Webscraper triggered triggerWebscraperHistoricalTask");
        final WebscraperTaskAbstract webscraperHistoricalTask;
        webscraperHistoricalTask = new WebscraperHistoricalTask(Main.options, dateTimeStart, dateTimeEnd);
        webscraperHistoricalTask.setOnCancelled(event -> onWebscraperHistoricalTaskCancelled(event, webscraperHistoricalTask));
        webscraperHistoricalTask.setOnFailed(event -> onWebscraperHistoricalTaskFailed(event, webscraperHistoricalTask));
        webscraperHistoricalTask.setOnSucceeded(event -> onWebscraperHistoricalTaskSucceeded(event, webscraperHistoricalTask));
        webscraperHistoricalTask.setOnRunning(event -> onWebscraperHistoricalTaskRunning(event, webscraperHistoricalTask));
        webscraperHistoricalTask.setOnScheduled(event -> onWebscraperHistoricalTaskScheduled(event, webscraperHistoricalTask));
        Thread webscraperHistoricalTaskThread = new Thread(webscraperHistoricalTask);
        webscraperHistoricalTaskThread.setName("Webscraper Historical Task Thread");
        webscraperHistoricalTaskThread.setDaemon(true);
        webscraperHistoricalTaskThread.start();
    }

    @Override
    public void onWindowClose() {
        LOG.debug("onWindowClose");
    }

    private void onWebscraperHistoricalTaskScheduled(WorkerStateEvent event, WebscraperTaskAbstract task) {
        LOG.debug("onWebscraperHistoricalTaskScheduled");
    }

    private void onWebscraperHistoricalTaskRunning(WorkerStateEvent event, WebscraperTaskAbstract task) {
        LOG.debug("onWebscraperHistoricalTaskRunning");

    }

    private void onWebscraperHistoricalTaskSucceeded(WorkerStateEvent event, WebscraperTaskAbstract task) {
        LOG.debug("onWebscraperHistoricalTaskSucceeded");
        List<Fichaje> fichajeList = task.getValue();
        logHistoricalScrapResults(fichajeList);
    }

    private void logHistoricalScrapResults(List<Fichaje> fichajeList) {
        for (Fichaje fichaje : fichajeList) {
            LOG.debug("Fichaje histórico encontrado: " + fichaje);
        }
    }

    private void onWebscraperHistoricalTaskFailed(WorkerStateEvent event, WebscraperTaskAbstract task) {
        LOG.debug("onWebscraperHistoricalTaskFailed");

    }

    private void onWebscraperHistoricalTaskCancelled(WorkerStateEvent event, WebscraperTaskAbstract task) {
        LOG.debug("onWebscraperHistoricalTaskCancelled");
    }
}
