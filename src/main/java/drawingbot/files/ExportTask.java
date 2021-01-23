package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.plotting.PlottedLine;
import drawingbot.plotting.PlottingTask;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.function.BiFunction;

public class ExportTask extends Task<Boolean> {

    public ExportFormats format;
    public String extension;
    public PlottingTask plottingTask;
    public BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter;
    public File saveLocation;
    public boolean seperatePens;

    public ExportTask(ExportFormats format, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter, FileChooser.ExtensionFilter extension, File saveLocation, boolean seperatePens){
        this.format = format;
        this.plottingTask = plottingTask;
        this.lineFilter = lineFilter;
        this.extension = extension.getExtensions().get(0).substring(1); //remove asterisk
        this.saveLocation = saveLocation;
        this.seperatePens = seperatePens;
    }

    @Override
    protected Boolean call() throws Exception {
        DrawingBotV3.INSTANCE.setActiveExportTask(this);
        updateTitle(format.displayName);
        if(!seperatePens){
            updateMessage(saveLocation + " " + "1 / 1");
            format.exportMethod.export(this, plottingTask, lineFilter, extension, saveLocation);
        }else{
            File path = FileUtils.removeExtension(saveLocation);
            for (int p = 0; p < plottingTask.plottedDrawing.getPenCount(); p ++) {
                updateMessage(saveLocation.toString() + " " +  + (p+1) + " / " + plottingTask.plottedDrawing.getPenCount());
                ObservableDrawingPen drawingPen = plottingTask.plottedDrawing.drawingPenSet.getPens().get(p);
                String fileName = path.getPath() + "_pen" + p + "_" + drawingPen.getName() + extension;
                format.exportMethod.export(this, plottingTask, (line, pen) -> lineFilter.apply(line, pen) && pen == drawingPen, extension, new File(fileName));
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
