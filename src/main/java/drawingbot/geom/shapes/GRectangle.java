package drawingbot.geom.shapes;

import drawingbot.geom.GeometryUtils;
import drawingbot.pfm.helpers.BresenhamHelper;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class GRectangle extends AbstractGeometry implements IGeometry {

    public Rectangle2D.Float awtRect;

    public GRectangle() {
        this.awtRect = new Rectangle2D.Float();
    }

    public GRectangle(float x, float y, float w, float h) {
        this.awtRect = new Rectangle2D.Float(x, y, w, h);
    }

    @Override
    public int getVertexCount() {
        return 4;
    }

    @Override
    public Shape getAWTShape() {
        return awtRect;
    }

    @Override
    public void renderFX(GraphicsContext graphics) {
        graphics.strokeRect(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void renderBresenham(BresenhamHelper helper, BresenhamHelper.IPixelSetter setter) {
        helper.plotLine((int)getX(), (int)getY(), (int)(getEndX()), (int)getY(), setter);
        helper.plotLine((int)getX(), (int)(getEndY()), (int)(getEndX()), (int)(getEndY()), setter);
        helper.plotLine((int)getX(), (int)getY(), (int)getX(), (int)(getEndY()), setter);
        helper.plotLine((int)(getEndX()), (int)getY(), (int)(getEndX()), (int)(getEndY()), setter);
    }

    @Override
    public String serializeData() {
        return GeometryUtils.serializeCoords(new float[]{getX(), getY(), getEndX(), getEndY()});
    }

    @Override
    public void deserializeData(String geometryData) {
        float[] coords = GeometryUtils.deserializeCoords(geometryData);
        awtRect.setFrame(coords[0], coords[1], coords[2] - coords[0], coords[3] - coords[1]);
    }

    //// Coordinates \\\\

    public float getX() {
        return awtRect.x;
    }

    public float getY() {
        return awtRect.y;
    }

    public float getWidth() {
        return awtRect.width;
    }

    public float getHeight() {
        return awtRect.height;
    }

    public float getEndX() {
        return getX() + getWidth();
    }

    public float getEndY() {
        return getY() + getHeight();
    }

    @Override
    public Coordinate getOriginCoordinate() {
        return new CoordinateXY(getX(), getY());
    }

    @Override
    public Coordinate getEndCoordinate() {
        return new CoordinateXY(getX(), getY());
    }

    @Override
    public IGeometry copyGeometry() {
        return GeometryUtils.copyGeometryData(new GRectangle(getX(), getY(), getWidth(), getHeight()), this);
    }
}
