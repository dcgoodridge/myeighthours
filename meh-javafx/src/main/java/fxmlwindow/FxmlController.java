package fxmlwindow;

import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;


public abstract class FxmlController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        onWindowInitialize(location,resources);
    }

    protected Scene scene;

    private Stage stage;

    public Scene getScene(){
        return scene;
    }

    public Stage getStage(){
        return stage;
    }

    public void setStage(Stage stage){
        this.stage = stage;
    }

    public abstract void onWindowInitialize(URL location, ResourceBundle resources);

    public abstract void onWindowOpen(Stage stage);

    public void hide(){
        stage.hide();
    }

    public void close(){
        stage.close();
    }

    public abstract void onWindowClose();




}
