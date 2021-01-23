package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.plotting.PlottingTask;
import drawingbot.plotting.PlottedLine;

import java.io.File;

import static processing.core.PApplet.*;

public class GCodeExporter {

    public static void gcodeHeader(PlottingTask task) {
        task.output.println("G21"); //programming in millimeters, mm
        task.output.println("G90"); //programming in absolute positioning
        //task.output.println("G28"); //auto home
        task.output.println(movePenUp());
    }

    public static void gcodeTrailer(PlottingTask task) {
        task.output.println(movePenUp());
        task.output.println(movePen(0.1F, 0.1F));
        task.output.println(movePen(0, 0));
    }

    /**moves the pen using GCODE Number Format*/
    public static String movePen(float xValue, float yValue){
        return "G1 X" + gcodeFormat(xValue) + " Y" + gcodeFormat(yValue);
    }

    public static String movePenUp(){
        return "G1 Z0"; //MAKE THIS NUMBER CHANGEABLE!
    }

    public static String movePenDown(){
        return "G1 Z1";
    }

    public static void gcodeComment(PlottingTask task, String comment) {
        task.gcode_comments += ("(" + comment + ")") + "\n"; //TODO ADD GCODE COMMENTS LATER NOT DURING PLOTTING.
        println(comment);
    }

    public static String addComment(String comment){
        return "(" + comment.replace(")", "") + ")";
    }

    /**formats the value into GCODE Number Format*/
    public static String gcodeFormat(Float value) {
        String s = nf(value, 0, DrawingBotV3.gcode_decimals);
        s = s.replace('.', DrawingBotV3.gcode_decimal_seperator);
        s = s.replace(',', DrawingBotV3.gcode_decimal_seperator);
        return s;
    }

    public static void createGcodeFiles(PlottingTask task, File file) {
        File path = FileUtils.removeExtension(file);
        boolean is_pen_down;
        int pen_lifts;
        float pen_movement;
        float pen_drawing;
        int   lines_drawn;
        float x;
        float y;
        float distance;

        // Loop over all lines for every pen.
        for (int p = 0; p < task.plottedDrawing.getPenCount(); p ++) {
            is_pen_down = false;
            pen_lifts = 2;
            pen_movement = 0;
            pen_drawing = 0;
            lines_drawn = 0;
            x = 0;
            y = 0;
            String gname = path + "_pen" + p + "_" + task.plottedDrawing.getPen(p).getName() + ".txt";
            task.output = DrawingBotV3.INSTANCE.createWriter(gname);
            task.output.println(task.gcode_comments);
            gcodeHeader(task);

            for (int i = 0 ; i < task.plottedDrawing.getDisplayedLineCount(); i ++) {
                PlottedLine line = task.plottedDrawing.plottedLines.get(i);
                if (line.pen_number == p) {

                    float gcode_scaled_x1 = line.x1 * task.gcode_scale + task.gcode_offset_x;
                    float gcode_scaled_y1 = line.y1 * task.gcode_scale + task.gcode_offset_y;
                    float gcode_scaled_x2 = line.x2 * task.gcode_scale + task.gcode_offset_x;
                    float gcode_scaled_y2 = line.y2 * task.gcode_scale + task.gcode_offset_y;
                    distance = sqrt( sq(abs(gcode_scaled_x1 - gcode_scaled_x2)) + sq(abs(gcode_scaled_y1 - gcode_scaled_y2)) );

                    if (x != gcode_scaled_x1 || y != gcode_scaled_y1) {
                        // Oh crap, where the line starts is not where I am, pick up the pen and move there.
                        task.output.println(movePenUp());
                        is_pen_down = false;
                        distance = sqrt( sq(abs(x - gcode_scaled_x1)) + sq(abs(y - gcode_scaled_y1)) );
                        task.output.println(movePen(gcode_scaled_x1, gcode_scaled_y1));
                        x = gcode_scaled_x1;
                        y = gcode_scaled_y1;
                        pen_movement += distance;
                        pen_lifts++;
                    }

                    if (line.pen_down) {
                        if (!is_pen_down) {
                            task.output.println(movePenDown());
                            is_pen_down = true;
                        }
                        pen_drawing += distance;
                        lines_drawn++;
                    } else {
                        if (is_pen_down) {
                            task.output.println(movePenUp());
                            is_pen_down = false;
                            pen_movement += distance;
                            pen_lifts++;
                        }
                    }
                    task.output.println(movePen(gcode_scaled_x2, gcode_scaled_y2));
                    x = gcode_scaled_x2;
                    y = gcode_scaled_y2;
                    task.dx.update_limit(gcode_scaled_x2);
                    task.dy.update_limit(gcode_scaled_y2);
                }
            }

            gcodeTrailer(task);
            task.output.println(addComment("Drew " + lines_drawn + " lines for " + pen_drawing  / 25.4 / 12 + " feet"));
            task.output.println(addComment("Pen was lifted " + pen_lifts + " times for " + pen_movement  / 25.4 / 12 + " feet"));
            task.output.println(addComment("Extremes of X: " + task.dx.min + " thru " + task.dx.max));
            task.output.println(addComment("Extremes of Y: " + task.dy.min + " thru " + task.dy.max));
            task.output.flush();
            task.output.close();
            println("GCode File Created:  " + gname);
        }
    }

    //TODO CHECK - THIS RELIES ON THE LIMITS FROM THE GCODE ITSELF
    public static void createGcodeTestFile(PlottingTask task, File file) {
        // The dx.min are already scaled to drawingbot.gcode.
        float test_length = 25.4F * 2F;

        String gname = file + "gcode_test.txt";
        task.output = DrawingBotV3.INSTANCE.createWriter(DrawingBotV3.INSTANCE.sketchPath("") + gname);
        task.output.println(addComment("This is a test file to draw the extremes of the drawing area."));
        task.output.println(addComment("Draws a 2 inch mark on all four corners of the paper."));
        task.output.println(addComment("WARNING:  pen will be down."));
        task.output.println(addComment("Extremes of X: " + task.dx.min + " thru " + task.dx.max));
        task.output.println(addComment("Extremes of Y: " + task.dy.min + " thru " + task.dy.max));
        gcodeHeader(task);

        task.output.println(addComment("Upper left"));
        task.output.println(movePen(task.dx.min, task.dy.min + test_length));
        task.output.println(movePenDown());
        task.output.println(movePen(task.dx.min, task.dy.min));
        task.output.println(movePen(task.dx.min + test_length, task.dy.min));
        task.output.println(movePenUp());

        task.output.println(addComment("Upper right"));
        task.output.println(movePen(task.dx.max - test_length, task.dy.min));
        task.output.println(movePenDown());
        task.output.println(movePen(task.dx.max, task.dy.min));
        task.output.println(movePen(task.dx.max, task.dy.min + test_length));
        task.output.println(movePenUp());

        task.output.println(addComment("Lower right"));
        task.output.println(movePen(task.dx.max,task.dy.max - test_length));
        task.output.println(movePenDown());
        task.output.println(movePen(task.dx.max, task.dy.max));
        task.output.println(movePen(task.dx.max - test_length, task.dy.max));
        task.output.println(movePenUp());

        task.output.println(addComment("Lower left"));
        task.output.println(movePen(task.dx.min + test_length, task.dy.max));
        task.output.println(movePenDown());
        task.output.println(movePen(task.dx.min, task.dy.max));
        task.output.println(movePen(task.dx.min, task.dy.max - test_length));
        task.output.println(movePenUp());

        gcodeTrailer(task);
        task.output.flush();
        task.output.close();
        println("GCode Test Created:  " + gname);
    }


}
