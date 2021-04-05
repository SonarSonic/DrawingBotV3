package drawingbot.integrations.vpype;

import drawingbot.DrawingBotV3;
import drawingbot.files.ExportFormats;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.geom.basic.IGeometry;
import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Matcher;

public class VpypeHelper {

    public static final String VPYPE_NAME = "vpype";
    public static final String TEMP_FILE_NAME = "vpype_export.svg";
    public static final String OUTPUT_FILE_WILDCARD = "%OUTPUT_FILE%";

    public static void exportToVpype(){
        Platform.runLater(() -> {
            if(DrawingBotV3.INSTANCE.getActiveTask() != null){
                if(DrawingBotV3.INSTANCE.vPypeExecutable.getValue().isEmpty() || Files.notExists(new File(DrawingBotV3.INSTANCE.vPypeExecutable.getValue()).toPath())){
                    File newPath = choosePathToExecutable();
                    if(newPath == null || Files.notExists(newPath.toPath())){
                        return;
                    }
                }
                if(DrawingBotV3.INSTANCE.vPypeWorkingDirectory.getValue().isEmpty() || !Files.isDirectory(new File(DrawingBotV3.INSTANCE.vPypeWorkingDirectory.getValue()).toPath())){
                    File newDirectory = choosePathToWorkingDirectory();
                    if(newDirectory == null || Files.isDirectory(newDirectory.toPath())){
                        return;
                    }
                }
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
                ExportTask task = DrawingBotV3.INSTANCE.createExportTask(ExportFormats.EXPORT_SVG, DrawingBotV3.INSTANCE.getActiveTask(), IGeometry.DEFAULT_FILTER, ".svg", tempSVG, false, DrawingBotV3.INSTANCE.vPypeBypassOptimisation.get());
                task.setOnSucceeded(event -> DrawingBotV3.INSTANCE.taskService.submit(new VpypeTask(command)));
            }
        });
    }

    public static File choosePathToExecutable(){
        FileChooser d = new FileChooser();
        d.setTitle("Choose " + VPYPE_NAME + " Executable");
        d.setInitialDirectory(DrawingBotV3.INSTANCE.vPypeExecutable.getValue().isEmpty() ? FileUtils.getImportDirectory() : new File(DrawingBotV3.INSTANCE.vPypeExecutable.getValue()).getParentFile());
        File file = d.showOpenDialog(null);
        if(file != null){
            DrawingBotV3.INSTANCE.vPypeExecutable.set(file.getPath());
        }
        return file;
    }

    public static File choosePathToWorkingDirectory(){
        DirectoryChooser d = new DirectoryChooser();
        d.setTitle("Choose " + VPYPE_NAME + " Working Directory");
        d.setInitialDirectory(DrawingBotV3.INSTANCE.vPypeWorkingDirectory.getValue().isEmpty() ? FileUtils.getImportDirectory() : new File(DrawingBotV3.INSTANCE.vPypeWorkingDirectory.getValue()));
        File directory = d.showDialog(null);
        if(directory != null){
            DrawingBotV3.INSTANCE.vPypeWorkingDirectory.set(directory.getPath());
        }
        return directory;
    }

    public static File chooseOutputFile(){
        FileChooser d = new FileChooser();
        d.setTitle("Save " + VPYPE_NAME + " output file");
        d.setInitialDirectory(new File(DrawingBotV3.INSTANCE.vPypeWorkingDirectory.get()));
        return d.showSaveDialog(null);
    }

}
