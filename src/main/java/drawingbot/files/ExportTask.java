package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.geom.GeometryUtils;
import drawingbot.image.PrintResolution;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.DrawingGeometryIterator;
import drawingbot.plotting.PlottedGroup;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.DBTask;
import javafx.application.Platform;
import javafx.scene.control.Dialog;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class ExportTask extends DBTask<Boolean> {

    public final DrawingExportHandler exportHandler;
    public final Mode exportMode;
    public final String extension;
    public final PlottingTask plottingTask;
    public final IGeometryFilter geometryFilter;
    public final File saveLocation;
    public final boolean overwrite;
    public final boolean forceBypassOptimisation;
    public final boolean isSubTask;

    public Map<ObservableDrawingPen, Integer> originalPenStats;

    public int renderedGeometries;

    public PlottedDrawing exportDrawing;
    public PrintResolution exportResolution;
    public List<ObservableDrawingPen> exportRenderOrder;
    public DrawingGeometryIterator exportIterator;
    public Map<ObservableDrawingPen, Integer> exportPenStats;

    public ExportTask(DrawingExportHandler exportHandler, Mode exportMode, PlottingTask plottingTask, IGeometryFilter geometryFilter, String extension, File saveLocation, boolean overwrite, boolean forceBypassOptimisation, boolean isSubTask, PrintResolution exportResolution){
        this.exportHandler = exportHandler;
        this.exportMode = exportMode;
        this.plottingTask = plottingTask;
        this.geometryFilter = geometryFilter;
        this.extension = extension;
        this.saveLocation = saveLocation;
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
        if(inExport ? !exportPenStats.containsKey(drawingPen) : !originalPenStats.containsKey(drawingPen)){
            return 0;
        }
        return inExport ? exportPenStats.get(drawingPen) : originalPenStats.get(drawingPen);
    }

    public void createExportPlottedDrawing(IGeometryFilter geometryFilter){
        exportDrawing = GeometryUtils.getOptimisedPlottedDrawing(this, geometryFilter, forceBypassOptimisation);
        exportPenStats = PlottedDrawing.getPerPenGeometryStats(exportDrawing);
        exportRenderOrder = filterActivePens(exportDrawing.getGlobalRenderOrder(), true);
        exportIterator = new DrawingGeometryIterator(exportDrawing, exportRenderOrder);
    }

    public void doExport(IGeometryFilter geometryFilter, File saveLocation){
        if(overwrite || Files.notExists(saveLocation.toPath())){

            updateMessage("Optimising Paths");
            createExportPlottedDrawing(geometryFilter);

            updateMessage("Exporting Paths");
            renderedGeometries = 0;
            exportHandler.exportMethod.export(this, saveLocation);
        }
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
        File baseSaveLocation = FileUtils.removeExtension(saveLocation);

        switch (exportMode){
            case PER_DRAWING:
                updateTitle(exportHandler.displayName + ": 1 / 1" + " - " + saveLocation.getPath());
                doExport(geometryFilter, saveLocation);
                break;
            case PER_GROUP:
                Collection<PlottedGroup> groups = plottingTask.plottedDrawing.groups.values();
                int groupPos = 0;
                for(PlottedGroup group : groups){
                    updateTitle(exportHandler.displayName + ": " + (groupPos+1) + " / " + groups.size() + " - " + saveLocation.getPath());
                    File fileName = new File(baseSaveLocation.getPath() + "_group" + (groupPos+1) + extension);
                    if(!group.geometries.isEmpty()){
                        doExport((drawing, geometry, pen) -> geometryFilter.filter(drawing, geometry, pen) && geometry.getGroupID() == group.groupID, fileName);
                    }
                    groupPos++;
                }
                break;
            case PER_PEN:
                List<ObservableDrawingPen> activePens = filterActivePens(plottingTask.plottedDrawing.getGlobalDisplayOrder(), false);

                int setPos = 0;
                for(ObservableDrawingSet drawingSet : DrawingBotV3.INSTANCE.drawingSetSlots){
                    int penPos = 0;
                    for(ObservableDrawingPen drawingPen : drawingSet.pens){
                        updateTitle(exportHandler.displayName + ": " + " Set: " + (setPos+1) + " / " + DrawingBotV3.INSTANCE.drawingSetSlots.size() +  " Pen: " + (penPos+1) + " / " + drawingSet.pens.size() + " - " + saveLocation.getPath());
                        File fileName = new File(baseSaveLocation.getPath() + "_set" + (setPos+1) + "_pen" + (penPos+1) + "_" + drawingPen.getName() + extension);
                        if(drawingPen.isEnabled() && activePens.contains(drawingPen)){
                            doExport((drawing, geometry, pen) -> geometryFilter.filter(drawing, geometry, pen) && pen == drawingPen, fileName);
                        }
                        penPos++;
                    }
                    setPos++;
                }
                break;
        }
        if(!error.isEmpty()){
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

    public enum Mode {
        PER_DRAWING,
        PER_GROUP,
        PER_PEN;

    }

}
