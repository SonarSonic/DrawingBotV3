package drawingbot;

import drawingbot.api.API;
import drawingbot.api_impl.DrawingBotV3API;
import drawingbot.files.ConfigFileHandler;
import drawingbot.javafx.FXController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.canvas.Canvas;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jfree.fx.FXGraphics2D;

import java.io.*;
import java.util.logging.Level;

public class FXLauncher extends Application {

    public static Stage primaryStage;
    public static Scene primaryScene;

    public Animation animation;
    public float frameRate = 60;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        DrawingBotV3.logger.setLevel(Level.ALL);
        DrawingBotV3.logger.entering("FXApplication", "start");
        FXLauncher.primaryStage = primaryStage;

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        //// PRE-INIT

        DrawingBotV3.logger.info("Loading configuration");
        ConfigFileHandler.init();

        DrawingBotV3.logger.info("Loading API");
        API.INSTANCE = new DrawingBotV3API();

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        //// INIT GUI
        Canvas canvas = new Canvas(500, 500);

        DrawingBotV3.controller = new FXController();
        DrawingBotV3.canvas = canvas;
        DrawingBotV3.graphics = new FXGraphics2D(canvas.getGraphicsContext2D());

        FXMLLoader loader = new FXMLLoader(FXLauncher.class.getResource("/fxml/userinterface.fxml")); // abs path to fxml file
        loader.setController(DrawingBotV3.controller);

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        FXLauncher.primaryScene = new Scene(loader.load(), visualBounds.getWidth()/1.2, visualBounds.getHeight()/1.2, false, SceneAntialiasing.BALANCED);
        FXLauncher.primaryScene.setOnKeyPressed(DrawingBotV3::keyPressed);
        FXLauncher.primaryScene.setOnKeyReleased(DrawingBotV3::keyReleased);
        primaryStage.setScene(primaryScene);

        primaryStage.setTitle(DrawingBotV3.appName + ", Version: " + DrawingBotV3.appVersion);
        primaryStage.setResizable(true);
        primaryStage.show();

        // set up main drawing loop
        KeyFrame keyFrame = new KeyFrame(Duration.millis(1000), event -> DrawingBotV3.draw());
        animation = new Timeline(keyFrame);
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setRate(-frameRate);// setting rate to negative so that event fires at the start of the key frame and first frame is drawn immediately
        animation.play();

        DrawingBotV3.logger.exiting("FXApplication", "start");
    }
}
