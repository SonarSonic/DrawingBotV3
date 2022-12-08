package drawingbot.integrations.vpype;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.registry.Register;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.regex.Matcher;

public class VpypeHelper {

    public static final String VPYPE_NAME = "vpype";
    public static final String TEMP_FILE_NAME = "vpype_export.svg";
    public static final String OUTPUT_FILE_WILDCARD = "%OUTPUT_FILE%";

    public static void exportToVpype(VpypeSettings settings){
        Platform.runLater(() -> {
            PlottedDrawing drawing = DrawingBotV3.taskManager().getCurrentDrawing();
            if(drawing!= null){

                String userCommand = settings.vPypeCommand.getValue();
                File tempSVG = new File(FileUtils.getUserDataDirectory(), TEMP_FILE_NAME);

                if(userCommand.contains(OUTPUT_FILE_WILDCARD)){
                    File outputFile = chooseOutputFile();
                    if(outputFile == null){
                        return;
                    }
                    userCommand = userCommand.replaceAll(OUTPUT_FILE_WILDCARD, Matcher.quoteReplacement(outputFile.toString()));
                }

                String command = settings.vPypeExecutable.getValue() + " read " + Matcher.quoteReplacement(tempSVG.toString()) + " " + userCommand;
                Task<?> task = DrawingBotV3.INSTANCE.createExportTask(Register.EXPORT_SVG, ExportTask.Mode.PER_DRAWING, drawing, IGeometryFilter.DEFAULT_EXPORT_FILTER, ".svg", tempSVG, settings.vPypeBypassOptimisation.get());
                task.setOnSucceeded(event -> DrawingBotV3.INSTANCE.taskMonitor.queueTask(new VpypeTask(command, settings)));
            }
        });
    }

    public static void choosePathToExecutable(VpypeSettings settings){
        FileChooser d = new FileChooser();
        d.setTitle("Choose " + VPYPE_NAME + " Executable");
        d.setInitialDirectory(settings.vPypeExecutable.getValue().isEmpty() ? FileUtils.getImportDirectory() : new File(settings.vPypeExecutable.getValue()).getParentFile());
        File file = d.showOpenDialog(null);
        if(file != null){
            settings.vPypeExecutable.setValue(file.getPath());
        }
    }

    public static File chooseOutputFile(){
        FileChooser d = new FileChooser();
        d.setTitle("Save " + VPYPE_NAME + " output file");
        d.setInitialDirectory(FileUtils.getExportDirectory());
        return d.showSaveDialog(null);
    }

}
