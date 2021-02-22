package drawingbot.files;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import drawingbot.DrawingBotV3;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.files.presets.types.ConfigApplicationSettings;
import drawingbot.files.presets.types.ConfigJsonLoader;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.logging.*;
import java.util.logging.Formatter;

public class ConfigFileHandler {

    public static ConfigApplicationSettings applicationSettings;

    public static void init() {
        //create the user data directory
        File userDir = new File(FileUtils.getUserDataDirectory());
        userDir.mkdirs();

        //start by loading the application jsons to allow settings to be used during configuration
        JsonLoaderManager.loadJSONFiles();

        //load any config objects for use during the loading phases
        applicationSettings = JsonLoaderManager.CONFIGS.getConfigData(ConfigApplicationSettings.class);

        //setup any console output files, now that we know what settings they require
        ConfigFileHandler.setupConsoleOutputFile();
    }

    public static ConfigApplicationSettings getApplicationSettings(){
        return applicationSettings;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void setupConsoleOutputFile(){
        try {
            if(!getApplicationSettings().isDeveloperMode){
                File outputFile = new File(FileUtils.getUserDataDirectory(), "latest_output.txt");
                System.setOut(new PrintStream(new FileOutputStream(outputFile)));
                System.setErr(new PrintStream(new FileOutputStream(outputFile)));
            }
            File logFile = new File(FileUtils.getUserDataDirectory(), "latest_log.txt");
            FileHandler fileHandler = new FileHandler(logFile.toString());
            fileHandler.setFormatter(new OutputFormat());
            fileHandler.setLevel(Level.ALL);

            StreamHandler streamHandler = new StreamHandler(System.out, new OutputFormat());
            streamHandler.setLevel(Level.ALL);

            DrawingBotV3.logger.setUseParentHandlers(false);
            DrawingBotV3.logger.addHandler(streamHandler);
            DrawingBotV3.logger.addHandler(fileHandler);
        } catch (Exception e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error setting up console output");
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    ///copy of java.util.logging.SimpleFormatter with custom format to fit logs onto one line
    private static class OutputFormat extends Formatter{
        private static final String formatWithoutThrowable = "%1$tc: %4$s - %2$s - %5$s%n";
        private static final String formatWithThrowable = "%1$tc: %4$s - %2$s - %5$s%n%6$s%n";
        private final Date dat = new Date();

        @Override
        public synchronized String format(LogRecord record) {
            dat.setTime(record.getMillis());
            String source;
            if (record.getSourceClassName() != null) {
                source = record.getSourceClassName();
                if (record.getSourceMethodName() != null) {
                    source += " " + record.getSourceMethodName();
                }
            } else {
                source = record.getLoggerName();
            }
            String message = formatMessage(record);
            String throwable = "";
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println();
                record.getThrown().printStackTrace(pw);
                pw.close();
                throwable = sw.toString();
            }
            return String.format(throwable.isEmpty() ? formatWithoutThrowable :formatWithThrowable, dat, source, record.getLoggerName(), record.getLevel().getName(), message, throwable);
        }
    }

}

