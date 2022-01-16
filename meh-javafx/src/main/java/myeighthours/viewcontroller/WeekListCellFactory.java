package myeighthours.viewcontroller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import myeighthours.CalendarioLaboral;
import myeighthours.Fichaje;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class WeekListCellFactory extends ListCell<WeekListController.FichajesSemana> {

    private static final DateFormat FORMAT_DATE = new SimpleDateFormat("yyyy MMM dd");

    private static final DateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm");

    private static final DateTimeZone DATETIMEZONE_LOCAL = DateTimeZone.getDefault();

    private static final Logger LOG = LoggerFactory.getLogger(WeekListCellFactory.class);

    private static final Color COLOR_RED = Color.web("#a80000"); //color rojo oscuro

    private static final Color COLOR_GREEN = Color.web("#22a100"); //color verde oscuro

    private static final Color COLOR_BLACK = Color.web("#000000");

    private Pane cellView;

    @FXML
    private Label labelDay0_num, labelDay1_num, labelDay2_num, labelDay3_num, labelDay4_num, labelDay5_num, labelDay6_num;

    @FXML
    private Label labelDay0_text0, labelDay1_text0, labelDay2_text0, labelDay3_text0, labelDay4_text0, labelDay5_text0, labelDay6_text0;

    @FXML
    private Label labelDay0_text1, labelDay1_text1, labelDay2_text1, labelDay3_text1, labelDay4_text1, labelDay5_text1, labelDay6_text1;

    @FXML
    private Label labelWeek_num, labelWeek_text0, labelWeek_text1;

    @FXML
    private HBox weekHbox;

    private Listener listener;

    interface Listener {
        void onEventClickDelete(Fichaje fichaje);

        void onEventClickEdit(Fichaje fichaje);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public WeekListCellFactory() {
        super();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/WeekListItem.fxml"));
        fxmlLoader.setController(this);
        try {
            cellView = fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.setGraphic(this.cellView);
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem();
        editItem.setText("Editar");

        editItem.setOnAction(event -> {
            WeekListController.FichajesSemana item = this.getItem();
            onEventClickEdit(item);
        });
        MenuItem deleteItem = new MenuItem();
        deleteItem.setText("Eliminar");

        deleteItem.setOnAction(event -> {
            WeekListController.FichajesSemana item = this.getItem();
            onEventClickDelete(item);
        });
        contextMenu.getItems().addAll(editItem, deleteItem);
    }

    private void onEventClickDelete(WeekListController.FichajesSemana item) {
        //listener.onEventClickDelete(item);
    }

    private void onEventClickEdit(WeekListController.FichajesSemana item) {
        //listener.onEventClickEdit(item);
    }

    private static String millisToHourMinuteString(long millis){
        long hourLong = millis / CalendarioLaboral.ONE_HOUR_MILLIS;
        long millisRestadoHoras = millis - hourLong * CalendarioLaboral.ONE_HOUR_MILLIS;
        long minutesLong = millisRestadoHoras / CalendarioLaboral.ONE_MINUTE_MILLIS; // AQUI HAY REDONDEO HACIA ABAJO, PERO SOLO SE MUESTRA A NIVEL DE DIA
        String result = "";
        //if (minutesLong>0) {
            String minutesTwoChars = String.format("%02d", minutesLong);
            result = minutesTwoChars+"m";
        //}
        result = hourLong+ "h" + result;
        return result;
    }

    private static String millisIntervalToHourMinuteString(long millis){
        long hourLong = millis / CalendarioLaboral.ONE_HOUR_MILLIS;
        long millisRestadoHoras = millis - hourLong * CalendarioLaboral.ONE_HOUR_MILLIS;
        long minutesLong = millisRestadoHoras / CalendarioLaboral.ONE_MINUTE_MILLIS; // AQUI HAY REDONDEO HACIA ABAJO, PERO SOLO SE MUESTRA A NIVEL DE DIA
        String result = "";
        String minutesTwoChars = String.format("%02d", Math.abs(minutesLong));
        result = Math.abs(hourLong)+ "h" + minutesTwoChars+"m";
        if (millis>=0) {
            result = "+"+result;
        }else{
            result = "-"+result;
        }
        return result;
    }

    private Label getLabelDay_num(int index){
        switch (index){
            case 0: return labelDay0_num;
            case 1: return labelDay1_num;
            case 2: return labelDay2_num;
            case 3: return labelDay3_num;
            case 4: return labelDay4_num;
            case 5: return labelDay5_num;
            case 6: return labelDay6_num;
            default: throw new RuntimeException("Invalid element with index="+index);
        }
    }

    private Label getLabelDay_text0(int index){
        switch (index){
            case 0: return labelDay0_text0;
            case 1: return labelDay1_text0;
            case 2: return labelDay2_text0;
            case 3: return labelDay3_text0;
            case 4: return labelDay4_text0;
            case 5: return labelDay5_text0;
            case 6: return labelDay6_text0;
            default: throw new RuntimeException("Invalid element with index="+index);
        }
    }

    private Label getLabelDay_text1(int index){
        switch (index){
            case 0: return labelDay0_text1;
            case 1: return labelDay1_text1;
            case 2: return labelDay2_text1;
            case 3: return labelDay3_text1;
            case 4: return labelDay4_text1;
            case 5: return labelDay5_text1;
            case 6: return labelDay6_text1;
            default: throw new RuntimeException("Invalid element with index="+index);
        }
    }

    @Override
    protected void updateItem(WeekListController.FichajesSemana item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || (item == null)) {
            setGraphic(null);
        } else {

            for (int i=0; i<7; i++){
                WeekListController.FichajesDia fichajesDia = item.fichajeDiaList.get(i);
                Label labelDay_num = getLabelDay_num(i);
                Label labelDay_text0 = getLabelDay_text0(i);
                Label labelDay_text1 = getLabelDay_text1(i);

                int currentDayNum = 0;

                labelDay_num.setText(String.valueOf(fichajesDia.dia));
                labelDay_text0.setText(millisToHourMinuteString(fichajesDia.millisATrabajar));
                long extraWorkTimeMillis = fichajesDia.computeExtraWorkTimeMillis();
                labelDay_text1.setText(millisIntervalToHourMinuteString(extraWorkTimeMillis));

                if (fichajesDia.isCurrentDay) {
                    //labelDay_num.setStyle("-fx-font-weight: bold;");
                    labelDay_num.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

                    //El intervalo de hoy lo ponemos nulo..ya que sino aparecen intervalos intermedios extraños
                    labelDay_text1.setText(millisIntervalToHourMinuteString(0));
                    fichajesDia.noData = true;//Para que salga en grisecito
                } else {
                    labelDay_num.setStyle("-fx-font-weight: normal; -fx-font-size: 14;");
                }

                labelDay_text0.setOpacity(0.6);
                labelDay_text1.setOpacity(0.6);

                labelDay_text0.setTextFill(COLOR_BLACK);
                labelDay_text1.setTextFill(COLOR_BLACK);

                if (fichajesDia.noData) {
                    //Este dia no tiene datos de fichajes
                    labelDay_text0.setOpacity(0.2);
                    labelDay_text1.setOpacity(0.2);

                }else{
                    //Este dia sí que tiene datos de fichajes
                    labelDay_text1.setOpacity(1);
                    if (extraWorkTimeMillis>=0) {
                        labelDay_text1.setTextFill(COLOR_GREEN); //Verde
                    }else{
                        labelDay_text1.setTextFill(COLOR_RED); //rojo
                    }
                }
            }

            labelWeek_num.setText("s"+String.valueOf(item.week));
            labelWeek_text0.setText(millisToHourMinuteString(item.millisATrabajar));
            labelWeek_text1.setText(millisIntervalToHourMinuteString(item.computeExtraWorkTimeMillis()));

            labelWeek_num.setOpacity(0.8);
            labelWeek_text0.setOpacity(0.5);
            labelWeek_text1.setOpacity(0.5);

            setGraphic(cellView);
        }
    }


}
