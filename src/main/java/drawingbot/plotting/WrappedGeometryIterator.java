package drawingbot.plotting;

import drawingbot.api.IGeometryFilter;
import drawingbot.geom.shapes.IGeometry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WrappedGeometryIterator extends AbstractGeometryIterator{

    private final Map<PlottedDrawing, AbstractGeometryIterator> iterators = new ConcurrentHashMap<>();
    private AbstractGeometryIterator currentIterator;

    public void addIterator(PlottedDrawing drawing, AbstractGeometryIterator iterator){
        iterators.put(drawing, iterator);
    }

    public void removeIterator(PlottedDrawing drawing){
        iterators.remove(drawing);
    }

    public void resetIterator(PlottedDrawing drawing){
        AbstractGeometryIterator iterator = iterators.get(drawing);
        if(iterator != null){
            iterator.reset();
        }
    }

    @Override
    public void setVertexLimit(int vertexLimit) {
        super.setVertexLimit(vertexLimit);
        iterators.values().forEach(i -> i.setVertexLimit(vertexLimit/iterators.size()));
    }

    @Override
    public void setGeometryFilter(IGeometryFilter geometryFilter) {
        super.setGeometryFilter(geometryFilter);
        iterators.values().forEach(i -> i.setGeometryFilter(geometryFilter));
    }

    @Override
    protected boolean hasNextInternal() {
        for(AbstractGeometryIterator iterator : iterators.values()){
            if(iterator.hasNext()){
                currentIterator = iterator;
                return true;
            }
        }
        return false;
    }

    @Override
    protected IGeometry nextInternal() {
        IGeometry geometry = currentIterator.next();
        updateFromGeometry(currentIterator.currentDrawing, geometry);
        return geometry;
    }

    @Override
    public void reset(){
        super.reset();
        iterators.values().forEach(AbstractGeometryIterator::reset);
    }

}