package myeighthours.viewcontroller;

import fxmlwindow.FxmlController;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import myeighthours.Fichaje;
import myeighthours.Main;
import myeighthours.database.dao.MehDb;
import myeighthours.database.dao.MehDbJavafx;
import myeighthours.database.dao.MehDbState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.time.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import static myeighthours.helper.HelperDigest.SHA1String;
import static myeighthours.helper.WindowUtils.showAlertErrorWindow;
import static myeighthours.helper.WindowUtils.showAlertInfoWindow;


public class FichajeEditController extends FxmlController {

    private static final Logger LOG = LoggerFactory.getLogger(FichajeEditController.class);

    public static String TITLE_UPDATE = "Editar fichaje";

    public static String TITLE_INSERT = "Nuevo fichaje";

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @FXML
    private Label l_direccion;

    @FXML
    private ChoiceBox cb_direccion;

    @FXML
    private Label l_timestamp;

    @FXML
    private DatePicker dp_fecha;

    @FXML
    private Button b_saveButton;

    @FXML
    private Button b_saveAndNewButton;

    @FXML
    private Button b_cancelButton;

    @FXML
    private TextField tf_fechaHora;
    @FXML
    private TextField tf_fechaMinuto;

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

        b_saveButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onEventClickSave();
            }
        });

        b_saveAndNewButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onEventClickSaveAndNew();
            }
        });

        b_cancelButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onEventClickCancel();
            }
        });

    }

    public void setState(Fichaje fichaje) {
        this.selectedFichaje = fichaje;
        if (fichaje == null) {
            mode = MODE.FICHAJE_INSERT;
        }else{
            mode = MODE.FICHAJE_UPDATE;
        }
    }

    private void onEventClickCancel() {
        close();
    }

    private void onEventClickSave() {
        try {
            databaseOpen(mehDbState);
            Fichaje fichaje = viewsRead();
            switch (mode) {
                case FICHAJE_UPDATE:
                    mehDb.fichaje().update(fichaje);
                    break;
                case FICHAJE_INSERT:
                    mehDb.fichaje().insert(fichaje);
                    break;
            }
            notifyOnEventClickSave(fichaje);
            close();
            showAlertInfoWindow("Fichaje insertado");
        } catch (Exception e){
            LOG.error("Error insertando fichaje", e);
            showAlertErrorWindow("Error insertando fichaje");
        } finally {
            databaseClose();
        }
    }

    private void onEventClickSaveAndNew() {
        try {
            databaseOpen(mehDbState);
            Fichaje fichaje = viewsRead();
            switch (mode) {
                case FICHAJE_UPDATE:
                    mehDb.fichaje().update(fichaje);
                    break;
                case FICHAJE_INSERT:
                    mehDb.fichaje().insert(fichaje);
                    break;
            }
            notifyOnEventClickSave(fichaje);
            showAlertInfoWindow("Fichaje insertado");
        } catch (Exception e){
            LOG.error("Error insertando fichaje", e);
            showAlertErrorWindow("Error insertando fichaje");
        } finally {
            databaseClose();
        }
    }

    private void notifyOnEventClickSave(Fichaje fichaje){
        if (listener != null) {
            listener.onEventClickSave(fichaje);
        }
    }

    @Override
    public void onWindowOpen(Stage stage) {
        LOG.debug("onWindowOpen");
        setCustomTitle();
        setCustomButtons();
        loadData();
    }

    private void setCustomButtons() {
        switch (mode){
            case FICHAJE_UPDATE:
                b_saveAndNewButton.setManaged(false);
                b_saveAndNewButton.setVisible(false);
                break;
        }
    }

    private void setCustomTitle() {
        switch (mode){
            case FICHAJE_UPDATE:
                getStage().setTitle(TITLE_UPDATE);
                break;
            case FICHAJE_INSERT:
                getStage().setTitle(TITLE_INSERT);
                break;
        }
    }

    private void loadData() {
        switch (mode){
            case FICHAJE_UPDATE:
                loadData_update();
                break;
            case FICHAJE_INSERT:
                loadData_insert();
                break;
        }
    }

    private Fichaje generateDefaultFichaje(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        long timestamp = calendar.getTimeInMillis();

        Fichaje fichaje = new Fichaje.Builder()
                .direccion(Fichaje.DIRECCION.ENTRADA)
                .fechaMarcaje(timestamp)
                .sincronizado(false) //Si se añade o se modifica, se convierte en Manual/ No-sincronizado
                .build();
        return fichaje;
    }

    private void loadData_insert() {
        selectedFichaje = generateDefaultFichaje();
        viewsWrite(selectedFichaje);
    }

    private void loadData_update() {
        try {
            databaseOpen(mehDbState);
            selectedFichaje = mehDb.fichaje().select(selectedFichaje);
            viewsWrite(selectedFichaje);
        } catch (Exception e) {
            LOG.error("Error seleccionando fichaje", e);
        } finally {
            databaseClose();
        }
    }

    private void viewsWrite(Fichaje fichaje) {
        viewsWrite_direccion(fichaje);
        viewsWrite_timestamp(fichaje.getFechaMarcaje());
    }

    private void viewsWrite_timestamp(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZONE_ID);
        LocalDate ld = ldt.toLocalDate();
        LocalTime lt = ldt.toLocalTime();
        dp_fecha.setValue(ld);

        tf_fechaHora.setText(String.valueOf(lt.getHour()));
        tf_fechaMinuto.setText(String.valueOf(lt.getMinute()));
    }

    private long viewsRead_timestamp() {
        LocalDate ld = dp_fecha.getValue();
        int hora = Integer.parseInt(tf_fechaHora.getText());
        int minuto = Integer.parseInt(tf_fechaMinuto.getText());
        LocalTime lt = LocalTime.of(hora,minuto);
        LocalDateTime ldt = LocalDateTime.of(ld,lt);
        ZonedDateTime zdt = ZonedDateTime.of(ldt,ZONE_ID);
        Instant instant = zdt.toInstant();
        long timestamp = instant.toEpochMilli();
        return timestamp;
    }

    private void viewsWrite_direccion(Fichaje fichaje) {
        List<String> direccionesPosiblesList = new ArrayList<>();
        direccionesPosiblesList.add("ENTRADA");
        direccionesPosiblesList.add("SALIDA");
        cb_direccion.setItems(FXCollections.observableArrayList(direccionesPosiblesList));
        if (fichaje.getDireccion() == Fichaje.DIRECCION.ENTRADA) {
            cb_direccion.getSelectionModel().select(0);
        } else {
            cb_direccion.getSelectionModel().select(1);
        }
    }

    private Fichaje viewsRead() throws Exception {
        Fichaje.Builder builder = new Fichaje.Builder(selectedFichaje);
        Fichaje.DIRECCION direccion = viewsRead_direccion();
        builder.direccion(direccion);
        long timestamp = viewsRead_timestamp();
        builder.fechaMarcaje(timestamp);
        builder.sincronizado(false); //Si se añade o se modifica, se convierte en Manual/ No-sincronizado
        return builder.build();
    }

    private Fichaje.DIRECCION viewsRead_direccion() throws Exception {
        int selectedIndex = cb_direccion.getSelectionModel().getSelectedIndex();
        Fichaje.DIRECCION direccion = Fichaje.DIRECCION.ENTRADA;
        if (selectedIndex == 0 ) {
            direccion = Fichaje.DIRECCION.ENTRADA;
        } else {
            direccion = Fichaje.DIRECCION.SALIDA;
        }
        return direccion;
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

    @Override
    public void onWindowClose() {
        LOG.debug("onWindowClose");
    }
}
