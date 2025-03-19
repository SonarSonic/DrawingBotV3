package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.api.IGeometryFilter;
import drawingbot.drawing.DrawingStats;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.geom.GeometryUtils;
import drawingbot.geom.operation.GeometryOperationAddExportPaths;
import drawingbot.geom.operation.PlottedDrawingSplitter;
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
import drawingbot.render.overlays.NotificationOverlays;
import drawingbot.utils.DBTask;
import drawingbot.utils.UnitsLength;
import javafx.application.Platform;
import javafx.scene.control.Dialog;
import org.controlsfx.control.action.Action;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

public class ExportTask extends DBTask<Boolean> {

    public final DrawingExportHandler exportHandler;
    public final Mode exportMode;
    public String extension;
    public PlottedDrawing plottedDrawing;
    public IGeometryFilter geometryFilter;
    public File saveLocation;
    public boolean overwrite;
    public boolean forceBypassOptimisation;
    public final boolean isSubTask;

    public Map<ObservableDrawingPen, Integer> originalPenStats;

    public int renderedGeometries;

    public double exportScale = 1D;
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

    public void setupDrawingExport(PlottedDrawing nextDrawing){
        exportDrawing = nextDrawing;
        exportPenStats = exportDrawing.getPerPenGeometryStats();
        exportRenderOrder = filterActivePens(exportDrawing.getGlobalRenderOrder(), true);
        exportIterator = new DrawingGeometryIterator(exportDrawing, exportRenderOrder);

        if(exportScale != 1D){
            double previousScale = exportDrawing.canvas.getPlottingScale();
            exportDrawing.canvas = CanvasUtils.normalisedCanvas(exportDrawing.canvas);
            exportDrawing.canvas = CanvasUtils.rescaleCanvas(exportDrawing.canvas, exportScale / previousScale);
        }
    }

