package drawingbot.javafx.util;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handlers the registering / unregistering of multiple listeners to properties, observables and observable lists
 * Extended from {@link com.sun.javafx.scene.control.LambdaMultiplePropertyChangeListenerHandler}
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MultipleListenerHandler {

    public Map<ObservableValue<?>, Set<ChangeListener>> propertyChangeListenerMap;
    private ChangeListener<Object> propertyChangeListener; //keep a reference to prevent garbage collection
    private final WeakChangeListener weakPropertyChangeListener;

    public Map<Observable, Set<InvalidationListener>> observableInvalidationListenerMap;
    private InvalidationListener observableInvalidationListener; //keep a reference to prevent garbage collection
    private final WeakInvalidationListener weakObservableInvalidationListener;

    public Map<ObservableList<?>, Set<ListChangeListener>> observableListChangeListenerMap;
    private ListChangeListener<Object> observableListChangeListener; //keep a reference to prevent garbage collection
    private final WeakListChangeListener weakObservableListChangeListener;

    public MultipleListenerHandler(){
        this.propertyChangeListenerMap = new HashMap<>();
        this.propertyChangeListener = (observable, oldValue, newValue) -> {
            Set<ChangeListener> listeners = propertyChangeListenerMap.get(observable);
            if (listeners != null) {
                listeners.forEach(listener -> listener.changed(observable, oldValue, newValue));
            }
        };
        this.weakPropertyChangeListener = new WeakChangeListener(propertyChangeListener);


        this.observableInvalidationListenerMap = new HashMap<>();
        observableInvalidationListener = (observable) -> {
            Set<InvalidationListener> listeners = observableInvalidationListenerMap.get(observable);
            if (listeners != null) {
                listeners.forEach(listener -> listener.invalidated(observable));
            }
        };
        this.weakObservableInvalidationListener = new WeakInvalidationListener(observableInvalidationListener);


        this.observableListChangeListenerMap = new HashMap<>();
        observableListChangeListener = (change) -> {
            Set<ListChangeListener> listeners = observableListChangeListenerMap.get(change.getList());
            if (listeners != null) {
                listeners.forEach(listener -> listener.onChanged(change));
            }
        };
        this.weakObservableListChangeListener = new WeakListChangeListener(observableListChangeListener);
    }

    public final <T> void registerChangeListener(ObservableValue<T> property, ChangeListener<T> changeListener) {
        if (property == null || changeListener == null){
            return;
        }

        if (!propertyChangeListenerMap.containsKey(property)) {
            property.addListener(weakPropertyChangeListener);
        }
        propertyChangeListenerMap.putIfAbsent(property, new HashSet<>());
        propertyChangeListenerMap.get(property).add(changeListener);
    }

    public final <T> void unregisterChangeListener(ObservableValue<T> property, ChangeListener<T> changeListener) {
        if (property == null || changeListener == null){
            return;
        }

        Set<ChangeListener> changeListeners = propertyChangeListenerMap.get(property);
        if(changeListeners == null){
            return;
        }

        if(changeListeners.remove(changeListener) && changeListeners.isEmpty()){
            property.removeListener(weakPropertyChangeListener);
            propertyChangeListenerMap.remove(property);
        }
    }

    public final Set<ChangeListener> unregisterChangeListeners(ObservableValue<?> property) {
        if (property == null){
            return null;
        }

        property.removeListener(weakPropertyChangeListener);
        return propertyChangeListenerMap.remove(property);
    }

    public final void registerInvalidationListener(Observable observable, InvalidationListener invalidationListener) {
        if (observable == null || invalidationListener == null){
            return;
        }

        if (!observableInvalidationListenerMap.containsKey(observable)) {
            observable.addListener(weakObservableInvalidationListener);
        }
        observableInvalidationListenerMap.putIfAbsent(observable, new HashSet<>());
        observableInvalidationListenerMap.get(observable).add(invalidationListener);
    }

    public final void unregisterInvalidationListener(Observable observable, InvalidationListener changeListener) {
        if (observable == null || changeListener == null){
            return;
        }

        Set<InvalidationListener> invalidationListeners = observableInvalidationListenerMap.get(observable);
        if(invalidationListeners == null){
            return;
        }
        if(invalidationListeners.remove(changeListener) && invalidationListeners.isEmpty()){
            observable.removeListener(weakObservableInvalidationListener);
            observableInvalidationListenerMap.remove(observable);
        }
    }

    public final Set<InvalidationListener> unregisterInvalidationListeners(Observable property) {
        if (property == null){
            return null;
        }
        property.removeListener(weakObservableInvalidationListener);
        return observableInvalidationListenerMap.remove(property);
    }

    public final <T> void registerListChangeListener(ObservableList<T> property, ListChangeListener<T> listChangeListener) {
        if (property == null || listChangeListener == null){
            return;
        }
        if (!observableListChangeListenerMap.containsKey(property)) {
            property.addListener(weakObservableListChangeListener);
        }
        observableListChangeListenerMap.putIfAbsent(property, new HashSet<>());
        observableListChangeListenerMap.get(property).add(listChangeListener);
    }

    public final <T> void unregisterInvalidationListener(ObservableList<T> observableList, ListChangeListener<T> listChangeListener) {
        if (observableList == null || listChangeListener == null){
            return;
        }
        Set<ListChangeListener> listChangeListeners = observableListChangeListenerMap.get(observableList);
        if(listChangeListeners == null){
            return;
        }
        if(listChangeListeners.remove(listChangeListener) && listChangeListeners.isEmpty()){
            observableList.removeListener(weakObservableListChangeListener);
            observableListChangeListenerMap.remove(observableList);
        }
    }

    public final Set<ListChangeListener> unregisterListChangeListeners(ObservableList<?> property) {
        if (property == null){
            return null;
        }
        property.removeListener(weakObservableListChangeListener);
        return observableListChangeListenerMap.remove(property);
    }

    public final void unregisterAll(){
        for (ObservableValue<?> value : propertyChangeListenerMap.keySet()) {
            value.removeListener(weakPropertyChangeListener);
        }
        propertyChangeListenerMap.clear();

        for (Observable value : observableInvalidationListenerMap.keySet()) {
            value.removeListener(weakObservableInvalidationListener);
        }
        observableInvalidationListenerMap.clear();

        for (ObservableList<?> list : observableListChangeListenerMap.keySet()) {
            list.removeListener(weakObservableListChangeListener);
        }
        observableListChangeListenerMap.clear();
    }

}
