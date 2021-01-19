package drawingbot.utils;

import drawingbot.DrawingBotV3;
import drawingbot.tasks.PlottingTask;
import drawingbot.helpers.CopicPenHelper;
import processing.core.PGraphics;
import static processing.core.PApplet.*;

///////////////////////////////////////////////////////////////////////////////////////////////////////
// A class to describe all the line segments
public class PlottedDrawing {

    public static DrawingBotV3 app = DrawingBotV3.INSTANCE;
    public PlottingTask task;


    public int line_count = 0;
    public PlottedLine[] lines = new PlottedLine[10000000];
    public String gcode_comment = "";

    public PlottedDrawing(PlottingTask image){
        this.task = image;
    }

    public void render_last () {
        lines[line_count].render_with_copic();
    }

    public void render_all () {
        for (int i=1; i<line_count; i++) {
            lines[i].render_with_copic();
        }
    }

    public void render_some(int line_count) {
        for (int i=1; i<line_count; i++) {
            lines[i].render_with_copic();
        }
    }

    public void render_between(int start, int end) {
        for (int i=start; i<end; i++) {
            lines[i].render_with_copic();
        }
    }

    public void render_one_pen(int line_count, int pen) {
        int c = app.color(255, 0, 0);

        for (int i=1; i<line_count; i++) {
            //for (int i=line_count; i>1; i--) {
            if (lines[i].pen_number == pen) {
                lines[i].render_with_copic();
            }
        }
    }

    public void render_to_pdf (int line_count) {
        String pdfname = "drawingbot.gcode\\gcode_" + app.basefile_selected + ".pdf";
        PGraphics pdf = app.createGraphics(task.getPlottingImage().width, task.getPlottingImage().height, app.PDF, pdfname);
        pdf.beginDraw();
        pdf.background(255, 255, 255);
        for(int i=line_count; i>0; i--) {
            if(lines[i].pen_down) {
                int c = app.copic.get_original_color(CopicPenHelper.copic_sets[app.current_copic_set][lines[i].pen_number]);
                pdf.stroke(c, 255);
                pdf.line(lines[i].x1, lines[i].y1, lines[i].x2, lines[i].y2);
            }
        }
        pdf.dispose();
        pdf.endDraw();
        println("PDF created:  " + pdfname);
    }

    public void render_each_pen_to_pdf (int line_count) {
        for (int p=0; p<=app.pen_count-1; p++) {
            String pdfname = "drawingbot.gcode\\gcode_" + app.basefile_selected + "_pen" + p + "_" + CopicPenHelper.copic_sets[app.current_copic_set][p] + ".pdf";
            PGraphics pdf = app.createGraphics(task.getPlottingImage().width, task.getPlottingImage().height, PDF, pdfname);
            pdf.beginDraw();
            pdf.background(255, 255, 255);
            for (int i=line_count; i>0; i--) {
                if (lines[i].pen_down & lines[i].pen_number == p) {
                    int c = app.copic.get_original_color(CopicPenHelper.copic_sets[app.current_copic_set][lines[i].pen_number]);
                    pdf.stroke(c, 255);
                    pdf.line(lines[i].x1, lines[i].y1, lines[i].x2, lines[i].y2);
                }
            }
            pdf.dispose();
            pdf.endDraw();
            println("PDF created:  " + pdfname);
        }
    }

    public void set_pen_continuation_flags () {
        float prev_x = 123456.0F;
        float prev_y = 654321.0F;
        boolean prev_pen_down = false;
        int prev_pen_number = 123456;

        for (int i=1; i<line_count; i++) {

            if (prev_x != lines[i].x1 || prev_y != lines[i].y1 || prev_pen_down != lines[i].pen_down  || prev_pen_number != lines[i].pen_number) {
                lines[i].pen_continuation = false;
            } else {
                lines[i].pen_continuation = true;
            }

            prev_x = lines[i].x2;
            prev_y = lines[i].y2;
            prev_pen_down = lines[i].pen_down;
            prev_pen_number = lines[i].pen_number;
        }
        println("set_pen_continuation_flags");
    }

    public void addline(int pen_number_, boolean pen_down_, float x1_, float y1_, float x2_, float y2_) {
        line_count++;
        lines[line_count] = new PlottedLine(pen_down_, pen_number_, x1_, y1_, x2_, y2_);
    }

    public int get_line_count() {
        return line_count;
    }

    public void evenly_distribute_pen_changes (int line_count, int total_pens) {
        println("evenly_distribute_pen_changes");
        for (int i=1; i<=line_count; i++) {
            int cidx = (int)map(i - 1, 0, line_count, 1, total_pens);
            lines[i].pen_number = cidx;
            //println (i + "   " + lines[i].pen_number);
        }
    }

    public void distribute_pen_changes_according_to_percentages (int line_count, int total_pens) {
        int p = 0;
        float p_total = 0;

        for (int i=1; i<=line_count; i++) {
            if (i > task.pen_distribution[p] + p_total) {
                p_total = p_total + task.pen_distribution[p];
                p++;
            }
            if (p > total_pens - 1) {
                // Hacky fix for off by one error FIXME
                println("ERROR: distribute_pen_changes_according_to_percentages, p:  ", p);
                p = total_pens - 1;
            }
            lines[i].pen_number = p;
            //println (i + "   " + lines[i].pen_number);
        }
    }

}