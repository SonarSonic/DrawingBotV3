package drawingbot.helpers;

import drawingbot.DrawingBotV3;
import drawingbot.PlottingTask;

import static processing.core.PApplet.*;

public class GCodeHelper {

    public static void gcode_header(PlottingTask task) {
        task.output.println("G21");
        task.output.println("G90");
        task.output.println("G1 Z0");
    }

    public static void gcode_trailer(PlottingTask task) {
        task.output.println("G1 Z0");
        task.output.println("G1 X" + gcode_format(0.1F) + " Y" + gcode_format(0.1F));
        task.output.println("G1 X0 y0");
    }

    public static void gcode_comment(PlottingTask task, String comment) {
        task.gcode_comments += ("(" + comment + ")") + "\n";
        println(comment);
    }

    public static void pen_up(PlottingTask task) {
        task.is_pen_down = false;
    }

    public static void pen_down(PlottingTask task) {
        task.is_pen_down = true;
    }

    public static void move_abs(PlottingTask task, int pen_number, float x, float y) {
        task.plottedDrawing.addline(pen_number, task.is_pen_down, task.old_x, task.old_y, x, y);
        if (task.is_pen_down) {
            task.plottedDrawing.render_last();
        }

        task.old_x = x;
        task.old_y = y;
    }


    public static String gcode_format (Float n) {
        String s = nf(n, 0, DrawingBotV3.gcode_decimals);
        s = s.replace('.', DrawingBotV3.gcode_decimal_seperator);
        s = s.replace(',', DrawingBotV3.gcode_decimal_seperator);
        return s;
    }

    public static void create_gcode_files(PlottingTask task, int line_count) {
        boolean is_pen_down;
        int pen_lifts;
        float pen_movement;
        float pen_drawing;
        int   lines_drawn;
        float x;
        float y;
        float distance;

        // Loop over all lines for every pen.
        for (int p = 0; p < DrawingBotV3.pen_count; p ++) {
            is_pen_down = false;
            pen_lifts = 2;
            pen_movement = 0;
            pen_drawing = 0;
            lines_drawn = 0;
            x = 0;
            y = 0;
            String gname = "drawingbot.gcode\\gcode_" + DrawingBotV3.INSTANCE.basefile_selected + "_pen" + p + "_" + CopicPenHelper.copic_sets[DrawingBotV3.INSTANCE.current_copic_set][p] + ".txt";
            task.output = DrawingBotV3.INSTANCE.createWriter(DrawingBotV3.INSTANCE.sketchPath("") + gname);
            task.output.println(task.gcode_comments);
            gcode_header(task);

            for (int i = 1 ; i < line_count; i ++) {
                if (task.plottedDrawing.lines[i].pen_number == p) {

                    float gcode_scaled_x1 = task.plottedDrawing.lines[i].x1 * task.gcode_scale + task.gcode_offset_x;
                    float gcode_scaled_y1 = task.plottedDrawing.lines[i].y1 * task.gcode_scale + task.gcode_offset_y;
                    float gcode_scaled_x2 = task.plottedDrawing.lines[i].x2 * task.gcode_scale + task.gcode_offset_x;
                    float gcode_scaled_y2 = task.plottedDrawing.lines[i].y2 * task.gcode_scale + task.gcode_offset_y;
                    distance = sqrt( sq(abs(gcode_scaled_x1 - gcode_scaled_x2)) + sq(abs(gcode_scaled_y1 - gcode_scaled_y2)) );

                    if (x != gcode_scaled_x1 || y != gcode_scaled_y1) {
                        // Oh crap, where the line starts is not where I am, pick up the pen and move there.
                        task.output.println("G1 Z0");
                        is_pen_down = false;
                        distance = sqrt( sq(abs(x - gcode_scaled_x1)) + sq(abs(y - gcode_scaled_y1)) );
                        String buf = "G1 X" + gcode_format(gcode_scaled_x1) + " Y" + gcode_format(gcode_scaled_y1);
                        task.output.println(buf);
                        x = gcode_scaled_x1;
                        y = gcode_scaled_y1;
                        pen_movement = pen_movement + distance;
                        pen_lifts++;
                    }

                    if (task.plottedDrawing.lines[i].pen_down) {
                        if (!is_pen_down) {
                            task.output.println("G1 Z1");
                            is_pen_down = true;
                        }
                        pen_drawing = pen_drawing + distance;
                        lines_drawn++;
                    } else {
                        if (is_pen_down) {
                            task.output.println("G1 Z0");
                            is_pen_down = false;
                            pen_movement = pen_movement + distance;
                            pen_lifts++;
                        }
                    }

                    String buf = "G1 X" + gcode_format(gcode_scaled_x2) + " Y" + gcode_format(gcode_scaled_y2);
                    task.output.println(buf);
                    x = gcode_scaled_x2;
                    y = gcode_scaled_y2;
                    task.dx.update_limit(gcode_scaled_x2);
                    task.dy.update_limit(gcode_scaled_y2);
                }
            }

            gcode_trailer(task);
            task.output.println("(Drew " + lines_drawn + " lines for " + pen_drawing  / 25.4 / 12 + " feet)");
            task.output.println("(Pen was lifted " + pen_lifts + " times for " + pen_movement  / 25.4 / 12 + " feet)");
            task.output.println("(Extreams of X: " + task.dx.min + " thru " + task.dx.max + ")");
            task.output.println("(Extreams of Y: " + task.dy.min + " thru " + task.dy.max + ")");
            task.output.flush();
            task.output.close();
            println("drawingbot.gcode created:  " + gname);
        }
    }

