package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.utils.Limit;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GCodeExporter {

    public static final String defaultStartCode = """
            G21 (programming in millimeters, mm)
            G90 (programming in absolute positioning)
            G28 (auto homing)
            G1 F8000 (set speed)""";
    public static final String defaultEndCode = """
            G0 Z1
            G0 X0 Y0
            """;
    public static final String defaultPenDownCode = """
            G1 Z0
            """;
    public static final String defaultPenUpCode = """
            G0 Z1
            """;
    public static final String defaultStartLayerCode = "";
    public static final String defaultEndLayerCode = "";


    public static List<GCodeWildcard> wildcards = new ArrayList<>();
    static {
        wildcards.add(new GCodeWildcard("%PEN_NAME%") {
            @Override
            public String formatWildcard(GCodeBuilder builder, String string) {
                //Legacy: fix for old wildcard ids
                string = string.replaceAll("%LAYER_NAME%", builder.layerName);

                return string.replaceAll(wildcard, builder.layerName);
            }
        });
        wildcards.add(new GCodeWildcard("%PEN_ID%") {
            @Override
            public String formatWildcard(GCodeBuilder builder, String string) {
                string = string.replaceAll(wildcard, "" + builder.layerID);

                //TODO PARSE PLUS + MINUS

                return string;
            }
        });
    }

    public static String replaceWildcards(GCodeBuilder builder, String command){
        for(GCodeWildcard wildcard : GCodeExporter.wildcards){
            command = wildcard.formatWildcard(builder, command);
        }
        return command;
    }


    public static AffineTransform createGCodeTransform(ICanvas canvas, GCodeSettings settings){
        AffineTransform transform = new AffineTransform();

        ///translate by the gcode offset
        transform.translate(settings.getGCodeXOffset(), settings.getGCodeYOffset());

        ///move into print scale
        transform.scale(1/canvas.getPlottingScale(), 1/canvas.getPlottingScale());

        //g-code y numbers go the other way
        transform.translate(0, canvas.getScaledHeight());

        //move with pre-scaled offsets
        transform.translate(canvas.getScaledDrawingOffsetX(), -canvas.getScaledDrawingOffsetY());

        if(settings.gcodeCenterZeroPoint.get()){
            transform.translate(-canvas.getScaledWidth()/2, -canvas.getScaledHeight()/2);
        }

        //flip y coordinates
        transform.scale(1, -1);
        return transform;
    }

    public static void exportGCode(ExportTask exportTask, File saveLocation){
        exportGCode(exportTask, DBPreferences.INSTANCE.gcodeSettings, saveLocation);
    }

    public static void exportGCode(ExportTask exportTask, GCodeSettings settings, File saveLocation){

        PrintWriter output = exportTask.createFileWriter(saveLocation);
        if(output == null){
            return;
        }

        GCodeBuilder builder = new GCodeBuilder(exportTask, settings, output);

        builder.open();

        AffineTransform transform = createGCodeTransform(exportTask.exportDrawing.getCanvas(), settings);

        float[] coords = new float[6];

        int index = 0;
        for(ObservableDrawingPen drawingPen : exportTask.exportRenderOrder){

            exportTask.exportIterator.reset();
            builder.layerName = drawingPen.getName();
            builder.layerID = index;
            builder.startLayer();
            while(exportTask.exportIterator.hasNext()){
                IGeometry geometry = exportTask.exportIterator.next();

                if(exportTask.exportIterator.currentPen == drawingPen) {
                    PathIterator iterator;
                    if (settings.gcodeEnableFlattening.get()) {
                        iterator = geometry.getAWTShape().getPathIterator(transform, settings.gcodeCurveFlatness.get());
                    } else {
                        iterator = geometry.getAWTShape().getPathIterator(transform);
                    }

                    builder.movePenUp(); //Check the pen is raised before we start drawing, the command will only be added if needed
                    while (!iterator.isDone()) {
                        int type = iterator.currentSegment(coords);
                        builder.move(coords, type);
                        iterator.next();
                    }
                    builder.movePenUp(); //Raise the pen once we've finished drawing, the command will only be added if needed

                    exportTask.onGeometryExported();
                }
            }
            builder.endLayer();
            index++;
        }
        builder.close();
        DrawingBotV3.logger.info("GCode File Created:  " +  saveLocation);
    }

    public static void exportGCodeTest(ExportTask exportTask, File saveLocation) {
        exportGCodeTest(exportTask, DBPreferences.INSTANCE.gcodeSettings, saveLocation);
    }

    public static void exportGCodeTest(ExportTask exportTask, GCodeSettings settings, File saveLocation) {
        String gname = FileUtils.removeExtension(saveLocation) + "gcode_test" + exportTask.extension;
        PrintWriter output = exportTask.createFileWriter(new File(gname));
        if(output == null){
            return;
        }
        AffineTransform transform = createGCodeTransform(exportTask.exportDrawing.getCanvas(), settings);

        Limit dx = new Limit(), dy = new Limit();

        float[] coords = new float[6];

        exportTask.exportIterator.reset();
        while(exportTask.exportIterator.hasNext()){
            IGeometry geometry = exportTask.exportIterator.next();
            PathIterator iterator;
            if(settings.gcodeEnableFlattening.get()){
                iterator = geometry.getAWTShape().getPathIterator(transform, settings.gcodeCurveFlatness.get() / transform.getScaleX());
            }else{
                iterator = geometry.getAWTShape().getPathIterator(transform);
            }
            while(!iterator.isDone()){
                int type = iterator.currentSegment(coords);
                dx.updateLimit(coords[0]);
                dy.updateLimit(coords[1]);
                iterator.next();
            }
            exportTask.onGeometryExported();
        }

        GCodeBuilder builder = new GCodeBuilder(exportTask, settings, output);

        builder.comment("This is a test file to draw the extremes of the drawing area.");
        builder.comment("Draws a 1cm mark on all four corners of the paper.");
        builder.comment("WARNING:  pen will be down.");
        builder.comment("Extremes of X: " + dx.min + " thru " + dx.max);
        builder.comment("Extremes of Y: " + dy.min + " thru " + dy.max);

        builder.open();

        float test_length = 10;

        builder.comment("Upper left");
        builder.linearMoveG1(dx.min, dy.min + test_length);
        builder.movePenDown();
        builder.linearMoveG1(dx.min, dy.min);
        builder.linearMoveG1(dx.min + test_length, dy.min);
        builder.movePenUp();

        builder.comment("Upper right");
        builder.linearMoveG1(dx.max - test_length, dy.min);
        builder.movePenDown();
        builder.linearMoveG1(dx.max, dy.min);
        builder.linearMoveG1(dx.max, dy.min + test_length);
        builder.movePenUp();

        builder.comment("Lower right");
        builder.linearMoveG1(dx.max,dy.max - test_length);
        builder.movePenDown();
        builder.linearMoveG1(dx.max, dy.max);
        builder.linearMoveG1(dx.max - test_length, dy.max);
        builder.movePenUp();

        builder.comment("Lower left");
        builder.linearMoveG1(dx.min + test_length, dy.max);
        builder.movePenDown();
        builder.linearMoveG1(dx.min, dy.max);
        builder.linearMoveG1(dx.min, dy.max - test_length);
        builder.movePenUp();

        exportTask.updateProgress(1,1);

        builder.close();
    }


}
