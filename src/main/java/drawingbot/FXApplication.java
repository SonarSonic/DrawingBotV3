package drawingbot;

import drawingbot.api.API;
import drawingbot.api_impl.DrawingBotV3API;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.javafx.FXController;
import drawingbot.registry.MasterRegistry;
import drawingbot.render.jfx.JavaFXRenderer;
import drawingbot.utils.DBConstants;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.*;
import java.util.logging.Level;

public class FXApplication extends Application {

    public static String[] launchArgs;
    public static Stage primaryStage;
    public static Scene primaryScene;

    public static void main(String[] args) {
        launchArgs = args;
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
        DrawingBotV3.INSTANCE.controller = new FXController();

        FXMLLoader uiLoader = new FXMLLoader(FXApplication.class.getResource("/fxml/userinterface.fxml")); // abs path to fxml file
        uiLoader.setController(DrawingBotV3.INSTANCE.controller);

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        FXApplication.primaryScene = new Scene(uiLoader.load(), visualBounds.getWidth()/1.2, visualBounds.getHeight()/1.2, false, SceneAntialiasing.BALANCED);
        FXApplication.primaryScene.setOnKeyPressed(DrawingBotV3.INSTANCE::keyPressed);
        FXApplication.primaryScene.setOnKeyReleased(DrawingBotV3.INSTANCE::keyReleased);
        primaryStage.setScene(primaryScene);

        ///// INIT RENDERER
        DrawingBotV3.RENDERER = new JavaFXRenderer();
        DrawingBotV3.RENDERER.init();

        // set up main drawing loop
        DrawTimer timer = new DrawTimer();
        timer.start();

        primaryStage.setTitle(DBConstants.appName + ", Version: " + DBConstants.appVersion);
        primaryStage.setResizable(true);
        applyDBIcon(primaryStage);
        primaryStage.show();

        ///////////////////////////////////////////////////////////////////////////////////////////////////////


        if(launchArgs.length >= 1){
            DrawingBotV3.logger.info("Attempting to load file at startup");
            try {
                File startupFile =  new File(launchArgs[0]);
                DrawingBotV3.INSTANCE.openImage(startupFile, false);
            } catch (Exception e) {
                DrawingBotV3.logger.log(Level.SEVERE, "Failed to load file at startup", e);
            }
        }

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
            DrawingBotV3.INSTANCE.updateUI();
            DrawingBotV3.RENDERER.draw();
        }
    }
}
