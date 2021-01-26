package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.plotting.PlottingTask;
import drawingbot.plotting.PlottedLine;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.function.BiFunction;

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

    public static void exportGCode(ExportTask exportTask, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter, String extension, File saveLocation) {
        plottingTask.output = createWriter(saveLocation);
        plottingTask.output.println(plottingTask.gcode_comments);
        gcodeHeader(plottingTask);

        boolean is_pen_down = false;
        int lines_drawn = 0;
        int pen_lifts = 2;
        float pen_movement = 0;
        float pen_drawing = 0;
        float x = 0, y = 0;

        int completedLines = 0;

        // Loop over all lines for every pen.
        for (int p = 0; p < plottingTask.plottedDrawing.getPenCount(); p ++) {
            ObservableDrawingPen drawingPen = plottingTask.plottedDrawing.drawingPenSet.getPens().get(p);
            for (int i = 0 ; i < plottingTask.plottedDrawing.getDisplayedLineCount(); i ++) {
                PlottedLine line = plottingTask.plottedDrawing.plottedLines.get(i);
                if(line.pen_number == p){
                    if (lineFilter.apply(line, drawingPen)) { // we apply the line filter also.

                        float gcode_scaled_x1 = line.x1 * plottingTask.gcode_scale + plottingTask.gcode_offset_x;
                        float gcode_scaled_y1 = line.y1 * plottingTask.gcode_scale + plottingTask.gcode_offset_y;
                        float gcode_scaled_x2 = line.x2 * plottingTask.gcode_scale + plottingTask.gcode_offset_x;
                        float gcode_scaled_y2 = line.y2 * plottingTask.gcode_scale + plottingTask.gcode_offset_y;
                        float distance = sqrt( sq(abs(gcode_scaled_x1 - gcode_scaled_x2)) + sq(abs(gcode_scaled_y1 - gcode_scaled_y2)) );

                        if (x != gcode_scaled_x1 || y != gcode_scaled_y1) {
                            // Oh crap, where the line starts is not where I am, pick up the pen and move there.
                            plottingTask.output.println(movePenUp());
                            is_pen_down = false;
                            distance = sqrt( sq(abs(x - gcode_scaled_x1)) + sq(abs(y - gcode_scaled_y1)) );
                            plottingTask.output.println(movePen(gcode_scaled_x1, gcode_scaled_y1));
                            x = gcode_scaled_x1;
                            y = gcode_scaled_y1;
                            pen_movement += distance;
                            pen_lifts++;
                        }

                        if (line.pen_down) {
                            if (!is_pen_down) {
                                plottingTask.output.println(movePenDown());
                                is_pen_down = true;
                            }
                            pen_drawing += distance;
                            lines_drawn++;
                        } else {
                            if (is_pen_down) {
                                plottingTask.output.println(movePenUp());
                                is_pen_down = false;
                                pen_movement += distance;
                                pen_lifts++;
                            }
                        }
                        plottingTask.output.println(movePen(gcode_scaled_x2, gcode_scaled_y2));
                        x = gcode_scaled_x2;
                        y = gcode_scaled_y2;
                        plottingTask.dx.update_limit(gcode_scaled_x2);
                        plottingTask.dy.update_limit(gcode_scaled_y2);
                    }
                    completedLines++;
                }
                exportTask.updateProgress(completedLines, plottingTask.plottedDrawing.getDisplayedLineCount());
            }
        }

        gcodeTrailer(plottingTask);
        plottingTask.output.println(addComment("Drew " + lines_drawn + " lines for " + pen_drawing  / 25.4 / 12 + " feet"));
        plottingTask.output.println(addComment("Pen was lifted " + pen_lifts + " times for " + pen_movement  / 25.4 / 12 + " feet"));
        plottingTask.output.println(addComment("Extremes of X: " + plottingTask.dx.min + " thru " + plottingTask.dx.max));
        plottingTask.output.println(addComment("Extremes of Y: " + plottingTask.dy.min + " thru " + plottingTask.dy.max));
        plottingTask.output.flush();
        plottingTask.output.close();
        println("GCode File Created:  " + saveLocation);
    }

    //TODO PREVENT GCODE TEST FILES BEING EXPORTED "PER PEN"
    public static void createGcodeTestFile(ExportTask exportTask, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter, String extension, File saveLocation) {

        for (PlottedLine line : plottingTask.plottedDrawing.plottedLines) { //to allow the export of the gcode test file seperately we must update the limits
            float gcode_scaled_x1 = line.x1 * plottingTask.gcode_scale + plottingTask.gcode_offset_x;
            float gcode_scaled_y1 = line.y1 * plottingTask.gcode_scale + plottingTask.gcode_offset_y;
            float gcode_scaled_x2 = line.x2 * plottingTask.gcode_scale + plottingTask.gcode_offset_x;
            float gcode_scaled_y2 = line.y2 * plottingTask.gcode_scale + plottingTask.gcode_offset_y;

            plottingTask.dx.update_limit(gcode_scaled_x1);
            plottingTask.dx.update_limit(gcode_scaled_x2);

            plottingTask.dy.update_limit(gcode_scaled_y1);
            plottingTask.dy.update_limit(gcode_scaled_y2);
        }


        float test_length = 25.4F * 2F; //TODO CHECK ME?

        String gname = FileUtils.removeExtension(saveLocation) + "gcode_test" + extension;
        plottingTask.output = DrawingBotV3.INSTANCE.createWriter(gname);
        plottingTask.output.println(addComment("This is a test file to draw the extremes of the drawing area."));
        plottingTask.output.println(addComment("Draws a 2 inch mark on all four corners of the paper."));
        plottingTask.output.println(addComment("WARNING:  pen will be down."));
        plottingTask.output.println(addComment("Extremes of X: " + plottingTask.dx.min + " thru " + plottingTask.dx.max));
        plottingTask.output.println(addComment("Extremes of Y: " + plottingTask.dy.min + " thru " + plottingTask.dy.max));
        gcodeHeader(plottingTask);

        plottingTask.output.println(addComment("Upper left"));
        plottingTask.output.println(movePen(plottingTask.dx.min, plottingTask.dy.min + test_length));
        plottingTask.output.println(movePenDown());
        plottingTask.output.println(movePen(plottingTask.dx.min, plottingTask.dy.min));
        plottingTask.output.println(movePen(plottingTask.dx.min + test_length, plottingTask.dy.min));
        plottingTask.output.println(movePenUp());

        plottingTask.output.println(addComment("Upper right"));
        plottingTask.output.println(movePen(plottingTask.dx.max - test_length, plottingTask.dy.min));
        plottingTask.output.println(movePenDown());
        plottingTask.output.println(movePen(plottingTask.dx.max, plottingTask.dy.min));
        plottingTask.output.println(movePen(plottingTask.dx.max, plottingTask.dy.min + test_length));
        plottingTask.output.println(movePenUp());

        plottingTask.output.println(addComment("Lower right"));
        plottingTask.output.println(movePen(plottingTask.dx.max,plottingTask.dy.max - test_length));
        plottingTask.output.println(movePenDown());
        plottingTask.output.println(movePen(plottingTask.dx.max, plottingTask.dy.max));
        plottingTask.output.println(movePen(plottingTask.dx.max - test_length, plottingTask.dy.max));
        plottingTask.output.println(movePenUp());

        plottingTask.output.println(addComment("Lower left"));
        plottingTask.output.println(movePen(plottingTask.dx.min + test_length, plottingTask.dy.max));
        plottingTask.output.println(movePenDown());
        plottingTask.output.println(movePen(plottingTask.dx.min, plottingTask.dy.max));
        plottingTask.output.println(movePen(plottingTask.dx.min, plottingTask.dy.max - test_length));
        plottingTask.output.println(movePenUp());

        gcodeTrailer(plottingTask);
        plottingTask.output.flush();
        plottingTask.output.close();
        println("GCode Test Created:  " + gname);

        exportTask.updateProgress(1,1);
    }


}
