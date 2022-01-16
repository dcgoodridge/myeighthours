package myeighthours.viewcontroller;


import customcontrols.CustomPasswordField;
import fxmlwindow.FxmlController;
import fxmlwindow.FxmlWindow;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import jnahelpers.JnaKeyboard;
import myeighthours.Main;
import myeighthours.MehOptions;
import myeighthours.MehProperties;
import myeighthours.SimulationProperties;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.List;

public class LoginController extends FxmlController
{

  private static final Logger LOG = LoggerFactory.getLogger( LoginController.class );

  private static final javafx.scene.image.Image image = new Image("/caps_lock_icon.png");

  @FXML
  private CustomPasswordField passwordField;

  @FXML
  private TextField usernameField;

  @FXML
  private ChoiceBox<String> choiceBox;

  @FXML
  private Button startButton;

  private MehProperties mehProperties;

  private SimulationProperties simulationProperties;

  private String versionString;

  private Stage stage;

  private MehOptions readOptions()
  {
    String user = usernameField.getText();
    String pass = passwordField.getText();

    if (simulationProperties.isActive()) {
      user = "SIMULATED:"+user;
    }

    MehOptions.Builder builder = new MehOptions.Builder();
    builder.username( user );
    builder.password( pass );
    builder.webdriver( readWebdriver() );
    return builder.build();
  }

  private MehOptions.WEBDRIVER readWebdriver() {
    String webdriverString = "";
    if (mehProperties.debugIsEnabled()) {
      webdriverString = choiceBox.getValue();
    }else{
      webdriverString = mehProperties.webscrapDriver();
    }

    return MehOptions.WEBDRIVER.valueOf(webdriverString);
  }

  public void showProgressWindow() throws Exception
  {
    FxmlWindow<ProgressController> progressWindow = new FxmlWindow( getClass().getResource( "/Progress.fxml" ) );
    progressWindow.setTitle( Main.getAppShortName() );
    progressWindow.setIcon( Main.ICON_MAIN );
    progressWindow.getStage().setResizable( false );
    progressWindow.showCenteredOn(getStage());
  }

  @Override
  public void initialize( URL location, ResourceBundle resources )
  {
    mehProperties = ConfigFactory.create( MehProperties.class );
    simulationProperties = ConfigFactory.create( SimulationProperties.class );
    usernameField.setText( mehProperties.user() );

    startButton.setOnMouseClicked( new EventHandler<MouseEvent>()
    {
      @Override
      public void handle( MouseEvent event )
      {
        MehOptions options = readOptions();
        start( options );
      }
    } );

    usernameField.setOnKeyPressed( new EventHandler<KeyEvent>()
    {
      @Override
      public void handle( KeyEvent keyEvent )
      {
        if( keyEvent.getCode() == KeyCode.ENTER )
        {
          MehOptions options = readOptions();
          start( options );
        }
      }
    } );

    ImageView capsLockFieldIcon = new ImageView(image);
    Tooltip t = new Tooltip("BLOQ MAYUS activado");
    Tooltip.install(capsLockFieldIcon, t);
    passwordField.setRight(capsLockFieldIcon);
    passwordField.getRight().setVisible(false);

    initChoiceBox();

  }

  private void initChoiceBox() {
    if (mehProperties.debugIsEnabled()) {
      choiceBox.setManaged(true);
      choiceBox.setVisible(true);
      choiceBox.setItems( FXCollections.observableArrayList( getWebcrawlerOptions() ) );
      choiceBox.setValue( choiceBox.getItems().get( 0 ) );
    }else{
      choiceBox.setManaged(false);
      choiceBox.setVisible(false);
    }

  }

  private List<String> getWebcrawlerOptions()
  {
    List<String> choices = new ArrayList<>();
    MehOptions.WEBDRIVER[] webdriverValues = MehOptions.WEBDRIVER.values();
    for( int i = 0; i < webdriverValues.length; i++ )
    {
      MehOptions.WEBDRIVER webdriverValue = webdriverValues[ i ];
      choices.add( webdriverValue.name() );
    }
    return choices;
  }

  private void capsLockPopupHide() {
    passwordField.getRight().setVisible(false);
  }


  private void capsLockPopupShow(){
    passwordField.getRight().setVisible(true);
  }

  private void eventKeyPressedPasswordField(KeyEvent keyEvent) {

    boolean keyIsCapsLock = keyEvent.getCode() == KeyCode.CAPS;
    if (keyIsCapsLock) {
      if (isCapsLockActive()) {
        capsLockPopupShow();
      }else{
        capsLockPopupHide();
      }
    }
    if( keyEvent.getCode() == KeyCode.ENTER )
    {
      MehOptions options = readOptions();
      start( options );
    }
  }

  private void start( MehOptions options )
  {
    Platform.setImplicitExit( false );
    windowHide();
    Main.options = options;
    try
    {
      showProgressWindow();
    }
    catch( Exception e )
    {
      LOG.error( "Error mostrando la ventana de progreso", e );
    }

  }

  @Override
  public void onWindowInitialize(URL location, ResourceBundle resources )
  {
  }

  @Override
  public void onWindowOpen(Stage stage) {
    this.stage = stage;

    passwordField.focusedProperty().addListener(new ChangeListener<Boolean>()
    {
      @Override
      public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean focused)
      {
        if (focused)
        {
          if (isCapsLockActive()) {
            capsLockPopupShow();
          }
        }
        else
        {
          capsLockPopupHide();
        }
      }
    });

    passwordField.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent keyEvent) {
        eventKeyPressedPasswordField(keyEvent);
      }
    });

    //Ejecutar en GUI en que acabe de iniciarse
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        passwordField.requestFocus();
      }
    });
  }

  @Override
  public void onWindowClose() {}

  private boolean isCapsLockActive(){
    boolean capsLockActive = JnaKeyboard.isCapsLockActiveWin32();
    return capsLockActive;
  }

  private void windowShow()
  {
    stage.show();
  }

  private void windowHide()
  {
    stage.hide();
  }

}
