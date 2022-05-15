package drawingbot.api_impl.actions;

import drawingbot.api.actions.IAction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ActionManager {

    private int position = -1;
    public ObservableList<IAction> actions = FXCollections.observableArrayList();

    public void runAction(IAction action){
        logAction(action);
        action.apply();
    }

    public void logAction(IAction action){
        deleteForwardActions(position);
        actions.add(action);
        position++;
    }

    public void undo(){
        if(position < 0){
            return;
        }
        IAction action = actions.get(position);
        action.invert().apply();
        position--;
    }

    public void redo(){
        if(position == actions.size() - 1){
            return;
        }
        position++;
        IAction action = actions.get(position);
        action.apply();
    }

    public void wipeHistory(){
        actions.clear();
        position = -1;
    }

    private void deleteForwardActions(int position){
        if(actions.size()<1){
            return;
        }
        if (actions.size() > position + 1) {
            actions.subList(position + 1, actions.size()).clear();
        }
    }

}
