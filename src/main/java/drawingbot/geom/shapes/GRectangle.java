package drawingbot.geom.shapes;

import drawingbot.geom.GeometryUtils;
import drawingbot.pfm.helpers.BresenhamHelper;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class GRectangle extends Rectangle2D.Float implements IGeometry {

    public GRectangle() {
        super();
    }

    public GRectangle(float x, float y, float w, float h) {
        super(x, y, w, h);
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
        graphics.strokeRect(x, y, width, height);
    }

    @Override
    public void renderBresenham(BresenhamHelper helper, BresenhamHelper.IPixelSetter setter) {
        helper.plotLine((int)x, (int)y, (int)(x + width), (int)y, setter);
        helper.plotLine((int)x, (int)(y + height), (int)(x + width), (int)(y + height), setter);
        helper.plotLine((int)x, (int)y, (int)(x), (int)(y + height), setter);
        helper.plotLine((int)(x + width), (int)y, (int)(x + width), (int)(y + height), setter);
    }

    @Override
    public String serializeData() {
        return GeometryUtils.serializeCoords(new float[]{x, y, x + width, y + height});
    }

    @Override
    public void deserializeData(String geometryData) {
        float[] coords = GeometryUtils.deserializeCoords(geometryData);
        setFrame(coords[0], coords[1], coords[2] - x, coords[3] - y);
    }

    @Override
    public Coordinate getOriginCoordinate() {
        return new CoordinateXY(x, y);
    }

    @Override
    public IGeometry copyGeometry() {
        return GeometryUtils.copyGeometryData(new GRectangle(x, y, width, height), this);
    }
}
