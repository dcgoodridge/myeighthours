package myeighthours.viewcontroller;


import fxmlwindow.FxmlController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import myeighthours.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;


public class AboutController extends FxmlController {

    private static final Logger LOG = LoggerFactory.getLogger(AboutController.class);

    private Stage stage;

    @FXML
    private Label currentBuildLabel;

    @FXML
    private Label appNameAndVersion;

    @FXML
    private Button buttonChangelog;

    @FXML
    private Button buttonLicenses;

    @FXML
    private Button buttonHelp;

    @FXML
    private WebView webview;
    private WebEngine engine;

    @Override
    public void onWindowInitialize(URL location, ResourceBundle resources) {
        currentBuildLabel.setText("Build: "+ Metadata.buildDateString());
        appNameAndVersion.setText(Metadata.appLongNameAndVersion());
        buttonChangelog.setOnMouseClicked(event -> loadChangelog());
        buttonHelp.setOnMouseClicked(event -> loadHelp());
        buttonLicenses.setOnMouseClicked(event -> loadLicenses());
    }

    @Override
    public void onWindowOpen(Stage stage) {
        this.stage = stage;
        initWebview();
        loadHelp();
        webview.getEngine().getLoadWorker().stateProperty().addListener(new HyperlinkRedirectListener(webview));
        webview.setContextMenuEnabled(false);
    }

    private void initWebview() {
        engine = webview.getEngine();
        engine.setUserStyleSheetLocation("data:,body { font: 12px Arial; }");
    }

    private void loadChangelog(){
        URL url = this.getClass().getResource("/webview/changelog.html");
        engine.load(url.toString());
    }

    private void loadHelp(){
        URL url = this.getClass().getResource("/webview/help.html");
        engine.load(url.toString());
    }

    private void loadLicenses(){
        URL url = this.getClass().getResource("/webview/licenses.html");
        engine.load(url.toString());
    }

    @Override
    public void onWindowClose() {

    }

}
