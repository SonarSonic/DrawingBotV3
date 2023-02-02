package drawingbot.api;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

import java.util.List;

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