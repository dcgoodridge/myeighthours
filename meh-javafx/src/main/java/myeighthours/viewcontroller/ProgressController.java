package myeighthours.viewcontroller;

import fxmlwindow.FxmlController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import myeighthours.*;
import org.aeonbits.owner.ConfigFactory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.ocpsoft.prettytime.PrettyTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;


public class ProgressController extends FxmlController {

    private static final Logger LOG = LoggerFactory.getLogger(ProgressController.class);

    private static final DateTimeFormatter TRAY_TOOLTIP_DATEFORMAT = DateTimeFormat.forPattern("HH:mm");

    private static final DateTimeFormatter PROGRESS_TIME_DATEFORMAT = DateTimeFormat.forPattern("HH:mm");

    @FXML
    private TextField timeStart;

    @FXML
    private TextField timeFinish;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ProgressFooterController progressFooterLayoutController;

    private MehOptions options;

    private MehService service;

    private PrettyTime prettyTime;

    private MehProperties mehProperties;

    private boolean trayFirstTime = true;

    private TrayIcon trayIcon;

    private boolean trayIconActive;

    private Stage stage;

    private double windowSavedPositionX = 100;

    private double windowSavedPositionY = 100;

    @Override
    public void onWindowInitialize(URL location, ResourceBundle resources) {
        this.options = Main.options;
        prettyTime = new PrettyTime(Locale.US);
        mehProperties = ConfigFactory.create(MehProperties.class);
        startMainService();
        progressFooterLayoutController.onWindowInitialize(location, resources);
        progressFooterLayoutController.setListener(new ProgressFooterController.Listener() {
            @Override
            public void onEventClickRefresh() {
                service.eventUserTriggeredWebscraper();
            }
        });
    }

