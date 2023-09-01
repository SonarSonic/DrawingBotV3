package drawingbot;

import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.utils.AbstractSoftware;
import drawingbot.utils.DBConstants;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

public class SoftwareDBV3Free extends AbstractSoftware {

    public static final SoftwareDBV3Free INSTANCE = new SoftwareDBV3Free();

    public static final String displayName = "DrawingBotV3 Free";
    public static final String rawVersion = "1.6.6";
    public static final String releaseType = "Beta";
    public static final String displayVersion = rawVersion + " " + releaseType;

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getRawVersion() {
        return rawVersion;
    }

    @Override
    public String getDisplayVersion() {
        return displayVersion;
    }

    @Override
    public String getUpdateLink() {
        return DBConstants.UPDATE_LINK_FREE;
    }

    @Override
    public Image getLogoImage(){
        InputStream stream = FXApplication.class.getResourceAsStream("/images/icon.png");
        return stream == null ? null : new Image(stream);
    }

    @Override
    public Image getSplashImage(){
        InputStream stream = FXApplication.class.getResourceAsStream("/images/splash.png");
        return stream == null ? null : new Image(stream);
    }

    @Override
    public void applyThemeToStage(Stage primaryStage){
        Image image = getLogoImage();
        if(image != null){
            primaryStage.getIcons().add(image);
        }
        applyThemeToScene(primaryStage.getScene());
    }

    @Override
    public void applyThemeToScene(Scene scene){
        if (DBPreferences.INSTANCE.darkTheme.get()) {
            scene.getRoot().setStyle("-fx-base: rgba(30, 30, 30, 255); -fx-accent: rgba(0, 100, 134, 255);");
        } else {
            scene.getRoot().setStyle("");
        }
    }
}