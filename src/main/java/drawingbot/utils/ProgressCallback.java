package drawingbot.utils;

import java.util.function.Consumer;

public class ProgressCallback {

    public float currentProgress = 0F;
    public String currentMessage = "";
    public String currentTitle = "";

    public float storedProgress = 0F;
    public float layerMultiplier = 1F;
    public String previousMessage = "";

    public Consumer<String> messageCallback;
    public Consumer<Float> progressCallback;

    public ProgressCallback(){}

    public ProgressCallback(DBTask<?> task){
        this.messageCallback = task::updateMessage;
        this.progressCallback = f -> task.updateProgress(f, 1F);
    }

    public void reset(){
        storedProgress = 0F;
        layerMultiplier = 1F;
    }

    public void updateTitle(String title){
        currentTitle = title;
    }

    public void updateMessage(String message){
        previousMessage = message;
        currentMessage = currentTitle + message;
        messageCallback.accept(currentMessage);
    }

    public void pushLayers(int tasks){
        layerMultiplier /= tasks;
    }

    public void popLayers(int tasks){
        layerMultiplier *= tasks;
    }

    public void startTask(){
        //NOP ?
    }

    public void finishTask(){
        storedProgress += layerMultiplier;
    }

    public void updateProgress(float progress){
        currentProgress = storedProgress + (progress * layerMultiplier);
        progressCallback.accept(currentProgress);
    }

    public void setTotalProgress(float totalProgress){
        currentProgress = totalProgress;
        progressCallback.accept(currentProgress);
    }

}