    @Override
    public void onWindowOpen(Stage stage) {
        if (mehProperties.minimizetotrayIsEnabled()) initTrayIconIfSupported(stage);
        this.stage = stage;
        windowSavedPositionX = stage.getX();
        windowSavedPositionY = stage.getY();
        stage.xProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double x = newValue.doubleValue();
                if (x > 0) windowSavedPositionX = x;
            }
        });
        stage.yProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double y = newValue.doubleValue();
                if (y > 0) windowSavedPositionY = y;
            }
        });
        progressFooterLayoutController.onWindowOpen(stage);
    }

    private void windowPositionSave() {
        //windowSavedPositionX = stage.getX();
        //windowSavedPositionY = stage.getY();
    }

    private void windowPositionRestore() {
        stage.setX(windowSavedPositionX);
        stage.setY(windowSavedPositionY);
    }

    private void initTrayIconIfSupported(Stage stage) {
        if (SystemTray.isSupported()) trayIconActive = true;
        if (trayIconActive) addTrayIcon(trayIconActionListener);
        stage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if (t1.booleanValue()) {
                    hideStage(stage);
                }
            }
        });
    }

    private void addTrayIcon(ActionListener trayIconActionListener) {
        trayIcon = createTrayIcon(trayIconActionListener);
        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            LOG.error("Error adding system tray icon", e);
        }
    }

    private void startMainService() {
        service = new MehService(options);
        service.setOnSucceeded(t -> onServiceSucceeded());
        service.setOnCancelled(t -> onServiceCancelled());
        service.setOnFailed(t -> onServiceFailed());
        service.setOnReady(t -> onServiceReady());
        service.setOnRunning(t -> onServiceRunning());
        service.setOnScheduled(t -> onServiceScheduled());
        service.start();
    }

    private void onServiceScheduled() {
        //LOG.debug("onServiceScheduled");
    }

    private void onServiceRunning() {
        //LOG.debug("onServiceRunning");
    }

    private void onServiceReady() {
        //LOG.debug("onServiceReady");
    }

    private void onServiceFailed() {
        LOG.error("MehService failed");
    }

    private void onServiceCancelled() {
        //Este metodo se llama continuamente, al hacer "reset()" del servicio.
    }

    private void onServiceSucceeded() {
        //LOG.debug("onServiceSucceeded");
        MehServiceState serviceState = service.getValue();
        onMehStateUpdated(serviceState);
    }

    private void onMehStateUpdated(MehServiceState serviceState) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ProgressController.this.setData(serviceState);
            }
        });
    }

    @Override
    public void onWindowClose() {
        progressFooterLayoutController.onWindowClose();
        finish();
    }

    public void finish() {
        final int serviceShutdownWaitMillis = 15000;
        final boolean webscraperIsRunning = service.isWebscraperTaskStarted();
        service.cancel();
        boolean takeCare = webscraperIsRunning;
        if (takeCare) {
            Thread thread = new Thread() {
                public void run() {
                    LOG.info("Finishing but MehService is still busy. Waiting " + serviceShutdownWaitMillis + "ms.");
                    service.waitShutdown(serviceShutdownWaitMillis);
                    LOG.info("Finish");
                    System.exit(0);
                }
            };
            thread.setName("ExitThread");
            thread.start();
        } else {
            LOG.warn("MehService is still busy. Forcing exit.");
            System.exit(0);
        }
    }

    public void setData(MehServiceState serviceState) {
        String timeStartString;
        String timeFinishString;
        WorkState workState = serviceState.getWorkState();
        if (trayIconActive && trayIcon != null) {
            updateTrayIconTooltip(trayIcon, serviceState);
        }
        WorkState.STATE mehState = workState.getState();
        switch (mehState) {
            case UNKNOWN: {
                timeStartString = "-";
                timeFinishString = "-";
                break;
            }
            default: {
                timeStartString = PROGRESS_TIME_DATEFORMAT.print(workState.getTimeStart());
                timeFinishString = PROGRESS_TIME_DATEFORMAT.print(workState.getTimeFinish());
                break;
            }
        }
        timeStart.setText(timeStartString);
        timeFinish.setText(timeFinishString);
        progressBar.setProgress(workState.getProgress());
        progressFooterLayoutController.setData(serviceState);
    }

    private void updateTrayIconTooltip(TrayIcon trayIcon, MehServiceState serviceState) {
        WorkState workState = serviceState.getWorkState();
        String line1 = Main.getAppShortNameWithVersion();
        String line2;
        if (workState.getState().equals(WorkState.STATE.UNKNOWN)) {
            line2 = "-";
        } else {
            line2 = TRAY_TOOLTIP_DATEFORMAT.print(workState.getTimeFinish());
        }
        if (serviceState.getScraperState().getState().equals(Worker.State.FAILED)) {
            line2 = "ERROR";
        }
        String finalString;
        if (line2.isEmpty()) {
            finalString = line1;
        } else {
            finalString = line1 + "\n" + line2;
        }
        trayIcon.setToolTip(finalString);
    }

    private void windowShow() {
        stage.setIconified(false);
        windowPositionRestore();
        stage.show();
    }

    private void windowHide() {
        stage.hide();
    }

    private TrayIcon createTrayIcon(ActionListener actionListener) {
        java.awt.Image image = null;
        try {
            URL url = Main.class.getResource("/icon.png");
            image = ImageIO.read(url);
        } catch (IOException ex) {
            LOG.error("Error reading icon resource", ex);
        }
        final ActionListener closeListener = new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                eventTrayPopupClose();
            }
        };
        PopupMenu popup = new PopupMenu();
        java.awt.MenuItem showItem = new java.awt.MenuItem("Mostrar");
        showItem.addActionListener(actionListener);
        popup.add(showItem);

        java.awt.MenuItem closeItem = new java.awt.MenuItem("Cerrar");
        closeItem.addActionListener(closeListener);
        popup.add(closeItem);
        String trayIconTooltip = Main.getAppShortNameWithVersion();
        TrayIcon trayIcon = new TrayIcon(image, trayIconTooltip, popup);
        trayIcon.addActionListener(actionListener);
        trayIcon.setImageAutoSize(true);
        return trayIcon;
    }

    private void eventTrayPopupClose() {
        System.exit(0);
    }

    ActionListener trayIconActionListener = new ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    eventTrayIconDoubleClick();
                }
            });
        }
    };

    private void eventTrayIconDoubleClick() {
        windowShow();
    }

    private void hideStage(final Stage stage) {
        windowPositionSave();
        progressFooterLayoutController.menuClose();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (SystemTray.isSupported()) {
                    stage.hide();
                } else {
                    //Don't hideStage
                }
            }
        });
    }

    private void showTestNotification() {
        javax.swing.SwingUtilities.invokeLater(() ->
                trayIcon.displayMessage(
                        "MEH",
                        "15 minutos restantes",
                        TrayIcon.MessageType.INFO
                )
        );
    }

}
