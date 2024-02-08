package drawingbot;

import drawingbot.files.json.JsonLoaderManager;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.javafx.util.JFXUtils;
import javafx.application.Platform;
import org.junit.Assert;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * src: http://awhite.blogspot.com/2013/04/javafx-junit-testing.html
 */
public class JUnitDBV3ClassRunner extends BlockJUnit4ClassRunner {

    public JUnitDBV3ClassRunner(final Class<?> clazz) throws InitializationError{
        super(clazz);
        JUnitDBV3Launcher.postLaunchMethod = JUnitDBV3ClassRunner::setupTestDBV3Free;
        synchronized (JUnitDBV3ClassRunner.class){
            JUnitDBV3Launcher.startJavaFx();
        }
    }

    public static boolean setupTestDBV3Free(){
        TestUtils.lazySetupDirectories();
        JFXUtils.runNow(() -> {
            DBPreferences.INSTANCE.autoRunPFM.set(false);
            JsonLoaderManager.loadDefaultPresetContainerJSON("pfm_unit_test.json");
        });

        //Load the default test image
        TestUtils.loadDefaultTestImage();
        return true;
    }
}