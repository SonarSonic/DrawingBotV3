package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.GraphicsContext;
import processing.core.PConstants;
import processing.javafx.PGraphicsFX2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static processing.core.PApplet.*;

//TODO FIX MEMORY LEAKS / APPLICATION SLOWING UP
public class PlottedDrawing {

    public static DrawingBotV3 app = DrawingBotV3.INSTANCE;

    public final List<PlottedLine> plottedLines;
    public ObservableDrawingSet drawingPenSet;
    public SimpleIntegerProperty displayedLineCount = new SimpleIntegerProperty(-1);
    public float[] pen_distribution;

    public PlottedDrawing(ObservableDrawingSet penSet){
        this.plottedLines = Collections.synchronizedList(new ArrayList<>());
        this.drawingPenSet = penSet;
        this.pen_distribution = new float[drawingPenSet.getPens().size()];
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

    public void renderLinesForPen(int start, int end, int pen) {
        for (int i = start; i < end; i++) {
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
                //app.stroke(c, 255-brightness(c));
                app.stroke(pen.getRGBColour());
                //strokeWeight(2);
                //blendMode(BLEND);
                //app.blendMode(PConstants.DARKEST); //TODO DECIDE ON BLEND MORE OR MAKE CONFIGURABLE???? - MULTIPLY = TOO DARK, BLEND = FAITHFUL BUT CARTOONY.
                app.line(line.x1, line.y1, line.x2, line.y2); //render line dangerously.
            }
        }
    }


    public void setPenContinuationFlagsForSVG() {
        PlottedLine prevLine = null;

        for (int i = 0; i < plottedLines.size(); i++) {
            PlottedLine line = plottedLines.get(i);
            line.pen_continuation = !(prevLine == null || prevLine.x2 != line.x1 || prevLine.y2 != line.y1 || prevLine.pen_down != line.pen_down  || prevLine.pen_number != line.pen_number);
            prevLine = line;
        }
        println("set_pen_continuation_flags");
    }

    public void addline(int penNumber, boolean penDown, float x1, float y1, float x2, float y2) {
        plottedLines.add(new PlottedLine(penDown, penNumber, x1, y1, x2, y2));
    }

    /**maps pens evenly in order of first to last*/
    public void evenlyDistributePenChanges() {
        println("evenly_distribute_pen_changes");
        for (int i = 0; i < getPlottedLineCount(); i++) {
            PlottedLine line = plottedLines.get(i);
            line.pen_number = (int)map(i, 0, getPlottedLineCount(), 0, drawingPenSet.getPens().size());
        }
    }

    /**maps pen colours based on the percentage in order from first to last*/
    public void distributePenChangesAccordingToPercentages() {
        int p = 0;
        float p_total = 0;

        for (int i = 0; i < getPlottedLineCount(); i ++) {
            PlottedLine line = plottedLines.get(i);
            if (i > pen_distribution[p] + p_total) {
                p_total = p_total + pen_distribution[p];
                p++;
            }
            if (p > getPlottedLineCount() - 1) {
                // Hacky fix for off by one error FIXME
                println("ERROR: distribute_pen_changes_according_to_percentages, p:  ", p);
                p = getPlottedLineCount() - 1;
            }
            line.pen_number = p;
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**increases the distribution value of a specific pen*/
    public void adjustDistribution(int pen, double value){
        if(getPenCount() > pen){
            pen_distribution[pen] *= value;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////


    /**sets the pen distribution for each pen, this is the number of lines to display on this pen...*/
    public void setEvenDistribution() {
        println("set_even_distribution");
        for (int p = 0; p < getPenCount(); p++) {
            pen_distribution[p] = getDisplayedLineCount() / getPenCount();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**set all lines to the first pen **/
    public void setBlackDistribution() {
        println("set_black_distribution");
        for (int p = 0; p < getPenCount(); p++) {
            pen_distribution[p] = 0;
        }
        pen_distribution[0] = getDisplayedLineCount();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /** takes the current distribution as a percentage and multiples it by visible lines...    */
    public void normalizeDistribution() {
        float total = 0;

        println();

        for (int p = 0; p < getPenCount(); p++) {
            total = total + pen_distribution[p];
        }

        for (int p = 0; p < getPenCount() ; p++) {
            pen_distribution[p] = getDisplayedLineCount() * pen_distribution[p] / total;
            print("Pen " + p + ", ");
            System.out.printf("%-4s", getPen(p).getName());
            System.out.printf("%8.0f  ", pen_distribution[p]);

            // Display approximately one star for every percent of total
            for (int s = 0; s < (int)(pen_distribution[p]/total*100); s++) {
                print("*");
            }
            println();
        }
    }

    public void reset(){
        plottedLines.clear();
        drawingPenSet = null;
        displayedLineCount = null;
        pen_distribution = null;
    }


}