package drawingbot.geom.shapes;

import drawingbot.geom.GeometryUtils;
import drawingbot.plotting.PathBuilder;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;

public class GCubicCurve extends AbstractGeometry implements IGeometry, IPathElement {

    public CubicCurve2D.Float awtCubicCurve = null;

    public GCubicCurve(){
        this.awtCubicCurve = new CubicCurve2D.Float();
    }

    public GCubicCurve(float x1, float y1, float ctrl1X, float ctrl1Y, float ctrl2X, float ctrl2Y, float x2, float y2) {
        this.awtCubicCurve = new CubicCurve2D.Float(x1, y1, ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, x2, y2);
    }

    public GCubicCurve(float[] coords) {
        this(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], coords[6], coords[7]);
    }

    public GCubicCurve(float[] p0, float[] p1, float[] p2, float[] p3) {
        this(p0[0], p0[1], p1[0], p1[1], p2[0], p2[1], p3[0], p3[1]);
    }

    public GCubicCurve(float[] p0, float[] p1, float[] p2, float[] p3, float catmullTension) {
        float[][] bezier = PathBuilder.catmullToBezier(new float[][]{p0, p1, p2, p3}, new float[4][2], catmullTension);
        setCurve(bezier[0][0], bezier[0][1], bezier[1][0], bezier[1][1], bezier[2][0], bezier[2][1], bezier[3][0], bezier[3][1]);
    }

    public GCubicCurve(GQuadCurve curve){
        float ctrlx1 = (curve.getX1() + 2 * curve.getCtrlX()) / 3;
        float ctrly1 = (curve.getY1() + 2 * curve.getCtrlY()) / 3;

        float ctrlx2 = (curve.getX2() + 2 * curve.getCtrlX()) / 3;
        float ctrly2 = (curve.getY2() + 2 * curve.getCtrlY()) / 3;
        setCurve(curve.getX2(), curve.getY1(), ctrlx1, ctrly1, ctrlx2, ctrly2, curve.getX2(), curve.getY2());
        GeometryUtils.copyGeometryData(this, curve);
    }

    public GCubicCurve(GLine line){
        setCurve(line.getX1(), line.getY1(), line.getX1(), line.getY1(), line.getX2(), line.getY2(), line.getX2(), line.getY2());
        GeometryUtils.copyGeometryData(this, line);
    }

    private void setCurve(float x1, float y1, float ctrl1X, float ctrl1Y, float ctrl2X, float ctrl2Y, float x2, float y2) {
        this.awtCubicCurve = new CubicCurve2D.Float(x1, y1, ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, x2, y2);
    }

    @Override
    public void addToPath(boolean addMove, GPath path) {
        if(addMove){
            path.moveTo(getX1(), getY1());
        }
        path.curveTo(getCtrlX1(), getCtrlY1(), getCtrlX2(), getCtrlY2(), getX2(), getY2());
    }

    public float[] toFloatArray(){
        return new float[]{getX1(), getY1(), getCtrlX1(), getCtrlY1(), getCtrlX2(), getCtrlY2(), getX2(), getY2()};
    }

    @Override
    public int getVertexCount() {
        return 4;
    }

    @Override
    public Shape getAWTShape() {
        return awtCubicCurve;
    }

    @Override
    public void renderFX(GraphicsContext graphics) {
        graphics.beginPath();
        graphics.moveTo(getX1(), getY1());
        graphics.bezierCurveTo(getCtrlX1(), getCtrlY1(), getCtrlX2(), getCtrlY2(), getX2(), getY2());
        graphics.stroke();
    }

    @Override
    public IGeometry transformGeometry(AffineTransform transform) {
        float[] coords = toFloatArray();
        transform.transform(coords, 0, coords, 0, 4);
        setCurve(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], coords[6], coords[7]);
        return this;
    }

    @Override
    public String serializeData() {
        return GeometryUtils.serializeCoords(toFloatArray());
    }

    @Override
    public void deserializeData(String geometryData) {
        float[] coords = GeometryUtils.deserializeCoords(geometryData);
        setCurve(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], coords[6], coords[7]);
    }

    //// Coordinates \\\\

    public float getX1() {
        return awtCubicCurve.x1;
    }

    public float getY1() {
        return awtCubicCurve.y1;
    }

    public float getCtrlX1() {
        return awtCubicCurve.ctrlx1;
    }

    public float getCtrlY1() {
        return awtCubicCurve.ctrly1;
    }

    public float getX2() {
        return awtCubicCurve.x2;
    }

    public float getY2() {
        return awtCubicCurve.y2;
    }

    public float getCtrlX2() {
        return awtCubicCurve.ctrlx2;
    }

    public float getCtrlY2() {
        return awtCubicCurve.ctrly2;
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
        return GeometryUtils.copyGeometryData(new GCubicCurve(toFloatArray()), this);
    }
}
