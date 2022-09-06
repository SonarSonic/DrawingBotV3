package drawingbot.render.overlays;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.utils.flags.Flags;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class DrawingBorderOverlays extends AbstractOverlay {

    public static final DrawingBorderOverlays INSTANCE = new DrawingBorderOverlays();

    public Rectangle rectangle;

    @Override
    public void init() {
        rectangle = new Rectangle(0, 0, 400, 400);
        rectangle.setStroke(Color.BLACK);
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setManaged(false);
        rectangle.setVisible(false);
        rectangle.setMouseTransparent(true);
        DrawingBotV3.INSTANCE.controller.viewportOverlayAnchorPane.getChildren().add(rectangle);
    }

    @Override
    public void doRender() {
        super.doRender();

        if (DrawingBotV3.project().displayMode.get().getRenderFlags().anyMatch(Flags.FORCE_REDRAW, Flags.CANVAS_MOVED, Flags.CANVAS_CHANGED, Flags.CHANGED_RENDERER)) {
            DrawingBotV3.project().displayMode.get().getRenderFlags().markForClear(Flags.FORCE_REDRAW, Flags.CANVAS_MOVED, Flags.CANVAS_CHANGED, Flags.CHANGED_RENDERER);
            Point2D drawingOrigin = new Point2D(0D, 0D);
            ICanvas canvas = DrawingBotV3.project().displayMode.get().getRenderer().getRefCanvas();

            Point2D viewportOrigin = DrawingBotV3.INSTANCE.controller.viewportScrollPane.localToScene(0, 0);

            Point2D drawingOriginScene = DrawingBotV3.project().displayMode.get().getRenderer().rendererToScene(drawingOrigin).subtract(viewportOrigin);
            Point2D drawingEndScene = DrawingBotV3.project().displayMode.get().getRenderer().rendererToScene(new Point2D(canvas.getScaledWidth(), canvas.getScaledHeight())).subtract(viewportOrigin);
            rectangle.relocate(drawingOriginScene.getX(), drawingOriginScene.getY());
            rectangle.setWidth(drawingEndScene.getX() - drawingOriginScene.getX());
            rectangle.setHeight(drawingEndScene.getY() - drawingOriginScene.getY());
        }
    }

    @Override
    protected void activate() {
        rectangle.setVisible(true);
    }

    @Override
    protected void deactivate() {
        rectangle.setVisible(false);
    }

    @Override
    public String getName() {
        return "Drawing Border";
    }
}