    public void doExport(IGeometryFilter geometryFilter, File saveLocation){
        if(overwrite || Files.notExists(saveLocation.toPath())){

            updateMessage("Optimising Paths");
            PlottedDrawing optimisedDrawing = GeometryUtils.getOptimisedPlottedDrawing(this, geometryFilter, forceBypassOptimisation);

            List<PlottedDrawing> outputDrawings = List.of(optimisedDrawing);
            boolean splitDrawing = false;

            if(exportHandler.isVector && !forceBypassOptimisation && DBPreferences.INSTANCE.pathOptimisationEnabled.get() && DBPreferences.INSTANCE.pathSplittingEnabled.get()){
                updateMessage("Splitting Paths");
                double splitDistance = DBPreferences.INSTANCE.pathSplittingDistance.get();
                UnitsLength splitUnits = DBPreferences.INSTANCE.pathSplittingUnits.get();

                double splitDistanceUnits = UnitsLength.convert(splitDistance, splitUnits, UnitsLength.MILLIMETRES) * optimisedDrawing.canvas.getPlottingScale();

                PlottedDrawingSplitter splitter = new PlottedDrawingSplitter(splitDistanceUnits, optimisedDrawing);
                outputDrawings = splitter.getOutputDrawings();
                splitDrawing = true;
            }

            updateMessage("Exporting Paths");
            renderedGeometries = 0;
            int splitIndex = 0;
            for(PlottedDrawing outputDrawing : outputDrawings){

                if(isCancelled()){
                    break;
                }

                File outputLocation = saveLocation;
                if(splitDrawing){
                    File file = FileUtils.removeExtension(outputLocation);
                    outputLocation = new File(file.getPath() + "_split" + splitIndex + extension);
                }

                setupDrawingExport(outputDrawing);
                exportHandler.exportMethod.export(this, outputLocation);
                onDrawingExported(exportDrawing, geometryFilter, outputLocation);
                splitIndex++;
            }
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
    protected Boolean call() throws InterruptedException, ExecutionException {
        DrawingBotV3.logger.info("Export Task: Started " + saveLocation.getPath());

        if(!isSubTask && !FXApplication.isUnitTesting){
            updateTitle(exportHandler.description + ": " + saveLocation.getPath());
            //show confirmation dialog, for special formats
            if(exportHandler.confirmDialog != null){
                FutureTask<Boolean> task = new FutureTask<>(() -> {
                    Dialog<Boolean> confirmDialog = exportHandler.confirmDialog.apply(this);
                    if(confirmDialog == null){
                        //If the export handler returns no dialog, assume none was required
                        return true;
                    }
                    confirmDialog.initOwner(FXApplication.primaryStage);
                    return confirmDialog.showAndWait().orElse(false);
                });

                Platform.runLater(task);

                if(!task.get()){
                    updateMessage("Cancelled");
                    DrawingBotV3.logger.info("Export Task: Cancelled " + saveLocation.getPath());
                    updateProgress(0,1);
                    return false;
                }
            }
            clearExportedDrawings();
        }

        exportHandler.setupExport(this);

        originalPenStats = plottedDrawing.getPerPenGeometryStats();
        File baseSaveLocation = FileUtils.removeExtension(saveLocation);

        switch (exportMode){
            case PER_DRAWING:
                updateTitle(exportHandler.description + ": 1 / 1" + " - " + saveLocation.getPath());
                doExport(geometryFilter, saveLocation);
                break;
            case PER_GROUP:
                Collection<PlottedGroup> groups = plottedDrawing.groups.values().stream().filter(g->!g.geometries.isEmpty()).collect(Collectors.toSet());
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
                        File fileName = new File(baseSaveLocation.getPath() + "_set" + (setPos+1) + "_pen" + (penPos+1) + "_" + FileUtils.getSafeFileName(drawingPen.getName()) + extension);
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

                FutureTask<Integer> task = new FutureTask<>(() -> {
                    Dialog<Integer> nPenDialog = new DialogExportNPens(activePens);
                    nPenDialog.initOwner(FXApplication.primaryStage);
                    return nPenDialog.showAndWait().orElse(-1);
                });

                Platform.runLater(task);

                int nPens = task.get();
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
            updateTitle("Export Failed");
            updateMessage(error);
            NotificationOverlays.INSTANCE.showWithSubtitle("WARNING", "Export Failed", error);
        }else{
            updateMessage("Finished");
            if(!isSubTask && saveLocation.exists()){
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
        if(isSubTask || !error.isEmpty()){
            return;
        }

        DrawingStats preStats = new DrawingStats(plottedDrawing, geometryFilter);
        PlottedDrawing exportPathsDrawing = new GeometryOperationAddExportPaths().run(drawing);
        DrawingStats postStats = new DrawingStats(drawing);

        ExportedDrawingEntry entry = new ExportedDrawingEntry(exportPathsDrawing, saveLocation, preStats, postStats);

        context.project().getExportedDrawings().add(entry);
        Platform.runLater(() -> context.project().selectedExportedDrawing.set(entry));

        // Custom pens can't be optimised fully, so don't show their export output.
        boolean customPens = drawing.getAllPens().stream().anyMatch(pen -> pen.getSpecialColorHandler() != null && !pen.getSpecialColorHandler().canOptimisePenPaths(pen));

        if(DBPreferences.INSTANCE.showExportedDrawing.get() && exportHandler.isVector && !customPens && !shownExportPage){
            Platform.runLater(() -> {
                context.project().setDisplayMode(Register.INSTANCE.DISPLAY_MODE_EXPORT_DRAWING);
            });
            shownExportPage = true;
        }
    }

    public void onGeometryExported(){
        renderedGeometries++;
        updateProgress(renderedGeometries, exportDrawing.getGeometryCount());
    }

    @Nullable
    public PrintWriter createFileWriter(File file) {
        try {
            File parent = file.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IOException("Unable to create directory: " + parent.getAbsolutePath());
            }
            return new PrintWriter(new FileWriter(file));
        }catch (Exception e){
            setException(e);
            e.printStackTrace();
            return null;
        }
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
