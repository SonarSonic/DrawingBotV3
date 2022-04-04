package drawingbot.javafx.util;

import drawingbot.api.IProperties;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Attach this listener to a list of ObservableValues, to get change updates for all of them.
 */
public class ElementChangeListener implements ListChangeListener<ObservableValue> {

    public final ChangeListener valueListener;

    public ElementChangeListener(ObservableList<Property<?>> list, ChangeListener valueListener) {
        this.valueListener = valueListener;
        list.addListener(this);
        list.forEach(e -> e.addListener(valueListener));
    }

    @Override
    public void onChanged(Change<? extends ObservableValue> c) {
        while (c.next()) {
            c.getRemoved().forEach(e -> e.removeListener(valueListener));
            c.getAddedSubList().forEach(e -> e.addListener(valueListener));
        }
    }

    public static ElementChangeListener addElementChangeListener(IProperties listenable, ChangeListener<?> valueListener){
        return new ElementChangeListener(listenable.getProperties(), valueListener);
    }
}
