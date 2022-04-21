package drawingbot.geom.shapes;

import drawingbot.geom.GeometryUtils;
import drawingbot.pfm.helpers.BresenhamHelper;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class GLine extends Line2D.Float implements IGeometry, IPathElement {

    public GLine() {
        super();
    }

    public GLine(float x1, float y1, float x2, float y2) {
        super(x1, y1, x2, y2);
    }

    public GLine(Point2D p1, Point2D p2) {
        super(p1, p2);
    }

    @Override
    public void addToPath(boolean addMove, GPath path) {
        if(addMove){
            path.moveTo(x1, y1);
        }
        path.lineTo(x2, y2);
    }

    //// IGeometry \\\\

    public int geometryIndex = -1;
    public int pfmPenIndex = -1;
    public int penIndex = -1;
    public int sampledRGBA = -1;
    public int groupID = -1;
    public int fillType = -1;

    @Override
    public int getVertexCount() {
        return 2;
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
    public int getFillType(){
        return fillType;
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
    public void setFillType(int fillType) {
        this.fillType = fillType;
    }

    @Override
    public void renderFX(GraphicsContext graphics) {
        graphics.strokeLine(x1, y1, x2, y2);
    }

    @Override
    public void renderBresenham(BresenhamHelper helper, BresenhamHelper.IPixelSetter setter) {
        helper.plotLine((int)x1, (int)y1, (int)x2, (int)y2, setter);
    }

    @Override
    public IGeometry transformGeometry(AffineTransform transform) {
        float[] coords = new float[]{x1, y1, x2, y2};
        transform.transform(coords, 0, coords, 0, 2);
        setLine(coords[0], coords[1], coords[2], coords[3]);
        return this;
    }

    @Override
    public String serializeData() {
        return GeometryUtils.serializeCoords(new float[]{x1, y1, x2, y2});
    }

    @Override
    public void deserializeData(String geometryData) {
        float[] coords = GeometryUtils.deserializeCoords(geometryData);
        setLine(coords[0], coords[1], coords[2], coords[3]);
    }

    @Override
    public Coordinate getOriginCoordinate() {
        return new CoordinateXY(x1, y1);
    }

    @Override
    public IGeometry copyGeometry() {
        return GeometryUtils.copyGeometryData(new GLine(x1, y1, x2, y2), this);
    }
}
