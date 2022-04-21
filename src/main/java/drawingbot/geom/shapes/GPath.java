package drawingbot.geom.shapes;

import drawingbot.image.ImageTools;
import drawingbot.geom.GeometryUtils;
import drawingbot.render.RenderUtils;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

public class GPath extends Path2D.Float implements IGeometry, IPathElement {

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

    public GPath(IPathElement pathElement, boolean addToPath){
        super();
        GeometryUtils.copyGeometryData(this, pathElement);

        if(addToPath){
            //add the path
            pathElement.addToPath(true, this);
        }
    }

    public int totalSamples = 0;
    public long averageAlpha = 0;
    public long averageRed = 0;
    public long averageGreen = 0;
    public long averageBlue = 0;

    public void addColourSample(int argb){
        averageAlpha += (ImageTools.alpha(argb) - averageAlpha) / (totalSamples+1);
        averageRed += (ImageTools.red(argb) - averageRed) / (totalSamples+1);
        averageGreen += (ImageTools.green(argb) - averageGreen) / (totalSamples+1);
        averageBlue += (ImageTools.blue(argb) - averageBlue) / (totalSamples+1);
    }

    public void resetColourSamples(int argb){
        if(argb != -1){
            totalSamples = 1;
            averageAlpha = ImageTools.alpha(argb);
            averageRed = ImageTools.red(argb);
            averageGreen = ImageTools.green(argb);
            averageBlue = ImageTools.blue(argb);
        }
    }

    //// IGeometry \\\\

    public int geometryIndex = -1;
    public int pfmPenIndex = -1;
    public int penIndex = -1;
    public int sampledRGBA = -1; //TODO CHECK, -1 is a valid RGBA value though???
    public int groupID = -1;
    public int fillType = -1;

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
    public int getGeometryIndex() {
        return geometryIndex;
    }

    @Override
    public int getPenIndex() {
        return penIndex;
    }

    @Override
    public int getPFMPenIndex() {
        return pfmPenIndex;
    }

    @Override
    public int getSampledRGBA() {
        return sampledRGBA;
    }

    @Override
    public int getFillType(){
        return fillType;
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
    public void setPFMPenIndex(int index) {
        pfmPenIndex = index;
    }

    @Override
    public void setSampledRGBA(int rgba) {
        sampledRGBA = rgba;
        resetColourSamples(rgba);
    }

    @Override
    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    @Override
    public void setFillType(int fillType) {
        this.fillType = fillType;
    }

    @Override
    public void renderFX(GraphicsContext graphics) {
        RenderUtils.renderAWTShapeToFX(graphics, getAWTShape());
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
        transform(transform);
        return this;
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

    @Override
    public IGeometry copyGeometry() {
        return GeometryUtils.copyGeometryData(new GPath(this), this);
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

    @Override
    public void addToPath(boolean addMove, GPath path) {
        PathIterator iterator = getPathIterator(null);
        float[] coords = new float[6];

        boolean addedFirstMove = false;

        while (!iterator.isDone()) {
            int type = iterator.currentSegment(coords);
            if(addedFirstMove || addMove){
                GPath.move(path, coords, type);
            }
            addedFirstMove = true;
            iterator.next();
        }
    }

    @Override
    public Point2D getP1() {
        Coordinate origin = getOriginCoordinate();
        return new Point2D.Double(origin.x, origin.y);
    }

    @Override
    public Point2D getP2() {
        return getCurrentPoint();
    }
}