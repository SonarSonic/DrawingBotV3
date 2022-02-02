package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.geom.GeometryUtils;
import drawingbot.geom.basic.IGeometry;
import drawingbot.image.PrintResolution;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.DBTask;
import javafx.application.Platform;
import javafx.scene.control.Dialog;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class ExportTask extends DBTask<Boolean> {

    public final DrawingExportHandler exportHandler;
    public final String extension;
    public final PlottingTask plottingTask;
    public final IGeometryFilter pointFilter;
    public final File saveLocation;
    public final boolean seperatePens;
    public final boolean overwrite;
    public final boolean forceBypassOptimisation;
    public final boolean isSubTask;

    private String error = null;
    public int totalGeometries;
    public int renderedGeometries;

    public PrintResolution exportResolution;

    public ExportTask(DrawingExportHandler exportHandler, PlottingTask plottingTask, IGeometryFilter pointFilter, String extension, File saveLocation, boolean seperatePens, boolean overwrite, boolean forceBypassOptimisation, boolean isSubTask, PrintResolution exportResolution){
        this.exportHandler = exportHandler;
        this.plottingTask = plottingTask;
        this.pointFilter = pointFilter;
        this.extension = extension;
        this.saveLocation = saveLocation;
        this.seperatePens = seperatePens;
        this.overwrite = overwrite;
        this.forceBypassOptimisation = forceBypassOptimisation;
        this.isSubTask = isSubTask;
        this.exportResolution = exportResolution;
    }

    @Override
    protected Boolean call() throws InterruptedException {
        DrawingBotV3.logger.info("Export Task: Started " + saveLocation.getPath());

        //TODO FIX ISSUE WITH DIALOGS TURNING INVISIBLE IN THEIR ORIGINAL POSITION.
        if(!isSubTask){
            updateTitle(exportHandler.displayName + ": " + saveLocation.getPath());
            //show confirmation dialog, for special formats
            if(exportHandler.confirmDialog != null){
                CountDownLatch latch = new CountDownLatch(1);
                AtomicReference<Boolean> result = new AtomicReference<>(false);
                Platform.runLater(() -> {
                    Dialog<Boolean> hpglDialog = exportHandler.confirmDialog.apply(this);
                    hpglDialog.resultProperty().addListener((observable, oldValue, newValue) -> result.set(newValue));
                    hpglDialog.setOnHidden(e -> latch.countDown());
                    hpglDialog.showAndWait();
                });

                latch.await();

                if(!result.get()){
                    updateMessage("Cancelled");
                    DrawingBotV3.logger.info("Export Task: Cancelled " + saveLocation.getPath());
                    updateProgress(0,1);
                    return false;
                }
            }
        }


        error = null;
        if(!seperatePens){
            updateTitle(exportHandler.displayName + ": 1 / 1" + " - " + saveLocation.getPath());
            if(overwrite || Files.notExists(saveLocation.toPath())){

                updateMessage("Optimising Paths");
                Map<Integer, List<IGeometry>> geometries = GeometryUtils.getGeometriesForExportTask(this, pointFilter, forceBypassOptimisation);

                totalGeometries = GeometryUtils.getTotalGeometries(geometries);
                renderedGeometries = 0;

                updateMessage("Exporting Paths");
                exportHandler.exportMethod.export(this, plottingTask, geometries, extension, saveLocation);
            }
        }else{
            File path = FileUtils.removeExtension(saveLocation);
            for (int p = 0; p < plottingTask.plottedDrawing.getPenCount(); p ++) {
                updateTitle(exportHandler.displayName + ": " + (p+1) + " / " + plottingTask.plottedDrawing.getPenCount() + " - " + saveLocation.getPath());
                ObservableDrawingPen drawingPen = plottingTask.plottedDrawing.drawingPenSet.getPens().get(p);
                File fileName = new File(path.getPath() + "_pen" + p + "_" + drawingPen.getName() + extension);
                if(drawingPen.isEnabled() && (overwrite || Files.notExists(fileName.toPath()))){
                    updateMessage("Optimising Paths");

                    Map<Integer, List<IGeometry>> geometries = GeometryUtils.getGeometriesForExportTask(this, (drawing, line, pen) -> pointFilter.filter(drawing, line, pen) && pen == drawingPen, forceBypassOptimisation);

                    totalGeometries = GeometryUtils.getTotalGeometries(geometries);
                    renderedGeometries = 0;

                    updateMessage("Exporting Paths");
                    exportHandler.exportMethod.export(this, plottingTask, geometries,  extension, fileName);
                }
            }
        }
        if(error != null){
            updateMessage("Export Error: " + error);
        }else{
            updateMessage("Finished");
        }
        DrawingBotV3.logger.info("Export Task: Finished " + saveLocation.getPath());
        return true;
    }

    public void onGeometryRendered(){
        renderedGeometries++;
        updateProgress(renderedGeometries, totalGeometries);
    }

}
