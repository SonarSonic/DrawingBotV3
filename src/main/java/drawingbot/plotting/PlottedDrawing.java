package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.drawing.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.GraphicsContext;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.javafx.PGraphicsFX2D;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static processing.core.PApplet.*;

public class PlottedDrawing {

    public static DrawingBotV3 app = DrawingBotV3.INSTANCE;

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
        return drawingPenSet.getPens().get(penNumber);
    }

    public void renderLines(int start, int end) {
        for (int i = start; i < end; i++) {
            renderLine(plottedLines.get(i));
        }
    }

    public void renderLinesReverse(int start, int end) {
        for (int i = start; i > end; i--) {
            renderLine(plottedLines.get(i));
        }
    }

    public void renderLinesForPen(int start, int end, int pen) {
        for (int i = start; i < end; i++) {
            PlottedLine line = plottedLines.get(i);
            if (line.pen_number == pen) {
                renderLine(line);
            }
        }
    }

    public void renderLinesReverse(int start, int end, int pen) {
        for (int i = start; i > end; i--) {
            PlottedLine line = plottedLines.get(i);
            if (line.pen_number == pen) {
                renderLine(line);
            }
        }
    }

    public void renderLine(PlottedLine line) {
        if (line.pen_down) {
            ObservableDrawingPen pen = drawingPenSet.getPens().get(line.pen_number);
            if(pen.isEnabled()){
                app.stroke(line.rgba != -1 ? line.rgba : pen.getRGBColour());
                app.line(line.x1, line.y1, line.x2, line.y2); //render line dangerously.
            }
        }
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
                pen.currentPercentage.set(PApplet.nf(percentage*100, 2, 1));

                //lines
                int linesPerPen = (int)(percentage * getPlottedLineCount());
                pen.currentLines.set(linesPerPen);

                //set pen references
                int end = currentLine + linesPerPen;
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

    public void addline(int penNumber, boolean penDown, float x1, float y1, float x2, float y2) {
        plottedLines.add(new PlottedLine(penDown, penNumber, x1, y1, x2, y2));
    }

    public void reset(){
        plottedLines.clear();
        drawingPenSet = null;
        displayedLineCount = null;
    }


}