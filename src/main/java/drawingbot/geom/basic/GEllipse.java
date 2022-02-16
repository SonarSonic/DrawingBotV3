package drawingbot.geom.basic;

import drawingbot.geom.GeometryUtils;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.pfm.helpers.BresenhamHelper;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

public class GEllipse extends Ellipse2D.Float implements IGeometry {

    public GEllipse() {
        super();
    }

    public GEllipse(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    //// IGeometry \\\\

    public int geometryIndex = -1;
    public int pfmPenIndex = -1;
    public int penIndex = -1;
    public int sampledRGBA = -1;
    public int groupID = -1;

    @Override
    public int getVertexCount() {
        return 6;
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
    public void setPFMPenIndex(int index) {
        pfmPenIndex = index;
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
        graphics.strokeOval(x, y, width, height);
    }

    @Override
    public void renderBresenham(BresenhamHelper helper, BresenhamHelper.IPixelSetter setter) {
        helper.plotEllipseRect((int)x, (int)y, (int)(x + width), (int)(y + height), setter);
    }

    @Override
    public void transform(AffineTransform transform) {
        float[] coords = new float[]{x, y, x + width, y + height};
        transform.transform(coords, 0, coords, 0, 2);
        setFrame(coords[0], coords[1], coords[2] - x, coords[3] - y);
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

    public static class Filled extends GEllipse{
        public Filled() {
            super();
        }

        public Filled(float x, float y, float w, float h) {
            super(x, y, w, h);
        }

        @Override
        public void renderAWT(Graphics2D graphics, ObservableDrawingPen pen) {
            graphics.setStroke(pen.getAWTStroke());
            graphics.setColor(pen.getAWTColor(getSampledRGBA()));
            graphics.fill(getAWTShape());
        }

        @Override
        public void renderFX(GraphicsContext graphics, ObservableDrawingPen pen) {
            graphics.setLineWidth(pen.getStrokeSize());
            graphics.setFill(pen.getFXColor(getSampledRGBA()));
            graphics.fillOval(x, y, width, height);
        }
    }
}
