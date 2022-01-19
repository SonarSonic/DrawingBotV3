package drawingbot.render;

import drawingbot.DrawingBotV3;
import drawingbot.render.jfx.JavaFXRenderer;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Pane;

public interface IRenderer {

    void init();

    void draw();

    void reRender();

    Point2D sceneToRenderer(Point2D point2D);

    Point2D rendererToScene(Point2D point2D);

    Rectangle2D getScreenBounds();

    Pane getPane();

    void updateCanvasPosition();

    ///// CANVAS SIZING \\\\\

    default double getCanvasScaleValue(){
        return 1;
    }

    default double getPaneActualWidth(){
        return getCanvasScaleValue() * getScreenBounds().getWidth();
    }

    default double getPaneActualHeight(){
        return getCanvasScaleValue() * getScreenBounds().getHeight();
    }

    default double getPaneMinWidth(){
        return Math.max(1, getPaneActualWidth());
    }

    default double getPaneMinHeight(){
        return Math.max(1, getPaneActualHeight());
    }

    default double getPaneScaledWidth(){
        return Math.max(DrawingBotV3.INSTANCE.controller.viewportScrollPane.getViewportBounds().getWidth(), getPaneMinWidth());
    }

    default double getPaneScaledHeight(){
        return Math.max(DrawingBotV3.INSTANCE.controller.viewportScrollPane.getViewportBounds().getHeight(), getPaneMinHeight());
    }

}
