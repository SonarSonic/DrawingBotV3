package drawingbot.geom.basic;

import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.pfm.helpers.BresenhamHelper;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.AffineTransform;
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
    public Integer penIndex = null;
    public Integer sampledRGBA = null;
    public int groupID = -1;

    @Override
    public int getSegmentCount() {
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
    public void transform(AffineTransform transform) {
        float[] coords = new float[]{x, y, x + width, y + height};
        transform.transform(coords, 0, coords, 0, 2);
        x = coords[0];
        y = coords[1];
        width = coords[2] - x;
        height = coords[3] - y;
    }

    @Override
    public Coordinate getOriginCoordinate() {
        return new CoordinateXY(x, y);
    }
}
