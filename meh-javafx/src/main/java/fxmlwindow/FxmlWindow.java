package fxmlwindow;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import myeighthours.helper.WindowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class FxmlWindow<T extends FxmlController> {

    private static final Logger LOG = LoggerFactory.getLogger(FxmlWindow.class);

    private Stage stage;

    private FXMLLoader loader;

    private T rootController;

    private URL fxmlResourceURL;

    public FxmlWindow(URL fxmlResourceURL) {
        this.fxmlResourceURL = fxmlResourceURL;
        try {
            stage = new Stage();
            loader = new FXMLLoader(fxmlResourceURL);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            rootController = loader.<T>getController();
            rootController.setStage(stage);
            setFxmlControllerExtraProperties(loader, scene);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    rootController.onWindowOpen(stage);
                }
            });

        } catch (IOException e) {
            LOG.error("No se ha podido mostrar la ventana", e);
        }
    }

    private OnWindowCloseListener onWindowCloseListener;

    public interface OnWindowCloseListener {
        void onWindowClose();
    }

    public void setOnWindowCloseListener(OnWindowCloseListener onWindowCloseListener) {
        this.onWindowCloseListener = onWindowCloseListener;
    }

    public T getController() {
        return rootController;
    }

    public Stage getStage() {
        return stage;
    }

    public void show() {
        stage.show();
    }

    public void showCenteredOn(Stage ref) {
        stage.show();
        WindowUtils.centerWindowWithWindow(stage, ref);
        WindowUtils.moveToDisplayWindowInScreen(stage);
    }

    public void close() {
        stage.close();
    }

    public void hide() {
        stage.hide();
    }

    public void setIcon(Image image) {
        stage.getIcons().clear();
        stage.getIcons().add(image);
    }

    public void setIcon(List<Image> imageList) {
        stage.getIcons().clear();
        stage.getIcons().addAll(imageList);
    }

    public void setIcon(String urlString) {
        setIcon(new Image(urlString));
    }

    public void setIcon(String[] urlString) {
        List<Image> imageList = new ArrayList<>();
        for (int i=0; i<urlString.length;i++){
            String url = urlString[i];
            imageList.add(new Image(url));
        }
        setIcon(imageList);
    }

    public void setTitle(String title) {
        stage.setTitle(title);
    }

    private void setFxmlControllerExtraProperties(FXMLLoader loader, Scene scene) {
        rootController.setStage(stage);
        rootController.scene = scene;
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (rootController != null) {
                    rootController.onWindowClose();
                }
                if (onWindowCloseListener != null) {
                    onWindowCloseListener.onWindowClose();
                }
            }
        });
    }

}