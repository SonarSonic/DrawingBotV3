package drawingbot.software;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.logging.Logger;

/**
 * Represents the software, there can only be one active software at a given time, but the software can contain other components which implement {@link ISoftware}
 */
public interface ISoftware extends IComponent, SoftwareManager.Listener {

    /**
     * @return the software's logo, to be displayed on launch and in windows launched by the software
     */
    Image getLogoImage();

    /**
     * @return the software's splash image, which will be used as the background for the splash screen at launch
     */
    Image getSplashImage();

    /**
     * @return the class of Splash Screen to load, must extend {@link Preloader}
     */
    Class<? extends Preloader> getSplashScreenClass();

    /**
     * Applies the software's specific styling to the given JavaX Stage
     * @param stage the JavaFX stage
     */
    void applyThemeToStage(Stage stage);

    /**
     * Applies the software's specific styling to the given JavaX scene
     * @param scene the JavaFX scene
     */
    void applyThemeToScene(Scene scene);

    /**
     * @return the default logger for the software
     */
    Logger getLogger();


}
