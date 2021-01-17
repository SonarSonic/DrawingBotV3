package drawingbot.helpers;

import drawingbot.DrawingBotV3;

import static processing.core.PApplet.*;

public class GCodeHelper {

    public static DrawingBotV3 app = DrawingBotV3.INSTANCE;

    public static void gcode_header() {
        app.output.println("G21");
        app.output.println("G90");
        app.output.println("G1 Z0");
    }

    public static void gcode_trailer() {
        app.output.println("G1 Z0");
        app.output.println("G1 X" + gcode_format(0.1F) + " Y" + gcode_format(0.1F));
        app.output.println("G1 X0 y0");
    }

    public static void gcode_comment(String comment) {
        app.gcode_comments += ("(" + comment + ")") + "\n";
        println(comment);
    }

    public static void pen_up() {
        app.is_pen_down = false;
    }

    public static void pen_down() {
        app.is_pen_down = true;
    }

    public static void move_abs(int pen_number, float x, float y) {
        app.plottedDrawing.addline(pen_number, app.is_pen_down, app.old_x, app.old_y, x, y);
        if (app.is_pen_down) {
            app.plottedDrawing.render_last();
        }

        app.old_x = x;
        app.old_y = y;
    }


    public static String gcode_format (Float n) {
        String s = nf(n, 0, app.gcode_decimals);
        s = s.replace('.', app.gcode_decimal_seperator);
        s = s.replace(',', app.gcode_decimal_seperator);
        return s;
    }

    public static void create_gcode_files(int line_count) {
        boolean is_pen_down;
        int pen_lifts;
        float pen_movement;
        float pen_drawing;
        int   lines_drawn;
        float x;
        float y;
        float distance;

        // Loop over all lines for every pen.
        for (int p = 0; p < app.pen_count; p ++) {
            is_pen_down = false;
            pen_lifts = 2;
            pen_movement = 0;
            pen_drawing = 0;
            lines_drawn = 0;
            x = 0;
            y = 0;
            String gname = "drawingbot.gcode\\gcode_" + app.basefile_selected + "_pen" + p + "_" + CopicPenHelper.copic_sets[app.current_copic_set][p] + ".txt";
            app.output = app.createWriter(app.sketchPath("") + gname);
            app.output.println(app.gcode_comments);
            gcode_header();

            for (int i = 1 ; i < line_count; i ++) {
                if (app.plottedDrawing.lines[i].pen_number == p) {

                    float gcode_scaled_x1 = app.plottedDrawing.lines[i].x1 * app.gcode_scale + app.gcode_offset_x;
                    float gcode_scaled_y1 = app.plottedDrawing.lines[i].y1 * app.gcode_scale + app.gcode_offset_y;
                    float gcode_scaled_x2 = app.plottedDrawing.lines[i].x2 * app.gcode_scale + app.gcode_offset_x;
                    float gcode_scaled_y2 = app.plottedDrawing.lines[i].y2 * app.gcode_scale + app.gcode_offset_y;
                    distance = sqrt( sq(abs(gcode_scaled_x1 - gcode_scaled_x2)) + sq(abs(gcode_scaled_y1 - gcode_scaled_y2)) );

                    if (x != gcode_scaled_x1 || y != gcode_scaled_y1) {
                        // Oh crap, where the line starts is not where I am, pick up the pen and move there.
                        app.output.println("G1 Z0");
                        is_pen_down = false;
                        distance = sqrt( sq(abs(x - gcode_scaled_x1)) + sq(abs(y - gcode_scaled_y1)) );
                        String buf = "G1 X" + gcode_format(gcode_scaled_x1) + " Y" + gcode_format(gcode_scaled_y1);
                        app.output.println(buf);
                        x = gcode_scaled_x1;
                        y = gcode_scaled_y1;
                        pen_movement = pen_movement + distance;
                        pen_lifts++;
                    }

                    if (app.plottedDrawing.lines[i].pen_down) {
                        if (!is_pen_down) {
                            app.output.println("G1 Z1");
                            is_pen_down = true;
                        }
                        pen_drawing = pen_drawing + distance;
                        lines_drawn++;
                    } else {
                        if (is_pen_down) {
                            app.output.println("G1 Z0");
                            is_pen_down = false;
                            pen_movement = pen_movement + distance;
                            pen_lifts++;
                        }
                    }

                    String buf = "G1 X" + gcode_format(gcode_scaled_x2) + " Y" + gcode_format(gcode_scaled_y2);
                    app.output.println(buf);
                    x = gcode_scaled_x2;
                    y = gcode_scaled_y2;
                    app.dx.update_limit(gcode_scaled_x2);
                    app.dy.update_limit(gcode_scaled_y2);
                }
            }

            gcode_trailer();
            app.output.println("(Drew " + lines_drawn + " lines for " + pen_drawing  / 25.4 / 12 + " feet)");
            app.output.println("(Pen was lifted " + pen_lifts + " times for " + pen_movement  / 25.4 / 12 + " feet)");
            app.output.println("(Extreams of X: " + app.dx.min + " thru " + app.dx.max + ")");
            app.output.println("(Extreams of Y: " + app.dy.min + " thru " + app.dy.max + ")");
            app.output.flush();
            app.output.close();
            println("drawingbot.gcode created:  " + gname);
        }
    }

