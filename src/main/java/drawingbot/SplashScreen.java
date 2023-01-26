package drawingbot;

import drawingbot.utils.DBConstants;
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class SplashScreen extends Preloader {

    public static PreloaderNotificationHandler notificationHandler;

    public static void initPreloader(){
        System.setProperty("javafx.preloader", SplashScreen.class.getName());
    }

    public static void startPreloader(Application app){
        notificationHandler = new SplashScreen.PreloaderNotificationHandler(app);
        DrawingBotV3.logger.addHandler(notificationHandler);
    }

    public static void stopPreloader(Application app){
        app.notifyPreloader(new SplashScreen.LoadCompleteNotification());
        DrawingBotV3.logger.removeHandler(notificationHandler);
    }

    ///////////////

    public Stage stage;
    public Label label;

    private Scene createPreloaderScene() {
        VBox vBox = new VBox();
        vBox.setFillWidth(true);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPrefSize(500, 250);

        Image splashImage = FXApplication.getDBV3SplashImage();
        if(splashImage != null){
            vBox.setBackground(new Background(new BackgroundImage(splashImage, null, null, null, null)));
        }else{
            vBox.setBackground(new Background(new BackgroundFill(new Color(0, 0, 0, 0), null, null)));
        }


        ImageView imageView = new ImageView(FXApplication.getDBV3LogoImage());
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(200);
        imageView.setSmooth(true);
        vBox.getChildren().add(imageView);


        label = new Label(DBConstants.versionName + ", " + "v" + DBConstants.appVersion);
        label.setFont(new Font(11));
        label.setTextFill(Color.LIGHTGREY);
        label.setBackground(new Background(new BackgroundFill(new Color(0, 0, 0, 0), null, null)));
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setTranslateY(-27);
        vBox.getChildren().add(label);


        return new Scene(vBox, 500, 250);
    }
    
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(createPreloaderScene());
        FXApplication.applyDBStyle(stage);
        stage.show();
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }

    public void handleApplicationNotification(PreloaderNotification info) {
        if(info instanceof LoadCompleteNotification){
            stage.hide();
        }
        if(info instanceof InfoNotification){
            InfoNotification infoNotification = (InfoNotification) info;
            label.setText(infoNotification.info);
        }
    }

    public static class LoadCompleteNotification implements PreloaderNotification{
        //NOP
    }

    public static class InfoNotification implements PreloaderNotification{

        public String info;

        public InfoNotification(String info){
            this.info = info;
        }
    }

    public static class PreloaderNotificationHandler extends Handler {

        public Application app;

        public PreloaderNotificationHandler(Application app){
            this.app = app;
        }

        @Override
        public void publish(LogRecord record) {
            this.app.notifyPreloader(new SplashScreen.InfoNotification(record.getMessage()));
        }

        @Override
        public void flush() {}

        @Override
        public void close() throws SecurityException {}
    }
}