    public static void create_gcode_test_file (PlottingTask task) {
        // The dx.min are already scaled to drawingbot.gcode.
        float test_length = 25.4F * 2F;

        String gname = "drawingbot.gcode\\gcode_" + DrawingBotV3.INSTANCE.basefile_selected + "_test.txt";
        task.output = DrawingBotV3.INSTANCE.createWriter(DrawingBotV3.INSTANCE.sketchPath("") + gname);
        task.output.println("(This is a test file to draw the extreams of the drawing area.)");
        task.output.println("(Draws a 2 inch mark on all four corners of the paper.)");
        task.output.println("(WARNING:  pen will be down.)");
        task.output.println("(Extreams of X: " + task.dx.min + " thru " + task.dx.max + ")");
        task.output.println("(Extreams of Y: " + task.dy.min + " thru " + task.dy.max + ")");
        gcode_header(task);

        task.output.println("(Upper left)");
        task.output.println("G1 X" + gcode_format(task.dx.min) + " Y" + gcode_format(task.dy.min + test_length));
        task.output.println("G1 Z1");
        task.output.println("G1 X" + gcode_format(task.dx.min) + " Y" + gcode_format(task.dy.min));
        task.output.println("G1 X" + gcode_format(task.dx.min + test_length) + " Y" + gcode_format(task.dy.min));
        task.output.println("G1 Z0");

        task.output.println("(Upper right)");
        task.output.println("G1 X" + gcode_format(task.dx.max - test_length) + " Y" + gcode_format(task.dy.min));
        task.output.println("G1 Z1");
        task.output.println("G1 X" + gcode_format(task.dx.max) + " Y" + gcode_format(task.dy.min));
        task.output.println("G1 X" + gcode_format(task.dx.max) + " Y" + gcode_format(task.dy.min + test_length));
        task.output.println("G1 Z0");

        task.output.println("(Lower right)");
        task.output.println("G1 X" + gcode_format(task.dx.max) + " Y" + gcode_format(task.dy.max - test_length));
        task.output.println("G1 Z1");
        task.output.println("G1 X" + gcode_format(task.dx.max) + " Y" + gcode_format(task.dy.max));
        task.output.println("G1 X" + gcode_format(task.dx.max - test_length) + " Y" + gcode_format(task.dy.max));
        task.output.println("G1 Z0");

        task.output.println("(Lower left)");
        task.output.println("G1 X" + gcode_format(task.dx.min + test_length) + " Y" + gcode_format(task.dy.max));
        task.output.println("G1 Z1");
        task.output.println("G1 X" + gcode_format(task.dx.min) + " Y" + gcode_format(task.dy.max));
        task.output.println("G1 X" + gcode_format(task.dx.min) + " Y" + gcode_format(task.dy.max - test_length));
        task.output.println("G1 Z0");

        gcode_trailer(task);
        task.output.flush();
        task.output.close();
        println("drawingbot.gcode test created:  " + gname);
    }


