package drawingbot.geom.basic;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.drawing.ObservableDrawingPen;
import javafx.scene.canvas.GraphicsContext;

import java.awt.*;
import java.awt.geom.AffineTransform;

public interface IGeometry {

    IGeometryFilter DEFAULT_FILTER = (point, pen) -> pen.isEnabled();
    IGeometryFilter SELECTED_PEN_FILTER = (point, pen) -> pen.isEnabled() && (DrawingBotV3.INSTANCE.controller.getSelectedPen() == null || DrawingBotV3.INSTANCE.controller.getSelectedPen().penNumber.get() == pen.penNumber.get());

    default int getSegmentCount(){
        return 1;
    }

    Shape getAWTShape();

    /**
     * @return the pen index, may be null
     */
    Integer getPenIndex();

    /**
     * @return the sampled rgba value, may be null
     */
    Integer getCustomRGBA();

    /**
     * @param index may be null
     */
    void setPenIndex(Integer index);

    /**
     * @param rgba may be null
     */
    void setCustomRGBA(Integer rgba);

    void renderFX(GraphicsContext graphics, ObservableDrawingPen pen);

    void transform(AffineTransform transform);

    default void renderAWT(Graphics2D graphics, ObservableDrawingPen pen){
        pen.preRenderAWT(graphics, this);
        graphics.draw(getAWTShape());
    }
}
