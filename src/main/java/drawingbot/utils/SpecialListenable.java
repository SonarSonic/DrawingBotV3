package drawingbot.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.function.Consumer;

public class SpecialListenable<LISTENER> implements ISpecialListenable<LISTENER>  {

    private ObservableList<LISTENER> listeners = null;

    public ObservableList<LISTENER> listeners(){
        if(listeners == null){
            listeners = FXCollections.observableArrayList();
        }
        return listeners;
    }

    public void sendListenerEvent(Consumer<LISTENER> listener){
        if(listeners == null){
            return;
        }
        ISpecialListenable.super.sendListenerEvent(listener);
    }

}
