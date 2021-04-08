package drawingbot.integrations.vpype;

import drawingbot.DrawingBotV3;
import drawingbot.files.FileUtils;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VpypeTask extends Task<Boolean> {

    public String command;

    public VpypeTask(String command) {
        this.command = command;
    }

    @Override
    protected Boolean call() throws Exception {

        updateTitle(VpypeHelper.VPYPE_NAME + " Command - Sending");
        updateMessage(command.replace(DrawingBotV3.INSTANCE.vPypeExecutable.getValue(), "vpype"));
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

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> new BufferedReader(new InputStreamReader(process.getInputStream())).lines().forEach(System.out::println));
        executor.submit(() -> new BufferedReader(new InputStreamReader(process.getErrorStream())).lines().forEach(System.out::println));

        updateTitle(VpypeHelper.VPYPE_NAME + "Command -  Finished");
        updateProgress(1, 1);
        return true;
    }
}
