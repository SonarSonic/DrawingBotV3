package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICustomPen;
import drawingbot.api.IGeometryFilter;
import drawingbot.drawing.DrawingStats;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.geom.GeometryUtils;
import drawingbot.geom.operation.GeometryOperationAddExportPaths;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.controls.DialogExportNPens;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.plotting.DrawingGeometryIterator;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottedGroup;
import drawingbot.plotting.canvas.CanvasUtils;
import drawingbot.registry.Register;
import drawingbot.render.overlays.ExportStatsOverlays;
import drawingbot.render.overlays.NotificationOverlays;
import drawingbot.utils.DBTask;
import javafx.application.Platform;
import javafx.scene.control.Dialog;
import org.controlsfx.control.action.Action;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
            onDrawingExported(exportDrawing, geometryFilter, saveLocation);
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
                    Dialog<Boolean> confirmDialog = exportHandler.confirmDialog.apply(this);
                    confirmDialog.resultProperty().addListener((observable, oldValue, newValue) -> {
                        result.set(newValue);
                        latch.countDown();
                    });
                    confirmDialog.show();
                });

                latch.await();

                if(!result.get()){
                    updateMessage("Cancelled");
                    DrawingBotV3.logger.info("Export Task: Cancelled " + saveLocation.getPath());
                    updateProgress(0,1);
                    return false;
                }
            }
            clearExportedDrawings();
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
                for(ObservableDrawingSet drawingSet : plottedDrawing.drawingSets.drawingSetSlots){
                    int penPos = 0;
                    for(ObservableDrawingPen drawingPen : drawingSet.pens){
                        updateTitle(exportHandler.description + ": " + " Set: " + (setPos+1) + " / " + plottedDrawing.drawingSets.drawingSetSlots.size() +  " Pen: " + (penPos+1) + " / " + drawingSet.pens.size() + " - " + saveLocation.getPath());
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
                    nPenDialog.resultProperty().addListener((observable, oldValue, newValue) -> {
                        result.set(newValue);
                        latch.countDown();
                    });
                    nPenDialog.show();
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
                    updateMessage("Cancelled");
                    DrawingBotV3.logger.info("Export Task: Cancelled " + saveLocation.getPath());
                    updateProgress(0,1);
                    return false;
                }
                break;
        }
        if(!error.isEmpty()){
            updateMessage("Export Error: " + error);
            NotificationOverlays.INSTANCE.showWithSubtitle("WARNING", "Export Error", error);
        }else{
            updateMessage("Finished");
            if(!isSubTask){
                NotificationOverlays.INSTANCE.showWithSubtitle("File Exported", saveLocation.getPath(), new Action("Open File", e -> FXHelper.openFolder(saveLocation)), new Action("Open Folder", e -> FXHelper.openFolder(saveLocation.getParentFile())));
            }
        }
        DrawingBotV3.logger.info("Export Task: Finished " + saveLocation.getPath());
        return true;
    }

    // We only want to make the user look at the page once
    private boolean shownExportPage = false;

    public void clearExportedDrawings(){
        if(isSubTask){
            return;
        }
        Platform.runLater(() -> {
            context.project().getExportedDrawings().forEach(entry -> entry.drawing.reset());
            context.project().getExportedDrawings().clear();
        });
    }

    public void onDrawingExported(PlottedDrawing drawing, IGeometryFilter geometryFilter, File saveLocation){
        if(isSubTask){
            return;
        }

        DrawingStats preStats = new DrawingStats(plottedDrawing, geometryFilter);
        PlottedDrawing exportPathsDrawing = new GeometryOperationAddExportPaths().run(drawing);
        DrawingStats postStats = new DrawingStats(drawing);

        ExportedDrawingEntry entry = new ExportedDrawingEntry(exportPathsDrawing, saveLocation, preStats, postStats);

        context.project().getExportedDrawings().add(entry);
        Platform.runLater(() -> ExportStatsOverlays.INSTANCE.selectedEntry.set(entry));

        // Custom pens can't be optimised fully, so don't show their export output.
        boolean customPens = drawing.getAllPens().stream().anyMatch(pen -> pen.source instanceof ICustomPen && !((ICustomPen) pen.source).canOptimisePenPaths());

        if(DBPreferences.INSTANCE.showExportedDrawing.get() && exportHandler.isVector && !customPens && !shownExportPage){
            Platform.runLater(() -> {
                DrawingBotV3.INSTANCE.displayMode.set(Register.INSTANCE.DISPLAY_MODE_EXPORT_DRAWING);
            });
            shownExportPage = true;
        }
    }

    public void onGeometryExported(){
        renderedGeometries++;
        updateProgress(renderedGeometries, exportDrawing.getGeometryCount());
    }

    public enum Mode {
        PER_DRAWING("per/drawing"),
        PER_PEN("per/pen"),
        PER_GROUP("per/group"),
        PER_N_PENS("per/n pens");

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
