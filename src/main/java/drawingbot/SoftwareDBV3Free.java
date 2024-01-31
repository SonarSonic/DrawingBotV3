package drawingbot;

import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.plugins.PremiumPluginDummy;
import drawingbot.registry.Register;
import drawingbot.software.IComponent;
import drawingbot.software.ISoftware;
import drawingbot.utils.DBConstants;
import javafx.application.Preloader;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

public class SoftwareDBV3Free implements ISoftware {

    public static final SoftwareDBV3Free INSTANCE = new SoftwareDBV3Free();

    public static final String displayName = "DrawingBotV3 Free";
    public static final String shortName = "DBV3";
    public static final String rawVersion = "1.6.12";
    public static final String releaseType = "Beta";
    public static final String displayVersion = rawVersion + " " + releaseType;

    public final BooleanProperty enabled = new SimpleBooleanProperty(true);

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getRegistryName() {
        return shortName;
    }

    @Override
    public String getVersion() {
        return rawVersion;
    }

    @Override
    public BooleanProperty enabledProperty() {
        return enabled;
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
    public Class<? extends Preloader> getSplashScreenClass() {
        return SplashScreen.class;
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

    @Override
    public Logger getLogger() {
        return DrawingBotV3.logger;
    }

    @Override
    public List<IComponent> getSubComponents() {
        return List.of(Register.INSTANCE, PremiumPluginDummy.INSTANCE);
    }
}