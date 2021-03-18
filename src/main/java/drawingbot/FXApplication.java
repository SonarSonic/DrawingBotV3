package drawingbot;

import drawingbot.api.API;
import drawingbot.api_impl.DrawingBotV3API;
import drawingbot.drawing.ObservableDrawingSet;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.javafx.FXController;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.DBConstants;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.jfree.fx.FXGraphics2D;

import java.io.*;
import java.util.logging.Level;

public class FXApplication extends Application {

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
        FXApplication.primaryStage = primaryStage;

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        //// PRE-INIT
        DrawingBotV3.logger.info("Loading Configuration");
        ConfigFileHandler.init();

        DrawingBotV3.logger.info("Loading API");
        API.INSTANCE = new DrawingBotV3API();

        DrawingBotV3.logger.info("Loading Registry");
        MasterRegistry.init();

        DrawingBotV3.logger.info("Init DrawingBotV3");
        DrawingBotV3.INSTANCE = new DrawingBotV3();

        DrawingBotV3.logger.info("Init Observable Drawing Set");
        DrawingBotV3.INSTANCE.observableDrawingSet = new ObservableDrawingSet(MasterRegistry.INSTANCE.getDefaultSet(MasterRegistry.INSTANCE.getDefaultSetType()));

        DrawingBotV3.logger.info("Loading Json Files");
        JsonLoaderManager.loadJSONFiles();

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        //// INIT GUI
        Canvas canvas = new Canvas(500, 500);

        DrawingBotV3.INSTANCE.controller = new FXController();
        DrawingBotV3.INSTANCE.canvas = canvas;
        DrawingBotV3.INSTANCE.graphicsFX = canvas.getGraphicsContext2D();
        DrawingBotV3.INSTANCE.graphicsAWT = new FXGraphics2D(canvas.getGraphicsContext2D());

        FXMLLoader uiLoader = new FXMLLoader(FXApplication.class.getResource("/fxml/userinterface.fxml")); // abs path to fxml file
        uiLoader.setController(DrawingBotV3.INSTANCE.controller);

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        FXApplication.primaryScene = new Scene(uiLoader.load(), visualBounds.getWidth()/1.2, visualBounds.getHeight()/1.2, false, SceneAntialiasing.BALANCED);
        FXApplication.primaryScene.setOnKeyPressed(DrawingBotV3.INSTANCE::keyPressed);
        FXApplication.primaryScene.setOnKeyReleased(DrawingBotV3.INSTANCE::keyReleased);
        primaryStage.setScene(primaryScene);

        primaryStage.setTitle(DBConstants.appName + ", Version: " + DBConstants.appVersion);
        primaryStage.setResizable(true);
        applyDBIcon(primaryStage);
        primaryStage.show();

        // set up main drawing loop
        DrawTimer timer = new DrawTimer();
        timer.start();

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        DrawingBotV3.logger.exiting("FXApplication", "start");
    }

    public static void applyDBIcon(Stage primaryStage){
        InputStream stream = FXApplication.class.getResourceAsStream("/images/icon.png");
        if(stream != null){
            primaryStage.getIcons().add(new Image(stream));
        }
    }

    private static class DrawTimer extends AnimationTimer{

        @Override
        public void handle(long now) {
            DrawingBotV3.INSTANCE.draw();
        }
    }
}
