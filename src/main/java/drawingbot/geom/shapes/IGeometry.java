package drawingbot.geom.shapes;

import drawingbot.geom.GeometryUtils;
import drawingbot.pfm.helpers.BresenhamHelper;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;

import java.awt.*;
import java.awt.geom.AffineTransform;

public interface IGeometry {
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

    int getFillType();

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

    void setFillType(int fillType);

    /**
     * Render the Geometry in JAVAFX, the {@link IGeometry} is only responsible for drawing the shape, the colour and stroke style are specified elsewhere
     */
    void renderFX(GraphicsContext graphics);

    /**
     * Render the Geometry in AWT
     */
    default void renderAWT(Graphics2D graphics){
        graphics.draw(getAWTShape());
        if(getFillType() == 0){
            graphics.fill(getAWTShape());
        }
    }

    /**
     * Geometries should override this method to optimise the bresenham calculations used
     */
    default void renderBresenham(BresenhamHelper helper, BresenhamHelper.IPixelSetter setter){
        helper.plotShape(getAWTShape(), setter);
    }

    String serializeData();

    void deserializeData(String geometryData);

    /**
     * Returns a transformed version of this geometry.
     * This generally results in a version created as a gPath.
     */
    default IGeometry transformGeometry(AffineTransform transform) {
        //TODO CHANGE TRANSFORM METHOD TO MAINTAIN CURVES i.e. so optimizing steps don't think they can just change them
        GPath gPath = new GPath(getAWTShape());
        GeometryUtils.copyGeometryData(gPath, this);
        gPath.awtPath.transform(transform);
        return gPath;
    }

    /**
     * Used for geometry sorting only
     */
    Coordinate getOriginCoordinate();

    /**
     * Used for geometry sorting only
     */
    Coordinate getEndCoordinate();

    IGeometry copyGeometry();
}
