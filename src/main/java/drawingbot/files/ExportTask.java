package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.plotting.PlottedLine;
import drawingbot.plotting.PlottingTask;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;

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
        DrawingBotV3.INSTANCE.setActiveExportTask(this);
        updateTitle(format.displayName);
        if(!seperatePens){
            updateMessage("1 / 1" + " - " + saveLocation);
            if(overwrite || Files.notExists(saveLocation.toPath())){
                format.exportMethod.export(this, plottingTask, lineFilter, extension, saveLocation);
            }
        }else{
            File path = FileUtils.removeExtension(saveLocation);
            for (int p = 0; p < plottingTask.plottedDrawing.getPenCount(); p ++) {
                updateMessage((p+1) + " / " + plottingTask.plottedDrawing.getPenCount() + " - " + saveLocation.toString());
                ObservableDrawingPen drawingPen = plottingTask.plottedDrawing.drawingPenSet.getPens().get(p);
                File fileName = new File(path.getPath() + "_pen" + p + "_" + drawingPen.getName() + extension);
                if(drawingPen.isEnabled() || (overwrite || Files.notExists(fileName.toPath()))){
                    format.exportMethod.export(this, plottingTask, (line, pen) -> lineFilter.apply(line, pen) && pen == drawingPen, extension, fileName);
                }
            }
        }
        return true;
    }

    ///MAKE UPDATE METHODS ACCESSIBLE TO EXPORTERS

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
