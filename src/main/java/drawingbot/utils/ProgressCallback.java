package drawingbot.utils;

import drawingbot.api.IProgressCallback;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ProgressCallback implements IProgressCallback {

    public float currentProgress = 0F;
    public String currentMessage = "";
    public String currentTitle = "";

    public float storedProgress = 0F;
    public float layerMultiplier = 1F;
    public String previousMessage = "";

    public Consumer<String> messageCallback;
    public BiConsumer<Float, Float> progressCallback;

    public ProgressCallback(){}

    public ProgressCallback(DBTask<?> task){
        this.messageCallback = task::updateMessage;
        this.progressCallback = task::updateProgress;
    }

    public void reset(){
        storedProgress = 0F;
        layerMultiplier = 1F;
    }

    @Override
    public void updateTitle(String title){
        currentTitle = title;
    }

    @Override
    public void updateMessage(String message){
        previousMessage = message;
        currentMessage = currentTitle + message;
        messageCallback.accept(currentMessage);
    }

    @Override
    public void updateProgress(float progress, float max) {
        currentProgress = storedProgress + (progress * layerMultiplier);
        progressCallback.accept(currentProgress, 1F);
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

    public void setTotalProgress(float totalProgress){
        currentProgress = totalProgress;
        progressCallback.accept(currentProgress, 1F);
    }

}
