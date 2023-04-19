package drawingbot.geom.shapes;

import drawingbot.geom.GeometryUtils;
import drawingbot.pfm.helpers.BresenhamHelper;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.QuadCurve2D;

public class GQuadCurve extends AbstractGeometry implements IPathElement {

    public QuadCurve2D.Float awtQuadCurve = null;

    public GQuadCurve(){
        this.awtQuadCurve = new QuadCurve2D.Float();
    }

    public GQuadCurve(float x1, float y1, float ctrlx, float ctrly, float x2, float y2) {
        this.awtQuadCurve = new QuadCurve2D.Float(x1, y1, ctrlx, ctrly, x2, y2);
    }

    @Override
    public void addToPath(boolean addMove, GPath path) {
        if(addMove){
            path.moveTo(getX1(), getY1());
        }
        path.quadTo(getCtrlX(), getCtrlY(), getX2(), getY2());
    }

    public float[] toFloatArray(){
        return new float[]{getX1(), getY1(), getCtrlX(), getCtrlY(), getX2(), getY2()};
    }

    @Override
    public Shape getAWTShape() {
        return awtQuadCurve;
    }

    @Override
    public void renderFX(GraphicsContext graphics) {
        graphics.beginPath();
        graphics.moveTo(getX1(), getY1());
        graphics.quadraticCurveTo(getCtrlX(), getCtrlY(), getX2(), getY2());
        graphics.stroke();
    }

    @Override
    public void renderBresenham(BresenhamHelper helper, BresenhamHelper.IPixelSetter setter) {
        helper.plotQuadBezier((int)getX1(), (int)getY1(), (int)getCtrlX(), (int)getCtrlY(), (int)getX2(), (int)getY2(), setter);
    }

    @Override
    public IGeometry transformGeometry(AffineTransform transform) {
        float[] coords = toFloatArray();
        transform.transform(coords, 0, coords, 0, 3);
        awtQuadCurve.setCurve(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
        return this;
    }

    @Override
    public String serializeData() {
        return GeometryUtils.serializeCoords(toFloatArray());
    }

    @Override
    public void deserializeData(String geometryData) {
        float[] coords = GeometryUtils.deserializeCoords(geometryData);
        awtQuadCurve.setCurve(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
    }

    //// Coordinates \\\\

    public float getX1() {
        return awtQuadCurve.x1;
    }

    public float getY1() {
        return awtQuadCurve.y1;
    }

    public float getCtrlX() {
        return awtQuadCurve.ctrlx;
    }

    public float getCtrlY() {
        return awtQuadCurve.ctrly;
    }

    public float getX2() {
        return awtQuadCurve.x2;
    }

    public float getY2() {
        return awtQuadCurve.y2;
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
        return GeometryUtils.copyGeometryData(new GQuadCurve(getX1(), getY1(), getCtrlX(), getCtrlY(), getX2(), getY2()), this);
    }
}
