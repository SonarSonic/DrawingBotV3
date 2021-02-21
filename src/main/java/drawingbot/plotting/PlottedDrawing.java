package drawingbot.plotting;

import drawingbot.api.IPointFilter;
import drawingbot.drawing.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.GraphicsContext;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlottedDrawing {

    public final List<PlottedPoint> plottedPoints;

    public ObservableDrawingSet drawingPenSet;
    public SimpleIntegerProperty displayedLineCount = new SimpleIntegerProperty(-1);

    public PlottedDrawing(ObservableDrawingSet penSet){
        this.plottedPoints = Collections.synchronizedList(new ArrayList<>());
        this.drawingPenSet = penSet;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public int getPenCount(){
        return drawingPenSet.getPens().size();
    }

    public int getDisplayedLineCount(){
        if(displayedLineCount.get() == -1){
            return getPlottedLineCount();
        }
        return displayedLineCount.get();
    }

    public int getPlottedLineCount(){
        return plottedPoints.size();
    }

    public ObservableDrawingPen getPen(int penNumber){
        if(penNumber < drawingPenSet.getPens().size()){
            return drawingPenSet.getPens().get(penNumber);
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public PlottedPoint addPoint(int pathIndex, int penNumber, float x1, float y1) {
        PlottedPoint line = new PlottedPoint(pathIndex, penNumber, x1, y1);
        plottedPoints.add(line);
        return line;
    }

    public void reset(){
        plottedPoints.clear();
        drawingPenSet = null;
        displayedLineCount = null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public PlottedPoint last = null;

    public void renderPointsFX(GraphicsContext graphics, int start, int end, IPointFilter pointFilter, boolean reverse) {
        last = null;
        if(!reverse){
            for (int i = start; i < end; i++) {
                renderPointsFX(graphics, i, pointFilter);
            }
        }else{
            for (int i = start; i > end; i--) {
                renderPointsFX(graphics, i, pointFilter);
            }
        }
    }

    private void renderPointsFX(GraphicsContext graphics, int nextPoint, IPointFilter pointFilter){
        PlottedPoint point = plottedPoints.get(nextPoint);
        ObservableDrawingPen pen = getPen(point.pen_number);
        if(pen != null && pointFilter.filter(point, pen)){
            if(isPathContinuation(last, point)){
                renderLineFX(graphics, pen, last, point);
            }
            last = point;
        }else{
            last = null; //break off the rendering of points
        }
    }

    public void renderLineFX(GraphicsContext graphics, ObservableDrawingPen pen, PlottedPoint start, PlottedPoint end) {
        graphics.setLineWidth(pen.getStrokeSize());
        graphics.setStroke(pen.getFXColor(end.rgba));
        graphics.strokeLine((int)start.x1, (int)start.y1, (int)end.x1, (int)end.y1);
    }

    public void renderLineAWT(Graphics2D graphics, ObservableDrawingPen pen, PlottedPoint start, PlottedPoint end) {
        graphics.setStroke(pen.getAWTStroke());
        graphics.setColor(pen.getAWTColor(end.rgba));
        graphics.drawLine((int)start.x1, (int)start.y1, (int)end.x1, (int)end.y1);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**updates every pen's unique number, and sets the correct pen number for every line based on their weighted distribution*/
    public void updateWeightedDistribution(){
        int totalWeight = 0;
        for(int i = 0; i < drawingPenSet.pens.size(); i++){
            ObservableDrawingPen pen = drawingPenSet.pens.get(i);
            pen.penNumber.set(i); //update pens number based on position
            if(pen.isEnabled()){
                totalWeight += pen.distributionWeight.get();
            }
        }

        int currentLine = 0;
        int[] renderOrder = drawingPenSet.getCurrentRenderOrder();

        for(int i = 0; i < renderOrder.length; i++){
            int penNumber = renderOrder[i];
            ObservableDrawingPen pen = drawingPenSet.pens.get(penNumber);
            if(pen.isEnabled()){ //if it's not enabled leave it at 0

                //percentage
                float percentage = (float)pen.distributionWeight.get() / totalWeight;
                pen.currentPercentage.set(NumberFormat.getPercentInstance().format(percentage));

                //lines
                int linesPerPen = (int)(percentage * getPlottedLineCount());
                pen.currentLines.set(linesPerPen);

                //set pen references
                int end = i == renderOrder.length-1 ? plottedPoints.size() : currentLine + linesPerPen;
                for (; currentLine < end; currentLine++) {
                    PlottedPoint line = plottedPoints.get(currentLine);
                    line.pen_number = penNumber;
                }
            }else{
                pen.currentPercentage.set("0.0");
                pen.currentLines.set(0);
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    private List<PlottedPath> plottedPaths = null;
    private PlottedPath currentPath = null;

    public List<PlottedPath> generatePlottedPaths(IPointFilter pointFilter){
        plottedPaths = new ArrayList<>();
        currentPath = null;

        PlottedPoint last = null;
        Integer lastARGB = null;

        for(PlottedPoint point : plottedPoints){
            ObservableDrawingPen pen = getPen(point.pen_number);
            boolean shouldRenderPen = pen != null && pointFilter.filter(point, pen);

            if(!shouldRenderPen){
                last = null;
                lastARGB = null;
                continue;
            }

            int argb = pen.getCustomARGB(point.rgba);
            boolean isContinuation = isPathContinuation(last, point) && lastARGB != null && argb == lastARGB;

            if(!isContinuation){
                openPath(point, pen);
                currentPath.path.moveTo(point.x1, point.y1);
                currentPath.pointCount++;
            }else{
                currentPath.path.lineTo(point.x1, point.y1); //TODO CHECK SPECIALS RENDER PROPERLY, POSSIBLY "LASTARGB" will mean they are always only one point long
                currentPath.pointCount++;
            }

            last = point;
            lastARGB = argb;

        }
        closePath();

        Collections.reverse(plottedPaths); //should probably use render order...
        return plottedPaths;
    }

    public void openPath(PlottedPoint point, ObservableDrawingPen pen){
        if(currentPath != null){
            closePath();
        }
        currentPath = new PlottedPath(new GeneralPath(), pen.getAWTStroke(), pen.getAWTColor(point.rgba));
    }

    public void closePath(){
        if(currentPath != null && currentPath.pointCount > 1){
            plottedPaths.add(currentPath);
        }
        currentPath = null;
    }


    public boolean isPathContinuation(PlottedPoint prev, PlottedPoint next){
        if(prev == null){
            return false;
        }
        return prev.pathIndex == next.pathIndex && prev.pen_number == next.pen_number;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////


}