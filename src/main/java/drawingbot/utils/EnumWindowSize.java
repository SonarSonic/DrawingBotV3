package drawingbot.utils;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public enum EnumWindowSize {
    DEFAULT,
    WINDOWED,
    FULL_SCREEN;

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }

    public void setupStage(Stage stage){
        switch (this){
            case DEFAULT -> {
                Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
                stage.setMaximized(false);
                stage.setFullScreen(false);
                stage.setWidth(visualBounds.getWidth()/1.1);
                stage.setHeight(visualBounds.getHeight()/1.1);
                stage.centerOnScreen();
            }
            case WINDOWED -> {
                stage.setFullScreen(false);
                stage.setMaximized(true);
            }
            case FULL_SCREEN -> {
                stage.setMaximized(false);
                stage.setFullScreen(true);
            }
        }
    }
}
