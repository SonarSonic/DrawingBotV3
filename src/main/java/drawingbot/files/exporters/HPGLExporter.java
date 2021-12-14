package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.files.serial.SerialWriteTask;
import drawingbot.geom.basic.IGeometry;
import drawingbot.javafx.controls.DialogExportHPGLComplete;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.EnumRotation;
import javafx.application.Platform;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;


public class HPGLExporter {


    public static void exportHPGL(ExportTask exportTask, PlottingTask plottingTask, Map<Integer, List<IGeometry>> geometries, String extension, File saveLocation) {
        PrintWriter output = FileUtils.createWriter(saveLocation);

        HPGLBuilder builder = new HPGLBuilder(plottingTask, output, getHPGLRotation(plottingTask));
        builder.open();

        AffineTransform transform = plottingTask.createHPGLTransform(getHPGLRotation(plottingTask));

        float[] coords = new float[6];
        int penCount = 1;
        for(Map.Entry<Integer, List<IGeometry>> entry : geometries.entrySet()){
            if(!entry.getValue().isEmpty()){
                builder.startLayer(penCount);
                int i = 0;
                for(IGeometry geometry : entry.getValue()){
                    PathIterator iterator = geometry.getAWTShape().getPathIterator(transform, DrawingBotV3.INSTANCE.hpglCurveFlatness.get());
                    while(!iterator.isDone()){
                        int type = iterator.currentSegment(coords);
                        builder.move(coords, type);
                        iterator.next();
                    }
                    i++;
                    exportTask.updateProgress(i, entry.getValue().size()-1);
                }
                builder.endLayer(penCount);
                penCount++;
            }
        }

        builder.close();

        Platform.runLater(() -> {
            DialogExportHPGLComplete dialogExportHPGLComplete = new DialogExportHPGLComplete(builder);
            dialogExportHPGLComplete.resultProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue){
                    DrawingBotV3.INSTANCE.taskMonitor.queueTask(new SerialWriteTask(saveLocation));
                }
            });
            dialogExportHPGLComplete.show();

        });

        DrawingBotV3.logger.info("HPGL File Created:  " +  saveLocation);
    }

    public static EnumRotation getHPGLRotation(PlottingTask plottingTask){
        EnumRotation rotation = DrawingBotV3.INSTANCE.hpglRotation.get();

        if(rotation == EnumRotation.AUTO){
            int plotterWidthHPGL = Math.abs(DrawingBotV3.INSTANCE.hpglXMin.get()) + DrawingBotV3.INSTANCE.hpglXMax.get();
            int plotterHeightHPGL = Math.abs(DrawingBotV3.INSTANCE.hpglYMin.get()) + DrawingBotV3.INSTANCE.hpglYMax.get();

            boolean plotterLandscape = plotterWidthHPGL >= plotterHeightHPGL;
            boolean drawingLandscape = plottingTask.resolution.getScaledWidth() >= plottingTask.resolution.getScaledHeight();

            if(plotterLandscape != drawingLandscape){
                rotation = EnumRotation.R90;
            }else{
                rotation = EnumRotation.R0;
            }
        }
        return rotation;
    }


}
