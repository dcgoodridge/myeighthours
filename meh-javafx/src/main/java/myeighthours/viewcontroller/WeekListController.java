package myeighthours.viewcontroller;


import fxmlwindow.FxmlController;
import fxmlwindow.FxmlWindow;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import myeighthours.*;
import myeighthours.database.dao.MehDb;
import myeighthours.database.dao.MehDbJavafx;
import myeighthours.database.dao.MehDbState;
import org.aeonbits.owner.ConfigFactory;
import org.joda.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static myeighthours.helper.HelperDigest.SHA1String;

public class WeekListController extends FxmlController {

    private static final Logger LOG = LoggerFactory.getLogger(WeekListController.class);

    public static final String TITLE = "Fichajes";

    private static final DateTimeZone DATETIMEZONE_LOCAL = DateTimeZone.getDefault();

    @FXML
    private ListView<FichajesSemana> listView;

    @FXML
    private Button fichajeListButton;

    @FXML
    private Button weekForward;

    @FXML
    private Button weekBackward;

    private static final int ROWS_PER_DB_QUERY = 20;

    private int pagesShown = 1;

    private MehDbState mehDbState;

    private MehDb mehDb;

    private MehProperties mehProperties;

    private boolean hitLastPage = false;

    private LocalDate lastWeekStart;