    public static void create_gcode_test_file () {
        // The dx.min are already scaled to drawingbot.gcode.
        float test_length = 25.4F * 2F;

        String gname = "drawingbot.gcode\\gcode_" + app.basefile_selected + "_test.txt";
        app.output = app.createWriter(app.sketchPath("") + gname);
        app.output.println("(This is a test file to draw the extreams of the drawing area.)");
        app.output.println("(Draws a 2 inch mark on all four corners of the paper.)");
        app.output.println("(WARNING:  pen will be down.)");
        app.output.println("(Extreams of X: " + app.dx.min + " thru " + app.dx.max + ")");
        app.output.println("(Extreams of Y: " + app.dy.min + " thru " + app.dy.max + ")");
        gcode_header();

        app.output.println("(Upper left)");
        app.output.println("G1 X" + gcode_format(app.dx.min) + " Y" + gcode_format(app.dy.min + test_length));
        app.output.println("G1 Z1");
        app.output.println("G1 X" + gcode_format(app.dx.min) + " Y" + gcode_format(app.dy.min));
        app.output.println("G1 X" + gcode_format(app.dx.min + test_length) + " Y" + gcode_format(app.dy.min));
        app.output.println("G1 Z0");

        app.output.println("(Upper right)");
        app.output.println("G1 X" + gcode_format(app.dx.max - test_length) + " Y" + gcode_format(app.dy.min));
        app.output.println("G1 Z1");
        app.output.println("G1 X" + gcode_format(app.dx.max) + " Y" + gcode_format(app.dy.min));
        app.output.println("G1 X" + gcode_format(app.dx.max) + " Y" + gcode_format(app.dy.min + test_length));
        app.output.println("G1 Z0");

        app.output.println("(Lower right)");
        app.output.println("G1 X" + gcode_format(app.dx.max) + " Y" + gcode_format(app.dy.max - test_length));
        app.output.println("G1 Z1");
        app.output.println("G1 X" + gcode_format(app.dx.max) + " Y" + gcode_format(app.dy.max));
        app.output.println("G1 X" + gcode_format(app.dx.max - test_length) + " Y" + gcode_format(app.dy.max));
        app.output.println("G1 Z0");

        app.output.println("(Lower left)");
        app.output.println("G1 X" + gcode_format(app.dx.min + test_length) + " Y" + gcode_format(app.dy.max));
        app.output.println("G1 Z1");
        app.output.println("G1 X" + gcode_format(app.dx.min) + " Y" + gcode_format(app.dy.max));
        app.output.println("G1 X" + gcode_format(app.dx.min) + " Y" + gcode_format(app.dy.max - test_length));
        app.output.println("G1 Z0");

        gcode_trailer();
        app.output.flush();
        app.output.close();
        println("drawingbot.gcode test created:  " + gname);
    }


