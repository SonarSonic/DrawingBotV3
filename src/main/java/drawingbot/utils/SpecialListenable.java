package drawingbot.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.function.Consumer;

public class SpecialListenable<LISTENER> {

    private ObservableList<LISTENER> listeners = null;

    public ObservableList<LISTENER> listeners(){
        if(listeners == null){
            listeners = FXCollections.observableArrayList();
        }
        return listeners;
    }

    public void addSpecialListener(LISTENER listener){
        listeners().add(listener);
    }

    public void removeSpecialListener(LISTENER listener){
        listeners().remove(listener);
    }

    public void sendListenerEvent(Consumer<LISTENER> listener){
        if(listeners == null){
            return;
        }
        listeners().forEach(listener);
    }

}
