package drawingbot.geom.shapes;

import drawingbot.geom.GeometryUtils;
import drawingbot.image.ImageTools;
import drawingbot.render.RenderUtils;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

public class GPath extends AbstractGeometry implements IGeometry, IPathElement {

    public GeneralPath awtPath = new GeneralPath();

    public GPath() {
        this.awtPath = new GeneralPath();
    }

    public GPath(Shape s) {
        this.awtPath = new GeneralPath(s);
    }

    public GPath(Shape s, AffineTransform at) {
        this.awtPath = new GeneralPath();
        this.awtPath.append(s.getPathIterator(at), false);
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

    public int vertexCount = -1;

    @Override
    public int getVertexCount() {
        if(vertexCount == -1){
            vertexCount = GeometryUtils.getSegmentCount(awtPath);
        }
        return vertexCount;
    }

    @Override
    public Shape getAWTShape() {
        return awtPath;
    }

    @Override
    public void setSampledRGBA(int rgba) {
        super.setSampledRGBA(rgba);
        resetColourSamples(rgba);
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
    public Coordinate getEndCoordinate() {
        //better to not cache this, cause it can change
        Point2D point2D = awtPath.getCurrentPoint();
        return new CoordinateXY(point2D.getX(), point2D.getY());
    }

    @Override
    public Coordinate getOriginCoordinate() {
        PathIterator iterator = awtPath.getPathIterator(null);
        float[] coords = new float[6];
        int type = iterator.currentSegment(coords);
        return new CoordinateXY(coords[0], coords[1]);
    }

    @Override
    public IGeometry copyGeometry() {
        return GeometryUtils.copyGeometryData(new GPath(awtPath), this);
    }

    /**
     * @return a copy of this GPath without any SEG_CLOSE segments, to help when combining paths together as JTS has low tolerance for missing move / close pairs
     */
    public GPath copyOpenPath(){
        GPath path = new GPath();
        addToPath(true, path);
        return (GPath) GeometryUtils.copyGeometryData(path, this);
    }


    public void markPathDirty(){
        vertexCount = -1;
    }


    public static void move(GPath gPath, float[] coords, int type){
        switch (type){
            case PathIterator.SEG_MOVETO:
                gPath.moveTo(coords[0], coords[1]);
                break;
            case PathIterator.SEG_LINETO:
                gPath.lineTo(coords[0], coords[1]);
                break;
            case PathIterator.SEG_QUADTO:
                gPath.quadTo(coords[0], coords[1], coords[2], coords[3]);
                break;
            case PathIterator.SEG_CUBICTO:
                gPath.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                break;
            case PathIterator.SEG_CLOSE:
                gPath.closePath();
                break;
        }
    }

    @Override
    public void addToPath(boolean addMove, GPath path) {
        PathIterator iterator = awtPath.getPathIterator(null);
        float[] coords = new float[6];

        float lastMoveX = 0;
        float lastMoveY = 0;
        boolean hasMove = false;

        while (!iterator.isDone()){
            int type = iterator.currentSegment(coords);

            if(type == PathIterator.SEG_MOVETO){
                lastMoveX = coords[0];
                lastMoveY = coords[1];
                if(!hasMove && !addMove){
                    hasMove = true;
                }else{
                    hasMove = true;
                    move(path, coords, type);
                }
            }else if(type != PathIterator.SEG_CLOSE){
                move(path, coords, type);
            }else if(hasMove){
                move(path, new float[]{lastMoveX, lastMoveY}, PathIterator.SEG_LINETO);
            }
            iterator.next();
        }

    }

    //// Convenience Methods \\\\

    public void moveTo(double x1, double y1) {
        awtPath.moveTo(x1, y1);
    }

    public void lineTo(double x1, double y1) {
        awtPath.lineTo(x1, y1);
    }

    public void quadTo(double ctrlX, double ctrlY, double x2, double y2) {
        awtPath.quadTo(ctrlX, ctrlY, x2, y2);
    }

    public void curveTo(double ctrlX1, double ctrlY1, double ctrlX2, double ctrlY2, double x3, double y3) {
        awtPath.curveTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, x3, y3);
    }

    public void closePath(){
        awtPath.closePath();
    }

    public void append(Shape nextGPath, boolean connect){
        awtPath.append(nextGPath, connect);
    }

    public void append(PathIterator iterator, boolean connect){
        awtPath.append(iterator, connect);
    }

}