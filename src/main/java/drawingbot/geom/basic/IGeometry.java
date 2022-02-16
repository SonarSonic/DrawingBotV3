package drawingbot.geom.basic;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.pfm.helpers.BresenhamHelper;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;

import java.awt.*;
import java.awt.geom.AffineTransform;

public interface IGeometry {

    IGeometryFilter BYPASS_FILTER = (drawing, geometry, pen) -> true;
    IGeometryFilter DEFAULT_EXPORT_FILTER = (drawing, geometry, pen) -> pen.isEnabled() && (!DrawingBotV3.INSTANCE.exportRange.get() || geometry.getGeometryIndex() >= drawing.getDisplayedShapeMin() && geometry.getGeometryIndex() <= drawing.getDisplayedShapeMax());
    IGeometryFilter DEFAULT_VIEW_FILTER = (drawing, geometry, pen) -> pen.isEnabled() && (geometry.getGeometryIndex() >= drawing.getDisplayedShapeMin() && geometry.getGeometryIndex() <= drawing.getDisplayedShapeMax());
    IGeometryFilter SELECTED_PEN_FILTER = (drawing, geometry, pen) -> DEFAULT_EXPORT_FILTER.filter(drawing, geometry, pen) && (DrawingBotV3.INSTANCE.controller.getSelectedPen() == null || DrawingBotV3.INSTANCE.controller.getSelectedPen().penNumber.get() == pen.penNumber.get());

    /**
     * This is used by the OpenGL Renderer, therefore this must accurately represent the number of vertices required
     */
    default int getVertexCount(){
        return 1;
    }

    /**
     * The AWT version of this geometry, for speed when rendering with Graphics2D
     */
    Shape getAWTShape();

    int getGeometryIndex();

    /**
     * @return the pen index, -1 if no pen has been chosen
     */
    int getPenIndex();

    /**
     * @return the pen index,  -1 if no pen was preconfigured
     */
    int getPFMPenIndex();

    /**
     * @return the sampled rgba value, may be null
     */
    int getSampledRGBA();

    /**
     * @return the group id, geometries with the same group id are considered to be from the same section of the drawing
     * therefore when optimising the drawing, geometries with matching group ids, will be optimised together and not mixed with other groups
     */
    int getGroupID();

    void setGeometryIndex(int index);

    void setPenIndex(int index);

    void setPFMPenIndex(int index);

    void setSampledRGBA(int rgba);

    /**
     * @param groupID sets the geometries group id
     */
    void setGroupID(int groupID);

    /**
     * Render the Geometry in JAVAFX
     */
    void renderFX(GraphicsContext graphics, ObservableDrawingPen pen);

    /**
     * Render the Geometry in AWT
     */
    default void renderAWT(Graphics2D graphics, ObservableDrawingPen pen){
        pen.preRenderAWT(graphics, this);
        graphics.draw(getAWTShape());
    }

    /**
     * Geometries should override this method to optimise the bresenham calculations used
     */
    default void renderBresenham(BresenhamHelper helper, BresenhamHelper.IPixelSetter setter){
        helper.plotShape(getAWTShape(), setter);
    }

    String serializeData();

    void deserializeData(String geometryData);

    void transform(AffineTransform transform);

    /**
     * Used for geometry sorting only
     */
    Coordinate getOriginCoordinate();

}
