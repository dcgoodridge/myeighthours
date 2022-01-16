package myeighthours.viewcontroller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import myeighthours.Fichaje;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FichajeListCellFactory extends ListCell<Fichaje> {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormat.forPattern("yyyy MMM dd");

    private static final DateTimeFormatter FORMAT_TIME = DateTimeFormat.forPattern("HH:mm");

    private static final Logger LOG = LoggerFactory.getLogger(FichajeListCellFactory.class);

    private Pane cellView;

    @FXML
    private Label labelDireccion;

    @FXML
    private Label labelFecha;

    @FXML
    private Label labelHora;

    @FXML
    private Label labelNotSync;

    private Listener listener;

    interface Listener {
        void onEventClickDelete(Fichaje fichaje);
        void onEventClickEdit(Fichaje fichaje);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public FichajeListCellFactory() {
        super();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FichajeListItem.fxml"));
        fxmlLoader.setController(this);
        try {
            cellView = fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.setGraphic(this.cellView);
        ContextMenu contextMenu = new ContextMenu();
        MenuItem itemEdit = new MenuItem();
        itemEdit.setText("Modificar");
        itemEdit.setOnAction(event -> {
            Fichaje item = this.getItem();
            onEventClickEdit(item);
        });
        MenuItem itemDelete = new MenuItem();
        itemDelete.setText("Eliminar");
        itemDelete.setOnAction(event -> {
            Fichaje item = this.getItem();
            onEventClickDelete(item);
        });
        contextMenu.getItems().addAll(itemEdit, itemDelete);
        this.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
            if (isNowEmpty) {
                setContextMenu(null);
            } else {
                setContextMenu(contextMenu);
            }
        });
    }

    private void onEventClickDelete(Fichaje item) {
        listener.onEventClickDelete(item);
    }

    private void onEventClickEdit(Fichaje item) {
        listener.onEventClickEdit(item);
    }

    @Override
    protected void updateItem(Fichaje item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || (item == null)) {
            setGraphic(null);
        } else {
            labelDireccion.setText(item.getDireccion().toString());
            String dateString = FORMAT_DATE.print(item.getFechaMarcaje());
            String timeString = FORMAT_TIME.print(item.getFechaMarcaje());
            labelFecha.setText(dateString);
            labelHora.setText(timeString);
            if (item.isSincronizado()) {
                labelNotSync.setVisible(false);
            }else{
                labelNotSync.setVisible(true);
            }
            setGraphic(cellView);
        }
    }

}
