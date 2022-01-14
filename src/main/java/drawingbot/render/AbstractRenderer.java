package drawingbot.render;

import drawingbot.DrawingBotV3;
import drawingbot.render.jfx.JavaFXRenderer;
import javafx.geometry.Rectangle2D;

public class AbstractRenderer {

    public Rectangle2D screenBounds;

    public AbstractRenderer(Rectangle2D screenBounds) {
        this.screenBounds = screenBounds;
    }

    public double getCanvasScaleValue(){
        if(this instanceof JavaFXRenderer){
            return 1; //the JFX canvas is scaled on the canvas itself
        }
        return DrawingBotV3.INSTANCE.controller.viewportScrollPane.scaleValue;
    }

    public double getPaneActualWidth(){
        return getCanvasScaleValue() * screenBounds.getWidth();
    }

    public double getPaneActualHeight(){
        return getCanvasScaleValue() * screenBounds.getHeight();
    }

    public double getPaneMinWidth(){
        return Math.max(1, getPaneActualWidth());
    }

    public double getPaneMinHeight(){
        return Math.max(1, getPaneActualHeight());
    }

    public double getPaneScaledWidth(){
        return Math.max(DrawingBotV3.INSTANCE.controller.viewportScrollPane.getViewportBounds().getWidth(), getPaneMinWidth());
    }

    public double getPaneScaledHeight(){
        return Math.max(DrawingBotV3.INSTANCE.controller.viewportScrollPane.getViewportBounds().getHeight(), getPaneMinHeight());
    }

}
