package drawingbot.plotting;

import drawingbot.api.IGeometryFilter;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.javafx.observables.ObservableDrawingPen;

import java.util.Iterator;

/**
 * Implementations of this iterator should update the "context" needed to render the geometry.
 * Including the current drawing, group, pen, filtering and the geometry itself will be handled in this abstract
 *
 * This implementation also allows for Geometry Filters and vertex render limits
 */
public abstract class AbstractGeometryIterator implements Iterator<IGeometry> {

    //the current geometry
    public PlottedDrawing currentDrawing;
    public PlottedGroup currentGroup;
    public ObservableDrawingPen currentPen;
    public IGeometry currentGeometry;
    public boolean currentFilterResult;

    //the last geometry which passed the geometry filter: may be null
    public PlottedDrawing lastDrawing;
    public PlottedGroup lastGroup;
    public ObservableDrawingPen lastPen;
    public IGeometry lastGeometry;

    protected int currentGeometryCount = 0; //the number of geometries which have been iterated, since the last reset
    protected long currentVertexCount = 0; //the number of vertices which have been iterated since the last reset, or the last denial in hasNext()
    protected int vertexLimit = 0; //the current vertex limit, see setVertexRenderLimit
    protected IGeometryFilter filter = IGeometryFilter.BYPASS_FILTER; //should not be null, see setGeometryFilter

    /**
     * The iterator will return false from {@link #hasNext()} when the limit has been exceeded.
     * When the iterator exceeds the vertex limit during {@link #hasNext()} it will reset the count, meaning the next call to {@link #hasNext()} could be true
     * A vertex limit of 0 will be ignored.
     */
    public void setVertexLimit(int vertexLimit){
        this.vertexLimit = vertexLimit;
    }

    /**
     * If you are using a {@link IGeometryFilter}, geometries which fail the filter will still be passed by the iterator, you should check getPassedFilter()
     * Geometries which fail the filter will not be included in vertex render limit checks, and the iterator treats them as "unused"
     */
    public void setGeometryFilter(IGeometryFilter geometryFilter){
        this.filter = geometryFilter;
    }


    @Override
    public final boolean hasNext() {
        if (vertexLimit != 0 && currentVertexCount >= vertexLimit) {
            this.currentVertexCount = 0;
            return false;
        }
        return hasNextInternal();
    }

    @Override
    public final IGeometry next() {

        if(currentFilterResult) {
            lastGeometry = currentGeometry;
            lastDrawing = currentDrawing;
            lastGroup = currentGroup;
            lastPen = currentPen;
        }

        currentGeometry = nextInternal();
        currentFilterResult = filter.filter(currentDrawing, currentGeometry, currentPen);
        if(vertexLimit != 0 && currentFilterResult){
            currentVertexCount += currentGeometry.getVertexCount();
        }
        currentGeometryCount++;
        return currentGeometry;
    }

    public void updateFromGeometry(PlottedDrawing plottedDrawing, IGeometry geometry){
        currentDrawing = plottedDrawing;
        currentGroup = currentDrawing.getPlottedGroup(geometry.getGroupID());
        currentPen = currentGroup.drawingSet.getPen(geometry.getPenIndex());
        currentGeometry = geometry;
    }

    public void updateFromGeometry(PlottedDrawing drawing, PlottedGroup group, ObservableDrawingPen pen, IGeometry geometry){
        currentDrawing = drawing;
        currentGroup = group;
        currentPen = pen;
        currentGeometry = geometry;
    }

    /**
     * Should not be called outside of {@link AbstractGeometryIterator}
     */
    protected abstract boolean hasNextInternal();

    /**
     * Should not be called outside of {@link AbstractGeometryIterator}
     */
    protected abstract IGeometry nextInternal();

    public void reset(){
        currentGeometry = lastGeometry = null;
        currentDrawing = lastDrawing = null;
        currentGroup = lastGroup = null;
        currentPen = lastPen = null;
        currentFilterResult = false;
        currentVertexCount = 0;
        currentGeometryCount = 0;
    }

    public float getCurrentGeometryProgress(){
        return (float) getCurrentGeometryCount() / getTotalGeometryCount();
    }

    public int getCurrentGeometryCount(){
        return currentGeometryCount;
    }

    public int getTotalGeometryCount(){
        return currentDrawing.getGeometryCount();
    }

    public float getCurrentVertexProgress(){
        return (float) getCurrentVertexCount() / getTotalVertexCount();
    }

    public long getCurrentVertexCount(){
        return currentVertexCount;
    }

    public long getTotalVertexCount(){
        return currentDrawing.getVertexCount();
    }

}