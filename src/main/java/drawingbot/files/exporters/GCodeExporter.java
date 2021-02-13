package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.plotting.PlottingTask;
import drawingbot.plotting.PlottedLine;
import drawingbot.utils.Limit;
import drawingbot.utils.Utils;

import java.io.File;
import java.io.PrintWriter;
import java.util.function.BiFunction;

public class GCodeExporter {

    private static PrintWriter output;
    public static final int gcode_decimals = 3; // numbers of decimal places used on gcode exports
    public static final char gcode_decimal_seperator = '.';

    private static void gcodeHeader(PlottingTask task) {
        output.println("G21"); //programming in millimeters, mm
        output.println("G90"); //programming in absolute positioning
        if(DrawingBotV3.enableAutoHome.get()){
            output.println("G28");
        }
        output.println("G1 F8000"); //SET SPEED
        output.println(movePenUp());
    }

    private static void gcodeTrailer(PlottingTask task) {
        output.println(movePenUp());
        output.println(movePen(0.1F, 0.1F));
        output.println(movePen(0, 0));
    }

    /**moves the pen using GCODE Number Format*/
    private static String movePen(float xValue, float yValue){
        return "G1 X" + gcodeFormat(xValue) + " Y" + gcodeFormat(yValue);
    }

    private static String movePenUp(){
        return "G1 Z" + gcodeFormat(DrawingBotV3.penUpZ.get());
    }

    private static String movePenDown(){
        return "G1 Z" + gcodeFormat(DrawingBotV3.penDownZ.get());
    }

    private static String comment(String comment){
        return "(" + comment.replace(")", "") + ")" + "\n";
    }

    private static void addComment(String comment){
        output.println(comment(comment));
    }

    /**formats the value into GCODE Number Format*/
    private static String gcodeFormat(Float value) {
        String s = Utils.formatGCode(value);
        s = s.replace('.', gcode_decimal_seperator);
        s = s.replace(',', gcode_decimal_seperator);
        return s;
    }

