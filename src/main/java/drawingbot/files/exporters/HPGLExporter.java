package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.files.serial.SerialWriteTask;
import drawingbot.geom.basic.IGeometry;
import drawingbot.javafx.controls.DialogExportHPGLComplete;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.EnumRotation;
import javafx.application.Platform;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class HPGLExporter {


    public static void exportHPGL(ExportTask exportTask, PlottingTask plottingTask, Map<Integer, List<IGeometry>> geometries, String extension, File saveLocation) {
        PrintWriter output = FileUtils.createWriter(saveLocation);

        HPGLBuilder builder = new HPGLBuilder(plottingTask, output, getHPGLRotation(plottingTask));
        builder.open();

        AffineTransform transform = plottingTask.createHPGLTransform(getHPGLRotation(plottingTask));

        float[] coords = new float[6];
        int hpglPenCount = DrawingBotV3.INSTANCE.hpglPenNumber.get() == 0 ? 1 : DrawingBotV3.INSTANCE.hpglPenNumber.get();

        int[] renderOrder = plottingTask.getDrawingSet().calculateRenderOrder();
        for(int p = 0; p < renderOrder.length; p++) {
            int penIndex = renderOrder[renderOrder.length - 1 - p];
            ObservableDrawingPen pen = plottingTask.getDrawingSet().getPen(penIndex);

            List<IGeometry> geometryList = geometries.get(penIndex);

            if(!geometryList.isEmpty()){
                builder.startLayer(hpglPenCount, pen);
                int i = 0;
                for(IGeometry geometry : geometryList){
                    PathIterator iterator = geometry.getAWTShape().getPathIterator(transform, DrawingBotV3.INSTANCE.hpglCurveFlatness.get());
                    while(!iterator.isDone()){
                        int type = iterator.currentSegment(coords);
                        builder.move(coords, type);
                        iterator.next();
                    }
                    i++;
                    exportTask.updateProgress(i, geometryList.size()-1);
                }
                builder.endLayer(hpglPenCount, pen);
                hpglPenCount++;
            }

        }

        builder.close();

        DrawingBotV3.logger.info("HPGL File Created:  " +  saveLocation);
        DrawingBotV3.INSTANCE.controller.serialConnectionController.outputHPGLFiles.add(0, saveLocation.toString());

        Platform.runLater(() -> {
            DialogExportHPGLComplete dialogExportHPGLComplete = new DialogExportHPGLComplete(builder);
            dialogExportHPGLComplete.resultProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue){
                    if(DrawingBotV3.INSTANCE.serialConnection.portOpen.get()){
                        int[] hardLimits = DrawingBotV3.INSTANCE.serialConnection.checkHardClipLimits();
                        if(hardLimits != null){
                            DrawingBotV3.INSTANCE.controller.serialConnectionController.showHPGLHardLimitsDialog(hardLimits);
                        }
                    }else{
                        DrawingBotV3.INSTANCE.serialConnection.checkHardClipLimits = true;
                    }
                    DrawingBotV3.INSTANCE.controller.serialConnectionController.comboBoxHPGLFile.setValue(saveLocation.toString());
                    DrawingBotV3.INSTANCE.controller.serialConnectionSettingsStage.show();
                }
            });
            dialogExportHPGLComplete.show();

        });
    }

    public static EnumRotation getHPGLRotation(PlottingTask plottingTask){
        EnumRotation rotation = DrawingBotV3.INSTANCE.hpglRotation.get();

        if(rotation == EnumRotation.AUTO){
            int plotterWidthHPGL = Math.abs(DrawingBotV3.INSTANCE.hpglXMin.get()) + DrawingBotV3.INSTANCE.hpglXMax.get();
            int plotterHeightHPGL = Math.abs(DrawingBotV3.INSTANCE.hpglYMin.get()) + DrawingBotV3.INSTANCE.hpglYMax.get();

            boolean plotterLandscape = plotterWidthHPGL >= plotterHeightHPGL;
            boolean drawingLandscape = plottingTask.resolution.getScaledWidth() >= plottingTask.resolution.getScaledHeight();

            if(plotterLandscape != drawingLandscape){
                rotation = EnumRotation.R270;
            }else{
                rotation = EnumRotation.R0;
            }
        }
        return rotation;
    }


}
