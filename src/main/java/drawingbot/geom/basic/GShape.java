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
 * A wrapper for any other special shape types, should not be used in Drawings, the shape should be converted to a GPath
 */
public class GShape implements IGeometry {

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


    public int geometryIndex = -1;
    public int penIndex = -1;
    public int sampledRGBA = -1;
    public int groupID = -1;

    @Override
    public Shape getAWTShape() {
        return shape;
    }

    @Override
    public int getGeometryIndex() {
        return geometryIndex;
    }

    @Override
    public int getPenIndex() {
        return penIndex;
    }

    @Override
    public int getSampledRGBA() {
        return sampledRGBA;
    }

    @Override
    public int getGroupID() {
        return groupID;
    }

    @Override
    public void setGeometryIndex(int index) {
        geometryIndex = index;
    }

    @Override
    public void setPenIndex(int index) {
        penIndex = index;
    }

    @Override
    public void setSampledRGBA(int rgba) {
        sampledRGBA = rgba;
    }

    @Override
    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    @Override
    public void renderFX(GraphicsContext graphics, ObservableDrawingPen pen) {
        pen.preRenderFX(graphics, this);
        GeometryUtils.renderAWTShapeToFX(graphics, shape);
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
