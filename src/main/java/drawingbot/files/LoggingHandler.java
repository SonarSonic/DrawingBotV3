package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.utils.Utils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    public static FileHandler fileHandler;
    public static String logPrefix = "_Log_";
    public static int logFileCount = 10; //How many log files to keep before deleting them
    public static File currentLogFile;

    public static void setupConsoleOutputFile(){
        try {
            // Send the loggers output to the System.err print stream
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new OutputFormat());
            consoleHandler.setLevel(Level.ALL);

            DrawingBotV3.logger.setUseParentHandlers(false);
            DrawingBotV3.logger.addHandler(consoleHandler);

            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + FXApplication.getSoftware().getShortName() + logPrefix + "*.txt");
            List<File> files = Files.list(new File(FileUtils.getUserLogsDirectory()).toPath()).filter(Files::isRegularFile).filter(Files::isReadable).filter(p -> pathMatcher.matches(p.getFileName())).map(Path::toFile).sorted(Comparator.comparingLong(File::lastModified)).collect(Collectors.toList());

            for(int i = 0; i < files.size()-logFileCount; i++){
                File file = files.get(i);
                if(!file.delete()){
                    DrawingBotV3.logger.log(Level.WARNING, "Failed to delete old log files");
                    break;
                }
            }

            // Send the loggers output to the latest_log.txt file
            currentLogFile = new File(FileUtils.getUserLogsDirectory() + File.separator + FXApplication.getSoftware().getShortName() + logPrefix + Utils.getDateAndTimeSafe() + "_%g" + ".txt");
            FileHandler fileHandler = new FileHandler(currentLogFile.toString());
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new OutputFormat());
            DrawingBotV3.logger.addHandler(fileHandler);
        } catch (Exception e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error setting up console output");
        }
    }

    public static void deleteLegacyFiles(){
        deleteLegacyFile(new File(FileUtils.getUserDataDirectory(), "latest_log.txt"));
        deleteLegacyFile(new File(FileUtils.getUserDataDirectory(), "prev_log.txt"));
        deleteLegacyFile(new File(FileUtils.getUserDataDirectory(), "latest_output.txt"));
        deleteLegacyFile(new File(FileUtils.getUserDataDirectory(), "prev_output.txt"));

        try {
            // Delete old log files which were missing the correct prefix, pre-1.6.12
            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + logPrefix + "*.txt");
            Files.list(new File(FileUtils.getUserLogsDirectory()).toPath()).filter(Files::isRegularFile).filter(Files::isReadable).filter(p -> pathMatcher.matches(p.getFileName())).forEach(path -> deleteLegacyFile(path.toFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void deleteLegacyFile(File file){
        if(file.exists() && file.delete()){
            DrawingBotV3.logger.info("Deleted Legacy File " + file.toString());
        }
    }

    public static void saveLoggingFiles(){
        if(fileHandler != null){
            fileHandler.flush();
            fileHandler.close();
        }
    }

    public static void logApplicationStatus(){
        DrawingBotV3.logger.config("Date: " + new Date(System.currentTimeMillis()));
        DrawingBotV3.logger.config("Java Home: " + System.getProperty("java.home"));
        DrawingBotV3.logger.config("Java Version: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
        DrawingBotV3.logger.config("Operating System: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        DrawingBotV3.logger.config("Running: " + FXApplication.getSoftware().getDisplayName() + " " + FXApplication.getSoftware().getDisplayVersion());
        DrawingBotV3.logger.config("Working Directory: " + FileUtils.getWorkingDirectory());
        DrawingBotV3.logger.config("Data Directory: " + FileUtils.getUserDataDirectory());

    }

    public static boolean createReportZip(String zipFilePath){
        try {
            try (ZipArchiveOutputStream zip = new ZipArchiveOutputStream(new FileOutputStream(zipFilePath))) {
                addFilteredFilesToZip(zip, FileUtils.getUserLogsDirectory(), "logs/", "glob:" + FXApplication.getSoftware().getShortName() + logPrefix + "*.txt");
                addFilteredFilesToZip(zip, FileUtils.getWorkingDirectory(), "crashes/", "glob:" + "hs_err*.log");
                addFilteredFilesToZip(zip, FileUtils.getUserDataDirectory(), "", "glob:" + "config_settings.json");
                zip.finish();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void addFilteredFilesToZip(ZipArchiveOutputStream zip, String sourceDir, String destDir, String filter) throws IOException{
        File sourceDirFile = new File(sourceDir);
        if(!sourceDirFile.exists()){
            return;
        }
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(filter);
        List<File> files = Files.list(sourceDirFile.toPath()).filter(Files::isRegularFile).filter(Files::isReadable).filter(p -> pathMatcher.matches(p.getFileName())).map(Path::toFile).collect(Collectors.toList());

        if(files.isEmpty()){
            return;
        }

        for (File f : files) {
            ZipArchiveEntry entry = zip.createArchiveEntry(f, destDir + f.getName());
            zip.putArchiveEntry(entry);
            if (f.isFile()) {
                try (InputStream i = Files.newInputStream(f.toPath())) {
                    IOUtils.copy(i, zip);
                }
            }
            zip.closeArchiveEntry();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    ///copy of java.util.logging.SimpleFormatter with custom format to fit logs onto one line
    public static class OutputFormat extends Formatter {
        private static final String formatWithoutThrowable = "%1$tY.%1$tm.%1$td %1$tl:%1$tM:%1$tS %1$Tp: %4$s - %2$s - %5$s%n";
        private static final String formatWithThrowable = "%1$tY.%1$tm.%1$td %1$tl:%1$tM:%1$tS %1$Tp: %4$s - %2$s - %5$s%n%6$s%n";

        private static final String formatConfigData = "%1$tY.%1$tm.%1$td %1$tl:%1$tM:%1$tS %1$Tp: %2$s - %3$s%n";

        @Override
        public String formatMessage(LogRecord record) {
            return super.formatMessage(record);
        }

        @Override
        public synchronized String format(LogRecord record) {
            ZonedDateTime zdt = ZonedDateTime.ofInstant(record.getInstant(), ZoneId.systemDefault());
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

            String source;
            if (record.getSourceClassName() != null) {
                source = record.getSourceClassName();
                if (record.getSourceMethodName() != null) {
                    source += " " + record.getSourceMethodName();
                }
            } else {
                source = record.getLoggerName();
            }

            return String.format(throwable.isEmpty() ? formatWithoutThrowable : formatWithThrowable, zdt, source, record.getLoggerName(), record.getLevel().getName(), message, throwable);
        }
    }
}

