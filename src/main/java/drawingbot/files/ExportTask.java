package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.geom.GeometryUtils;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.controls.DialogExportNPens;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.DrawingGeometryIterator;
import drawingbot.plotting.PlottedGroup;
import drawingbot.plotting.canvas.CanvasUtils;
import drawingbot.utils.DBTask;
import javafx.application.Platform;
import javafx.scene.control.Dialog;
import org.controlsfx.control.action.Action;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class ExportTask extends DBTask<Boolean> {

    public final DrawingExportHandler exportHandler;
    public final Mode exportMode;
    public final String extension;
    public final PlottedDrawing plottedDrawing;
    public final IGeometryFilter geometryFilter;
    public final File saveLocation;
    public final boolean overwrite;
    public final boolean forceBypassOptimisation;
    public final boolean isSubTask;

    public Map<ObservableDrawingPen, Integer> originalPenStats;

    public int renderedGeometries;

    public float exportScale = 1F;
    public PlottedDrawing exportDrawing;
    public List<ObservableDrawingPen> exportRenderOrder;
    public DrawingGeometryIterator exportIterator;
    public Map<ObservableDrawingPen, Integer> exportPenStats;

    public ExportTask(DBTaskContext context, DrawingExportHandler exportHandler, Mode exportMode, PlottedDrawing plottedDrawing, IGeometryFilter geometryFilter, String extension, File saveLocation, boolean overwrite, boolean forceBypassOptimisation, boolean isSubTask){
        super(context);
        this.exportHandler = exportHandler;
        this.exportMode = exportMode;
        this.plottedDrawing = plottedDrawing;
        this.geometryFilter = geometryFilter;
        this.extension = extension;
        this.saveLocation = saveLocation;
        this.overwrite = overwrite;
        this.forceBypassOptimisation = forceBypassOptimisation;
        this.isSubTask = isSubTask;
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

        if(exportScale != 1F){
            float previousScale = exportDrawing.canvas.getPlottingScale();
            exportDrawing.canvas = CanvasUtils.normalisedCanvas(exportDrawing.canvas);
            exportDrawing.canvas = CanvasUtils.rescaleCanvas(exportDrawing.canvas, exportScale / previousScale);
        }
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
            updateTitle(exportHandler.description + ": " + saveLocation.getPath());
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

        originalPenStats = PlottedDrawing.getPerPenGeometryStats(plottedDrawing);
        File baseSaveLocation = FileUtils.removeExtension(saveLocation);

        switch (exportMode){
            case PER_DRAWING:
                updateTitle(exportHandler.description + ": 1 / 1" + " - " + saveLocation.getPath());
                doExport(geometryFilter, saveLocation);
                break;
            case PER_GROUP:
                Collection<PlottedGroup> groups = plottedDrawing.groups.values();
                int groupPos = 0;
                for(PlottedGroup group : groups){
                    updateTitle(exportHandler.description + ": " + (groupPos+1) + " / " + groups.size() + " - " + saveLocation.getPath());
                    File fileName = new File(baseSaveLocation.getPath() + "_group" + (groupPos+1) + extension);
                    if(!group.geometries.isEmpty()){
                        doExport((drawing, geometry, pen) -> geometryFilter.filter(drawing, geometry, pen) && geometry.getGroupID() == group.groupID, fileName);
                    }
                    groupPos++;
                }
                break;
            case PER_PEN:
                List<ObservableDrawingPen> activePens = filterActivePens(plottedDrawing.getGlobalDisplayOrder(), false);
                int setPos = 0;
                for(ObservableDrawingSet drawingSet : plottedDrawing.drawingSets.drawingSetSlots.get()){
                    int penPos = 0;
                    for(ObservableDrawingPen drawingPen : drawingSet.pens){
                        updateTitle(exportHandler.description + ": " + " Set: " + (setPos+1) + " / " + plottedDrawing.drawingSets.drawingSetSlots.get().size() +  " Pen: " + (penPos+1) + " / " + drawingSet.pens.size() + " - " + saveLocation.getPath());
                        File fileName = new File(baseSaveLocation.getPath() + "_set" + (setPos+1) + "_pen" + (penPos+1) + "_" + drawingPen.getName() + extension);
                        if(drawingPen.isEnabled() && activePens.contains(drawingPen)){
                            doExport((drawing, geometry, pen) -> geometryFilter.filter(drawing, geometry, pen) && pen == drawingPen, fileName);
                        }
                        penPos++;
                    }
                    setPos++;
                }
                break;
            case PER_N_PENS:
                activePens = filterActivePens(plottedDrawing.getGlobalRenderOrder(), false);

                CountDownLatch latch = new CountDownLatch(1);
                AtomicReference<Integer> result = new AtomicReference<>(-1);
                Platform.runLater(() -> {
                    Dialog<Integer> nPenDialog = new DialogExportNPens(activePens);
                    nPenDialog.resultProperty().addListener((observable, oldValue, newValue) -> result.set(newValue));
                    nPenDialog.setOnHidden(e -> latch.countDown());
                    nPenDialog.showAndWait();
                });

                latch.await();

                int nPens = result.get();
                if(nPens != -1){
                    for(int i = 0; i < activePens.size(); i+=nPens){
                        List<ObservableDrawingPen> nextPens = new ArrayList<>();
                        for(int j = 0; j < nPens; j++){
                            int index = i + j;
                            if(index < activePens.size()){
                                ObservableDrawingPen pen = activePens.get(index);
                                nextPens.add(pen);
                            }
                        }
                        if(!nextPens.isEmpty()){
                            updateTitle(exportHandler.description + ": " + " Pens: " + (i+1) + " to " + (i+nextPens.size()) + " - " + saveLocation.getPath());
                            File fileName = new File(baseSaveLocation.getPath() + "_pens" + (i+1) + "_to_" + (i+nextPens.size()) + extension);
                            doExport((drawing, geometry, pen) -> geometryFilter.filter(drawing, geometry, pen) && nextPens.contains(pen), fileName);
                        }
                    }
                }else{
                    updateProgress(1,1);
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
        PER_DRAWING("Export per/drawing"),
        PER_PEN("Export per/pen"),
        PER_GROUP("Export per/group"),
        PER_N_PENS("Export per/n pens");

        private final String displayName;

        Mode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
    }

}
