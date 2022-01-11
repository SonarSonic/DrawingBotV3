package drawingbot.geom.basic;

import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.geom.GeometryUtils;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

public class GPath extends Path2D.Float implements IGeometry {

    public GPath() {
        super();
    }

    public GPath(int rule) {
        super(rule);
    }

    public GPath(int rule, int initialCapacity) {
        super(rule, initialCapacity);
    }

    public GPath(Shape s) {
        super(s);
    }

    public GPath(Shape s, AffineTransform at) {
        super(s, at);
    }

    public GPath(IPathElement pathElement){
        super();
        this.setCustomRGBA(pathElement.getCustomRGBA());
        this.setPenIndex(pathElement.getPenIndex());

        //add the path
        pathElement.addToPath(true, this);
    }

    //// IGeometry \\\\

    public int geometryIndex = -1;
    public Integer penIndex = null;
    public Integer customRGBA = null;
    public int groupID = -1;

    public int segmentCount = -1;

    @Override
    public int getVertexCount() {
        if(segmentCount == -1){
            segmentCount = GeometryUtils.getSegmentCount(this);
        }
        return segmentCount;
    }

    @Override
    public Shape getAWTShape() {
        return this;
    }

    @Override
    public Integer getGeometryIndex() {
        return geometryIndex;
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
    public int getGroupID() {
        return groupID;
    }

    @Override
    public void setGeometryIndex(Integer index) {
        geometryIndex = index;
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
    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    @Override
    public void renderFX(GraphicsContext graphics, ObservableDrawingPen pen) {
        pen.preRenderFX(graphics, this);
        GeometryUtils.renderAWTShapeToFX(graphics, getAWTShape());
    }

    private Coordinate origin = null;
    private Coordinate endCoord = null;

    public Coordinate getEndCoordinate() {
        if(origin == null){
            Point2D point2D = getCurrentPoint();
            origin = new CoordinateXY(point2D.getX(), point2D.getY());
        }
        return origin;
    }

    @Override
    public Coordinate getOriginCoordinate() {
        if(origin == null){
            PathIterator iterator = this.getPathIterator(null);
            float[] coords = new float[6];
            int type = iterator.currentSegment(coords);
            origin = new CoordinateXY(coords[0], coords[1]);
        }
        return origin;
    }

    public void addToPath(GPath path) {
        PathIterator iterator = this.getPathIterator(null);
        float[] coords = new float[6];
        while(!iterator.isDone()){
            int type = iterator.currentSegment(coords);
            move(path, coords, type);
            iterator.next();
        }
        markPathDirty();
    }

    public void markPathDirty(){
        segmentCount = -1;
    }

    public static void move(GPath path, float[] coords, int type){
        switch (type){
            case PathIterator.SEG_MOVETO:
                path.moveTo(coords[0], coords[1]);
                break;
            case PathIterator.SEG_LINETO:
                path.lineTo(coords[0], coords[1]);
                break;
            case PathIterator.SEG_QUADTO:
                path.quadTo(coords[0], coords[1], coords[2], coords[3]);
                break;
            case PathIterator.SEG_CUBICTO:
                path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                break;
            case PathIterator.SEG_CLOSE:
                path.closePath();
                break;
        }
    }
}