package drawingbot.geom.shapes;

import drawingbot.geom.GeometryUtils;
import drawingbot.pfm.helpers.BresenhamHelper;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

public class GLine extends AbstractGeometry implements IGeometry, IPathElement {

    public Line2D.Float awtLine;

    public GLine() {
        this.awtLine = new Line2D.Float();
    }

    public GLine(float x1, float y1, float x2, float y2) {
        this.awtLine = new Line2D.Float(x1, y1, x2, y2);
    }

    public GLine(Coordinate p1, Coordinate p2) {
        this((float)p1.x, (float)p1.y, (float)p2.x, (float)p2.y);
    }

    @Override
    public void addToPath(boolean addMove, GPath path) {
        if(addMove){
            path.moveTo(getX1(), getY1());
        }
        path.lineTo(getX2(), getY2());
    }

    @Override
    public int getVertexCount() {
        return 2;
    }

    @Override
    public Shape getAWTShape() {
        return awtLine;
    }

    @Override
    public void renderFX(GraphicsContext graphics) {
        graphics.strokeLine(getX1(), getY1(), getX2(), getY2());
    }

    @Override
    public void renderBresenham(BresenhamHelper helper, BresenhamHelper.IPixelSetter setter) {
        helper.plotLine((int)getX1(), (int)getY1(), (int)getX2(), (int)getY2(), setter);
    }

    @Override
    public IGeometry transformGeometry(AffineTransform transform) {
        float[] coords = new float[]{getX1(), getY1(), getX2(), getY2()};
        transform.transform(coords, 0, coords, 0, 2);
        awtLine.setLine(coords[0], coords[1], coords[2], coords[3]);
        return this;
    }

    @Override
    public String serializeData() {
        return GeometryUtils.serializeCoords(new float[]{getX1(), getY1(), getX2(), getY2()});
    }

    @Override
    public void deserializeData(String geometryData) {
        float[] coords = GeometryUtils.deserializeCoords(geometryData);
        awtLine.setLine(coords[0], coords[1], coords[2], coords[3]);
    }

    //// Coordinates \\\\

    public float getX1() {
        return awtLine.x1;
    }

    public float getY1() {
        return awtLine.y1;
    }

    public float getX2() {
        return awtLine.x2;
    }

    public float getY2() {
        return awtLine.y2;
    }

    @Override
    public Coordinate getOriginCoordinate() {
        return new CoordinateXY(getX1(), getY1());
    }

    @Override
    public Coordinate getEndCoordinate() {
        return new CoordinateXY(getX2(), getY2());
    }

    @Override
    public IGeometry copyGeometry() {
        return GeometryUtils.copyGeometryData(new GLine(getX1(), getY1(), getX2(), getY2()), this);
    }

}
