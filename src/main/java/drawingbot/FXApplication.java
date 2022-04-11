package drawingbot;

import drawingbot.api.API;
import drawingbot.api.IPlugin;
import drawingbot.api_impl.DrawingBotV3API;
import drawingbot.drawing.DrawingPen;
import drawingbot.image.ImageTools;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.javafx.FXController;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.render.jfx.JavaFXRenderer;
import drawingbot.render.opengl.OpenGLRendererImpl;
import drawingbot.utils.DBConstants;
import drawingbot.utils.LazyTimer;
import drawingbot.javafx.util.MouseMonitor;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class FXApplication extends Application {

    public static String[] launchArgs = new String[0];
    public static Stage primaryStage;
    public static Scene primaryScene;
    public static List<Stage> childStages = new ArrayList<>();
    public static DrawTimer drawTimer;
    public static boolean isPremiumEnabled;
    public static boolean isHeadless;
    public static MouseMonitor mouseMonitor;

    public static void main(String[] args) {
        launchArgs = args;
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        DrawingBotV3.logger.setLevel(Level.FINE);
        ConfigFileHandler.setupConsoleOutputFile();
        ConfigFileHandler.logApplicationStatus();

        DrawingBotV3.logger.entering("FXApplication", "start");
        FXApplication.primaryStage = primaryStage;

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        DrawingBotV3.logger.info("Find Plugins");
        MasterRegistry.findPlugins();

        DrawingBotV3.logger.info("Found " + MasterRegistry.PLUGINS.size() + " Plugins");
        MasterRegistry.PLUGINS.forEach(plugin -> DrawingBotV3.logger.info("Plugin: " + plugin.getPluginName()));

        DrawingBotV3.logger.info("Plugins Pre Init");
        MasterRegistry.PLUGINS.forEach(IPlugin::preInit);

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
        DrawingBotV3.INSTANCE.drawingSetSlots.get().add(new ObservableDrawingSet(MasterRegistry.INSTANCE.getDefaultDrawingSet()));
        DrawingBotV3.INSTANCE.activeDrawingSet.set(DrawingBotV3.INSTANCE.drawingSetSlots.get().get(0));
        DrawingBotV3.INSTANCE.activeDrawingSet.get().name.set("Default");
        DrawingBotV3.INSTANCE.pfmFactory.set(MasterRegistry.INSTANCE.getDefaultPFM());

        DrawingBotV3.logger.info("Loading Json Files");
        JsonLoaderManager.loadJSONFiles();

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        //// INIT GUI
        DrawingBotV3.INSTANCE.controller = new FXController();

        FXMLLoader uiLoader = new FXMLLoader(FXApplication.class.getResource("userinterface.fxml")); // abs path to fxml file
        uiLoader.setController(DrawingBotV3.INSTANCE.controller);

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        FXApplication.primaryScene = new Scene(uiLoader.load(), visualBounds.getWidth()/1.2, visualBounds.getHeight()/1.2, false, SceneAntialiasing.BALANCED);
        FXApplication.primaryScene.setOnKeyPressed(DrawingBotV3.INSTANCE::keyPressed);
        FXApplication.primaryScene.setOnKeyReleased(DrawingBotV3.INSTANCE::keyReleased);
        FXApplication.primaryScene.setOnMouseMoved(mouseMonitor = new MouseMonitor());

        if(!isHeadless) {
            primaryStage.setScene(primaryScene);
        }

        // SET DISPLAY MODE
        DrawingBotV3.INSTANCE.displayMode.set(Register.INSTANCE.DISPLAY_MODE_IMAGE);

        // INIT JAVAFX RENDERER
        DrawingBotV3.RENDERER = new JavaFXRenderer(Screen.getPrimary().getBounds());
        DrawingBotV3.RENDERER.init();

        // INIT OPENGL RENDERER
        DrawingBotV3.OPENGL_RENDERER = new OpenGLRendererImpl(Screen.getPrimary().getBounds());
        DrawingBotV3.OPENGL_RENDERER.init();

        // APPLY THE DISPLAY MODE SETTINGS
        DrawingBotV3.INSTANCE.displayMode.get().applySettings();
        DrawingBotV3.INSTANCE.displayMode.addListener((observable, oldValue, newValue) -> {
            DrawingBotV3.INSTANCE.onDisplayModeChanged(oldValue, newValue);
        });

        // set up main drawing loop
        drawTimer = new DrawTimer(this);
        drawTimer.start();

        DrawingBotV3.logger.info("Load Default Presets");
        JsonLoaderManager.loadDefaults();

        DrawingBotV3.logger.info("Plugins Post Init");
        MasterRegistry.PLUGINS.forEach(IPlugin::postInit);

        if(!isHeadless){
            primaryStage.setTitle(DBConstants.versionName + ", Version: " + DBConstants.appVersion);
            primaryStage.setResizable(true);
            applyDBStyle(primaryStage);
            primaryStage.show();
        }


        DrawingBotV3.logger.info("Plugins Load JFX Stages");
        for(IPlugin plugin : MasterRegistry.PLUGINS){
            plugin.loadJavaFXStages();
        }

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

    public static void applyDBStyle(Stage primaryStage){

        InputStream stream = FXApplication.class.getResourceAsStream("/images/icon.png");
        if(stream != null){
            primaryStage.getIcons().add(new Image(stream));
        }

        applyCurrentTheme(primaryStage.getScene());
    }

    public static void applyCurrentTheme(){
        applyCurrentTheme(primaryScene);
        childStages.forEach(stage -> applyCurrentTheme(stage.getScene()));
    }

    public static void applyCurrentTheme(Scene scene){
        if (ConfigFileHandler.getApplicationSettings().darkTheme) {
            scene.getRoot().setStyle("-fx-base: rgba(30, 30, 30, 255); -fx-accent: rgba(0, 100, 134, 255);");
        } else {
            scene.getRoot().setStyle("");
        }
    }

    public void onFirstTick(){
        //NOP
    }

    public static class DrawTimer extends AnimationTimer{

        public final FXApplication fxApplication;
        private final LazyTimer timer = new LazyTimer();
        public int resetLayoutTimer = 0;

        private boolean isFirstTick = true;

        public DrawTimer(FXApplication fxApplication){
            this.fxApplication = fxApplication;
        }

        @Override
        public void handle(long now) {
            if(isFirstTick){
                DrawingBotV3.INSTANCE.resetView();
                isFirstTick = false;
                fxApplication.onFirstTick();
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

            DrawingBotV3.INSTANCE.displayMode.get().getRenderer().draw();

            timer.finish();

            if(!DrawingBotV3.INSTANCE.displayMode.get().getRenderer().isOpenGL() && timer.getElapsedTime() > 1000/60){
                DrawingBotV3.logger.finest("RENDERER TOOK: " + timer.getElapsedTimeFormatted() + " milliseconds" + " expected " + 1000/60);
            }
        }
    }
}
