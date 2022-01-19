package drawingbot.utils;

import javafx.concurrent.Task;

public abstract class DBTask<V> extends Task<V> {

    protected boolean updateInstantly = false;
    private String title = "";
    private String message = "";
    public String error = "";
    public double workDone = -1;
    public double max = 1;

    public boolean isPlottingTask(){
        return false;
    }

    public void setError(String error){
        this.error = error;
    }

    @Override
    public void updateProgress(long workDone, long max) {
        if(updateInstantly){
            super.updateProgress(workDone, max);
        }
        this.workDone = workDone;
        this.max = max;
    }

    @Override
    public void updateProgress(double workDone, double max) {
        if(updateInstantly){
            super.updateProgress(workDone, max);
        }
        this.workDone = workDone;
        this.max = max;
    }

    @Override
    public void updateMessage(String message) {
        if(updateInstantly){
            super.updateMessage(message);
        }
        this.message = message;
    }

    @Override
    public void updateTitle(String title) {
        if(updateInstantly){
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

}
