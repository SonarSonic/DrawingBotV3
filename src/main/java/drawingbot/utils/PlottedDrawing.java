package drawingbot.utils;

import drawingbot.DrawingBotV3;
import drawingbot.tasks.PlottingTask;
import drawingbot.helpers.CopicPenHelper;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static processing.core.PApplet.*;

///////////////////////////////////////////////////////////////////////////////////////////////////////
// A class to describe all the line segments
public class PlottedDrawing {

    public static DrawingBotV3 app = DrawingBotV3.INSTANCE;

    public PlottingTask task;
    public final List<PlottedLine> plottedLines = Collections.synchronizedList(new ArrayList<>());

    public PlottedDrawing(PlottingTask image){
        this.task = image;
    }

    public int getPlottedLineCount(){
        return plottedLines.size();
    }

    public void renderLines(int start, int end) {
        for (int i = start; i < end; i++) {
            plottedLines.get(i).render_with_copic();
        }
    }

    public void renderLines(int start, int end, int pen) {
        for (int i = start; i < end; i++) {
            PlottedLine line = plottedLines.get(i);
            if (line.pen_number == pen) {
                line.render_with_copic();
            }
        }
    }

    public void renderToPDF(int lineCount) {
        String pdfname = "drawingbot.gcode\\gcode_" + app.basefile_selected + ".pdf";
        PGraphics pdf = app.createGraphics(task.getPlottingImage().width, task.getPlottingImage().height, app.PDF, pdfname);
        pdf.beginDraw();
        pdf.background(255, 255, 255);
        for(int i = lineCount; i > 0; i--) {
            PlottedLine line = plottedLines.get(i);
            if(line.pen_down) {
                int c = app.copic.get_original_color(CopicPenHelper.copic_sets[app.current_copic_set][line.pen_number]);
                pdf.stroke(c, 255);
                pdf.line(line.x1, line.y1, line.x2, line.y2);
            }
        }
        pdf.dispose();
        pdf.endDraw();
        println("PDF created:  " + pdfname);
    }

    public void renderEachPenToPdf(int line_count) {
        for (int p=0; p<=app.pen_count-1; p++) {
            String pdfname = "drawingbot.gcode\\gcode_" + app.basefile_selected + "_pen" + p + "_" + CopicPenHelper.copic_sets[app.current_copic_set][p] + ".pdf";
            PGraphics pdf = app.createGraphics(task.getPlottingImage().width, task.getPlottingImage().height, PDF, pdfname);
            pdf.beginDraw();
            pdf.background(255, 255, 255);
            for (int i = line_count; i >= 0; i--) {
                PlottedLine line = plottedLines.get(i);
                if (line.pen_down & line.pen_number == p) {
                    int c = app.copic.get_original_color(CopicPenHelper.copic_sets[app.current_copic_set][line.pen_number]);
                    pdf.stroke(c, 255);
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

    public void evenly_distribute_pen_changes (int line_count, int total_pens) {
        println("evenly_distribute_pen_changes");
        for (int i=0; i < line_count; i++) {
            PlottedLine line = plottedLines.get(i);
            line.pen_number = (int)map(i - 1, 0, line_count, 1, total_pens);
        }
    }

    public void distribute_pen_changes_according_to_percentages (int line_count, int total_pens) {
        int p = 0;
        float p_total = 0;

        for (int i = 0; i < line_count; i ++) {
            PlottedLine line = plottedLines.get(i);
            if (i > task.pen_distribution[p] + p_total) {
                p_total = p_total + task.pen_distribution[p];
                p++;
            }
            if (p > total_pens - 1) {
                // Hacky fix for off by one error FIXME
                println("ERROR: distribute_pen_changes_according_to_percentages, p:  ", p);
                p = total_pens - 1;
            }
            line.pen_number = p;
        }
    }

}