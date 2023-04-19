package drawingbot.geom.converters;

import drawingbot.geom.shapes.GLine;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.geom.shapes.JFXGeometryConverter;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

public class JFXLineConverter implements JFXGeometryConverter {

    @Override
    public boolean canConvert(IGeometry geometry) {
        return geometry instanceof GLine;
    }

    @Override
    public Shape convert(IGeometry geometry) {
        GLine line = (GLine) geometry;
        Line jfxLine = new Line();
        update(jfxLine, line);
        return jfxLine;
    }

    @Override
    public boolean canUpdate(Shape shape) {
        return shape instanceof Line;
    }

    @Override
    public void update(Shape shape, IGeometry geometry) {
        GLine line = (GLine) geometry;
        Line jfxLine = (Line) shape;
        jfxLine.setStartX(line.getX1());
        jfxLine.setStartY(line.getY1());
        jfxLine.setEndX(line.getX2());
        jfxLine.setEndY(line.getY2());
    }
}
