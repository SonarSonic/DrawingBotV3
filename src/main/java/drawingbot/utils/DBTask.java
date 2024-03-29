package drawingbot.utils;

import drawingbot.DrawingBotV3;
import drawingbot.api.IExceptionCallback;
import drawingbot.api.IProgressCallback;
import drawingbot.files.json.projects.DBTaskContext;
import javafx.concurrent.Task;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public abstract class DBTask<V> extends Task<V> implements IProgressCallback, IExceptionCallback {

    public static final AtomicInteger idCounter = new AtomicInteger(0);
    public final int id = idCounter.addAndGet(1);


    public final DBTaskContext context;
    public boolean updateProgressInstantly = true;
    private String title = "";
    private String message = "";
    public String error = "";
    public double workDone = -1;
    public double max = 1;

    public DBTask(DBTaskContext context){
        this.context = context;
    }

    public int getTaskID(){
        return id;
    }

    /**
     * @return a simple type identifier, typically matching the name of the class, note we don't use {@link Class#getSimpleName()} due to obfuscation
     */
    public String getTaskType(){
        return "DBTask";
    }

    /**
     * @return A simple task name which includes a unique task id, which is increments as the program runs
     */
    public String getTaskName(){
        return "%s #%s".formatted(getTaskType(), getTaskID());
    }

    public boolean isPlottingTask(){
        return false;
    }

    public void setError(String error){
        this.error = error;
    }

    @Override
    public void setException(Throwable t) {
        super.setException(t);
        if(t != null){
            DrawingBotV3.logger.log(Level.SEVERE, "TASK FAILED", t);
            setError(t.getMessage());
            updateProgress(-1, 1);
        }
    }

    @Override
    public void updateProgress(long workDone, long max) {
        if(updateProgressInstantly){
            super.updateProgress(workDone, max);
        }
        this.workDone = workDone;
        this.max = max;
    }

    @Override
    public void updateProgress(double workDone, double max) {
        if(updateProgressInstantly){
            super.updateProgress(workDone, max);
        }
        this.workDone = workDone;
        this.max = max;
    }

    @Override
    public void updateMessage(String message) {
        if(updateProgressInstantly){
            super.updateMessage(message);
        }
        this.message = message;
    }

    @Override
    public void updateTitle(String title) {
        if(updateProgressInstantly){
            super.updateTitle(title);
        }
        this.title = title;
    }

    //called on JAVA FX Thread

    /**
     * By avoiding calling the super methods straight away we avoid a performance issue with JavaFX
     * In which progress updates are sent repeatedly, wrapped in runnables.
     * If the JavaFX thread runs behind or the progress updates are too frequent this has a massive impact on performance, so instead we call this method.
     * This allows us to update progress / message / title of a task as many times as we wish without a performance impact.
     */
    public void tick(){
        super.updateProgress(workDone, max);
        super.updateMessage(message);
        super.updateTitle(title);
    }

    public void tryDestroy(){
        if(!isRunning()){
            destroy();
        }
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        tryDestroy();
    }

    @Override
    protected void failed() {
        super.failed();
        tryDestroy();
    }

    /**
     * Called when the outputs of this task are no longer required on any thread, destroy should be able to be called multiple times without issues, as it should only de-reference and not process
     */
    public void destroy(){}

    public void stopElegantly(){}
}
