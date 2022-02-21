package drawingbot.plotting;

import drawingbot.geom.shapes.IGeometry;
import drawingbot.javafx.observables.ObservableDrawingPen;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Iterates through the geometries in the given PlottedDrawing in the order they should be rendered
 */
public class DrawingGeometryIterator extends AbstractGeometryIterator {

    public List<PlottedDrawing> drawings;
    public List<ObservableDrawingPen> pens;
    public List<PlottedGroup> groups;
    public ListIterator<IGeometry> geometries;

    private int currentDrawingIndex;
    private int currentPenIndex;
    private int currentGroupIndex;

    private boolean reverseDrawings;
    private boolean reversePens;
    private boolean reverseGroups;
    private boolean reverseGeometries;

    public DrawingGeometryIterator(PlottedDrawing plottedDrawing){
        this.reset(plottedDrawing);
    }

    public DrawingGeometryIterator(PlottedDrawing plottedDrawing, List<ObservableDrawingPen> pens){
        this.reset(plottedDrawing, pens);
    }

    public void reset(PlottedDrawing plottedDrawing){
        reset(plottedDrawing, plottedDrawing.getGlobalRenderOrder());
    }

    public void reverse(boolean reverseDrawings, boolean reversePens, boolean reverseGroups, boolean reverseGeometries){
        this.reverseDrawings = reverseDrawings;
        this.reversePens = reversePens;
        this.reverseGroups = reverseGroups;
        this.reverseGeometries = reverseGeometries;
    }

    public void reset(PlottedDrawing plottedDrawing, List<ObservableDrawingPen> renderOrder){
        this.drawings = List.of(plottedDrawing);
        this.pens = renderOrder;
        this.groups = new ArrayList<>(plottedDrawing.groups.values());
        this.reset();
    }

    public void reset(){
        super.reset();
        this.currentPen = null;
        this.currentGroup = null;
        this.geometries = null;
        this.currentPenIndex = reversePens ? pens.size()-1 : 0;
        this.currentGroupIndex = reverseGroups ? groups.size()-1 : 0;
    }

    @Override
    protected boolean hasNextInternal() {
        if(geometries != null && (reverseGeometries ? geometries.hasPrevious() : geometries.hasNext())){
            return true;
        }
        return updateIterator(true);
    }

    @Override
    protected IGeometry nextInternal() {
        if(geometries != null && (reverseGeometries ? geometries.hasPrevious() : geometries.hasNext())){
            return reverseGeometries ? geometries.previous() : geometries.next();
        }
        if(updateIterator(false)){
            return reverseGeometries ? geometries.previous() : geometries.next();
        }
        throw new NoSuchElementException();
    }

    protected boolean updateIterator(boolean check){
        PlottedDrawing drawing = currentDrawing;
        ObservableDrawingPen pen = currentPen;
        PlottedGroup group = currentGroup;

        int drawingIndex = currentDrawingIndex;
        int penIndex = currentPenIndex;
        int groupIndex = currentGroupIndex;

        ListIterator<IGeometry> iterator = null;

        drawings: for(; reverseDrawings ? drawingIndex >= 0 : drawingIndex < drawings.size(); drawingIndex += reverseDrawings ? -1 : 1) {
            drawing = drawings.get(drawingIndex);
            for (; reversePens ? penIndex >= 0 : penIndex < pens.size(); penIndex += reversePens ? -1 : 1) {
                pen = pens.get(penIndex);
                for (; reverseGroups ? groupIndex >= 0 : groupIndex < groups.size(); groupIndex += reverseGroups ? -1 : 1) {
                    group = groups.get(groupIndex);
                    List<IGeometry> geometries = group.getGeometriesPerPen().get(pen);
                    if (geometries != null && !geometries.isEmpty() && (drawing != currentDrawing || currentGroup != group || pen != currentPen)) {
                        iterator = geometries.listIterator(reverseGeometries ? geometries.size() - 1 : 0);
                        break drawings;
                    }
                }
                groupIndex = reverseGroups ? groups.size() - 1 : 0;
            }
        }
        if(!check){
            currentDrawingIndex = drawingIndex;
            currentPenIndex = penIndex;
            currentGroupIndex = groupIndex;
            currentDrawing = drawing;
            currentPen = pen;
            currentGroup = group;
            geometries = iterator;
        }
        return iterator != null;
    }

}
