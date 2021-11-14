package drawingbot.plotting;

import drawingbot.api.IGeometryFilter;
import drawingbot.drawing.DrawingStyle;
import drawingbot.geom.basic.IGeometry;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.pfm.PFMFactory;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.GraphicsContext;

import java.awt.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

public class PlottedDrawing {

    public final List<IGeometry> geometries;
    public long vertexCount;

    public ObservableDrawingSet drawingPenSet;
    public SimpleIntegerProperty displayedLineCount = new SimpleIntegerProperty(-1);
    public boolean ignoreWeightedDistribution = false; //used for disabling distributions within sub tasks, will use the pfms default

    public PlottedDrawing(ObservableDrawingSet penSet){
        this.geometries = Collections.synchronizedList(new ArrayList<>());
        this.groups = Collections.synchronizedMap(new HashMap<>());
        this.groupPFMType = new HashMap<>();
        this.drawingPenSet = penSet;
        this.vertexCount = 0;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public int getPenCount(){
        return drawingPenSet.getPens().size();
    }

    public int getDisplayedGeometryCount(){
        if(displayedLineCount.get() == -1){
            return getGeometryCount();
        }
        return displayedLineCount.get();
    }

    public int getGeometryCount(){
        return geometries.size();
    }

    public long getVertexCount(){
        return vertexCount;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void addGeometry(IGeometry geometry) {
        geometries.add(geometry);
        vertexCount += geometry.getSegmentCount();

        addGeometryToGroups(geometry);
    }

    public void addGeometry(PlottedDrawing drawing){
        addGeometry(drawing.geometries);
    }

    public void addGeometry(List<IGeometry> orderedGeometries) {
        orderedGeometries.forEach(this::addGeometry);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public final Map<Integer, List<IGeometry>> groups;
    public final Map<Integer, PFMFactory<?>> groupPFMType;

    public void addGeometryToGroups(IGeometry geometry){
        groups.putIfAbsent(geometry.getGroupID(), new ArrayList<>());
        groups.get(geometry.getGroupID()).add(geometry);
    }

    public void addGroupPFMType(int groupID, PFMFactory<?> factory){
        groupPFMType.put(groupID, factory);
    }

    public PFMFactory<?> getGroupPFMType(int groupID){
        return groupPFMType.get(groupID);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void clearGeometries(){
        geometries.clear();
        vertexCount = 0;
    }

    public void reset(){
        clearGeometries();
        drawingPenSet = null;
        displayedLineCount = null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public int renderGeometryFX(GraphicsContext graphics, int start, int end, IGeometryFilter pointFilter, int vertexRenderLimit, boolean reverse) {
        int renderCount = 0;
        for (int index = reverse ? end : start; (reverse ? index >= start : index < end); index += reverse ? -1 : 1) {
            if(index != start && index != end && renderCount >= vertexRenderLimit){
                return index;
            }
            IGeometry next = geometries.get(index);
            ObservableDrawingPen pen = drawingPenSet.getPen(next.getPenIndex());
            if(pointFilter.filter(next, pen)){
                next.renderFX(graphics, pen);
                renderCount += next.getSegmentCount();
            }
        }
        return reverse ? start : end;
    }

    public int renderGeometryAWT(Graphics2D graphics, int start, int end, IGeometryFilter pointFilter, int vertexRenderLimit, boolean reverse) {
        int renderCount = 0;
        for (int index = reverse ? end : start; (reverse ? index >= start : index < end); index += reverse ? -1 : 1) {
            if(index != start && index != end && renderCount >= vertexRenderLimit){
                return index;
            }
            IGeometry next = geometries.get(index);
            ObservableDrawingPen pen = drawingPenSet.getPen(next.getPenIndex());
            if(pointFilter.filter(next, pen)){
                next.renderAWT(graphics, pen);
                renderCount += next.getSegmentCount();
            }
        }
        return reverse ? start : end;
    }

    public void updatePenDistribution(){
        if(!ignoreWeightedDistribution){
            drawingPenSet.distributionType.get().distribute.accept(this);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void updatePenNumbers(){
        for(int i = 0; i < drawingPenSet.pens.size(); i++){
            ObservableDrawingPen pen = drawingPenSet.pens.get(i);
            pen.penNumber.set(i); //update pens number based on position
        }
    }

    public void updateFromGeometryCounts(int[] geometryCounts){
        for(int p = 0; p < geometryCounts.length; p++){
            ObservableDrawingPen pen = drawingPenSet.getPen(p);
            int geometryCount = geometryCounts[p];
            if(pen.isEnabled()){
                pen.currentPercentage.set(NumberFormat.getPercentInstance().format((float)geometryCount / geometries.size()));
                pen.currentGeometries.set(geometryCount);
            }else{
                //if the pen's not enabled set it to 0
                pen.currentPercentage.set("0.0");
                pen.currentGeometries.set(0);
            }
        }
    }

    /**updates every pen's unique number, and sets the correct pen number for every line based on their weighted distribution*/
    public void updateEvenDistribution(boolean weighted, boolean random){
        int currentGeometry = 0;
        int totalWeight = 0;
        int[] weights = new int[drawingPenSet.pens.size()];
        for(int i = 0; i < drawingPenSet.pens.size(); i++){
            ObservableDrawingPen pen = drawingPenSet.pens.get(i);
            pen.penNumber.set(i); //update pens number based on position
            if(pen.isEnabled()){
                weights[i] = weighted ? pen.distributionWeight.get() : 100;
                totalWeight += weights[i];
            }
        }

        int[] renderOrder = drawingPenSet.calculateRenderOrder();

        if(!random){
            for(int i = 0; i < renderOrder.length; i++){
                int penNumber = renderOrder[i];
                ObservableDrawingPen pen = drawingPenSet.getPen(penNumber);
                if(pen.isEnabled()){
                    //update percentage
                    float percentage = (weighted ? (float)pen.distributionWeight.get() : 100) / totalWeight;
                    pen.currentPercentage.set(NumberFormat.getPercentInstance().format(percentage));

                    //update geometry count
                    int geometriesPerPen = (int)(percentage * getGeometryCount());
                    pen.currentGeometries.set(geometriesPerPen);

                    //set pen references
                    int end = i == renderOrder.length-1 ? geometries.size() : currentGeometry + geometriesPerPen;
                    for (; currentGeometry < end; currentGeometry++) {
                        IGeometry geometry = geometries.get(currentGeometry);
                        geometry.setPenIndex(penNumber);
                    }
                }else{
                    //if the pen's not enabled set it to 0
                    pen.currentPercentage.set("0.0");
                    pen.currentGeometries.set(0);
                }
            }
        }else{
            int[] geometryCounts = new int[getPenCount()];

            Random rand = new Random(0);

            for(IGeometry geometry : geometries){
                int weightedRand = rand.nextInt(totalWeight);
                int currentWeight = 0;
                for(int w = 0; w < weights.length; w++){
                    currentWeight += weights[w];
                    if(weightedRand < currentWeight){
                        geometryCounts[w]++;
                        geometry.setPenIndex(w);
                        break;
                    }
                }
            }
            updateFromGeometryCounts(geometryCounts);

        }
    }

    public void updatePreConfiguredPenDistribution(){
        //don't update the pen numbers so they match the original ones

        int[] geometryCounts = new int[getPenCount()];
        for(IGeometry geometry : geometries){
            Integer penIndex = geometry.getPenIndex();
            if(penIndex != null){
                geometryCounts[penIndex] ++;
            }
        }
        updateFromGeometryCounts(geometryCounts);
    }

    public void updateSinglePenDistribution(){
        updatePenNumbers();

        boolean addedFirstPen = false;
        int[] renderOrder = drawingPenSet.calculateRenderOrder();

        for (int penNumber : renderOrder) {
            ObservableDrawingPen pen = drawingPenSet.pens.get(penNumber);
            if (!addedFirstPen && pen.isEnabled()) {

                //update percentage
                pen.currentPercentage.set(NumberFormat.getPercentInstance().format(1));

                //update geometry count
                pen.currentGeometries.set(geometries.size());

                geometries.forEach(g -> g.setPenIndex(penNumber));

                addedFirstPen = true;
            } else {
                //if the pen's not enabled set it to 0
                pen.currentPercentage.set("0.0");
                pen.currentGeometries.set(0);
            }
        }

    }

    /**sets all the pens to default distribution / even*/
    public void resetWeightedDistribution(){
        for(ObservableDrawingPen pen : drawingPenSet.getPens()){
            pen.distributionWeight.setValue(100);
        }
    }

    /**increases the distribution weight of a given pen*/
    public void adjustWeightedDistribution(ObservableDrawingPen selected, int adjust){
        int current = selected.distributionWeight.get();
        selected.distributionWeight.set(Math.max(0, current + adjust));
    }
}