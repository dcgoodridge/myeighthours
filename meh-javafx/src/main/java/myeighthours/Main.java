package myeighthours;

import fxmlwindow.FxmlWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import myeighthours.viewcontroller.LoginController;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static final String[] ICON_MAIN = {"icon_16.png","icon_32.png","icon_64.png", "icon_128.png"};

    public static MehOptions options;

    public static String getAppShortName() {
        return "MEH";
    }

    /**
     * Ej: MEH 0.8.1
     */
    public static String getAppShortNameWithVersion() {
        return Metadata.appNameAndVersion();
    }

    public void showLoginWindow(Stage stage) throws Exception {
        FxmlWindow<LoginController> loginWindow = new FxmlWindow(getClass().getResource("/Login.fxml"));
        loginWindow.setTitle(getAppShortName());
        loginWindow.setIcon(ICON_MAIN);
        loginWindow.getStage().setResizable(false);
        loginWindow.show();
    }

    @Override
    public void start(Stage stage) throws Exception {
        showLoginWindow(stage);
    }

    private static void uncaughtExceptionHandler(Thread thread, Throwable throwable) {
        LOG.error("Uncaught Exception in thread " + thread.getName(), throwable);
    }

    public static void main(String[] args) {
        initApp();
        launch(args);
    }

    private static void initApp() {
        Thread.setDefaultUncaughtExceptionHandler(Main::uncaughtExceptionHandler);
        MehProperties mehProperties = ConfigFactory.create( MehProperties.class );
        String javaVersion = System.getProperty("java.version");
        String javaOsArch = System.getProperty("os.arch");
        configureLogLevel(mehProperties.getLogLevel());
        LOG.info("Iniciando "+Metadata.appNameAndVersion());
        LOG.info("JRE " + javaVersion + " " + javaOsArch);
    }

    private static void configureLogLevel(String level){
        Level loglevel = Level.toLevel(level,Level.INFO);
        Configurator.setRootLevel(loglevel);
    }
}
