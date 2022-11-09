package drawingbot;

import drawingbot.api.API;
import drawingbot.api.IPlugin;
import drawingbot.api_impl.DrawingBotV3API;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.registry.MasterRegistry;
import drawingbot.render.jfx.JavaFXRenderer;
import drawingbot.render.opengl.OpenGLRendererImpl;
import drawingbot.render.overlays.*;
import drawingbot.utils.DBConstants;
import drawingbot.utils.LazyTimer;
import drawingbot.javafx.util.MouseMonitor;
import drawingbot.utils.flags.Flags;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

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

    public static boolean isDeveloperMode = true;//TODO CHANGE ME

    public static void main(String[] args) {
        launchArgs = args;
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        ///// SETUP OUTPUTS \\\\\

        DrawingBotV3.logger.setLevel(Level.FINE);
        ConfigFileHandler.setupConsoleOutputFile();
        ConfigFileHandler.logApplicationStatus();

        DrawingBotV3.logger.entering("FXApplication", "start");
        FXApplication.primaryStage = primaryStage;

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        ///// PRE INIT \\\\\\

        DrawingBotV3.logger.info("Plugins: Finding Plugins");
        MasterRegistry.findPlugins();

        DrawingBotV3.logger.info("Plugins: Found " + MasterRegistry.PLUGINS.size() + " Plugins");
        MasterRegistry.PLUGINS.forEach(plugin -> DrawingBotV3.logger.info("Plugin: " + plugin.getPluginName()));

        DrawingBotV3.logger.info("Plugins: Pre-Init");
        MasterRegistry.PLUGINS.forEach(IPlugin::preInit);

        DrawingBotV3.logger.info("DrawingBotV3: Loading Configuration");
        ConfigFileHandler.init();

        DrawingBotV3.logger.info("DrawingBotV3: Loading API");
        API.INSTANCE = new DrawingBotV3API();

        DrawingBotV3.logger.info("Master Registry: Init");
        MasterRegistry.init();

        DrawingBotV3.logger.info("Renderers: Pre-Init");
        DrawingBotV3.RENDERER = new JavaFXRenderer(Screen.getPrimary().getBounds());
        DrawingBotV3.OPENGL_RENDERER = new OpenGLRendererImpl(Screen.getPrimary().getBounds());

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        ///// INIT \\\\\

        DrawingBotV3.logger.info("DrawingBotV3: Init");
        DrawingBotV3.INSTANCE = new DrawingBotV3();
        DrawingBotV3.INSTANCE.init();

        DrawingBotV3.logger.info("DrawingBotV3: Loading Dummy Project");
        DrawingBotV3.INSTANCE.activeProject.set(new ObservableProject());
        DrawingBotV3.INSTANCE.activeProjects.add(DrawingBotV3.INSTANCE.activeProject.get());
        DrawingBotV3.INSTANCE.activeProject.get().init();

        DrawingBotV3.logger.info("Json Loader: Load JSON Files");
        JsonLoaderManager.loadJSONFiles();

        DrawingBotV3.logger.info("DrawingBotV3: Loading User Interface");
        FXMLLoader loader = new FXMLLoader(FXApplication.class.getResource("userinterface.fxml"));
        Parent root = loader.load();
        DrawingBotV3.INSTANCE.controller = loader.getController();
        DrawingBotV3.INSTANCE.controller.initSeparateStages();
        DrawingBotV3.INSTANCE.controller.setupBindings();

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        FXApplication.primaryScene = new Scene(root, visualBounds.getWidth()/1.2, visualBounds.getHeight()/1.2, false, SceneAntialiasing.BALANCED);

        if(!isHeadless) {
            primaryStage.setScene(primaryScene);
        }

        DrawingBotV3.logger.info("Renderers: Init JFX Renderer");
        DrawingBotV3.RENDERER.init();

        DrawingBotV3.logger.info("Renderers: Init OpenGL Renderer");
        DrawingBotV3.OPENGL_RENDERER.init();

        DrawingBotV3.logger.info("Renderers: Load Display Mode");
        DrawingBotV3.INSTANCE.displayMode.get().applySettings();
        DrawingBotV3.INSTANCE.displayMode.addListener((observable, oldValue, newValue) -> {
            if(oldValue == null || newValue.getRenderer() == oldValue.getRenderer()){
                DrawingBotV3.INSTANCE.setRenderFlag(Flags.FORCE_REDRAW, true);
            }
            if(oldValue == null || newValue.getRenderer() != oldValue.getRenderer()){
                DrawingBotV3.INSTANCE.setRenderFlag(Flags.CHANGED_RENDERER, true);
            }
            DrawingBotV3.INSTANCE.onDisplayModeChanged(oldValue, newValue);
        });

        DrawingBotV3.logger.info("Renderers: Load Overlays");
        MasterRegistry.INSTANCE.overlays.forEach(AbstractOverlay::init);

        //FXProgramSettings.init();

        // set up main drawing loop
        drawTimer = new DrawTimer(this);
        drawTimer.start();

        DrawingBotV3.logger.info("Json Loader: Load Defaults");
        JsonLoaderManager.loadDefaults();

        DrawingBotV3.logger.info("Plugins: Post Init");
        MasterRegistry.PLUGINS.forEach(IPlugin::postInit);

        DBPreferences.INSTANCE.postInit();

        if(!isHeadless){
            DrawingBotV3.INSTANCE.projectName.set("Untitled");
            primaryStage.titleProperty().bind(Bindings.createStringBinding(() -> DBConstants.versionName + ", Version: " + DBConstants.appVersion + ", " + "'" + DrawingBotV3.INSTANCE.projectNameBinding.get() + "'", DrawingBotV3.INSTANCE.applicationName, DrawingBotV3.INSTANCE.versionName, DrawingBotV3.INSTANCE.projectNameBinding));
            primaryStage.setResizable(true);
            applyDBStyle(primaryStage);
            primaryStage.show();
        }

        DrawingBotV3.logger.info("Plugins: Load JFX Stages");
        for(IPlugin plugin : MasterRegistry.PLUGINS){
            plugin.loadJavaFXStages();
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        DrawingBotV3.logger.info("DrawingBotV3: Loading Event Handlers");

        FXApplication.primaryScene.addEventHandler(MouseEvent.MOUSE_MOVED, mouseMonitor = new MouseMonitor());

        DrawingBotV3.INSTANCE.controller.viewportScrollPane.addEventHandler(MouseEvent.MOUSE_MOVED, DrawingBotV3.INSTANCE::onMouseMovedViewport);
        DrawingBotV3.INSTANCE.controller.viewportScrollPane.addEventHandler(MouseEvent.MOUSE_PRESSED, DrawingBotV3.INSTANCE::onMousePressedViewport);
        DrawingBotV3.INSTANCE.controller.viewportScrollPane.addEventHandler(KeyEvent.KEY_PRESSED, DrawingBotV3.INSTANCE::onKeyPressedViewport);

        ///////////////////////////////////////////////////////////////////////////////////////////////////////


        if(launchArgs.length >= 1){
            DrawingBotV3.logger.info("Attempting to load file at startup");
            try {
                File startupFile =  new File(launchArgs[0]);
                DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), startupFile, false, true);
            } catch (Exception e) {
                DrawingBotV3.logger.log(Level.SEVERE, "Failed to load file at startup", e);
            }
        }

        DrawingBotV3.logger.info("DrawingBotV3: Loaded");

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        DrawingBotV3.logger.exiting("FXApplication", "start");
    }

    public static Image getDBV3LogoImage(){
        InputStream stream = FXApplication.class.getResourceAsStream("/images/icon.png");
        return stream == null ? null : new Image(stream);
    }

    public static void applyDBStyle(Stage primaryStage){
        InputStream stream = FXApplication.class.getResourceAsStream("/images/icon.png");
        if(stream != null){
            primaryStage.getIcons().add(getDBV3LogoImage());
        }
        applyCurrentTheme(primaryStage.getScene());
    }

    public static void applyCurrentTheme(){
        applyCurrentTheme(primaryScene);
        childStages.forEach(stage -> applyCurrentTheme(stage.getScene()));
    }

    public static void applyCurrentTheme(Scene scene){
        if (DrawingBotV3.INSTANCE.getPreferences().darkTheme.get()) {
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

            timer.start();
            DrawingBotV3.project().displayMode.get().getRenderer().preRender();
            MasterRegistry.INSTANCE.overlays.forEach(o -> {
                if(o.isActive()){
                    o.preRender();
                }
            });

            DrawingBotV3.project().displayMode.get().getRenderer().doRender();
            MasterRegistry.INSTANCE.overlays.forEach(o -> {
                if(o.isActive()){
                    o.doRender();
                }
            });
            DrawingBotV3.INSTANCE.tick();

            DrawingBotV3.project().displayMode.get().getRenderer().postRender();
            MasterRegistry.INSTANCE.overlays.forEach(o -> {
                if(o.isActive()){
                    o.postRender();
                }
            });


            timer.finish();

            if(!DrawingBotV3.project().displayMode.get().getRenderer().isOpenGL() && timer.getElapsedTime() > 1000/60){
                DrawingBotV3.logger.finest("RENDERER TOOK: " + timer.getElapsedTimeFormatted() + " milliseconds" + " expected " + 1000/60);
            }
        }
    }
}
