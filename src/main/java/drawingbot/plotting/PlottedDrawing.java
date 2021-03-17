package drawingbot.plotting;

import drawingbot.api.IGeometryFilter;
import drawingbot.drawing.*;
import drawingbot.geom.GeometryUtils;
import drawingbot.geom.basic.IGeometry;
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
    public boolean ignoreWeightedDistribution = false;

    public PlottedDrawing(ObservableDrawingSet penSet){
        this.geometries = Collections.synchronizedList(new ArrayList<>());
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
        vertexCount += geometry.getVertexCount();
    }

    public void addGeometry(PlottedDrawing drawing){
        geometries.addAll(drawing.geometries);
        vertexCount += drawing.vertexCount;
    }

    public void reset(){
        geometries.clear();
        drawingPenSet = null;
        displayedLineCount = null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public int renderGeometryFX(GraphicsContext graphics, int start, int end, IGeometryFilter pointFilter, int vertexRenderLimit, boolean reverse) {
        int maxPoints = end-start;
        int renderCount = 0;
        for (int i = 0; i < maxPoints; i++) {
            int index = reverse ? end - i : start + i;
            if(renderCount >= vertexRenderLimit){
                return index;
            }
            IGeometry next = geometries.get(index);
            ObservableDrawingPen pen = drawingPenSet.getPen(next.getPenIndex());
            if(pointFilter.filter(next, pen)){
                next.renderFX(graphics, pen);
                renderCount += next.getVertexCount();
            }
        }
        return reverse ? start : end;
    }

    public int renderGeometryAWT(Graphics2D graphics, int start, int end, IGeometryFilter pointFilter, int vertexRenderLimit, boolean reverse) {
        int maxPoints = end-start;
        int renderCount = 0;
        for (int i = 0; i < maxPoints; i++) {
            int index = reverse ? end - i : start + i;
            if(renderCount >= vertexRenderLimit){
                return index;
            }
            IGeometry next = geometries.get(index);
            ObservableDrawingPen pen = drawingPenSet.getPen(next.getPenIndex());
            if(pointFilter.filter(next, pen)){
                next.renderAWT(graphics, pen);
                renderCount += next.getVertexCount();
            }
        }
        return reverse ? start : end;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**updates every pen's unique number, and sets the correct pen number for every line based on their weighted distribution*/
    public void updateWeightedDistribution(){

        if(!ignoreWeightedDistribution){
            int totalWeight = 0;
            for(int i = 0; i < drawingPenSet.pens.size(); i++){
                ObservableDrawingPen pen = drawingPenSet.pens.get(i);
                pen.penNumber.set(i); //update pens number based on position
                if(pen.isEnabled()){
                    totalWeight += pen.distributionWeight.get();
                }
            }

            int currentGeometry = 0;
            int[] renderOrder = drawingPenSet.getCurrentRenderOrder();

            for(int i = 0; i < renderOrder.length; i++){
                int penNumber = renderOrder[i];
                ObservableDrawingPen pen = drawingPenSet.pens.get(penNumber);
                if(pen.isEnabled()){ //if it's not enabled leave it at 0

                    //percentage
                    float percentage = (float)pen.distributionWeight.get() / totalWeight;
                    pen.currentPercentage.set(NumberFormat.getPercentInstance().format(percentage));

                    //lines
                    int geometriesPerPen = (int)(percentage * getGeometryCount());
                    pen.currentLines.set(geometriesPerPen);

                    //set pen references
                    int end = i == renderOrder.length-1 ? geometries.size() : currentGeometry + geometriesPerPen;
                    for (; currentGeometry < end; currentGeometry++) {
                        IGeometry geometry = geometries.get(currentGeometry);
                        geometry.setPenIndex(penNumber);
                    }
                }else{
                    pen.currentPercentage.set("0.0");
                    pen.currentLines.set(0);
                }
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