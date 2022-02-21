package drawingbot.plotting;

import drawingbot.geom.shapes.IGeometry;

/**
 * An implementation of the Geometry Iterator which can be used to perform Asynchronous rendering as the drawing is being generated
 */
public class AsynchronousGeometryIterator extends AbstractGeometryIterator {

    public final PlottedDrawing plottedDrawing;

    public AsynchronousGeometryIterator(PlottedDrawing plottedDrawing) {
        this.plottedDrawing = plottedDrawing;
    }

    @Override
    protected boolean hasNextInternal() {
        return currentGeometryCount < plottedDrawing.getGeometryCount();
    }

    @Override
    protected IGeometry nextInternal() {
        IGeometry next = plottedDrawing.geometries.get(currentGeometryCount);
        updateFromGeometry(plottedDrawing, next);
        return next;
    }
}