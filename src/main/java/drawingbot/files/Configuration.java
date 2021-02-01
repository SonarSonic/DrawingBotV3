package drawingbot.files;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import drawingbot.DrawingBotV3;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Configuration {

    public static void init() {
        Gson gson = new Gson();
        try {
            //TODO
            File userDir = new File(FileUtils.getUserDataDirectory());
            userDir.mkdirs();
            File configFile = new File(userDir,"config.json");
            if(Files.exists(configFile.toPath())){
                JsonReader reader = gson.newJsonReader(new FileReader(configFile));
                TestFile test = gson.fromJson(reader, TestFile.class);
                System.out.println("loaded test: " + test);
                reader.close();
            }else{
                JsonWriter writer = gson.newJsonWriter(new FileWriter(configFile));
                gson.toJson(new TestFile(1, "big cat 1"), TestFile.class, writer);
                System.out.println("Saved Config oo: " + configFile.getAbsolutePath());
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class TestFile{
        public int num;
        public String cats;

        public TestFile(){}

        public TestFile(int num, String cats){
            this.num = num;
            this.cats = cats;
        }

        @Override
        public String toString() {
            return "Num: " + num + " Cats: " + cats;
        }
    }

}
