package drawingbot.geom.converters;

import drawingbot.geom.shapes.GRectangle;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.geom.shapes.JFXGeometryConverter;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class JFXRectangleConverter implements JFXGeometryConverter {

    @Override
    public boolean canConvert(IGeometry geometry) {
        return geometry instanceof GRectangle;
    }

    @Override
    public Shape convert(IGeometry geometry) {
        GRectangle rect = (GRectangle) geometry;
        Rectangle jfxRectangle = new Rectangle();
        update(jfxRectangle, rect);
        return jfxRectangle;
    }

    @Override
    public boolean canUpdate(Shape shape) {
        return shape instanceof Rectangle;
    }

    @Override
    public void update(Shape shape, IGeometry geometry) {
        GRectangle rect = (GRectangle) geometry;
        Rectangle jfxRectangle = (Rectangle) shape;
        jfxRectangle.setX(rect.getX());
        jfxRectangle.setY(rect.getY());
        jfxRectangle.setWidth(rect.getWidth());
        jfxRectangle.setHeight(rect.getHeight());
    }
}
