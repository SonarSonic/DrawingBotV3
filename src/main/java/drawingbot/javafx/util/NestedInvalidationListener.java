package drawingbot.javafx.util;

import drawingbot.api.IProperties;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * This class was created as a brute force approach to listening to properties which contained multiple nested properties
 * However, it's better for debugging stuff and isn't a good method to use in practice, as it generate many unnecessary listeners
 */
public class NestedInvalidationListener implements InvalidationListener {

    public FilteredInvalidationListener internalListener;
    public ChangeListener<Observable> genericChangeListener = (observable, oldValue, newValue) -> {
        if (oldValue != null) {
            removeNestedListener(newValue);
        }
        if (newValue != null) {
            addNestedListener(newValue);
        }
    };

    public ListChangeListener<Observable> genericListChangeListener = (change) -> {
        while (change.next()) {
            for (Observable removed : change.getRemoved()) {
                removeNestedListener(removed);
            }
            for (Observable added : change.getAddedSubList()) {
                addNestedListener(added);
            }
        }
    };

    public ListChangeListener<Object> genericUnknownListChangeListener = (change) -> {
        while (change.next()) {
            for (Object removed : change.getRemoved()) {
                if (removed instanceof Observable) {
                    removeNestedListener((Observable) removed);
                }
            }
            for (Object added : change.getAddedSubList()) {
                if (added instanceof Observable) {
                    addNestedListener((Observable) added);
                }
            }
        }
    };

    public NestedInvalidationListener(FilteredInvalidationListener internalListener) {
        this.internalListener = internalListener;
    }

    public void addListenerToValue(ObservableValue<? extends Observable> observableValue) {
        observableValue.addListener(genericChangeListener);
        if (observableValue.getValue() != null) {
            addNestedListener(observableValue.getValue());
        }
    }

    public void removeListenerFromValue(ObservableValue<? extends Observable> observableValue) {
        observableValue.removeListener(genericChangeListener);
        if (observableValue.getValue() != null) {
            removeNestedListener(observableValue.getValue());
        }
    }

    public void addListenerToUnknownList(ObservableList<?> observableList) {
        observableList.addListener(genericUnknownListChangeListener);
        for (Object obj : observableList) {
            if (obj instanceof Observable) {
                addNestedListener((Observable) obj);
            }
        }
    }

    public void removeListenerFromUnknownList(ObservableList<?> observableList) {
        observableList.removeListener(genericUnknownListChangeListener);
        for (Object obj : observableList) {
            if (obj instanceof Observable) {
                removeNestedListener((Observable) obj);
            }
        }
    }

    public void addListenerToList(ObservableList<? extends Observable> observableList) {
        observableList.addListener(genericListChangeListener);
        for (Observable observable : observableList) {
            addNestedListener(observable);
        }
    }

    public void removeListenerFromList(ObservableList<? extends Observable> observableList) {
        observableList.removeListener(genericListChangeListener);
        for (Observable observable : observableList) {
            removeNestedListener(observable);
        }
    }

    public void addNestedListener(Observable observable) {
        if (observable instanceof IProperties) {
            IProperties properties = (IProperties) observable;

            if (internalListener.addPropertyRoot()) {
                observable.addListener(this);
            }
            if (internalListener.addPropertyDefaults()) {
                addListenerToList(properties.getPropertyList());
            }
            /*
            List<? extends ObservableValue<? extends Observable>> nested = properties.getNestedProperties();
            if(nested != null){
                for(ObservableValue<? extends Observable> observableValue : nested){
                    addListenerToValue(observableValue);
                }
            }

            List<? extends ObservableList<? extends Observable>> nestedLists = properties.getNestedLists();
            if(nestedLists != null){
                for(ObservableList<? extends Observable> observableValue : nestedLists){
                    addListenerToList(observableValue);
                }
            }
             */
        } else {
            if (observable instanceof ObservableList) {
                addListenerToUnknownList((ObservableList<?>) observable);
            } else {
                // IProperties provides controls over which elements should be listened too so we ignore it's root
                observable.addListener(this);
            }
        }

    }

    public void removeNestedListener(Observable observable) {
        if (observable instanceof IProperties) {
            IProperties properties = (IProperties) observable;

            if (internalListener.addPropertyRoot()) {
                observable.removeListener(this);
            }

            if (internalListener.addPropertyDefaults()) {
                removeListenerFromList(properties.getPropertyList());
            }

            /*
            List<? extends ObservableValue<? extends Observable>> nested = properties.getNestedProperties();
            if(nested != null){
                for(ObservableValue<? extends Observable> observableValue : nested){
                    removeListenerFromValue(observableValue);
                }
            }

            List<? extends ObservableList<? extends Observable>> nestedLists = properties.getNestedLists();
            if(nestedLists != null){
                for(ObservableList<? extends Observable> observableValue : nestedLists){
                    removeListenerFromList(observableValue);
                }
            }
             */
        } else {
            if (observable instanceof ObservableList) {
                removeListenerFromUnknownList((ObservableList<?>) observable);
            } else {
                // IProperties provides controls over which elements should be listened too so we ignore it's root
                observable.removeListener(this);
            }
        }
    }

    @Override
    public void invalidated(Observable observable) {
        internalListener.invalidated(observable);
    }

    public static NestedInvalidationListener addNestedPropertyListenerDirect(IProperties property, FilteredInvalidationListener listener) {
        NestedInvalidationListener nestedListener = new NestedInvalidationListener(listener);
        nestedListener.addNestedListener(property);
        return nestedListener;
    }

    public static NestedInvalidationListener addPropertyListenerToValue(ObservableValue<? extends Observable> property, FilteredInvalidationListener listener) {
        NestedInvalidationListener nestedListener = new NestedInvalidationListener(listener);
        nestedListener.addListenerToValue(property);
        return nestedListener;
    }

    public static NestedInvalidationListener addNestedPropertyListenerToList(ObservableList<? extends Observable> property, FilteredInvalidationListener listener) {
        NestedInvalidationListener nestedListener = new NestedInvalidationListener(listener);
        nestedListener.addListenerToList(property);
        return nestedListener;
    }

    public static void removePropertyListenerDirect(IProperties property, NestedInvalidationListener nestedListener) {
        nestedListener.removeNestedListener(property);
    }

    public static void removePropertyFromValue(ObservableValue<Observable> property, NestedInvalidationListener nestedListener) {
        nestedListener.removeListenerFromValue(property);
    }

    public static void removePropertyFromList(ObservableList<? extends Observable> property, NestedInvalidationListener nestedListener) {
        nestedListener.removeListenerFromList(property);
    }

    public interface FilteredInvalidationListener extends InvalidationListener {

        void invalidated(Object source, String name, Observable observable);

        @Override
        default void invalidated(Observable observable) {
            if (observable instanceof ReadOnlyProperty) {
                ReadOnlyProperty<?> prop = (ReadOnlyProperty<?>) observable;
                invalidated(prop.getBean(), prop.getName(), observable);
            } else {
                invalidated(null, null, observable);
            }
        }

        default boolean addPropertyRoot() {
            return false;
        }

        default boolean addPropertyDefaults() {
            return true;
        }

    }
}
