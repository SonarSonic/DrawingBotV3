package drawingbot.geom.shapes;

import drawingbot.geom.GeometryUtils;
import drawingbot.render.RenderUtils;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

/**
 * A wrapper for any other special shape types, should not be used in Drawings, the shape should be converted to a GPath
 */
public class GShape extends AbstractGeometry implements IGeometry {

    private Shape shape;
    public int vertexCount;

    public GShape(){}

    public GShape(Shape shape){
        this.shape = shape;
        this.vertexCount = GeometryUtils.getSegmentCount(shape);
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }

    @Override
    public Shape getAWTShape() {
        return shape;
    }

    @Override
    public void renderFX(GraphicsContext graphics) {
        RenderUtils.renderAWTShapeToFX(graphics, shape);
        if(fillType == 0){
            graphics.fill();
        }
    }

    @Override
    public String serializeData() {
        //HANDLED BY THE SERIALIZER
        return "";
    }

    @Override
    public void deserializeData(String geometryData) {
        //HANDLED BY THE SERIALIZER
    }

    @Override
    public IGeometry transformGeometry(AffineTransform transform) {
        shape = transform.createTransformedShape(shape);
        return this;
    }

    @Override
    public Coordinate getOriginCoordinate() {
        PathIterator iterator = shape.getPathIterator(null);
        float[] coords = new float[6];
        int type = iterator.currentSegment(coords);
        return new CoordinateXY(coords[0], coords[1]);
    }

    @Override
    public Coordinate getEndCoordinate() {
        return getOriginCoordinate(); //TODO ?
    }

    @Override
    public IGeometry copyGeometry() {
        return GeometryUtils.copyGeometryData(new GPath(shape), this);
    }
}
