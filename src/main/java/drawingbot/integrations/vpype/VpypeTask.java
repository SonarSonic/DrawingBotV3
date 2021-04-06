package drawingbot.integrations.vpype;

import drawingbot.DrawingBotV3;
import drawingbot.files.FileUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;

public class VpypeTask extends Task<Boolean> {

    public String command;

    public VpypeTask(String command) {
        this.command = command;
    }

    @Override
    protected Boolean call() throws Exception {
        Platform.runLater(() -> DrawingBotV3.INSTANCE.vPypeTask = this);

        updateTitle(VpypeHelper.VPYPE_NAME + " Command - Waiting - " + command.replace(DrawingBotV3.INSTANCE.vPypeExecutable.getValue(), "vpype"));
        updateProgress(-1, 1);

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", command);
        } else {
            builder.command("sh", "-c", command);
        }
        builder.directory(new File(FileUtils.getUserHomeDirectory()));
        Process process = builder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        Executors.newSingleThreadExecutor().submit(() -> reader.lines().forEach(System.out::println));

        //int exitCode = process.waitFor();

        updateMessage(VpypeHelper.VPYPE_NAME + " Command - Finished");
        updateProgress(1, 1);

        Platform.runLater(() -> DrawingBotV3.INSTANCE.vPypeTask = null);

        return true;
    }
}
