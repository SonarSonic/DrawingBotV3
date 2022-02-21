package drawingbot.integrations.vpype;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.FileUtils;
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

    public static void exportToVpype(){
        Platform.runLater(() -> {
            if(DrawingBotV3.INSTANCE.getActiveTask() != null){
                String userCommand = DrawingBotV3.INSTANCE.vPypeCommand.getValue();
                File tempSVG = new File(FileUtils.getUserDataDirectory(), TEMP_FILE_NAME);

                if(userCommand.contains(OUTPUT_FILE_WILDCARD)){
                    File outputFile = chooseOutputFile();
                    if(outputFile == null){
                        return;
                    }
                    userCommand = userCommand.replaceAll(OUTPUT_FILE_WILDCARD, Matcher.quoteReplacement(outputFile.toString()));
                }

                String command = DrawingBotV3.INSTANCE.vPypeExecutable.getValue() + " read " + Matcher.quoteReplacement(tempSVG.toString()) + " " + userCommand;
                Task<?> task = DrawingBotV3.INSTANCE.createExportTask(Register.EXPORT_SVG, DrawingBotV3.INSTANCE.getActiveTask(), IGeometryFilter.DEFAULT_EXPORT_FILTER, ".svg", tempSVG, false, DrawingBotV3.INSTANCE.vPypeBypassOptimisation.get());
                task.setOnSucceeded(event -> DrawingBotV3.INSTANCE.taskMonitor.queueTask(new VpypeTask(command)));
            }
        });
    }

    public static void choosePathToExecutable(){
        FileChooser d = new FileChooser();
        d.setTitle("Choose " + VPYPE_NAME + " Executable");
        d.setInitialDirectory(DrawingBotV3.INSTANCE.vPypeExecutable.getValue().isEmpty() ? FileUtils.getImportDirectory() : new File(DrawingBotV3.INSTANCE.vPypeExecutable.getValue()).getParentFile());
        File file = d.showOpenDialog(null);
        if(file != null){
            DrawingBotV3.INSTANCE.vPypeExecutable.set(file.getPath());
        }
    }

    public static File chooseOutputFile(){
        FileChooser d = new FileChooser();
        d.setTitle("Save " + VPYPE_NAME + " output file");
        d.setInitialDirectory(FileUtils.getExportDirectory());
        return d.showSaveDialog(null);
    }

}
