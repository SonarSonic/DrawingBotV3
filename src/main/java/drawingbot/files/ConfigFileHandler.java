package drawingbot.files;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import drawingbot.DrawingBotV3;
import drawingbot.image.ImageFilterRegistry;
import drawingbot.pfm.PFMMasterRegistry;
import drawingbot.utils.EnumPresetType;
import drawingbot.utils.GenericPreset;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.logging.*;
import java.util.logging.Formatter;

public class ConfigFileHandler {

    public static DBSettings settings;

    public static void init() {
        File userDir = new File(FileUtils.getUserDataDirectory());
        userDir.mkdirs();
        settings = getOrCreateJSONFile(DBSettings.class, new File(userDir,"settings.json"), c -> new DBSettings());
        ConfigFileHandler.setupConsoleOutputFile();

        PresetManager.loadJSONFiles();
    }

    public static <I> I getOrCreateJSONFile(Class<I> clazz, File file, Function<Class<I>, I> iProvider) {
        I loaded = null;
        try {
            boolean createSettingsFile = true;

            if(Files.exists(file.toPath())){
                Gson gson = new Gson();
                JsonReader reader = gson.newJsonReader(new FileReader(file));
                loaded = gson.fromJson(reader, clazz);
                reader.close();
                if(loaded != null){
                    createSettingsFile = false;
                }
            }

            if(createSettingsFile){
                Gson gson = new Gson();
                JsonWriter writer = gson.newJsonWriter(new FileWriter(file));
                gson.toJson(loaded = iProvider.apply(clazz), clazz, writer);
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error loading " + file.getName() + " using defaults");
            loaded = iProvider.apply(clazz);
        }
        return loaded;
    }

    public static void setupConsoleOutputFile(){
        try {
            if(!ConfigFileHandler.settings.isDeveloperMode){
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

    ///copy of java.util.logging.SimpleFormatter with custom format to fit logs onto one line
    public static class OutputFormat extends Formatter{
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

    /**general settings*/
    public static class DBSettings {
        public boolean isDeveloperMode;

    }

}