    // Thanks to Vladimir Bochkov for helping me debug the SVG international decimal separators problem.
    public static String svg_format (Float n) {
        final char regional_decimal_separator = ',';
        final char svg_decimal_seperator = '.';

        String s = nf(n, 0, DrawingBotV3.svg_decimals);
        s = s.replace(regional_decimal_separator, svg_decimal_seperator);
        return s;
    }


    // Thanks to John Cliff for getting the SVG output moving forward.
    public static void create_svg_file (PlottingTask task, int line_count) {
        boolean drawing_polyline = false;

        // Inkscape versions before 0.91 used 90dpi, Today most software assumes 96dpi.
        float svgdpi = 96.0F / 25.4F;

        String gname = "drawingbot.gcode\\gcode_" + DrawingBotV3.INSTANCE.basefile_selected + ".svg";
        task.output = DrawingBotV3.INSTANCE.createWriter(DrawingBotV3.INSTANCE.sketchPath("") + gname);
        task.output.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        task.output.println("<svg width=\"" + svg_format(task.width() * task.gcode_scale) + "mm\" height=\"" + svg_format(task.getPlottingImage().height * task.gcode_scale) + "mm\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">");
        task.plottedDrawing.set_pen_continuation_flags();

        // Loop over pens backwards to display dark lines last.
        // Then loop over all displayed lines.
        for (int p=DrawingBotV3.pen_count-1; p>=0; p--) {
            task.output.println("<g id=\"" + CopicPenHelper.copic_sets[DrawingBotV3.INSTANCE.current_copic_set][p] + "\">");
            for (int i=1; i<line_count; i++) {
                if (task.plottedDrawing.lines[i].pen_number == p) {

                    // Do we add gcode_offsets needed by my bot, or zero based?
                    //float gcode_scaled_x1 = d1.lines[i].x1 * gcode_scale * svgdpi + gcode_offset_x;
                    //float gcode_scaled_y1 = d1.lines[i].y1 * gcode_scale * svgdpi + gcode_offset_y;
                    //float gcode_scaled_x2 = d1.lines[i].x2 * gcode_scale * svgdpi + gcode_offset_x;
                    //float gcode_scaled_y2 = d1.lines[i].y2 * gcode_scale * svgdpi + gcode_offset_y;

                    float gcode_scaled_x1 = task.plottedDrawing.lines[i].x1 * task.gcode_scale * svgdpi;
                    float gcode_scaled_y1 = task.plottedDrawing.lines[i].y1 * task.gcode_scale * svgdpi;
                    float gcode_scaled_x2 = task.plottedDrawing.lines[i].x2 * task.gcode_scale * svgdpi;
                    float gcode_scaled_y2 = task.plottedDrawing.lines[i].y2 * task.gcode_scale * svgdpi;

                    if (!task.plottedDrawing.lines[i].pen_continuation && drawing_polyline) {
                        task.output.println("\" />");
                        drawing_polyline = false;
                    }

                    if (task.plottedDrawing.lines[i].pen_down) {
                        if (task.plottedDrawing.lines[i].pen_continuation) {
                            String buf = svg_format(gcode_scaled_x2) + "," + svg_format(gcode_scaled_y2);
                            task.output.println(buf);
                            drawing_polyline = true;
                        } else {
                            int c = DrawingBotV3.INSTANCE.copic.get_original_color(CopicPenHelper.copic_sets[DrawingBotV3.INSTANCE.current_copic_set][p]);
                            task.output.println("<polyline fill=\"none\" stroke=\"#" + hex(c, 6) + "\" stroke-width=\"1.0\" stroke-opacity=\"1\" points=\"");
                            String buf = svg_format(gcode_scaled_x1) + "," + svg_format(gcode_scaled_y1);
                            task.output.println(buf);
                            drawing_polyline = true;
                        }
                    }
                }
            }
            if (drawing_polyline) {
                task.output.println("\" />");
                drawing_polyline = false;
            }
            task.output.println("</g>");
        }
        task.output.println("</svg>");
        task.output.flush();
        task.output.close();
        println("SVG created:  " + gname);
    }

}
