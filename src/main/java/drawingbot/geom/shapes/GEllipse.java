package drawingbot.geom.shapes;

import drawingbot.geom.GeometryUtils;
import drawingbot.pfm.helpers.BresenhamHelper;
import javafx.scene.canvas.GraphicsContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class GEllipse extends AbstractGeometry implements IGeometry {

    public Ellipse2D.Float awtEllipse;

    public GEllipse() {
        this.awtEllipse = new Ellipse2D.Float();
    }

    public GEllipse(float x, float y, float w, float h) {
        this.awtEllipse = new Ellipse2D.Float(x, y, w, h);
    }

    @Override
    public int getVertexCount() {
        return 6;
    }

    @Override
    public Shape getAWTShape() {
        return awtEllipse;
    }

    @Override
    public void renderFX(GraphicsContext graphics) {
        graphics.strokeOval(awtEllipse.x, awtEllipse.y, awtEllipse.width, awtEllipse.height);
        if(fillType == 0){
            graphics.fillOval(awtEllipse.x, awtEllipse.y, awtEllipse.width, awtEllipse.height);
        }
    }

    @Override
    public void renderBresenham(BresenhamHelper helper, BresenhamHelper.IPixelSetter setter) {
        helper.plotEllipseRect((int)getX(), (int)getY(), (int)(getEndX()), (int)(getEndY()), setter);
    }

    @Override
    public String serializeData() {
        return GeometryUtils.serializeCoords(new float[]{getX(), getY(), getEndX(), getEndY()});
    }

    @Override
    public void deserializeData(String geometryData) {
        float[] coords = GeometryUtils.deserializeCoords(geometryData);
        awtEllipse.setFrame(coords[0], coords[1], coords[2] - coords[0], coords[3] - coords[1]);
    }

    //// Coordinates \\\\

    public float getX() {
        return awtEllipse.x;
    }

    public float getY() {
        return awtEllipse.y;
    }

    public float getWidth() {
        return awtEllipse.width;
    }

    public float getHeight() {
        return awtEllipse.height;
    }

    public float getEndX() {
        return getX() + getWidth();
    }

    public float getEndY() {
        return getY() + getHeight();
    }

    public double getCenterX() {
        return awtEllipse.getCenterX();
    }

    public double getCenterY() {
        return awtEllipse.getCenterY();
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
        return GeometryUtils.copyGeometryData(new GEllipse(getX(), getY(), getWidth(), getHeight()), this);
    }

}
