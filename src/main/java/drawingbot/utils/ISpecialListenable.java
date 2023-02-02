package drawingbot.utils;

import javafx.collections.ObservableList;

import java.util.function.Consumer;

public interface ISpecialListenable<LISTENER> {

    ObservableList<LISTENER> listeners();

    default void addSpecialListener(LISTENER listener){
        listeners().add(listener);
    }

    default void removeSpecialListener(LISTENER listener){
        listeners().remove(listener);
    }

    default void sendListenerEvent(Consumer<LISTENER> listener){
        listeners().forEach(listener);
    }
}
