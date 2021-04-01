package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.geom.basic.IGeometry;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.Limit;
import drawingbot.utils.Utils;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class GCodeExporter {

    public static final String defaultStartCode =   "G21 (programming in millimeters, mm)" +
                                                    "\n" +
                                                    "G90 (programming in absolute positioning)" +
                                                    "\n" +
                                                    "G28 (auto homing)" +
                                                    "\n" +
                                                    "G1 F8000 (set speed)";
    public static final String defaultEndCode = "";
    public static final String defaultPenDownCode = "G1 Z0";
    public static final String defaultPenUpCode = "G1 Z1";


    public static void exportGCode(ExportTask exportTask, PlottingTask plottingTask, Map<Integer, List<IGeometry>> geometries, String extension, File saveLocation) {
        PrintWriter output = FileUtils.createWriter(saveLocation);
        GCodeBuilder builder = new GCodeBuilder(plottingTask, output);

        plottingTask.comments.forEach(builder::comment);
        builder.open();

        AffineTransform transform = plottingTask.createGCodeTransform();

        float[] coords = new float[6];
        for(List<IGeometry> geometryList : geometries.values()){
            int i = 0;
            for(IGeometry geometry : geometryList){
                PathIterator iterator = geometry.getAWTShape().getPathIterator(transform);
                while(!iterator.isDone()){
                    int type = iterator.currentSegment(coords);
                    builder.move(coords, type);
                    iterator.next();
                }
                i++;
                exportTask.updateProgress(i, geometryList.size()-1);
            }
        }
        builder.close();
        DrawingBotV3.logger.info("GCode File Created:  " +  saveLocation);
    }

    public static void exportGCodeTest(ExportTask exportTask, PlottingTask plottingTask, Map<Integer, List<IGeometry>> geometries, String extension, File saveLocation) {
        AffineTransform transform = plottingTask.createGCodeTransform();

        Limit dx = new Limit(), dy = new Limit();

        float[] coords = new float[6];
        for(List<IGeometry> geometryList : geometries.values()){
            int i = 0;
            for(IGeometry geometry : geometryList){
                PathIterator iterator = geometry.getAWTShape().getPathIterator(transform);
                while(!iterator.isDone()){
                    int type = iterator.currentSegment(coords);
                    dx.update_limit(coords[0]);
                    dy.update_limit(coords[1]);
                    iterator.next();
                }
                i++;
                exportTask.updateProgress(i, geometryList.size()-1);
            }
        }

        String gname = FileUtils.removeExtension(saveLocation) + "gcode_test" + extension;
        PrintWriter output = FileUtils.createWriter(new File(gname));
        GCodeBuilder builder = new GCodeBuilder(plottingTask, output);

        builder.comment("This is a test file to draw the extremes of the drawing area.");
        builder.comment("Draws a 1cm mark on all four corners of the paper.");
        builder.comment("WARNING:  pen will be down.");
        builder.comment("Extremes of X: " + dx.min + " thru " + dx.max);
        builder.comment("Extremes of Y: " + dy.min + " thru " + dy.max);

        builder.open();

        float test_length = 10;

        builder.comment("Upper left");
        builder.movePen(dx.min, dy.min + test_length);
        builder.movePenDown();
        builder.movePen(dx.min, dy.min);
        builder.movePen(dx.min + test_length, dy.min);
        builder.movePenUp();

        builder.comment("Upper right");
        builder.movePen(dx.max - test_length, dy.min);
        builder.movePenDown();
        builder.movePen(dx.max, dy.min);
        builder.movePen(dx.max, dy.min + test_length);
        builder.movePenUp();

        builder.comment("Lower right");
        builder.movePen(dx.max,dy.max - test_length);
        builder.movePenDown();
        builder.movePen(dx.max, dy.max);
        builder.movePen(dx.max - test_length, dy.max);
        builder.movePenUp();

        builder.comment("Lower left");
        builder.movePen(dx.min + test_length, dy.max);
        builder.movePenDown();
        builder.movePen(dx.min, dy.max);
        builder.movePen(dx.min, dy.max - test_length);
        builder.movePenUp();

        exportTask.updateProgress(1,1);
    }


}
