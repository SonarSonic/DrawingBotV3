package drawingbot.geom.basic;

import drawingbot.geom.GeometryUtils;
import drawingbot.geom.PathBuilder;
import drawingbot.javafx.observables.ObservableDrawingPen;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;

public class GCubicCurve extends CubicCurve2D.Float implements IGeometry, IPathElement {

    public GCubicCurve(){}

    public GCubicCurve(float[] p0, float[] p1, float[] p2, float[] p3) {
        setCurve(p0[0], p0[1], p1[0], p1[1], p2[0], p2[1], p3[0], p3[1]);
    }

    public GCubicCurve(float[] p0, float[] p1, float[] p2, float[] p3, float catmullTension) {
        float[][] bezier = PathBuilder.catmullToBezier(new float[][]{p0, p1, p2, p3}, new float[4][2], catmullTension);
        setCurve(bezier[0][0], bezier[0][1], bezier[1][0], bezier[1][1], bezier[2][0], bezier[2][1], bezier[3][0], bezier[3][1]);
    }

    public GCubicCurve(float x1, float y1, float ctrl1X, float ctrl1Y, float ctrl2X, float ctrl2Y, float x2, float y2) {
        setCurve(x1, y1, ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, x2, y2);
    }

    @Override
    public void addToPath(boolean addMove, GPath path) {
        if(addMove){
            path.moveTo(x1, y1);
        }
        path.curveTo(ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2);
    }

    //// IGeometry \\\\

    public int geometryIndex = -1;
    public int penIndex = -1;
    public int sampledRGBA = -1;
    public int groupID = -1;

    @Override
    public int getVertexCount() {
        return 4;
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
        graphics.beginPath();
        graphics.moveTo(x1, y1);
        graphics.bezierCurveTo(ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2);
        graphics.stroke();
    }

    @Override
    public void transform(AffineTransform transform) {
        float[] coords = new float[]{x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2};
        transform.transform(coords, 0, coords, 0, 4);
        setCurve(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], coords[6], coords[7]);
    }

    @Override
    public String serializeData() {
        return GeometryUtils.serializeCoords(new float[]{x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2});
    }

    @Override
    public void deserializeData(String geometryData) {
        float[] coords = GeometryUtils.deserializeCoords(geometryData);
        setCurve(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], coords[6], coords[7]);
    }

    @Override
    public Coordinate getOriginCoordinate() {
        return new CoordinateXY(x1, y1);
    }
}
