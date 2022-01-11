package drawingbot.geom.basic;

import drawingbot.geom.PathBuilder;
import drawingbot.javafx.observables.ObservableDrawingPen;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;

public class GCubicCurve extends CubicCurve2D.Float implements IGeometry, IPathElement {

    public GCubicCurve(float[] p0, float[] p1, float[] p2, float[] p3) {
        setCurve(p0[0], p0[1], p1[0], p1[1], p2[0], p2[1], p3[0], p3[1]);
    }

    public GCubicCurve(float[] p0, float[] p1, float[] p2, float[] p3, float catmullTension) {
        float[][] bezier = PathBuilder.catmullToBezier(new float[][]{p0, p1, p2, p3}, new float[4][2], catmullTension);
        setCurve(bezier[0][0], bezier[0][1], bezier[1][0], bezier[1][1], bezier[2][0], bezier[2][1], bezier[3][0], bezier[3][1]);
    }

    public GCubicCurve(int x1, int y1, int ctrl1X, int ctrl1Y, int ctrl2X, int ctrl2Y, int x2, int y2) {
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
    public Integer penIndex = null;
    public Integer sampledRGBA = null;
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
    public Integer getGeometryIndex() {
        return geometryIndex;
    }

    @Override
    public Integer getPenIndex() {
        return penIndex;
    }

    @Override
    public Integer getCustomRGBA() {
        return sampledRGBA;
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
        x1 = coords[0];
        y1 = coords[1];
        ctrlx1 = coords[2];
        ctrly1 = coords[3];
        ctrlx2 = coords[4];
        ctrly2 = coords[5];
        x2 = coords[6];
        y2 = coords[7];
    }

    @Override
    public Coordinate getOriginCoordinate() {
        return new CoordinateXY(x1, y1);
    }
}
