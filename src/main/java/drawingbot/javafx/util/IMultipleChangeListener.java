package drawingbot.javafx.util;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Set;

/**
 * Convenience interface to add clear methods that link back to the implementers {@link MultipleListenerHandler}
 */
public interface IMultipleChangeListener {
    
    MultipleListenerHandler getListenerHandler();

    default <T> void registerChangeListener(ObservableValue<T> property, ChangeListener<T> changeListener) {
        getListenerHandler().registerChangeListener(property, changeListener);
    }

    default <T> void unregisterChangeListener(ObservableValue<T> property, ChangeListener<T> changeListener) {
        getListenerHandler().unregisterChangeListener(property, changeListener);
    }

    default Set<ChangeListener> unregisterChangeListeners(ObservableValue<?> property) {
        return getListenerHandler().unregisterChangeListeners(property);
    }

    default void registerInvalidationListener(Observable observable, InvalidationListener invalidationListener) {
        getListenerHandler().registerInvalidationListener(observable, invalidationListener);
    }

    default void unregisterInvalidationListener(Observable observable, InvalidationListener changeListener) {
        getListenerHandler().unregisterInvalidationListener(observable, changeListener);
    }

    default Set<InvalidationListener> unregisterInvalidationListeners(Observable property) {
        return getListenerHandler().unregisterInvalidationListeners(property);
    }

    default <T> void registerListChangeListener(ObservableList<T> property, ListChangeListener<T> listChangeListener) {
        getListenerHandler().registerListChangeListener(property, listChangeListener);
    }

    default <T> void unregisterInvalidationListener(ObservableList<T> observableList, ListChangeListener<T> listChangeListener) {
        getListenerHandler().unregisterInvalidationListener(observableList, listChangeListener);
    }

    default Set<ListChangeListener> unregisterListChangeListeners(ObservableList<?> property) {
        return getListenerHandler().unregisterListChangeListeners(property);
    }
}
