package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.files.presets.types.ConfigApplicationSettings;
import drawingbot.registry.Register;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

public class ConfigFileHandler {

    public static ConfigApplicationSettings applicationSettings;

    public static void init() {
        //create the user data directory
        File userDir = new File(FileUtils.getUserDataDirectory());
        userDir.mkdirs();

        //create the thumbnail directory
        File userThumbs = new File(FileUtils.getUserThumbnailDirectory());
        userThumbs.mkdirs();

        //load the JSON config files
        JsonLoaderManager.loadConfigFiles();

        //load any config objects for use during the loading phases
        applicationSettings = Register.PRESET_LOADER_CONFIGS.getConfigData(ConfigApplicationSettings.class);

        //setup any console output files, now that we know what settings they require
        ConfigFileHandler.setupConsoleOutputFile();
    }

    public static ConfigApplicationSettings getApplicationSettings(){
        return applicationSettings;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void setupConsoleOutputFile(){
        try {
            if(!applicationSettings.isDeveloperMode){
                File outputFile = new File(FileUtils.getUserDataDirectory(), "latest_output.txt");
                File logFile = new File(FileUtils.getUserDataDirectory(), "latest_log.txt");

                if(outputFile.exists()){
                    if(outputFile.renameTo(new File(FileUtils.getUserDataDirectory(), "prev_output.txt"))){
                        DrawingBotV3.logger.info("Renamed old output file");
                    }
                }

                if(logFile.exists()){
                    if(logFile.renameTo(new File(FileUtils.getUserDataDirectory(), "prev_log.txt"))){
                        DrawingBotV3.logger.info("Renamed old log file");
                    }
                }

                System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outputFile)), true));
                System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile)), true));
            }
            //send the logger to the System.err print stream
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new OutputFormat());
            consoleHandler.setLevel(Level.ALL);
            DrawingBotV3.logger.addHandler(consoleHandler);
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

