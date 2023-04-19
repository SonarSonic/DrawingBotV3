package drawingbot.api;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;

public interface IProperties extends Observable {

    ObservableList<? extends Observable> getPropertyList();

    @Override
    default void addListener(InvalidationListener listener){
        getPropertyList().addListener(listener);
    }

    @Override
    default void removeListener(InvalidationListener listener){
        getPropertyList().removeListener(listener);
    }
}