    @Override
    public void onWindowInitialize(URL location, ResourceBundle resources) {
        LOG.debug("onWindowInitialize");
        mehProperties = ConfigFactory.create(MehProperties.class);
        initMehDbState();
        listView.setCellFactory(new Callback<ListView<FichajesSemana>, ListCell<FichajesSemana>>() {
            @Override
            public ListCell<FichajesSemana> call(ListView<FichajesSemana> listView) {
                WeekListCellFactory listCellFactory = new WeekListCellFactory();
                listCellFactory.setListener(new MyFichajeListCellFactoryListener());
                return listCellFactory;
            }
        });

        listView.setMouseTransparent(true);
        listView.setFocusTraversable(false);
        fichajeListButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onEventClickFichajeList();
            }
        });
        weekForward.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onEventClickWeekForward();
            }
        });
        weekBackward.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onEventClickWeekBackward();
            }
        });
        fichajeListButton.setVisible(false);
    }

    private void onEventClickWeekBackward()
    {
        lastWeekStart = lastWeekStart.minusWeeks( 1 );
        populateList();
    }

    private void onEventClickWeekForward()
    {
        lastWeekStart = lastWeekStart.plusWeeks( 1 );
        populateList();
    }

    private void startFichajeListWindow() {
        FxmlWindow<FichajeListController> fichajeListWindow = new FxmlWindow(getClass().getResource("/FichajeList.fxml"));
        fichajeListWindow.setTitle("Listado de fichajes");
        fichajeListWindow.setIcon(Main.ICON_MAIN);
        fichajeListWindow.getStage().setResizable(false);
        fichajeListWindow.getStage().initModality(Modality.APPLICATION_MODAL);
        fichajeListWindow.show();
    }

    private void onEventClickFichajeList() {
        startFichajeListWindow();
    }

    private void onEventClickExtra() {
    }

    private void initMehDbState() {
        String userName = Main.options.getUsername();
        String fileName = SHA1String(userName);//.substring(0,12);

        File dbFile = new File(MehDbJavafx.getFilesDir(), fileName + ".mehdb");
        String dbPath = dbFile.getPath();
        MehDbState mehDbState = new MehDbState.Builder().dbPath(dbPath).build();
        this.mehDbState = mehDbState;
    }

    private class MyFichajeListCellFactoryListener implements WeekListCellFactory.Listener {

        @Override
        public void onEventClickDelete(Fichaje fichaje) {
            startFichajeDeleteConfirmation(fichaje);
        }

        @Override
        public void onEventClickEdit(Fichaje fichaje) {
        }
    }

    private void startFichajeDeleteConfirmation(Fichaje transaction) {

    }

    /**
     * Calcula el listado de fichajes en bbdd de las ultimas 4 semanas
     * Warning: Long running operation
     * @return
     */
    private List<FichajesSemana> calcularFichajesVentana(LocalDate localDate) {
        LocalDate currentWeekStart = localDate.withDayOfWeek(DateTimeConstants.MONDAY);

        LocalDate weekMinus0 = currentWeekStart.withFieldAdded(DurationFieldType.weeks(), -0);
        LocalDate weekMinus1 = currentWeekStart.withFieldAdded(DurationFieldType.weeks(), -1);
        LocalDate weekMinus2 = currentWeekStart.withFieldAdded(DurationFieldType.weeks(), -2);
        LocalDate weekMinus3 = currentWeekStart.withFieldAdded(DurationFieldType.weeks(), -3);
        LocalDate weekMinus4 = currentWeekStart.withFieldAdded(DurationFieldType.weeks(), -4);

        long weekMinus0_timestamp = weekMinus0.toDateTime(LocalTime.MIDNIGHT, DATETIMEZONE_LOCAL).getMillis();
        long weekMinus1_timestamp = weekMinus1.toDateTime(LocalTime.MIDNIGHT, DATETIMEZONE_LOCAL).getMillis();
        long weekMinus2_timestamp = weekMinus2.toDateTime(LocalTime.MIDNIGHT, DATETIMEZONE_LOCAL).getMillis();
        long weekMinus3_timestamp = weekMinus3.toDateTime(LocalTime.MIDNIGHT, DATETIMEZONE_LOCAL).getMillis();
        long weekMinus4_timestamp = weekMinus4.toDateTime(LocalTime.MIDNIGHT, DATETIMEZONE_LOCAL).getMillis();

        List<Long> timestampsLuneses = new ArrayList<>();

        timestampsLuneses.add(weekMinus0_timestamp);
        timestampsLuneses.add(weekMinus1_timestamp);
        timestampsLuneses.add(weekMinus2_timestamp);
        timestampsLuneses.add(weekMinus3_timestamp);
        timestampsLuneses.add(weekMinus4_timestamp);

        timestampsLuneses = timestampsLuneses.stream().sorted((f1, f2) -> Long.compare(f1, f2)).collect(Collectors.toList());

        List<FichajesSemana> fichajesSemanaListResult = new ArrayList<>();
        for (int i = 0; i < timestampsLuneses.size(); i++) {
            long timestamp = timestampsLuneses.get(i);
            FichajesSemana fichajesSemana = calculaFichajesSemanaConBD(timestamp);
            fichajesSemanaListResult.add(fichajesSemana);
        }

        return fichajesSemanaListResult;
    }

    private void populateList() {
        Task<ObservableList<FichajesSemana>> task = new Task<ObservableList<FichajesSemana>>() {
            @Override
            protected ObservableList<FichajesSemana> call() throws Exception {
                ObservableList<FichajesSemana> fichajesSemanaObservable = FXCollections.observableList(new ArrayList<>());
                databaseCreateIfNeeded(mehDbState);
                try {
                    databaseOpen(mehDbState);
                    fichajesSemanaObservable.addAll(calcularFichajesVentana(lastWeekStart));
                } finally {
                    databaseClose();
                }
                return fichajesSemanaObservable;
            }
        };
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                ObservableList<FichajesSemana> fichajesSemanas = task.getValue();
                populateList(fichajesSemanas);
            }
        });

        Thread thread = new Thread(task);
        thread.setName("WeekList Task");
        thread.setDaemon(true);
        thread.start();
    }


    private void populateList(ObservableList<FichajesSemana> fichajesSemanas) {
        listView.setItems(fichajesSemanas);
    }

    private FichajesSemana calculaFichajesSemanaConBD(long timestampStart) {
        long timestampEnd = timestampStart + CalendarioLaboral.WEEK_MILLIS;

        Instant instantWeekStart = Instant.ofEpochMilli(timestampStart);
        Instant instantWeekEnd = Instant.ofEpochMilli(timestampEnd);

        List<Fichaje> fichajesSemanaList = new ArrayList<>();
        try {
            fichajesSemanaList.addAll(mehDb.fichaje().selectTimeRange(timestampStart, timestampEnd));
        } catch (Exception e) {
            LOG.error("Error selecting fichajes", e);
        }
        FichajesSemana fichajesSemana = calcularFichajesSemana(fichajesSemanaList, timestampStart);
        return fichajesSemana;
    }

    private FichajesSemana calcularFichajesSemana(List<Fichaje> fichajesSemanaList, long timestampStart) {
        FichajesSemana fichajesSemana = new FichajesSemana();
        LocalDate currentLocalDate = new LocalDate(DATETIMEZONE_LOCAL);
        LocalDate lunes = new LocalDate(timestampStart, DATETIMEZONE_LOCAL);
        int year = lunes.getYear();
        int week = lunes.getWeekOfWeekyear();
        fichajesSemana.year = year;
        fichajesSemana.week = week;
        long millisTrabajadosSemana = 0;
        long millisATrabajarSemana = 0;
        fichajesSemana.fichajeDiaList.clear();

        for (int i = 0; i < 7; i++) {
            long timestampDiaStart = timestampStart + i * CalendarioLaboral.DAY_MILLIS;
            long timestampDiaEnd = timestampDiaStart + CalendarioLaboral.DAY_MILLIS;
            LocalDate diaLocalDate = new LocalDate(timestampDiaStart, DATETIMEZONE_LOCAL);
            int daysBetween = Days.daysBetween(currentLocalDate, diaLocalDate).getDays();
            boolean isCurrentDay = (daysBetween == 0);
            List<Fichaje> fichajesDiaList = dameFichajesEntreRango(fichajesSemanaList, timestampDiaStart, timestampDiaEnd);
            Duration duracionTrabajada = Duration.millis( MehStateLogic.calcularMillisTrabajados(fichajesDiaList).toMillis() );
            Duration duracionJornada = Duration.millis( CalendarioLaboral.calcularTiempoATrabajar(timestampDiaStart) );
            int dia = diaLocalDate.getDayOfMonth();

            FichajesDia fichajesDia = new FichajesDia();
            fichajesDia.dia = dia;
            fichajesDia.millisATrabajar = duracionJornada.getMillis();
            fichajesDia.millisTrabajados = duracionTrabajada.getMillis();
            if (fichajesDiaList.isEmpty()) {
                fichajesDia.noData = true;
            }
            if (duracionTrabajada.getMillis() == 0) {
                fichajesDia.noData = true;
            }
            fichajesDia.isCurrentDay = isCurrentDay;
            fichajesSemana.fichajeDiaList.add(fichajesDia);
            millisATrabajarSemana += duracionJornada.getMillis();
            millisTrabajadosSemana += duracionTrabajada.getMillis();
        }
        fichajesSemana.millisATrabajar = millisATrabajarSemana;
        fichajesSemana.millisTrabajados = millisTrabajadosSemana;
        return fichajesSemana;
    }

    public static class FichajesSemana {

        public int year = 0;

        public int week = 0;

        public long millisATrabajar = 0;

        public long millisTrabajados = 0;

        public List<FichajesDia> fichajeDiaList = new ArrayList<>(7);

        public long computeExtraWorkTimeMillis() {
            long dif = 0;
            for (int i = 0; i < fichajeDiaList.size(); i++) {
                dif += fichajeDiaList.get(i).computeExtraWorkTimeMillis();
            }
            return dif;
        }
    }

    public static class FichajesDia {

        public boolean isCurrentDay = false;

        public int dia = 0;

        public long millisATrabajar = 0;

        public long millisTrabajados = 0;

        /**
         * Indica que este objeto no proviene de datos reales
         */
        public boolean noData;

        public long computeExtraWorkTimeMillis() {
            long dif = millisTrabajados - millisATrabajar;
            if (noData) return 0;
            else return dif;
        }

    }

    private List<Fichaje> dameFichajesEntreRango(List<Fichaje> fichajesSemanaList, long timestampDiaStart, long timestampDiaEnd) {
        List<Fichaje> result = new ArrayList<>();
        for (Fichaje fichaje : fichajesSemanaList) {
            long t = fichaje.getFechaMarcaje();
            if (t >= timestampDiaStart && t < timestampDiaEnd) {
                result.add(fichaje);
            }
        }
        return result;
    }

    @Override
    public void onWindowOpen(Stage stage) {
        LOG.debug("onWindowOpen");

        final KeyCombination debugKeyCombination = new KeyCodeCombination(
                KeyCode.D, KeyCombination.CONTROL_DOWN);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (debugKeyCombination.match(event)) {
                if (fichajeListButton.isVisible()) {
                    fichajeListButton.setVisible(false);
                } else {
                    fichajeListButton.setVisible(true);
                }
            }
        });

        //Run in UI after window load
        Platform.runLater( new Runnable() {
            @Override public void run() {

                LocalDate now = new LocalDate(DATETIMEZONE_LOCAL);
                lastWeekStart = now.withDayOfWeek(DateTimeConstants.MONDAY);

                populateList();
            }
        });
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

    private void databaseClose() {
        try {
            LOG.debug("Calling db.close()");
            mehDb.close();
        } catch (Exception e) {
            String userMsg = "Error closing database";
            LOG.error(userMsg, e);
        }
    }

    @Override
    public void onWindowClose() {
        LOG.debug("onWindowClose");
    }

    public void setState(MehDbState state) {
        LOG.debug("setState");
        this.mehDbState = state;
    }

}