    // Thanks to Vladimir Bochkov for helping me debug the SVG international decimal separators problem.
    public static String svg_format (Float n) {
        final char regional_decimal_separator = ',';
        final char svg_decimal_seperator = '.';

        String s = nf(n, 0, app.svg_decimals);
        s = s.replace(regional_decimal_separator, svg_decimal_seperator);
        return s;
    }


    // Thanks to John Cliff for getting the SVG output moving forward.
    public static void create_svg_file (int line_count) {
        boolean drawing_polyline = false;

        // Inkscape versions before 0.91 used 90dpi, Today most software assumes 96dpi.
        float svgdpi = 96.0F / 25.4F;

        String gname = "drawingbot.gcode\\gcode_" + app.basefile_selected + ".svg";
        app.output = app.createWriter(app.sketchPath("") + gname);
        app.output.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        app.output.println("<svg width=\"" + svg_format(app.img.width * app.gcode_scale) + "mm\" height=\"" + svg_format(app.img.height * app.gcode_scale) + "mm\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">");
        app.plottedDrawing.set_pen_continuation_flags();

        // Loop over pens backwards to display dark lines last.
        // Then loop over all displayed lines.
        for (int p=app.pen_count-1; p>=0; p--) {
            app.output.println("<g id=\"" + CopicPenHelper.copic_sets[app.current_copic_set][p] + "\">");
            for (int i=1; i<line_count; i++) {
                if (app.plottedDrawing.lines[i].pen_number == p) {

                    // Do we add gcode_offsets needed by my bot, or zero based?
                    //float gcode_scaled_x1 = d1.lines[i].x1 * gcode_scale * svgdpi + gcode_offset_x;
                    //float gcode_scaled_y1 = d1.lines[i].y1 * gcode_scale * svgdpi + gcode_offset_y;
                    //float gcode_scaled_x2 = d1.lines[i].x2 * gcode_scale * svgdpi + gcode_offset_x;
                    //float gcode_scaled_y2 = d1.lines[i].y2 * gcode_scale * svgdpi + gcode_offset_y;

                    float gcode_scaled_x1 = app.plottedDrawing.lines[i].x1 * app.gcode_scale * svgdpi;
                    float gcode_scaled_y1 = app.plottedDrawing.lines[i].y1 * app.gcode_scale * svgdpi;
                    float gcode_scaled_x2 = app.plottedDrawing.lines[i].x2 * app.gcode_scale * svgdpi;
                    float gcode_scaled_y2 = app.plottedDrawing.lines[i].y2 * app.gcode_scale * svgdpi;

                    if (!app.plottedDrawing.lines[i].pen_continuation && drawing_polyline) {
                        app.output.println("\" />");
                        drawing_polyline = false;
                    }

                    if (app.plottedDrawing.lines[i].pen_down) {
                        if (app.plottedDrawing.lines[i].pen_continuation) {
                            String buf = svg_format(gcode_scaled_x2) + "," + svg_format(gcode_scaled_y2);
                            app.output.println(buf);
                            drawing_polyline = true;
                        } else {
                            int c = app.copic.get_original_color(CopicPenHelper.copic_sets[app.current_copic_set][p]);
                            app.output.println("<polyline fill=\"none\" stroke=\"#" + hex(c, 6) + "\" stroke-width=\"1.0\" stroke-opacity=\"1\" points=\"");
                            String buf = svg_format(gcode_scaled_x1) + "," + svg_format(gcode_scaled_y1);
                            app.output.println(buf);
                            drawing_polyline = true;
                        }
                    }
                }
            }
            if (drawing_polyline) {
                app.output.println("\" />");
                drawing_polyline = false;
            }
            app.output.println("</g>");
        }
        app.output.println("</svg>");
        app.output.flush();
        app.output.close();
        println("SVG created:  " + gname);
    }

}
