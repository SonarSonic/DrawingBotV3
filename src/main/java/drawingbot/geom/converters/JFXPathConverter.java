package drawingbot.geom.converters;

import drawingbot.geom.shapes.IGeometry;
import drawingbot.geom.shapes.JFXGeometryConverter;
import javafx.scene.shape.*;

import java.awt.geom.PathIterator;

public class JFXPathConverter implements JFXGeometryConverter {

    @Override
    public boolean canConvert(IGeometry geometry) {
        return true;
    }

    @Override
    public Shape convert(IGeometry geometry) {
        Path path = new Path();
        update(path, geometry);
        return path;
    }

    @Override
    public boolean canUpdate(Shape shape) {
        return shape instanceof Path;
    }

    @Override
    public void update(Shape shape, IGeometry geometry) {
        Path path = (Path) shape;

        double[] coords = new double[6];
        path.getElements().clear();
        PathIterator iterator = geometry.getAWTShape().getPathIterator(null);
        while (!iterator.isDone()) {
            int segType = iterator.currentSegment(coords);
            switch (segType) {
                case PathIterator.SEG_MOVETO:
                    path.getElements().add(new MoveTo(coords[0], coords[1]));
                    break;
                case PathIterator.SEG_LINETO:
                    path.getElements().add(new LineTo(coords[0], coords[1]));
                    break;
                case PathIterator.SEG_QUADTO:
                    path.getElements().add(new QuadCurveTo(coords[0], coords[1], coords[2], coords[3]));
                    break;
                case PathIterator.SEG_CUBICTO:
                    path.getElements().add(new CubicCurveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]));
                    break;
                case PathIterator.SEG_CLOSE:
                    path.getElements().add(new ClosePath());
                    break;
            }
            iterator.next();
        }
    }

}
