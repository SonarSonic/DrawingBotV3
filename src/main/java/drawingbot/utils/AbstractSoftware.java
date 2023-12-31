package drawingbot.utils;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public abstract class AbstractSoftware {

    public abstract String getDisplayName();

    public abstract String getShortName();

    public abstract String getRawVersion();

    public abstract String getDisplayVersion();

    public abstract String getUpdateLink();

    public abstract Image getLogoImage();

    public abstract Image getSplashImage();

    public abstract void applyThemeToStage(Stage stage);

    public abstract void applyThemeToScene(Scene scene);

}
