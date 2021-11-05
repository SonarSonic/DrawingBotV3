package drawingbot.geom.basic;

import drawingbot.javafx.observables.ObservableDrawingPen;
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

    public Integer penIndex = null;
    public Integer sampledRGBA = null;

    @Override
    public int getSegmentCount() {
        return 6;
    }

    @Override
    public Shape getAWTShape() {
        return this;
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
    public void setPenIndex(Integer index) {
        penIndex = index;
    }

    @Override
    public void setCustomRGBA(Integer rgba) {
        sampledRGBA = rgba;
    }

    @Override
    public void renderFX(GraphicsContext graphics, ObservableDrawingPen pen) {
        pen.preRenderFX(graphics, this);
        graphics.strokeOval(x, y, width, height);
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
            graphics.setColor(pen.getAWTColor(getCustomRGBA()));
            graphics.fill(getAWTShape());
        }

        @Override
        public void renderFX(GraphicsContext graphics, ObservableDrawingPen pen) {
            graphics.setLineWidth(pen.getStrokeSize());
            graphics.setFill(pen.getFXColor(getCustomRGBA()));
            graphics.fillOval(x, y, width, height);
        }
    }
}
