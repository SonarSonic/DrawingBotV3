package drawingbot.geom.basic;

import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.geom.GeometryUtils;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

/**
 * A wrapper for any other special shape types
 */
public class GShape implements IGeometry {

    private Shape shape;
    public int vertexCount;

    public GShape(Shape shape){
        this.shape = shape;
        this.vertexCount = GeometryUtils.getSegmentCount(shape);
    }

    @Override
    public int getSegmentCount() {
        return vertexCount;
    }

    public Integer penIndex = null;
    public Integer customRGBA = null;

    @Override
    public Shape getAWTShape() {
        return shape;
    }

    @Override
    public Integer getPenIndex() {
        return penIndex;
    }

    @Override
    public Integer getCustomRGBA() {
        return customRGBA;
    }

    @Override
    public void setPenIndex(Integer index) {
        penIndex = index;
    }

    @Override
    public void setCustomRGBA(Integer rgba) {
        customRGBA = rgba;
    }

    @Override
    public void renderFX(GraphicsContext graphics, ObservableDrawingPen pen) {
        pen.preRenderFX(graphics, this);
        GeometryUtils.renderAWTShapeToFX(graphics, shape);
    }

    @Override
    public void transform(AffineTransform transform) {
        shape = transform.createTransformedShape(shape);
    }

    @Override
    public Coordinate getOriginCoordinate() {
        PathIterator iterator = shape.getPathIterator(null);
        float[] coords = new float[6];
        int type = iterator.currentSegment(coords);
        return new CoordinateXY(coords[0], coords[1]);
    }
}
