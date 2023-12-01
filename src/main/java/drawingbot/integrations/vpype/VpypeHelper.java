package drawingbot.integrations.vpype;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.api.IProgressCallback;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.files.exporters.SVGExporter;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.util.JFXUtils;
import drawingbot.utils.Utils;
import javafx.stage.FileChooser;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VpypeHelper {

    public static final String VPYPE_NAME = "vpype";
    public static final String OUTPUT_FILE_WILDCARD = "%OUTPUT_FILE%";

    public static ProcessBuilder buildGenericProcess(String command){
        ProcessBuilder builder = new ProcessBuilder();
        if (Utils.getOS().isWindows()) {
            builder.command("cmd.exe", "/c", command);
        } else {
            builder.command("sh", "-c", command);
        }
        builder.directory(new File(FileUtils.getUserHomeDirectory()));
        return builder;
    }

    public static void vpypeExport(VpypeSettings vpypeSettings, ExportTask exportTask, File saveLocation){

        // Check we have a valid executable
        if(!VpypeHelper.hasExecutable(vpypeSettings) && !VpypeHelper.autoDetectVpype(vpypeSettings)){
            Boolean result = JFXUtils.runTaskNow(new FutureTask<>(() -> {
                VpypeHelper.choosePathToExecutable(vpypeSettings);
                return VpypeHelper.hasExecutable(vpypeSettings);
            }));

            if(result == null || !result){
                exportTask.updateMessage("Cancelled");
                DrawingBotV3.logger.info("Export to vpype: Cancelled " + "Invalid Executable");
                exportTask.updateProgress(0,1);
                return;
            }
        }

        //Check if the command contains an output
        boolean hasOutput = vpypeSettings.vpypeCommand.get().contains(VpypeHelper.OUTPUT_FILE_WILDCARD);
        boolean waitForCompletion = hasOutput || exportTask.exportMode != ExportTask.Mode.PER_DRAWING;
        String requestedOutput = "";
        String requestedExtension = "";
        FileChooser.ExtensionFilter requestedFilter = null;

        if(hasOutput) {
            //Extract the final destination from and set this as the save location so we can open it later
            Pattern pattern = Pattern.compile("\"(.*"+VpypeHelper.OUTPUT_FILE_WILDCARD+".*)\"");
            Matcher matcher = pattern.matcher(vpypeSettings.vpypeCommand.get());
            if (matcher.find()) {
                requestedOutput = matcher.group(1);
                requestedExtension = FileUtils.getExtension(requestedOutput);
                requestedFilter = FileUtils.findMatchingExtensionFilter(requestedExtension);
            }

            //Note: We earlier prevented the default destination picker
            if(!exportTask.isSubTask && exportTask.exportMode == ExportTask.Mode.PER_DRAWING){
                FileChooser.ExtensionFilter finalFilter = requestedFilter == null ? FileUtils.FILTER_ALL_FILES : requestedFilter;
                saveLocation = JFXUtils.runTaskNow(new FutureTask<>(() -> {
                    return VpypeHelper.chooseOutputFile(exportTask.context, finalFilter);
                }));

                if(saveLocation == null){
                    exportTask.updateMessage("Cancelled");
                    DrawingBotV3.logger.info("Export to vpype: Cancelled ");
                    exportTask.updateProgress(0,1);
                    return;
                }else{
                    exportTask.saveLocation = saveLocation;
                    exportTask.extension = requestedExtension;
                }
            }

        }

        File tempFile = null;
        try {
            tempFile = Files.createTempFile(VPYPE_NAME, ".svg").toFile();
            SVGExporter.exportBasicSVG(exportTask, tempFile);
            String finalCommand = VpypeHelper.createFinalCommand(vpypeSettings, tempFile, requestedFilter != null ? FileUtils.removeExtension(saveLocation) : saveLocation);

            VpypeHelper.runVpypeCommand(finalCommand, waitForCompletion, exportTask);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Attempt to delete our temporary SVG straight away, if not possible mark for deleteOnExit.
            if(tempFile != null && (!waitForCompletion || !tempFile.delete())){
                tempFile.deleteOnExit();
            }
        }
    }

    public static void runVpypeCommand(String command, boolean waitForCompletion, IProgressCallback callback) throws IOException, InterruptedException {
        callback.updateMessage(VpypeHelper.VPYPE_NAME + " - Command - Processing");
        callback.updateProgress(-1, 1);

        Process process = buildGenericProcess(command).start();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> new BufferedReader(new InputStreamReader(process.getInputStream())).lines().forEach(System.out::println));
        executor.submit(() -> new BufferedReader(new InputStreamReader(process.getErrorStream())).lines().forEach(System.out::println));

        if(waitForCompletion){
            CountDownLatch latch = new CountDownLatch(1);
            executor.submit(latch::countDown);
            latch.await();
        }
        executor.shutdown();

        callback.updateMessage(VpypeHelper.VPYPE_NAME + " - Command - Finished");
        callback.updateProgress(1, 1);
    }

    public static String matchUserCommand(VpypeSettings settings, File outputFile){
        String userCommand = settings.vpypeCommand.getValue();

        if(userCommand.contains(OUTPUT_FILE_WILDCARD) && outputFile != null) {
            userCommand = userCommand.replaceAll(OUTPUT_FILE_WILDCARD, Matcher.quoteReplacement(outputFile.toString()));
        }

        return userCommand;
    }

    public static String createFinalCommand(VpypeSettings settings, File inputFile, File outputFile){
        return settings.vpypeExecutable.get() + " read " + Matcher.quoteReplacement(inputFile.toString()) + " " + matchUserCommand(settings, outputFile);
    }

    public static boolean hasExecutable(VpypeSettings settings){
        return new File(settings.vpypeExecutable.get()).exists();
    }

    public static boolean autoDetectVpype(VpypeSettings settings)  {
        Process process = null;
        String stderr = "", stdout = "";
        try {
            process = buildGenericProcess("where " + VPYPE_NAME).start();
            stderr = IOUtils.toString(process.getErrorStream(), String.valueOf(Charset.defaultCharset()));
            stdout = IOUtils.toString(process.getInputStream(), String.valueOf(Charset.defaultCharset()));
        } catch (IOException e) {
            DrawingBotV3.logger.log(Level.WARNING, "Auto Detect Vpype: Failed", e);
        }
        Optional<String> vpypeLocation = stdout.lines().findFirst();
        if(stderr.isEmpty() && vpypeLocation.isPresent()){
            File file = new File(vpypeLocation.get());
            if(file.exists()){
                settings.vpypeExecutable.set(file.getPath());
                return true;
            }
        }
        return false;
    }

    public static void choosePathToExecutable(VpypeSettings settings){
        FileChooser d = new FileChooser();
        d.setTitle("Choose " + VPYPE_NAME + " Executable");
        d.setInitialDirectory(settings.vpypeExecutable.getValue().isEmpty() ? new File(FileUtils.getUserHomeDirectory()) : new File(settings.vpypeExecutable.getValue()).getParentFile());
        File file = d.showOpenDialog(null);
        if(file != null){
            settings.vpypeExecutable.setValue(file.getPath());
        }
    }

    public static File chooseOutputFile(DBTaskContext context, FileChooser.ExtensionFilter filter){
        FileChooser d = new FileChooser();
        d.setTitle("Save " + VPYPE_NAME + " output file");
        d.setInitialDirectory(context.project().getExportDirectory());
        d.getExtensionFilters().add(filter);
        d.setSelectedExtensionFilter(filter);
        return d.showSaveDialog(FXApplication.primaryStage);
    }

}
