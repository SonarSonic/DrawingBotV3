package drawingbot;

import drawingbot.plugins.PremiumPluginDummy;
import drawingbot.registry.MasterRegistry;

///Fixes JAVA FX loading from jars : https://stackoverflow.com/questions/52569724/javafx-11-create-a-jar-file-with-gradle
public class Launcher {

    public static void main(String[] args) {
        MasterRegistry.PLUGINS.add(new PremiumPluginDummy());
        FXApplication.setSoftware(SoftwareDBV3Free.INSTANCE);
        FXApplication.main(args);
    }

}
