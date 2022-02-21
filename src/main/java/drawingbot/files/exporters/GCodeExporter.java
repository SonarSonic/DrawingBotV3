package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.geom.basic.IGeometry;
import drawingbot.image.PrintResolution;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.Limit;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.PrintWriter;

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
    public static final String defaultPenUpCode = "G0 Z1";
    public static final String defaultStartLayerCode = "";
    public static final String defaultEndLayerCode = "";


    public static float getGCodeXOffset(){
        return DrawingBotV3.INSTANCE.gcodeUnits.get().toMM(DrawingBotV3.INSTANCE.gcodeOffsetX.get());
    }

    public static float getGCodeYOffset(){
        return DrawingBotV3.INSTANCE.gcodeUnits.get().toMM(DrawingBotV3.INSTANCE.gcodeOffsetY.get());
    }

    public static AffineTransform createGCodeTransform(PrintResolution resolution){
        AffineTransform transform = new AffineTransform();

        ///translate by the gcode offset
        transform.translate(getGCodeXOffset(), getGCodeYOffset());

        ///move into print scale
        transform.scale(resolution.getPrintScale(), resolution.getPrintScale());

        //g-code y numbers go the other way
        transform.translate(0, resolution.getScaledHeight());

        //move with pre-scaled offsets
        transform.translate(resolution.getScaledOffsetX(), -resolution.getScaledOffsetY());

        if(DrawingBotV3.INSTANCE.gcodeCenterZeroPoint.get()){
            transform.translate(-resolution.getScaledWidth()/2, -resolution.getScaledHeight()/2);
        }

        //flip y coordinates
        transform.scale(1, -1);
        return transform;
    }

    public static void exportGCode(ExportTask exportTask, File saveLocation){
        PrintWriter output = FileUtils.createWriter(saveLocation);
        GCodeBuilder builder = new GCodeBuilder(exportTask, output);

        builder.open();

        AffineTransform transform = createGCodeTransform(exportTask.exportResolution);

        float[] coords = new float[6];

        for(ObservableDrawingPen drawingPen : exportTask.exportRenderOrder){

            exportTask.exportIterator.reset();
            builder.startLayer(drawingPen.getName());
            while(exportTask.exportIterator.hasNext()){
                IGeometry geometry = exportTask.exportIterator.next();

                if(exportTask.exportIterator.currentPen == drawingPen) {
                    PathIterator iterator;
                    if (DrawingBotV3.INSTANCE.gcodeEnableFlattening.get()) {
                        iterator = geometry.getAWTShape().getPathIterator(transform, DrawingBotV3.INSTANCE.gcodeCurveFlatness.get());
                    } else {
                        iterator = geometry.getAWTShape().getPathIterator(transform);
                    }

                    while (!iterator.isDone()) {
                        int type = iterator.currentSegment(coords);
                        builder.move(coords, type);
                        iterator.next();
                    }
                    exportTask.onGeometryExported();
                }
            }
            builder.endLayer(drawingPen.getName());
        }
        builder.close();
        DrawingBotV3.logger.info("GCode File Created:  " +  saveLocation);
    }

    public static void exportGCodeTest(ExportTask exportTask, File saveLocation){
        AffineTransform transform = createGCodeTransform(exportTask.exportResolution);

        Limit dx = new Limit(), dy = new Limit();

        float[] coords = new float[6];

        exportTask.exportIterator.reset();
        while(exportTask.exportIterator.hasNext()){
            IGeometry geometry = exportTask.exportIterator.next();
            PathIterator iterator;
            if(DrawingBotV3.INSTANCE.gcodeEnableFlattening.get()){
                iterator = geometry.getAWTShape().getPathIterator(transform, DrawingBotV3.INSTANCE.gcodeCurveFlatness.get() / transform.getScaleX());
            }else{
                iterator = geometry.getAWTShape().getPathIterator(transform);
            }
            while(!iterator.isDone()){
                int type = iterator.currentSegment(coords);
                dx.update_limit(coords[0]);
                dy.update_limit(coords[1]);
                iterator.next();
            }
            exportTask.onGeometryExported();
        }

        String gname = FileUtils.removeExtension(saveLocation) + "gcode_test" + exportTask.extension;
        PrintWriter output = FileUtils.createWriter(new File(gname));
        GCodeBuilder builder = new GCodeBuilder(exportTask, output);

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
