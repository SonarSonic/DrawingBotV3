package drawingbot.geom.converters;

import drawingbot.geom.shapes.GEllipse;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.geom.shapes.JFXGeometryConverter;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Shape;

public class JFXEllipseConverter implements JFXGeometryConverter {

    @Override
    public boolean canConvert(IGeometry geometry) {
        return geometry instanceof GEllipse;
    }

    @Override
    public Shape convert(IGeometry geometry) {
        GEllipse line = (GEllipse) geometry;
        Ellipse jfxEllipse = new Ellipse();
        update(jfxEllipse, line);
        return jfxEllipse;
    }

    @Override
    public boolean canUpdate(Shape shape) {
        return shape instanceof Ellipse;
    }

    @Override
    public void update(Shape shape, IGeometry geometry) {
        GEllipse ellipse = (GEllipse) geometry;
        Ellipse jfxEllipse = (Ellipse) shape;
        jfxEllipse.setCenterX(ellipse.getCenterX());
        jfxEllipse.setCenterY(ellipse.getCenterY());
        jfxEllipse.setRadiusX(ellipse.getWidth()/2);
        jfxEllipse.setRadiusY(ellipse.getHeight()/2);
    }
}
