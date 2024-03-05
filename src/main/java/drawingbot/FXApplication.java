package drawingbot;

import drawingbot.api.API;
import drawingbot.api.IPlugin;
import drawingbot.api_impl.DrawingBotV3API;
import drawingbot.files.LoggingHandler;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.javafx.util.MouseMonitor;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.render.modes.DisplayModeBase;
import drawingbot.software.ISoftware;
import drawingbot.software.SoftwareManager;
import drawingbot.utils.LazyTimer;
import drawingbot.utils.LazyTimerUtils;
import drawingbot.utils.Utils;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

public class FXApplication extends Application {

    public static FXApplication INSTANCE;
    public static String[] launchArgs = new String[0];

    public static Stage primaryStage;
    public static Scene primaryScene;
    public static List<Stage> childStages = new ArrayList<>();

    public static DrawTimer drawTimer;
    public static boolean isPremiumEnabled;
    public static boolean isHeadless;
    public static boolean isUnitTesting;

    public static SimpleBooleanProperty isLoaded = new SimpleBooleanProperty(false);
    public static MouseMonitor mouseMonitor;

    public static boolean isDeveloperMode = false;

    public static void main(String[] args) {
        launchArgs = args;
        LazyTimerUtils.startTimer("launch");

        if(Utils.getOS().isMac()){
            // Disable LCD Font Smoothing on MacOS, before JavaFX is initialised
            System.setProperty("prism.lcdtext", "false");
        }

        // Setup console / file logging
        LoggingHandler.init();

        SplashScreen.initPreloader(SoftwareManager.getSoftware().getSplashScreenClass());
        launch(args);
    }

    ////////////////////////////////////////////////////////

    public static Scene getPrimaryScene(){
        return primaryScene;
    }

    public static Stage getPrimaryStage(){
        return primaryStage;
    }

    public static List<Stage> getChildStages(){
        return childStages;
    }

    ////////////////////////////////////////////////////////

    public static void applyTheme(Stage primaryStage){
        ISoftware software = SoftwareManager.getSoftware();
        software.applyThemeToStage(primaryStage);
        software.applyThemeToScene(primaryStage.getScene());
    }

    public static void applyCurrentTheme(){
        ISoftware software = SoftwareManager.getSoftware();
        software.applyThemeToScene(FXApplication.getPrimaryScene());
        FXApplication.getChildStages().forEach(stage -> software.applyThemeToScene(stage.getScene()));
    }


    ////////////////////////////////////////////////////////

    public static class InitialLoadTask extends Task<Boolean> {

        @Override
        protected void setException(Throwable t) {
            super.setException(t);
            DrawingBotV3.logger.log(Level.SEVERE, "LOAD TASK FAILED", t);
        }

