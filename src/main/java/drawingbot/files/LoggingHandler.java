package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.utils.Utils;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.*;
import java.util.stream.Collectors;

public class LoggingHandler {

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

        //create the logs directory
        File userLogsDir = new File(FileUtils.getUserLogsDirectory());
        if(!userLogsDir.exists() && !userLogsDir.mkdirs()){
            DrawingBotV3.logger.severe("Failed to create User Logs Directory");
        }

        DrawingBotV3.logger.setLevel(Level.FINE);
        setupConsoleOutputFile();
        logApplicationStatus();
        deleteLegacyFiles();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static PrintStream logFileStream;
    public static String logPrefix = "DBV3_Log_";
    public static int logFileCount = 10; //How many log files to keep before deleting them

    public static void setupConsoleOutputFile(){
        try {
            // Send the loggers output to the System.err print stream
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new OutputFormat());
            consoleHandler.setLevel(Level.ALL);

            DrawingBotV3.logger.setUseParentHandlers(false);
            DrawingBotV3.logger.addHandler(consoleHandler);


            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + logPrefix + "*.txt");
            List<File> files = Files.list(new File(FileUtils.getUserLogsDirectory()).toPath()).filter(Files::isRegularFile).filter(Files::isReadable).filter(p -> pathMatcher.matches(p.getFileName())).map(Path::toFile).sorted(Comparator.comparingLong(File::lastModified)).collect(Collectors.toList());

            for(int i = 0; i < files.size()-logFileCount; i++){
                File file = files.get(i);
                if(!file.delete()){
                    DrawingBotV3.logger.log(Level.WARNING, "Failed to delete old log files");
                    break;
                }
            }

            // Send the loggers output to the latest_log.txt file
            File logFile = Files.createTempFile(new File(FileUtils.getUserLogsDirectory()).toPath(), logPrefix + Utils.getDateAndTimeSafe() + "_", ".txt").toFile();

            logFileStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile)), true);
            StreamHandler streamHandler = new StreamHandler(logFileStream, new OutputFormat());
            streamHandler.setLevel(Level.ALL);
            DrawingBotV3.logger.addHandler(streamHandler);


        } catch (Exception e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error setting up console output");
        }
    }

    public static void deleteLegacyFiles(){
        deleteLegacyFile(new File(FileUtils.getUserDataDirectory(), "latest_log.txt"));
        deleteLegacyFile(new File(FileUtils.getUserDataDirectory(), "prev_log.txt"));
        deleteLegacyFile(new File(FileUtils.getUserDataDirectory(), "latest_output.txt"));
        deleteLegacyFile(new File(FileUtils.getUserDataDirectory(), "prev_output.txt"));
    }

    public static void deleteLegacyFile(File file){
        if(file.exists() && file.delete()){
            DrawingBotV3.logger.info("Deleted Legacy File " + file.toString());
        }
    }

    public static void saveLoggingFiles(){
        if(logFileStream != null){
            logFileStream.flush();
            logFileStream.close();
        }
    }

    public static void logApplicationStatus(){
        DrawingBotV3.logger.info("Java Home: " + System.getProperty("java.home"));
        DrawingBotV3.logger.info("Java Version: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
        DrawingBotV3.logger.info("Operating System: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        DrawingBotV3.logger.info("Running: " + FXApplication.getSoftware().getDisplayName() + " " + FXApplication.getSoftware().getDisplayVersion());
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    ///copy of java.util.logging.SimpleFormatter with custom format to fit logs onto one line
    public static class OutputFormat extends Formatter {
        private static final String formatWithoutThrowable = "%1$tc: %4$s - %2$s - %5$s%n";
        private static final String formatWithThrowable = "%1$tc: %4$s - %2$s - %5$s%n%6$s%n";
        private final Date dat = new Date();

        @Override
        public String formatMessage(LogRecord record) {
            return super.formatMessage(record);
        }

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
            return String.format(throwable.isEmpty() ? formatWithoutThrowable : formatWithThrowable, dat, source, record.getLoggerName(), record.getLevel().getName(), message, throwable);
        }
    }
}

