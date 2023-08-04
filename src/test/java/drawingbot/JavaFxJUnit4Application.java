package drawingbot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * This is the application which starts JavaFx.  It is controlled through the startJavaFx() method.
 * src: http://awhite.blogspot.com/2013/04/javafx-junit-testing.html
 */
public class JavaFxJUnit4Application{

    /** The lock that guarantees that only one JavaFX thread will be started. */
    private static final ReentrantLock LOCK = new ReentrantLock();

    /** Started flag. */
    private static final AtomicBoolean started = new AtomicBoolean();

    public static final Consumer<String[]> launchMethod = Launcher::main;

    /**
     * Start JavaFx.
     */
    public static void startJavaFx()
    {
        try
        {
            // Lock or wait.  This gives another call to this method time to finish
            // and release the lock before another one has a go
            LOCK.lock();

            if (!started.get())
            {
                // start the JavaFX application
                final ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(JavaFxJUnit4Application::launch);

                while (!started.get())
                {
                    Thread.yield();
                }
            }
        }
        finally
        {
            LOCK.unlock();
        }
    }

    /**
     * Launch.
     */
    protected static void launch(){
        FXApplication.isLoaded.addListener((observable, oldValue, newValue) -> {
            if(newValue){
                started.set(true);
            }
        });
        launchMethod.accept(new String[0]);
    }
}