        @Override
        protected Boolean call() throws Exception {
            DrawingBotV3.logger.entering("FXApplication", "start");

            ///////////////////////////////////////////////////////////////////////////////////////////////////////

            ///// PRE INIT \\\\\\

            DrawingBotV3.logger.config("Components: Finding Components");
            SoftwareManager.INSTANCE.findComponents();

            DrawingBotV3.logger.config("Components: Found %s Components".formatted(SoftwareManager.getLoadedComponents().size()));
            SoftwareManager.getLoadedComponents().forEach(component -> DrawingBotV3.logger.config("Component: %s [%s]".formatted(component.getDisplayName(), component.getDisplayVersion())));

            DrawingBotV3.logger.config("Plugins: Pre-Init");
            SoftwareManager.getLoadedPlugins().forEach(IPlugin::preInit);

            DrawingBotV3.logger.config("DrawingBotV3: Loading Configuration");
            JsonLoaderManager.loadConfigFiles();

            DrawingBotV3.logger.config("DrawingBotV3: Loading API");
            API.INSTANCE = new DrawingBotV3API();

            DrawingBotV3.logger.config("Master Registry: Init");
            SoftwareManager.getLoadedPlugins().forEach(IPlugin::init);
            SoftwareManager.getLoadedPlugins().forEach(IPlugin::registerPFMS);
            SoftwareManager.getLoadedPlugins().forEach(IPlugin::registerPFMSettings);
            SoftwareManager.getLoadedPlugins().forEach(IPlugin::registerDrawingTools);
            SoftwareManager.getLoadedPlugins().forEach(IPlugin::registerImageFilters);
            SoftwareManager.getLoadedPlugins().forEach(IPlugin::registerDrawingExportHandlers);
            SoftwareManager.getLoadedPlugins().forEach(IPlugin::registerColourSplitterHandlers);

            //Sort the PFM settings
            MasterRegistry.INSTANCE.sortPFMSettings();
            MasterRegistry.INSTANCE.sortDataLoaders();

            ///////////////////////////////////////////////////////////////////////////////////////////////////////

            ///// INIT \\\\\

            DrawingBotV3.logger.config("DrawingBotV3: Init");
            DrawingBotV3.INSTANCE = new DrawingBotV3();
            DrawingBotV3.INSTANCE.init();

            DrawingBotV3.logger.config("DrawingBotV3: Loading Dummy Project");
            DrawingBotV3.INSTANCE.activeProject.set(new ObservableProject());
            DrawingBotV3.INSTANCE.activeProjects.add(DrawingBotV3.INSTANCE.activeProject.get());

            SoftwareManager.getLoadedPlugins().forEach(IPlugin::registerPreferencePages);
            DBPreferences.INSTANCE.postInit();

            DrawingBotV3.logger.config("Json Loader: Load JSON Files");
            JsonLoaderManager.loadJSONFiles();

            DrawingBotV3.logger.config("DrawingBotV3: Loading User Interface");
            CountDownLatch latchA = new CountDownLatch(1);
            Platform.runLater(() -> {

                FXMLLoader loader = new FXMLLoader(FXApplication.class.getResource("userinterface.fxml"));
                Parent root = null;
                try {
                    root = loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                DrawingBotV3.INSTANCE.controller = loader.getController();
                DrawingBotV3.INSTANCE.controller.initSeparateStages();
                DrawingBotV3.INSTANCE.controller.setupBindings();

                Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
                FXApplication.primaryScene = new Scene(root, visualBounds.getWidth(), visualBounds.getHeight(), false, SceneAntialiasing.BALANCED);
                DBPreferences.INSTANCE.uiWindowSize.get().setupStage(primaryStage);

                if(!isHeadless) {
                    primaryStage.setScene(primaryScene);
                }
                latchA.countDown();
            });

            latchA.await();


            //// ADD ACCELERATORS \\\\
            int keypad = 1;
            for(DisplayModeBase displayMode : MasterRegistry.INSTANCE.displayModes){
                FXApplication.primaryScene.getAccelerators().put(KeyCombination.valueOf("Shift + " + keypad), () -> DrawingBotV3.project().displayMode.set(displayMode));
                keypad++;
            }

            FXApplication.primaryScene.getAccelerators().put(KeyCombination.valueOf("Shift + V"), () -> DrawingBotV3.INSTANCE.controller.versionControlController.saveVersion());

            CountDownLatch latchB = new CountDownLatch(1);
            Platform.runLater(() -> {
                //save the default UI State before applying the users own defaults
                FXHelper.saveDefaultUIStates();

                DrawingBotV3.logger.config("Json Loader: Load Defaults");
                JsonLoaderManager.loadDefaults(DrawingBotV3.context());
                latchB.countDown();
            });
            latchB.await();

            DrawingBotV3.logger.config("Plugins: Post Init");
            SoftwareManager.getLoadedPlugins().forEach(IPlugin::postInit);

            JsonLoaderManager.postInit();

            if(!isHeadless){
                CountDownLatch latchC = new CountDownLatch(1);
                DrawingBotV3.logger.config("Plugins: Load JFX Stages");
                Platform.runLater(() -> {
                    SoftwareManager.getLoadedPlugins().forEach(IPlugin::loadJavaFXStages);
                    DrawingBotV3.logger.config("DrawingBotV3: Loading Event Handlers");
                    FXApplication.primaryScene.addEventHandler(MouseEvent.MOUSE_MOVED, mouseMonitor = new MouseMonitor());

                    // set up main drawing loop
                    drawTimer = new DrawTimer(FXApplication.INSTANCE);
                    drawTimer.start();

                    latchC.countDown();
                });

                latchC.await();
            }

            if(!isHeadless) {
                DrawingBotV3.logger.config("DrawingBotV3: Loading User Interface");
                CountDownLatch latchD = new CountDownLatch(1);
                Platform.runLater(() -> {

                    ///////////////////////////////////////////////////////////////////////////////////////////////////////
                    //Re-apply the config setings, ensures the UI is loaded properly
                    Register.PRESET_LOADER_PREFERENCES.applyConfigs();

                    DrawingBotV3.INSTANCE.resetView();
                    primaryStage.titleProperty().bind(Bindings.createStringBinding(() -> FXApplication.getSoftware().getDisplayName() + ", Version: " + FXApplication.getSoftware().getDisplayVersion() + ", " + "'" + DrawingBotV3.INSTANCE.projectName.get() + "'", DrawingBotV3.INSTANCE.projectName));
                    primaryStage.setResizable(true);
                    applyTheme(primaryStage);
                    primaryStage.show();
                    latchD.countDown();
                });

                latchD.await();
            }

            ///////////////////////////////////////////////////////////////////////////////////////////////////////

            if(launchArgs.length >= 1){
                DrawingBotV3.logger.info("Attempting to load file at startup");
                try {
                    File startupFile =  new File(launchArgs[0]);
                    DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), startupFile, false, false);
                } catch (Exception e) {
                    DrawingBotV3.logger.log(Level.SEVERE, "Failed to load file at startup", e);
                }
            }

            String loadTime = LazyTimerUtils.finishTimer("launch").getElapsedTimeFormatted();
            DrawingBotV3.logger.config("DrawingBotV3: Loaded %s".formatted(loadTime));
            SplashScreen.stopPreloader(FXApplication.INSTANCE);

            isLoaded.set(true);

            ///////////////////////////////////////////////////////////////////////////////////////////////////////

            DrawingBotV3.logger.exiting("FXApplication", "start");
            return null;
        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        INSTANCE = this;
        FXApplication.primaryStage = primaryStage;
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            DrawingBotV3.logger.log(Level.SEVERE, e, e::getMessage);
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        ///// SETUP OUTPUTS \\\\\
        SplashScreen.startPreloader(this);
    }

    public static ISoftware getSoftware(){
        return SoftwareManager.getSoftware();
    }

    public void onFirstTick(){
        //NOP
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        DrawingBotV3.logger.info("Starting Shutdown");

        DrawingBotV3.logger.info("Stopping Plugins");
        SoftwareManager.getLoadedPlugins().forEach(IPlugin::shutdown);

        DrawingBotV3.logger.info("Saving Config Files");
        Register.PRESET_LOADER_PREFERENCES.onShutdown();

        DrawingBotV3.logger.info("Saving Logging Files");
        LoggingHandler.saveLoggingFiles();

        DrawingBotV3.logger.info("Finish Shutdown");
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
                //DrawingBotV3.INSTANCE.resetView();
                isFirstTick = false;
                fxApplication.onFirstTick();
                return;
            }

            timer.start();

            DrawingBotV3.INSTANCE.tick();

            timer.finish();
        }
    }
}
