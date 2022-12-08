package drawingbot.geom.shapes;

import javafx.scene.shape.Shape;

public interface JFXGeometryConverter {

    boolean canConvert(IGeometry geometry);

    Shape convert(IGeometry geometry);

    boolean canUpdate(Shape shape);

    void update(Shape shape, IGeometry geometry);

}