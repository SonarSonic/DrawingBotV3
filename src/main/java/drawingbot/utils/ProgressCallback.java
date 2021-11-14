package drawingbot.utils;

import java.util.ArrayList;
import java.util.List;

public class ProgressCallback {

    public float currentProgress = 0F;
    public String currentMessage = "";

    public float storedProgress = 0F;
    public float layerMultiplier = 1F;
    public List<String> prefixMessages = new ArrayList<>();
    public String fullPrefixMessage = "";
    public String previousMessage = "";

    public void reset(){
        storedProgress = 0F;
        layerMultiplier = 1F;
        prefixMessages.clear();
    }

    public void updateMessage(String message){
        previousMessage = message;
        currentMessage = fullPrefixMessage + message;
    }

    public void pushLayers(int tasks){
        layerMultiplier /= tasks;
        if(!prefixMessages.isEmpty()){
            prefixMessages.add(previousMessage);
        }
    }

    public void popLayers(int tasks){

        layerMultiplier *= tasks;

        if(!prefixMessages.isEmpty()){
            prefixMessages.remove(prefixMessages.get(prefixMessages.size()-1));
        }
        fullPrefixMessage = getPrefixMessage();
    }

    public String getPrefixMessage(){
        StringBuilder builder = new StringBuilder();
        for(String s : prefixMessages){
            builder.append(s);
            builder.append(" | ");
        }
        return builder.toString();
    }

    public void startTask(){
        //NOP ?
    }

    public void finishTask(){
        storedProgress += layerMultiplier;
    }

    public void updateProgress(float progress){
        currentProgress = storedProgress + (progress * layerMultiplier);
    }

    public void setTotalProgress(float totalProgress){
        currentProgress = totalProgress;
    }

}
