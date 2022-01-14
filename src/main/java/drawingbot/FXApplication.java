package drawingbot;

import drawingbot.api.API;
import drawingbot.api_impl.DrawingBotV3API;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.javafx.FXController;
import drawingbot.registry.MasterRegistry;
import drawingbot.render.jfx.JavaFXRenderer;
import drawingbot.render.opengl.OpenGLRenderer;
import drawingbot.utils.DBConstants;
import drawingbot.utils.LazyTimer;
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
    public static DrawTimer drawTimer;

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

        // INIT JAVAFX RENDERER
        DrawingBotV3.RENDERER = new JavaFXRenderer(Screen.getPrimary().getBounds());
        DrawingBotV3.RENDERER.init();

        // INIT OPENGL RENDERER
        DrawingBotV3.OPENGL_RENDERER = new OpenGLRenderer(Screen.getPrimary().getBounds());
        DrawingBotV3.OPENGL_RENDERER.init();

        // set up main drawing loop
        drawTimer = new DrawTimer();
        drawTimer.start();

        primaryStage.setTitle(DBConstants.appName + ", Version: " + DBConstants.appVersion);
        primaryStage.setResizable(true);
        applyDBIcon(primaryStage);
        primaryStage.show();

        ///////////////////////////////////////////////////////////////////////////////////////////////////////


        if(launchArgs.length >= 1){
            DrawingBotV3.logger.info("Attempting to load file at startup");
            try {
                File startupFile =  new File(launchArgs[0]);
                DrawingBotV3.INSTANCE.openFile(startupFile, false);
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

    public static class DrawTimer extends AnimationTimer{

        private final LazyTimer timer = new LazyTimer();
        public int resetLayoutTimer = 0;

        private boolean isFirstTick = true;

        @Override
        public void handle(long now) {
            if(isFirstTick){
                DrawingBotV3.INSTANCE.resetView();
                isFirstTick = false;
                return;
            }

            DrawingBotV3.INSTANCE.updateUI();

            if(resetLayoutTimer > 0){
                resetLayoutTimer--;
                if(resetLayoutTimer == 0) {
                    DrawingBotV3.INSTANCE.controller.viewportScrollPane.setHvalue(0.5);
                    DrawingBotV3.INSTANCE.controller.viewportScrollPane.setVvalue(0.5);
                }
            }

            timer.start();

            if(!DrawingBotV3.INSTANCE.display_mode.get().isOpenGL()){
                DrawingBotV3.RENDERER.draw();
            }else{
                DrawingBotV3.OPENGL_RENDERER.draw();
            }

            timer.finish();

            if(!DrawingBotV3.INSTANCE.display_mode.get().isOpenGL() && timer.getElapsedTime() > 1000/60){
                DrawingBotV3.logger.finest("RENDERER TOOK: " + timer.getElapsedTimeFormatted() + " milliseconds" + " expected " + 1000/60);
            }
        }
    }
}
