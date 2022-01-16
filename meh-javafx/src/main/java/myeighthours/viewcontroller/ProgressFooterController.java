package myeighthours.viewcontroller;

import fxmlwindow.FxmlController;
import fxmlwindow.FxmlWindow;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import myeighthours.Main;
import myeighthours.MehServiceState;
import myeighthours.scraper.ScraperState;
import myeighthours.WorkState;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.ocpsoft.prettytime.PrettyTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;


public class ProgressFooterController extends FxmlController {

    private static final Logger LOG = LoggerFactory.getLogger(ProgressFooterController.class);

    private Stage stage;

    private PrettyTime prettyTime;

    private boolean menuIsOpen = false;

    private Listener listener;

    private FxmlWindow weeklistWindow;

    interface Listener {
        void onEventClickRefresh();
    }

    private WorkState currentState = null;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @FXML
    private Button moreButton;

    @FXML
    private HBox progressFooterLeft;

    @FXML
    private HBox progressFooterRight;

    @FXML
    private Label espaciadorRight;

    @FXML
    private Button calendarButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button aboutButton;

    @FXML
    private Label espaciadorLeft;

    @FXML
    private Label infoThread;
    private double menuTranslateX;

    @Override
    public void onWindowInitialize(URL location, ResourceBundle resources) {
        prettyTime = new PrettyTime();
        infoThread.setTooltip(new Tooltip());
        progressFooterRight.setTranslateY(0);
        progressFooterRight.setTranslateX(1000);
        espaciadorRight.setMouseTransparent(true);
        espaciadorLeft.setMouseTransparent(true);
        moreButton.setTooltip(new Tooltip("Mostrar opciones"));
        aboutButton.setTooltip(new Tooltip("Acerca de..."));
        refreshButton.setTooltip(new Tooltip("Recargar"));
        calendarButton.setTooltip(new Tooltip("Calendario"));
    }

    private void startFichajeListWindow() {
        FxmlWindow<FichajeListController> window = new FxmlWindow<>(getClass().getResource("/FichajeList.fxml"));
        window.setIcon(Main.ICON_MAIN);
        window.setTitle("Fichajes");
        window.getStage().setResizable(false);
        window.getStage().initModality(Modality.APPLICATION_MODAL);
        window.show();
    }

    private void startWeekListWindow() {
        if (weeklistWindow != null) {
            restoreWeekListWindow(weeklistWindow);
            return;
        }
        weeklistWindow = new FxmlWindow(getClass().getResource("/WeekList.fxml"));
        weeklistWindow.setTitle("Calendario");
        weeklistWindow.setIcon(Main.ICON_MAIN);
        weeklistWindow.getStage().setResizable(false);
        weeklistWindow.getStage().initModality(Modality.APPLICATION_MODAL);
        weeklistWindow.setOnWindowCloseListener(new FxmlWindow.OnWindowCloseListener() {
            @Override
            public void onWindowClose() {
                weeklistWindow = null;
            }
        });
        weeklistWindow.showCenteredOn(stage);
    }


    private void restoreWeekListWindow(FxmlWindow weeklistWindow) {
        weeklistWindow.getStage().setIconified(false);
        weeklistWindow.getStage().toFront();
    }

    private void showAboutWindow() {
        FxmlWindow aboutWindow = new FxmlWindow(getClass().getResource("/About.fxml"));
        aboutWindow.setTitle("Acerca de...");
        aboutWindow.setIcon(Main.ICON_MAIN);
        aboutWindow.getStage().setResizable(false);
        aboutWindow.getStage().initModality(Modality.APPLICATION_MODAL);
        aboutWindow.showCenteredOn(stage);
    }

    private void onEventClickAbout() {
        showAboutWindow();
        menuAnimationToggle();
    }

    public void menuClose() {
        if (menuIsOpen) menuAnimationToggle();
    }

    private void menuAnimationToggle() {
        double stageWidth = stage.getWidth();
        double nodeWidth = moreButton.getLayoutBounds().getWidth();
        menuTranslateX = -(stageWidth - nodeWidth - 10 - 10 - 5 - 1);

        progressFooterRight.setTranslateX(-menuTranslateX + nodeWidth);
        progressFooterRight.setMaxWidth(Math.abs(menuTranslateX));
        progressFooterRight.setPrefWidth(Math.abs(menuTranslateX));


        Node node = moreButton;
        TranslateTransition tt = new TranslateTransition(Duration.millis(150), node);
        if (menuIsOpen) {
            tt.setByX(-menuTranslateX); //Desplazar derecha (Cerrar opciones extra)
            moreButton.setTooltip(new Tooltip("Mostrar opciones"));
        } else {
            tt.setByX(menuTranslateX); //Desplazar izquierda (Abrir opciones extra)
            moreButton.setTooltip(new Tooltip("Ocultar opciones"));
        }
        menuIsOpen = !menuIsOpen;
        tt.setNode(progressFooterLeft.getParent());
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    @Override
    public void onWindowOpen(Stage stage) {
        this.stage = stage;
        moreButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                menuAnimationToggle();
            }
        });
        calendarButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onEventClickCalendar();
            }
        });
        aboutButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onEventClickAbout();
            }
        });
        refreshButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onEventClickRefresh();
            }
        });
    }

    private void onEventClickCalendar() {
        startWeekListWindow();
        menuAnimationToggle();
    }

    private void onEventClickRefresh() {
        listener.onEventClickRefresh();
        menuAnimationToggle();
    }

    @Override
    public void onWindowClose() {}

    public void setData(MehServiceState serviceState) {
        ScraperState scraperState = serviceState.getScraperState();
        switch (scraperState.getState()) {
            case READY:
                infoThread.setText("OK");
                infoThread.getTooltip().setText("OK");
                break;
            case SCHEDULED:
            case RUNNING:
                infoThread.setText("Webscrap en progreso...");
                infoThread.getTooltip().setText("Webscrap en progreso...");
                break;
            case SUCCEEDED:
                infoThread.setText("OK");
                DateTime dateTime_SUCCEEDED = new DateTime(scraperState.getTimeFinish(), DateTimeZone.UTC);
                infoThread.getTooltip().setText("Webscrap terminó " + prettyTime.format(dateTime_SUCCEEDED.toDate()));
                break;
            case CANCELLED:
            case FAILED:
                infoThread.setText("ERROR");
                DateTime dateTime_CANCELLED = new DateTime(scraperState.getTimeFinish(), DateTimeZone.UTC);
                infoThread.getTooltip().setText("Webscrap terminó " + prettyTime.format(dateTime_CANCELLED.toDate()));
                break;
        }
    }

}
