package drawingbot.render.opengl;

import drawingbot.DrawingBotV3;
import drawingbot.render.IRenderer;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Pane;

public class OpenGLRendererImpl implements IRenderer {

    public final Rectangle2D screenBounds;

    public OpenGLRendererImpl(Rectangle2D screenBounds) {
        this.screenBounds = screenBounds;
    }

    @Override
    public void init() {
        //NOP
    }

    @Override
    public void draw() {
        //NOP
    }

    @Override
    public void reRender() {
        //NOP
    }

    @Override
    public void switchToRenderer() {
        DrawingBotV3.RENDERER.switchToRenderer();
    }

    @Override
    public void updateCanvasPosition() {
        //NOP
    }

    @Override
    public Point2D sceneToRenderer(Point2D point2D) {
        return DrawingBotV3.RENDERER.sceneToRenderer(point2D);
    }

    @Override
    public Point2D rendererToScene(Point2D point2D) {
        return DrawingBotV3.RENDERER.rendererToScene(point2D);
    }

    @Override
    public Rectangle2D getScreenBounds() {
        return screenBounds;
    }

    @Override
    public Pane getPane() {
        return DrawingBotV3.RENDERER.pane;
    }

    @Override
    public boolean isDefaultRenderer() {
        return false;
    }

    @Override
    public boolean isOpenGL() {
        return false;
    }
}
