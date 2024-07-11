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
        MoveTo lastMoveTo = null;
        PathElement lastElement = null;

        while (!iterator.isDone()) {
            int segType = iterator.currentSegment(coords);
            switch (segType) {
                case PathIterator.SEG_MOVETO:
                    path.getElements().add(lastMoveTo = new MoveTo(coords[0], coords[1]));
                    break;
                case PathIterator.SEG_LINETO:
                    path.getElements().add(lastElement = new LineTo(coords[0], coords[1]));
                    break;
                case PathIterator.SEG_QUADTO:
                    path.getElements().add(lastElement = new QuadCurveTo(coords[0], coords[1], coords[2], coords[3]));
                    break;
                case PathIterator.SEG_CUBICTO:
                    path.getElements().add(lastElement = new CubicCurveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]));
                    break;
                case PathIterator.SEG_CLOSE:
                    if(lastMoveTo != null){
                        if(lastElement instanceof LineTo lineTo && lineTo.getX() == lastMoveTo.getX() && lineTo.getY() == lastMoveTo.getY()){
                            lineTo.xProperty().bindBidirectional(lastMoveTo.xProperty());
                            lineTo.yProperty().bindBidirectional(lastMoveTo.yProperty());
                        }else if(lastElement instanceof QuadCurveTo quadTo && quadTo.getX() == lastMoveTo.getX() && quadTo.getY() == lastMoveTo.getY()){
                            quadTo.xProperty().bindBidirectional(lastMoveTo.xProperty());
                            quadTo.yProperty().bindBidirectional(lastMoveTo.yProperty());
                        }else if(lastElement instanceof CubicCurveTo cubicTo && cubicTo.getX() == lastMoveTo.getX() && cubicTo.getY() == lastMoveTo.getY()){
                            cubicTo.xProperty().bindBidirectional(lastMoveTo.xProperty());
                            cubicTo.yProperty().bindBidirectional(lastMoveTo.yProperty());
                        }else{
                            LineTo lineTo = new LineTo();
                            lineTo.xProperty().bindBidirectional(lastMoveTo.xProperty());
                            lineTo.yProperty().bindBidirectional(lastMoveTo.yProperty());
                            path.getElements().add(lineTo);
                        }
                    }
                    path.getElements().add(new ClosePath());

                    break;
            }
            iterator.next();
        }
    }

}
