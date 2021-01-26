package drawingbot;

import drawingbot.javafx.FXController;
import drawingbot.javafx.PGraphicsFX9;
import drawingbot.javafx.PSurfaceFX9;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import processing.core.PApplet;
import processing.core.PConstants;

import java.io.IOException;

public class FXApplication extends Application {

    public static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        FXApplication.primaryStage = primaryStage;
        PApplet.main(DrawingBotV3.class);
    }

    public static void setupSurface(DrawingBotV3 app){
        try {
            final Stage primaryStage = FXApplication.primaryStage;
            final PGraphicsFX9 graphicsFX9 = (PGraphicsFX9) app.getGraphics();
            final PSurfaceFX9 surfaceFX9 = (PSurfaceFX9) app.getSurface();
            final Canvas canvas = surfaceFX9.canvas;

            graphicsFX9.context = canvas.getGraphicsContext2D();
            surfaceFX9.sketch = app;

            app.canvas = surfaceFX9.canvas;
            app.controller = new FXController();

            FXMLLoader loader = new FXMLLoader(FXApplication.class.getResource("/fxml/userinterface.fxml")); // abs path to fxml file
            loader.setController(app.controller);
            final Parent sceneFromFXML = loader.load();
            final Scene newScene = new Scene(sceneFromFXML, primaryStage.getWidth(), primaryStage.getHeight(), false, getAntialiasing(app));


            app.displayWidth = (int) primaryStage.getWidth();
            app.displayHeight = (int) primaryStage.getHeight();
            app.controller.viewportStackPane.setOnMousePressed(app::mousePressedJavaFX);
            app.controller.viewportStackPane.setOnMouseDragged(app::mouseDraggedJavaFX);
            app.controller.viewportStackPane.getChildren().add(canvas);
            canvas.setWidth(400);
            canvas.setHeight(400);
            primaryStage.setScene(newScene);

            surfaceFX9.stage = primaryStage;
            surfaceFX9.setProcessingIcon(primaryStage);

            surfaceFX9.startExceptionHandlerThread();
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    public static SceneAntialiasing getAntialiasing(DrawingBotV3 app){
        int smooth = app.sketchSmooth();
        // Workaround for https://bugs.openjdk.java.net/browse/JDK-8136495
        // https://github.com/processing/processing/issues/3823
        if ((PApplet.platform == PConstants.MACOSX || PApplet.platform == PConstants.LINUX) && PApplet.javaVersionName.compareTo("1.8.0_60") >= 0 && PApplet.javaVersionName.compareTo("1.8.0_72") < 0) {
            System.err.println("smooth() disabled for JavaFX with this Java version due to Oracle bug");
            System.err.println("https://github.com/processing/processing/issues/3795");
            smooth = 0;
        }
        return (smooth == 0) ? SceneAntialiasing.DISABLED : SceneAntialiasing.BALANCED;
    }

}
