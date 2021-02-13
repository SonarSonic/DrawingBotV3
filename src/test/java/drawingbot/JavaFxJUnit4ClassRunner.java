package drawingbot;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
/**
 * src: http://awhite.blogspot.com/2013/04/javafx-junit-testing.html
 */
public class JavaFxJUnit4ClassRunner extends BlockJUnit4ClassRunner {

    public JavaFxJUnit4ClassRunner(final Class<?> clazz) throws InitializationError{
        super(clazz);
        JavaFxJUnit4Application.startJavaFx();
    }
}