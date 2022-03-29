package drawingbot.geom.operation;

import drawingbot.geom.shapes.IGeometry;
import drawingbot.plotting.PlottedDrawing;

import java.awt.geom.AffineTransform;

public class GeometryOperationTransform extends AbstractGeometryOperation{

    public AffineTransform transform;

    public GeometryOperationTransform(AffineTransform transform){
        this.transform = transform;
    }

    @Override
    public PlottedDrawing run(PlottedDrawing originalDrawing) {
        PlottedDrawing transformedDrawing = createPlottedDrawing(originalDrawing);
        for(IGeometry geometry : originalDrawing.geometries){
            transformedDrawing.addGeometry(geometry.transformGeometry(transform));
        }
        return transformedDrawing;
    }

    @Override
    public boolean isDestructive() {
        return false;
    }
}
