package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.JsonLoaderManager;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

public class ConfigFileHandler {

    public static void init() {
        //create the user data directory
        File userDir = new File(FileUtils.getUserDataDirectory());
        if(!userDir.exists() && !userDir.mkdirs()){
            DrawingBotV3.logger.severe("Failed to create User Data Directory");
        }

        //create the thumbnail directory
        File userThumbsDir = new File(FileUtils.getUserThumbnailDirectory());
        if(!userThumbsDir.exists() && !userThumbsDir.mkdirs()){
            DrawingBotV3.logger.severe("Failed to create User Thumbnail Directory");
        }

        //create the fonts directory
        File userFontDir = new File(FileUtils.getUserFontsDirectory());
        if(!userFontDir.exists() && !userFontDir.mkdirs()){
            DrawingBotV3.logger.severe("Failed to create User Fonts Directory");
        }

        //load the JSON config files
        JsonLoaderManager.loadConfigFiles();

        //load any config objects for use during the loading phases
        //applicationSettings = Register.PRESET_LOADER_CONFIGS.getConfigData(PresetApplicationSettings.class);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void setupConsoleOutputFile(){
        try {
            //add the VM argument -Ddrawingbot.DrawingBotV3.dLogging=true
            String developerLogging = System.getProperty("drawingbot.DrawingBotV3.dLogging");
            if(developerLogging == null || !developerLogging.equals("true")){
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

                PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(outputFile)), false);
                PrintStream err = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile)), false);

                System.setOut(out);
                System.setErr(err);
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

    public static void logApplicationStatus(){
        DrawingBotV3.logger.info("Java Home: " + System.getProperty("java.home"));
        DrawingBotV3.logger.info("Java Version: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
        DrawingBotV3.logger.info("Operating System: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
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

