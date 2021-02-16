package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.plotting.PlottedLine;
import drawingbot.plotting.PlottingTask;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.Files;
import java.util.function.BiFunction;

public class ExportTask extends Task<Boolean> {

    public ExportFormats format;
    public String extension;
    public PlottingTask plottingTask;
    public BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter;
    public File saveLocation;
    public boolean seperatePens;
    public boolean overwrite;

    private String error = null;

    public ExportTask(ExportFormats format, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter, String extension, File saveLocation, boolean seperatePens, boolean overwrite){
        this.format = format;
        this.plottingTask = plottingTask;
        this.lineFilter = lineFilter;
        this.extension = extension; //remove asterisk
        this.saveLocation = saveLocation;
        this.seperatePens = seperatePens;
        this.overwrite = overwrite;
    }

    @Override
    protected Boolean call() throws Exception {
        Platform.runLater(() -> DrawingBotV3.setActiveExportTask(this)); //avoid clashing with render thread
        DrawingBotV3.logger.info("Export Task: Started " + saveLocation.getPath());
        updateMessage("Processing");
        error = null;
        if(!seperatePens){
            updateTitle(format.displayName + ": 1 / 1" + " - " + saveLocation.getPath());
            if(overwrite || Files.notExists(saveLocation.toPath())){
                format.exportMethod.export(this, plottingTask, lineFilter, extension, saveLocation);
            }
        }else{
            File path = FileUtils.removeExtension(saveLocation);
            for (int p = 0; p < plottingTask.plottedDrawing.getPenCount(); p ++) {
                updateTitle(format.displayName + ": " + (p+1) + " / " + plottingTask.plottedDrawing.getPenCount() + " - " + saveLocation.getPath());
                ObservableDrawingPen drawingPen = plottingTask.plottedDrawing.drawingPenSet.getPens().get(p);
                File fileName = new File(path.getPath() + "_pen" + p + "_" + drawingPen.getName() + extension);
                if(drawingPen.isEnabled() || (overwrite || Files.notExists(fileName.toPath()))){
                    format.exportMethod.export(this, plottingTask, (line, pen) -> lineFilter.apply(line, pen) && pen == drawingPen, extension, fileName);
                }
            }
        }
        if(error != null){
            updateMessage("Export Error: " + error);
        }else{
            updateMessage("Finished");
        }
        DrawingBotV3.logger.info("Export Task: Finished " + saveLocation.getPath());
        Platform.runLater(() -> { DrawingBotV3.updateUI(); DrawingBotV3.setActiveExportTask(null);});
        return true;
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
