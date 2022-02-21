package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.geom.GeometryUtils;
import drawingbot.image.PrintResolution;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.DrawingGeometryIterator;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.DBTask;
import javafx.application.Platform;
import javafx.scene.control.Dialog;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

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

    public Map<ObservableDrawingPen, Integer> originalPenStats;

    private String error = null;
    public int renderedGeometries;

    public PlottedDrawing exportDrawing;
    public PrintResolution exportResolution;
    public List<ObservableDrawingPen> exportRenderOrder;
    public DrawingGeometryIterator exportIterator;
    public Map<ObservableDrawingPen, Integer> exportPenStats;

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

    /**
     * If the pen should be treated as active while exporting
     */
    public boolean isPenActive(ObservableDrawingPen drawingPen, boolean inExport){
        return getGeometryCountForPen(drawingPen, inExport) > 0;
    }

    /**
     * Returns how many geometries will be rendered with the given pen
     */
    public int getGeometryCountForPen(ObservableDrawingPen drawingPen, boolean inExport){
        return inExport ? exportPenStats.get(drawingPen) : originalPenStats.get(drawingPen);
    }

    public void createExportPlottedDrawing(IGeometryFilter geometryFilter){
        exportDrawing = GeometryUtils.getOptimisedPlottedDrawing(this, geometryFilter, forceBypassOptimisation);
        exportPenStats = PlottedDrawing.getPerPenGeometryStats(exportDrawing);
        exportRenderOrder = filterActivePens(exportDrawing.getGlobalRenderOrder(), true);
        exportIterator = new DrawingGeometryIterator(exportDrawing, exportRenderOrder);
    }

    public void startExport(File saveLocation){
        renderedGeometries = 0;
        exportHandler.exportMethod.export(this, saveLocation);
    }

    public List<ObservableDrawingPen> filterActivePens(List<ObservableDrawingPen> globalOrder, boolean inExport){
        List<ObservableDrawingPen> activeOrder = new ArrayList<>();
        for(ObservableDrawingPen drawingPen : globalOrder){
            if(isPenActive(drawingPen, inExport)){
                activeOrder.add(drawingPen);
            }
        }
        return activeOrder;
    }

    @Override
    protected Boolean call() throws InterruptedException {
        DrawingBotV3.logger.info("Export Task: Started " + saveLocation.getPath());

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

        originalPenStats = PlottedDrawing.getPerPenGeometryStats(plottingTask.plottedDrawing);


        error = null;
        if(!seperatePens){
            updateTitle(exportHandler.displayName + ": 1 / 1" + " - " + saveLocation.getPath());
            if(overwrite || Files.notExists(saveLocation.toPath())){

                updateMessage("Optimising Paths");
                createExportPlottedDrawing(pointFilter);

                updateMessage("Exporting Paths");
                startExport(saveLocation);
            }
        }else{
            File path = FileUtils.removeExtension(saveLocation);
            List<ObservableDrawingPen> activePens = filterActivePens(plottingTask.plottedDrawing.getGlobalDisplayOrder(), false);

            int p = 0;
            for(ObservableDrawingPen drawingPen : activePens){
                updateTitle(exportHandler.displayName + ": " + (p+1) + " / " + activePens.size() + " - " + saveLocation.getPath());
                File fileName = new File(path.getPath() + "_pen" + p + "_" + drawingPen.getName() + extension);
                if(drawingPen.isEnabled() && (overwrite || Files.notExists(fileName.toPath()))){

                    updateMessage("Optimising Paths");
                    createExportPlottedDrawing((drawing, line, pen) -> pointFilter.filter(drawing, line, pen) && pen == drawingPen);

                    updateMessage("Exporting Paths");
                    startExport(fileName);
                }
                p++;
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

    public void onGeometryExported(){
        renderedGeometries++;
        updateProgress(renderedGeometries, exportDrawing.getGeometryCount());
    }

}
