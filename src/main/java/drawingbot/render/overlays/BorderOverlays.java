package drawingbot.render.overlays;

import drawingbot.render.viewport.Viewport;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Viewport overlay for adding a coloured border to the drawing / image
 */
public class BorderOverlays extends ViewportOverlayBase {

    public final SimpleObjectProperty<Color> borderColour = new SimpleObjectProperty<>(Color.BLACK);

    public Rectangle rectangle;

    @Override
    public void initOverlay() {
        rectangle = new Rectangle(0, 0, 400, 400);
        rectangle.strokeProperty().bind(borderColour);
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setManaged(false);
        rectangle.setVisible(false);
        rectangle.setMouseTransparent(true);
    }

    @Override
    public void activateViewportOverlay(Viewport viewport) {
        super.activateViewportOverlay(viewport);
        rectangle.setVisible(true);
        rectangle.getTransforms().add(viewport.getCanvasToViewportTransform());
        rectangle.strokeWidthProperty().bind(viewport.canvasToViewportScaleProperty());
        rectangle.widthProperty().bind(viewport.canvasWidthProperty());
        rectangle.heightProperty().bind(viewport.canvasHeightProperty());
        viewport.getForegroundOverlayNodes().add(rectangle);
    }

    @Override
    public void deactivateViewportOverlay(Viewport viewport) {
        super.deactivateViewportOverlay(viewport);
        rectangle.setVisible(false);
        rectangle.getTransforms().remove(viewport.getCanvasToViewportTransform());
        rectangle.strokeWidthProperty().unbind();
        rectangle.widthProperty().unbind();
        rectangle.heightProperty().unbind();
        viewport.getForegroundOverlayNodes().remove(rectangle);
    }

    @Override
    public String getName() {
        return "Drawing Border";
    }
}