    public static void exportGCode(ExportTask exportTask, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter, String extension, File saveLocation) {
        output = FileUtils.createWriter(saveLocation);
        plottingTask.comments.forEach(GCodeExporter::addComment); //add all task comments
        gcodeHeader(plottingTask);


        Limit dx = new Limit(), dy = new Limit();
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

                        float gcode_scaled_x1 = line.x1 * plottingTask.getGCodeScale() + plottingTask.getGCodeXOffset();
                        float gcode_scaled_y1 = line.y1 * plottingTask.getGCodeScale() + plottingTask.getGCodeYOffset();
                        float gcode_scaled_x2 = line.x2 * plottingTask.getGCodeScale() + plottingTask.getGCodeXOffset();
                        float gcode_scaled_y2 = line.y2 * plottingTask.getGCodeScale() + plottingTask.getGCodeYOffset();
                        double distance = Math.sqrt( Math.pow(Math.abs(gcode_scaled_x1 - gcode_scaled_x2), 2) + Math.pow(Math.abs(gcode_scaled_y1 - gcode_scaled_y2), 2) );

                        if (x != gcode_scaled_x1 || y != gcode_scaled_y1) {
                            // Oh crap, where the line starts is not where I am, pick up the pen and move there.
                            output.println(movePenUp());
                            is_pen_down = false;
                            distance = Math.sqrt( Math.pow(Math.abs(x - gcode_scaled_x1), 2) + Math.pow(Math.abs(y - gcode_scaled_y1), 2) );
                            output.println(movePen(gcode_scaled_x1, gcode_scaled_y1));
                            x = gcode_scaled_x1;
                            y = gcode_scaled_y1;
                            pen_movement += distance;
                            pen_lifts++;
                        }

                        if (line.pen_down) {
                            if (!is_pen_down) {
                                output.println(movePenDown());
                                is_pen_down = true;
                            }
                            pen_drawing += distance;
                            lines_drawn++;
                        } else {
                            if (is_pen_down) {
                                output.println(movePenUp());
                                is_pen_down = false;
                                pen_movement += distance;
                                pen_lifts++;
                            }
                        }
                        output.println(movePen(gcode_scaled_x2, gcode_scaled_y2));
                        x = gcode_scaled_x2;
                        y = gcode_scaled_y2;
                        dx.update_limit(gcode_scaled_x2);
                        dy.update_limit(gcode_scaled_y2);
                    }
                    completedLines++;
                }
                exportTask.updateProgress(completedLines, plottingTask.plottedDrawing.getDisplayedLineCount());
            }
        }

        gcodeTrailer(plottingTask);
        output.println(comment("Drew " + lines_drawn + " lines for " + pen_drawing  / 25.4 / 12 + " feet"));
        output.println(comment("Pen was lifted " + pen_lifts + " times for " + pen_movement  / 25.4 / 12 + " feet"));
        output.println(comment("Extremes of X: " + dx.min + " thru " + dx.max));
        output.println(comment("Extremes of Y: " + dy.min + " thru " + dy.max));
        output.flush();
        output.close();
        output = null;
        DrawingBotV3.logger.info("GCode File Created:  " +  saveLocation);
    }

    //TODO PREVENT GCODE TEST FILES BEING EXPORTED "PER PEN"
    public static void createGcodeTestFile(ExportTask exportTask, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter, String extension, File saveLocation) {

        Limit dx = new Limit(), dy = new Limit();
        for (PlottedLine line : plottingTask.plottedDrawing.plottedLines) { //to allow the export of the gcode test file seperately we must update the limits
            float gcode_scaled_x1 = line.x1 * plottingTask.getGCodeScale() + plottingTask.getGCodeXOffset();
            float gcode_scaled_y1 = line.y1 * plottingTask.getGCodeScale() + plottingTask.getGCodeYOffset();
            float gcode_scaled_x2 = line.x2 * plottingTask.getGCodeScale() + plottingTask.getGCodeXOffset();
            float gcode_scaled_y2 = line.y2 * plottingTask.getGCodeScale() + plottingTask.getGCodeYOffset();

            dx.update_limit(gcode_scaled_x1);
            dx.update_limit(gcode_scaled_x2);

            dy.update_limit(gcode_scaled_y1);
            dy.update_limit(gcode_scaled_y2);
        }


        float test_length = 10;

        String gname = FileUtils.removeExtension(saveLocation) + "gcode_test" + extension;
        output = FileUtils.createWriter(new File(gname));
        output.println(comment("This is a test file to draw the extremes of the drawing area."));
        output.println(comment("Draws a 1cm mark on all four corners of the paper."));
        output.println(comment("WARNING:  pen will be down."));
        output.println(comment("Extremes of X: " + dx.min + " thru " + dx.max));
        output.println(comment("Extremes of Y: " + dy.min + " thru " + dy.max));
        gcodeHeader(plottingTask);

        output.println(comment("Upper left"));
        output.println(movePen(dx.min, dy.min + test_length));
        output.println(movePenDown());
        output.println(movePen(dx.min, dy.min));
        output.println(movePen(dx.min + test_length, dy.min));
        output.println(movePenUp());

        output.println(comment("Upper right"));
        output.println(movePen(dx.max - test_length, dy.min));
        output.println(movePenDown());
        output.println(movePen(dx.max, dy.min));
        output.println(movePen(dx.max, dy.min + test_length));
        output.println(movePenUp());

        output.println(comment("Lower right"));
        output.println(movePen(dx.max,dy.max - test_length));
        output.println(movePenDown());
        output.println(movePen(dx.max, dy.max));
        output.println(movePen(dx.max - test_length, dy.max));
        output.println(movePenUp());

        output.println(comment("Lower left"));
        output.println(movePen(dx.min + test_length, dy.max));
        output.println(movePenDown());
        output.println(movePen(dx.min, dy.max));
        output.println(movePen(dx.min, dy.max - test_length));
        output.println(movePenUp());

        gcodeTrailer(plottingTask);
        output.flush();
        output.close();
        output = null;
        DrawingBotV3.logger.info("GCode Test Created:  " + gname);

        exportTask.updateProgress(1,1);
    }


}
