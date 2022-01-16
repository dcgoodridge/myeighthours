package myeighthours.viewcontroller;


import fxmlwindow.FxmlController;
import fxmlwindow.FxmlWindow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import myeighthours.Fichaje;
import myeighthours.Main;
import myeighthours.database.dao.MehDb;
import myeighthours.database.dao.MehDbJavafx;
import myeighthours.database.dao.MehDbState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static myeighthours.helper.HelperDigest.SHA1String;

public class FichajeListController extends FxmlController {

    private static final Logger LOG = LoggerFactory.getLogger(FichajeListController.class);

    public static final String TITLE = "Fichajes";

    @FXML
    private ListView<Fichaje> listView;

    @FXML
    private Button b_fichajeAdd;

    @FXML
    private Button b_fichajeSynchronize;

    private static final int ROWS_PER_DB_QUERY = 20;

    private int pagesShown = 1;

    private MehDbState mehDbState;

    private MehDb mehDb;

    private boolean hitLastPage = false;

    @Override
    public void onWindowInitialize(URL location, ResourceBundle resources) {
        LOG.debug("onWindowInitialize");
        initMehDbState();
        listView.setCellFactory(new Callback<ListView<Fichaje>, ListCell<Fichaje>>() {
            @Override
            public ListCell<Fichaje> call(ListView<Fichaje> listView) {
                FichajeListCellFactory listCellFactory = new FichajeListCellFactory();
                listCellFactory.setListener(new MyFichajeListCellFactoryListener());
                return listCellFactory;
            }
        });
        b_fichajeAdd.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onEventClickFichajeAdd();
            }
        });
        b_fichajeSynchronize.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onEventClickFichajeSynchronize();
            }
        });
        b_fichajeSynchronize.setDisable(true);
        b_fichajeSynchronize.setTooltip(new Tooltip("Sincronizar fichajes"));
        b_fichajeAdd.setTooltip(new Tooltip("Añadir fichaje"));

    }

    private void onEventClickFichajeSynchronize() {
        startFichajeSynchronizeWindow();
    }

    private void startFichajeSynchronizeWindow() {
        FxmlWindow<FichajeEditController> fichajeListWindow = new FxmlWindow(getClass().getResource("/FichajeSynchronize.fxml"));
        fichajeListWindow.setIcon(Main.ICON_MAIN);
        fichajeListWindow.getStage().setResizable(false);
        fichajeListWindow.getStage().initModality(Modality.APPLICATION_MODAL);
        fichajeListWindow.show();
    }

    private void onEventClickFichajeAdd() {
        startFichajeAddWindow();
    }

    private void startFichajeAddWindow() {
        startFichajeEditWindow(null);
    }

    private void startFichajeEditWindow(Fichaje fichaje) {
        FxmlWindow<FichajeEditController> fichajeListWindow = new FxmlWindow(getClass().getResource("/FichajeEdit.fxml"));
        fichajeListWindow.setIcon(Main.ICON_MAIN);
        fichajeListWindow.getStage().setResizable(false);
        fichajeListWindow.getStage().initModality(Modality.APPLICATION_MODAL);
        fichajeListWindow.getController().setState(fichaje);
        fichajeListWindow.getController().setListener(f -> populateList());
        fichajeListWindow.show();
    }

    private void initMehDbState() {
        String userName = Main.options.getUsername();
        String fileName = SHA1String(userName);
        File dbFile = new File(MehDbJavafx.getFilesDir(), fileName + ".mehdb");
        String dbPath = dbFile.getPath();
        MehDbState mehDbState = new MehDbState.Builder().dbPath(dbPath).build();
        this.mehDbState = mehDbState;
    }

    private class MyFichajeListCellFactoryListener implements FichajeListCellFactory.Listener {
        @Override
        public void onEventClickDelete(Fichaje fichaje) {
            startFichajeDeleteConfirmation(fichaje);
        }

        @Override
        public void onEventClickEdit(Fichaje fichaje) {
            startFichajeEditWindow(fichaje);
        }
    }

    private void startFichajeDeleteConfirmation(Fichaje fichaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Eliminar fichaje");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        List<Image> imageList = new ArrayList<>();
        for (int i=0; i<Main.ICON_MAIN.length;i++){
            String url = Main.ICON_MAIN[i];
            imageList.add(new Image(url));
        }
        stage.getIcons().addAll(imageList);
        alert.setHeaderText("¿Está seguro de que quiere eliminar este fichaje?");
        ButtonType buttonYes = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonYes, buttonCancel);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonYes){
            deleteFichaje(fichaje);
        } else {
            // ... user chose CANCEL or closed the dialog
        }
    }

    private void deleteFichaje(Fichaje fichaje) {
        try {
            databaseOpen(mehDbState);
            mehDb.fichaje().delete(fichaje);
        } catch (Exception e) {
            LOG.error("Error eliminando fichaje", e);
        } finally {
            databaseClose();
        }
        populateList();
    }

    private void populateList() {
        List<Fichaje> fichajeList = new ArrayList<>();
        try {
            databaseOpen(mehDbState);
            fichajeList.addAll(mehDb.fichaje().selectPages(pagesShown * ROWS_PER_DB_QUERY, 0));
        } catch (Exception e) {
            LOG.error("Error seleccionando fichajes", e);
        } finally {
            databaseClose();
        }
        ObservableList<Fichaje> observable = FXCollections.observableList(fichajeList);
        listView.setItems(observable);
    }

    @Override
    public void onWindowOpen(Stage stage) {
        LOG.debug("onWindowOpen");
        populateList();
        setListviewReachedBottomListener();
    }

    private void setListviewReachedBottomListener() {
        ScrollBar bar = (ScrollBar) listView.lookup(".scroll-bar");
        double scrollBarPositionTrigger = 0.7;
        bar.valueProperty().addListener((src, ov, nv) -> {
            if (nv.doubleValue() > scrollBarPositionTrigger) {
                addPageToList();
            }
        });
    }

    private void addPageToList() {
        if (hitLastPage) return;
        pagesShown = pagesShown + 1;
        int pageIndex = pagesShown - 1;
        List<Fichaje> fichajePageList = new ArrayList<>();
        try {
            databaseOpen(mehDbState);
            fichajePageList.addAll(mehDb.fichaje().selectPages(ROWS_PER_DB_QUERY, pageIndex));
        } catch (Exception e) {
            LOG.error("Error selecting " + ROWS_PER_DB_QUERY + " rows, page=" + pagesShown + "from fichajes", e);
        } finally {
            databaseClose();
        }
        listView.getItems().addAll(fichajePageList);
        if (fichajePageList.size() < ROWS_PER_DB_QUERY) {
            hitLastPage = true;
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

    @Override
    public void onWindowClose() {
        LOG.debug("onWindowClose");
    }

    public void setState(MehDbState state) {
        LOG.debug("setState");
        this.mehDbState = state;
    }

}
