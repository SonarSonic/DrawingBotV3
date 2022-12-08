package drawingbot.api;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;

public interface IProperties extends Observable {

    ObservableList<? extends Observable> getObservables();

    @Override
    default void addListener(InvalidationListener listener){
        getObservables().addListener(listener);
    }

    @Override
    default void removeListener(InvalidationListener listener){
        getObservables().removeListener(listener);
    }
}