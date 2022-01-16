package myeighthours.helper;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

import static myeighthours.Main.ICON_MAIN;

public class WindowUtils {

    public static void centerWindowWithWindow(Stage centeredStage, Stage referenceStage) {
        double refCenterX = referenceStage.getX() + referenceStage.getWidth()/2;
        double refCenterY = referenceStage.getY() + referenceStage.getHeight()/2;
        double centeredStageWidth = centeredStage.getWidth();
        double centeredStageHeight = centeredStage.getHeight();
        double stageX = refCenterX - centeredStageWidth/2;
        double stageY = refCenterY - centeredStageHeight/2;
        centeredStage.setX(stageX);
        centeredStage.setY(stageY);
    }

    public static void moveToDisplayWindowInScreen(Stage stage){
        double stageXmin = stage.getX();
        double stageXmax = stage.getX() + stage.getWidth();
        double stageYmin = stage.getY();
        double stageYmax = stage.getY() + stage.getHeight();
        double stageXcenter = (stageXmin+stageXmax)/2;
        double stageYcenter = (stageYmin+stageYmax)/2;

        for (Screen screen : Screen.getScreens()) {
            Rectangle2D bounds = screen.getVisualBounds();
            boolean stageCenterIsInScreen = bounds.contains(stageXcenter,stageYcenter);
            if (stageCenterIsInScreen){
                boolean sobresaleParteSuperior = stageYmin < bounds.getMinY();
                if (sobresaleParteSuperior) stage.setY(bounds.getMinY());
                boolean sobresaleParteInferior = stageYmax > bounds.getMaxY();
                if (sobresaleParteInferior) stage.setY(bounds.getMaxY()-stage.getHeight());
                boolean sobresaleParteDerecha = stageXmax > bounds.getMaxX();
                if (sobresaleParteDerecha) stage.setX(bounds.getMaxX()-stage.getWidth());
                boolean sobresaleParteIzquierda = stageXmin < bounds.getMinX();
                if (sobresaleParteIzquierda) stage.setX(bounds.getMinX());

            }
            //.....TODO
        }
    }

    public static void showAlertInfoWindow(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        String windowTitle = "Info";
        alert.setTitle(windowTitle);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        setIcon(stage, ICON_MAIN);
        alert.setHeaderText(mensaje);
        alert.showAndWait();
    }

    public static void showAlertErrorWindow(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        String windowTitle = "Error";
        alert.setTitle(windowTitle);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        setIcon(stage, ICON_MAIN);
        alert.setHeaderText(mensaje);
        alert.showAndWait();
    }

    private static void setIcon(Stage stage, Image image) {
        stage.getIcons().clear();
        stage.getIcons().add(image);
    }

    private static void setIcon(Stage stage, List<Image> imageList) {
        stage.getIcons().clear();
        stage.getIcons().addAll(imageList);
    }

    private static void setIcon(Stage stage, String urlString) {
        setIcon(stage, new Image(urlString));
    }

    private static void setIcon(Stage stage, String[] urlString) {
        List<Image> imageList = new ArrayList<>();
        for (int i=0; i<urlString.length;i++){
            String url = urlString[i];
            imageList.add(new Image(url));
        }
        setIcon(stage, imageList);
    }

}
