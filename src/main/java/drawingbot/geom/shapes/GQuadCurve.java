package drawingbot.geom.shapes;

import drawingbot.geom.GeometryUtils;
import drawingbot.pfm.helpers.BresenhamHelper;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.QuadCurve2D;

public class GQuadCurve extends QuadCurve2D.Float implements IGeometry, IPathElement {

    public GQuadCurve(){}

    public GQuadCurve(float x1, float y1, float ctrlx, float ctrly, float x2, float y2) {
        super(x1, y1, ctrlx, ctrly, x2, y2);
    }

    @Override
    public void addToPath(boolean addMove, GPath path) {
        if(addMove){
            path.moveTo(x1, y1);
        }
        path.quadTo(ctrlx, ctrly, x2, y2);
    }

    //// IGeometry \\\\

    public int geometryIndex = -1;
    public int pfmPenIndex = -1;
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
    public int getPFMPenIndex() {
        return pfmPenIndex;
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
    public void setPFMPenIndex(int index) {
        pfmPenIndex = index;
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
    public void renderFX(GraphicsContext graphics) {
        graphics.beginPath();
        graphics.moveTo(x1, y1);
        graphics.quadraticCurveTo(ctrlx, ctrly, x2, y2);
        graphics.stroke();
    }

    @Override
    public void renderBresenham(BresenhamHelper helper, BresenhamHelper.IPixelSetter setter) {
        helper.plotQuadBezier((int)x1, (int)y1, (int)ctrlx, (int)ctrly, (int)x2, (int)y2, setter);
    }

    @Override
    public IGeometry transformGeometry(AffineTransform transform) {
        float[] coords = new float[]{x1, y1, ctrlx, ctrly, x2, y2};
        transform.transform(coords, 0, coords, 0, 3);
        setCurve(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
        return this;
    }

    @Override
    public String serializeData() {
        return GeometryUtils.serializeCoords(new float[]{x1, y1, ctrlx, ctrly, x2, y2});
    }

    @Override
    public void deserializeData(String geometryData) {
        float[] coords = GeometryUtils.deserializeCoords(geometryData);
        setCurve(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
    }

    @Override
    public Coordinate getOriginCoordinate() {
        return new CoordinateXY(x1, y1);
    }

    @Override
    public IGeometry copyGeometry() {
        return GeometryUtils.copyGeometryData(new GQuadCurve(x1, y1, ctrlx, ctrly, x2, y2), this);
    }
}
