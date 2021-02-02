package drawingbot.files;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.nio.file.Files;

public class Configuration {

    public static DBSettings settings;

    public static void init() {
        try {
            File userDir = new File(FileUtils.getUserDataDirectory());
            userDir.mkdirs();
            File configFile = new File(userDir,"settings.json");
            boolean createSettingsFile = true;

            if(Files.exists(configFile.toPath())){
                Gson gson = new Gson();
                JsonReader reader = gson.newJsonReader(new FileReader(configFile));
                settings = gson.fromJson(reader, DBSettings.class);
                reader.close();
                if(settings != null){
                    createSettingsFile = false;
                }
            }

            if(createSettingsFile){
                Gson gson = new Gson();
                JsonWriter writer = gson.newJsonWriter(new FileWriter(configFile));
                gson.toJson(settings = new DBSettings(), DBSettings.class, writer);
                writer.flush();
                writer.close();

            }
            Configuration.setupConsoleOutputFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(settings == null){ //probably unnecessary
            settings = new DBSettings();
        }
    }

    public static void setupConsoleOutputFile() throws FileNotFoundException {
        System.setOut(new PrintStream(new FileOutputStream(new File(FileUtils.getUserDataDirectory(), "output.txt"))));
    }

    /**general settings*/
    public static class DBSettings {
        public boolean isDeveloperMode;

    }

}

