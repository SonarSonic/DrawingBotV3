package drawingbot.utils;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.*;
import drawingbot.tasks.PlottingTask;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static processing.core.PApplet.*;

public class PlottedDrawing {

    public static DrawingBotV3 app = DrawingBotV3.INSTANCE;

    public final PlottingTask task;
    public final List<PlottedLine> plottedLines;
    public ObservableDrawingSet drawingPenSet; //TODO CHECK - if we don't copy (this is observable) and we should add a listener for the drawing, or we "deep copy"
    public float[] pen_distribution;
    public int displayedLineCount = -1;

    public PlottedDrawing(PlottingTask task, ObservableDrawingSet penSet){
        this.task = task;
        this.plottedLines = Collections.synchronizedList(new ArrayList<>());
        this.drawingPenSet = penSet;
        this.pen_distribution = new float[drawingPenSet.getPens().size()];
    }

    public int getPenCount(){
        return drawingPenSet.getPens().size();
    }

    public int getDisplayedLineCount(){
        if(displayedLineCount == -1){
            return getPlottedLineCount();
        }
        return displayedLineCount;
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
                //stroke(c, 255-brightness(c));
                app.stroke(pen.getRGBColour());
                //strokeWeight(2);
                //blendMode(BLEND);
                app.blendMode(PConstants.MULTIPLY);
                app.line(line.x1, line.y1, line.x2, line.y2);
            }
        }
    }

    public void renderToPDF() {
        String pdfname = "drawingbot.gcode\\gcode_" + app.basefile_selected + ".pdf";
        PGraphics pdf = app.createGraphics(task.getPlottingImage().width, task.getPlottingImage().height, app.PDF, pdfname);
        pdf.beginDraw();
        pdf.background(255, 255, 255);
        for(int i = getDisplayedLineCount(); i > 0; i--) {
            PlottedLine line = plottedLines.get(i);
            if(line.pen_down) {
                int rgb = drawingPenSet.getPens().get(line.pen_number).getRGBColour();
                pdf.stroke(rgb, 255);
                pdf.line(line.x1, line.y1, line.x2, line.y2);
            }
        }
        pdf.dispose();
        pdf.endDraw();
        println("PDF created:  " + pdfname);
    }

    public void renderEachPenToPdf() {
        for (int p = 0; p < getPenCount(); p ++) {
            IDrawingPen pen = drawingPenSet.getPens().get(p);
            String pdfname = "drawingbot.gcode\\gcode_" + app.basefile_selected + "_pen" + p + "_" + pen.getName() + ".pdf";
            PGraphics pdf = app.createGraphics(task.getPlottingImage().width, task.getPlottingImage().height, PDF, pdfname);
            pdf.beginDraw();
            pdf.background(255, 255, 255);
            for (int i = getDisplayedLineCount(); i >= 0; i--) {
                PlottedLine line = plottedLines.get(i);
                if (line.pen_down & line.pen_number == p) {
                    pdf.stroke(pen.getRGBColour(), 255);
                    pdf.line(line.x1, line.y1, line.x2, line.y2);
                }
            }
            pdf.dispose();
            pdf.endDraw();
            println("PDF created:  " + pdfname);
        }
    }

    public void setPenContinuationFlagsForSVG() {
        PlottedLine prevLine = null;

        for (int i = 0; i < plottedLines.size(); i++) {
            PlottedLine line = plottedLines.get(i);
            line.pen_continuation = !(prevLine == null || prevLine.x1 != line.x1 || prevLine.y1 != line.y1 || prevLine.pen_down != line.pen_down  || prevLine.pen_number != line.pen_number);
            prevLine = line;
        }
        println("set_pen_continuation_flags");
    }

    public void addline(int penNumber, boolean penDown, float x1, float y1, float x2, float y2) {
        plottedLines.add(new PlottedLine(penDown, penNumber, x1, y1, x2, y2));
    }

    public void evenlyDistributePenChanges() {
        println("evenly_distribute_pen_changes");
        for (int i = 0; i < getPlottedLineCount(); i++) {
            PlottedLine line = plottedLines.get(i);
            line.pen_number = (int)map(i, 0, getPlottedLineCount(), 0, drawingPenSet.getPens().size());
        }
    }

    public void distributePenChangesAccordingToPercentages() {
        int p = 0;
        float p_total = 0;

        for (int i = 0; i < getPlottedLineCount(); i ++) {
            PlottedLine line = plottedLines.get(i);
            if (i > pen_distribution[p] + p_total) {
                p_total = p_total + pen_distribution[p];
                p++;
            }
            line.pen_number = p;
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void adjustDistribution(int pen, double value){
        if(getPenCount() > pen){
            pen_distribution[pen] *= value;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////


    public void setEvenDistribution() {
        println("set_even_distribution");
        for (int p = 0; p < getPenCount(); p++) {
            pen_distribution[p] = getDisplayedLineCount() / getPenCount();
            //println("pen_distribution[" + p + "] = " + pen_distribution[p]);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setBlackDistribution() {
        println("set_black_distribution");
        for (int p = 0; p < getPenCount(); p++) {
            pen_distribution[p] = 0;
        }
        pen_distribution[0] = getDisplayedLineCount();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

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


}