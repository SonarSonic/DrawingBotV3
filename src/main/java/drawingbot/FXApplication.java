package drawingbot;

import drawingbot.api.API;
import drawingbot.api_impl.DrawingBotV3API;
import drawingbot.files.ConfigFileHandler;
import drawingbot.javafx.FXController;
import drawingbot.javafx.PGraphicsFX9;
import drawingbot.javafx.PSurfaceFX9;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.canvas.Canvas;
import javafx.stage.Screen;
import javafx.stage.Stage;
import processing.core.PApplet;
import processing.core.PConstants;

import java.io.*;
import java.util.logging.Level;

//TODO - FIX NULL POINTER EXCEPTION WHEN PROGRAM HAS BEEN RUNNING FOR A WHILE
public class FXApplication extends Application {

    public static Stage primaryStage;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        DrawingBotV3.logger.setLevel(Level.ALL);
        DrawingBotV3.logger.entering("FXApplication", "start");
        FXApplication.primaryStage = primaryStage;

        DrawingBotV3.logger.info("Loading configuration");
        ConfigFileHandler.init();

        DrawingBotV3.logger.info("Loading Processing Environment");
        PApplet.main(DrawingBotV3.class);

        DrawingBotV3.logger.info("Loading API");
        API.INSTANCE = new DrawingBotV3API();

        DrawingBotV3.logger.exiting("FXApplication", "start");
    }

    public static void setupSurface(DrawingBotV3 app){
        DrawingBotV3.logger.entering("FXApplication", "setupSurface");
        try {
            final Stage primaryStage = FXApplication.primaryStage;
            final PGraphicsFX9 graphicsFX9 = (PGraphicsFX9) app.getGraphics();
            final PSurfaceFX9 surfaceFX9 = (PSurfaceFX9) app.getSurface();
            final Canvas canvas = surfaceFX9.canvas;
            final Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

            graphicsFX9.context = canvas.getGraphicsContext2D();
            surfaceFX9.sketch = app;

            app.controller = new FXController();
            app.canvas = surfaceFX9.canvas;

            FXMLLoader loader = new FXMLLoader(FXApplication.class.getResource("/fxml/userinterface.fxml")); // abs path to fxml file
            loader.setController(app.controller);
            final Parent sceneFromFXML = loader.load();
            final Scene newScene = new Scene(sceneFromFXML, visualBounds.getWidth()/1.2, visualBounds.getHeight()/1.2, false, getAntialiasing(app));

            app.displayWidth = (int) newScene.getWidth();
            app.displayHeight = (int) newScene.getHeight();
            app.controller.viewportStackPane.setOnMousePressed(app::mousePressedJavaFX);
            app.controller.viewportStackPane.setOnMouseDragged(app::mouseDraggedJavaFX);
            app.controller.viewportStackPane.getChildren().add(canvas);

            app.controller.viewportStackPane.prefHeightProperty().bind(canvas.heightProperty().multiply(3));
            app.controller.viewportStackPane.prefWidthProperty().bind(canvas.widthProperty().multiply(3));
            //app.controller.viewportStackPane.minHeightProperty().bind(app.controller.viewportScrollPane.heightProperty().multiply(2));
            //app.controller.viewportStackPane.minWidthProperty().bind(app.controller.viewportScrollPane.widthProperty().multiply(2));

            app.controller.viewportScrollPane.setHvalue(0.5);
            app.controller.viewportScrollPane.setVvalue(0.5);

            canvas.setWidth(500);
            canvas.setHeight(500);
            primaryStage.setScene(newScene);

            surfaceFX9.stage = primaryStage;
            surfaceFX9.setProcessingIcon(primaryStage);

            primaryStage.setTitle(DrawingBotV3.appName + ", Version: " + DrawingBotV3.appVersion);
            primaryStage.setResizable(true);

            surfaceFX9.startExceptionHandlerThread();

        } catch (IOException e) {
            DrawingBotV3.logger.log(Level.SEVERE, e, () -> "JAVA FX failed to setup primary stage");
            Platform.exit();
        }
        DrawingBotV3.logger.exiting("FXApplication", "setupSurface");
    }

    public static SceneAntialiasing getAntialiasing(DrawingBotV3 app){
        int smooth = app.sketchSmooth();
        // Workaround for https://bugs.openjdk.java.net/browse/JDK-8136495
        // https://github.com/processing/processing/issues/3823
        if ((PApplet.platform == PConstants.MACOSX || PApplet.platform == PConstants.LINUX) && PApplet.javaVersionName.compareTo("1.8.0_60") >= 0 && PApplet.javaVersionName.compareTo("1.8.0_72") < 0) {
            DrawingBotV3.logger.warning("smooth() disabled for JavaFX with this Java version due to Oracle bug");
            DrawingBotV3.logger.warning("https://github.com/processing/processing/issues/3795");
            smooth = 0;
        }
        return (smooth == 0) ? SceneAntialiasing.DISABLED : SceneAntialiasing.BALANCED;
    }

}
