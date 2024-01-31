package drawingbot;

import drawingbot.software.SoftwareManager;

///Fixes JAVA FX loading from jars : https://stackoverflow.com/questions/52569724/javafx-11-create-a-jar-file-with-gradle
public class Launcher {

    public static void main(String[] args) {
        SoftwareManager.setSoftware(SoftwareDBV3Free.INSTANCE);
        FXApplication.main(args);
    }

}
