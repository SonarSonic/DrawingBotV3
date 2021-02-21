package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.*;
import drawingbot.image.ImageTools;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlottedDrawing {

    public final List<PlottedLine> plottedLines;

    public ObservableDrawingSet drawingPenSet;
    public SimpleIntegerProperty displayedLineCount = new SimpleIntegerProperty(-1);

    public PlottedDrawing(ObservableDrawingSet penSet){
        this.plottedLines = Collections.synchronizedList(new ArrayList<>());
        this.drawingPenSet = penSet;
    }

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
        return plottedLines.size();
    }

    public ObservableDrawingPen getPen(int penNumber){
        if(penNumber < drawingPenSet.getPens().size()){
            return drawingPenSet.getPens().get(penNumber);
        }
        return null;
    }

    public void renderLinesFX(GraphicsContext graphics, int start, int end) {

        for (int i = start; i < end; i++) {
            renderLineFX(graphics, plottedLines.get(i));
        }
    }

    public void renderLinesReverseFX(GraphicsContext graphics, int start, int end) {
        for (int i = start; i > end; i--) {
            renderLineFX(graphics, plottedLines.get(i));
        }
    }

    public void renderLineFX(GraphicsContext graphics, PlottedLine line) {
        if (line.pen_down) {
            ObservableDrawingPen pen = getPen(line.pen_number);
            if(pen != null && pen.isEnabled()){
                renderLineFX(graphics, pen, line);
            }
        }
    }

    public void renderLineFX(GraphicsContext graphics, ObservableDrawingPen pen, PlottedLine line) {
        graphics.setLineWidth(pen.getStrokeSize());
        graphics.setStroke(pen.getFXColor(line.rgba));
        graphics.strokeLine((int)line.x1, (int)line.y1, (int)line.x2, (int)line.y2);
    }

    public void renderLineAWT(Graphics2D graphics, ObservableDrawingPen pen, PlottedLine line) {
        graphics.setStroke(pen.getAWTStroke());
        graphics.setColor(pen.getAWTColor(line.rgba));
        graphics.drawLine((int)line.x1, (int)line.y1, (int)line.x2, (int)line.y2);
    }

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
                int end = i == renderOrder.length-1 ? plottedLines.size() : currentLine + linesPerPen;
                for (; currentLine < end; currentLine++) {
                    PlottedLine line = plottedLines.get(currentLine);
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

    public void setPenContinuationFlagsForSVG() {
        PlottedLine prevLine = null;

        for (int i = 0; i < plottedLines.size(); i++) {
            PlottedLine line = plottedLines.get(i);
            line.pen_continuation = !(prevLine == null || prevLine.x2 != line.x1 || prevLine.y2 != line.y1 || prevLine.pen_down != line.pen_down  || prevLine.pen_number != line.pen_number);
            prevLine = line;
        }
        DrawingBotV3.logger.fine("set_pen_continuation_flags");
    }

    public PlottedLine addline(int penNumber, boolean penDown, float x1, float y1, float x2, float y2) {
        PlottedLine line = new PlottedLine(penDown, penNumber, x1, y1, x2, y2);
        plottedLines.add(line);
        return line;
    }

    public void reset(){
        plottedLines.clear();
        drawingPenSet = null;
        displayedLineCount = null;
    }


}