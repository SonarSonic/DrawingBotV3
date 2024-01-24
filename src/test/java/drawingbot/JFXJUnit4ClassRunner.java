package drawingbot;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * src: http://awhite.blogspot.com/2013/04/javafx-junit-testing.html
 */
public class JFXJUnit4ClassRunner extends BlockJUnit4ClassRunner {

    public JFXJUnit4ClassRunner(final Class<?> clazz) throws InitializationError{
        super(clazz);
        synchronized (JFXJUnit4ClassRunner.class){
            JFXJUnit4Launcher.startJavaFx();
        }
    }
}