package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.geom.GeometryUtils;
import drawingbot.geom.basic.IGeometry;
import drawingbot.plotting.PlottingTask;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ExportTask extends Task<Boolean> {

    public ExportFormats format;
    public String extension;
    public PlottingTask plottingTask;
    public IGeometryFilter pointFilter;
    public File saveLocation;
    public boolean seperatePens;
    public boolean overwrite;
    public boolean forceBypassOptimisation;

    private String error = null;
    public int totalGeometries;
    public int renderedGeometries;

    public ExportTask(ExportFormats format, PlottingTask plottingTask, IGeometryFilter pointFilter, String extension, File saveLocation, boolean seperatePens, boolean overwrite, boolean forceBypassOptimisation){
        this.format = format;
        this.plottingTask = plottingTask;
        this.pointFilter = pointFilter;
        this.extension = extension;
        this.saveLocation = saveLocation;
        this.seperatePens = seperatePens;
        this.overwrite = overwrite;
        this.forceBypassOptimisation = forceBypassOptimisation;
    }

    @Override
    protected void setException(Throwable t) {
        super.setException(t);
        DrawingBotV3.logger.log(Level.SEVERE, "Export Task Failed", t);
    }

    @Override
    protected Boolean call() throws Exception {
        DrawingBotV3.logger.info("Export Task: Started " + saveLocation.getPath());
        error = null;
        if(!seperatePens){
            updateTitle(format.displayName + ": 1 / 1" + " - " + saveLocation.getPath());
            if(overwrite || Files.notExists(saveLocation.toPath())){

                updateMessage("Optimising Paths");
                Map<Integer, List<IGeometry>> geometries = GeometryUtils.getGeometriesForExportTask(this, pointFilter, forceBypassOptimisation);

                totalGeometries = GeometryUtils.getTotalGeometries(geometries);
                renderedGeometries = 0;

                updateMessage("Exporting Paths");
                format.exportMethod.export(this, plottingTask, geometries, extension, saveLocation);
            }
        }else{
            File path = FileUtils.removeExtension(saveLocation);
            for (int p = 0; p < plottingTask.plottedDrawing.getPenCount(); p ++) {
                updateTitle(format.displayName + ": " + (p+1) + " / " + plottingTask.plottedDrawing.getPenCount() + " - " + saveLocation.getPath());
                ObservableDrawingPen drawingPen = plottingTask.plottedDrawing.drawingPenSet.getPens().get(p);
                File fileName = new File(path.getPath() + "_pen" + p + "_" + drawingPen.getName() + extension);
                if(drawingPen.isEnabled() || (overwrite || Files.notExists(fileName.toPath()))){
                    updateMessage("Optimising Paths");

                    Map<Integer, List<IGeometry>> geometries = GeometryUtils.getGeometriesForExportTask(this, (line, pen) -> pointFilter.filter(line, pen) && pen == drawingPen, forceBypassOptimisation);

                    totalGeometries = GeometryUtils.getTotalGeometries(geometries);
                    renderedGeometries = 0;

                    updateMessage("Exporting Paths");
                    format.exportMethod.export(this, plottingTask, geometries,  extension, fileName);
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

    ///MAKE UPDATE METHODS ACCESSIBLE TO EXPORTERS

    public void setError(String error){
        this.error = error;
    }

    @Override
    public void updateProgress(long workDone, long max) {
        super.updateProgress(workDone, max);
    }

    @Override
    public void updateProgress(double workDone, double max) {
        super.updateProgress(workDone, max);
    }

    @Override
    public void updateMessage(String message) {
        super.updateMessage(message);
    }

    @Override
    public void updateTitle(String title) {
        super.updateTitle(title);
    }

}
