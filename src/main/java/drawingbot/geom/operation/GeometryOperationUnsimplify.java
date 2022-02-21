package drawingbot.geom.operation;

import drawingbot.geom.GeometryUtils;
import drawingbot.geom.shapes.GPath;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.plotting.PlottedDrawing;

public class GeometryOperationUnsimplify extends AbstractGeometryOperation{

    @Override
    public PlottedDrawing run(PlottedDrawing originalDrawing) {
        PlottedDrawing newDrawing = createPlottedDrawing(originalDrawing);

        for(IGeometry geometry : originalDrawing.geometries){
            if(geometry instanceof GPath){
                GeometryUtils.splitGPath((GPath) geometry, newDrawing::addGeometry);
            }else{
                newDrawing.addGeometry(geometry);
            }
        }
        return newDrawing;
    }

    @Override
    public boolean isDestructive() {
        return false;
    }
}
