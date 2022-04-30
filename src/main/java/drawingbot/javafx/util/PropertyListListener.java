package drawingbot.javafx.util;

import drawingbot.api.IProperties;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PropertyListListener<P extends IProperties> implements ChangeListener<P>, ListChangeListener<Property<?>> {

    public P listenable;
    public BiConsumer<P, List<Property<?>>> onChange;
    public List<Property<?>> changed = new ArrayList<>();

    public PropertyListListener(P listenable, BiConsumer<P, List<Property<?>>> onChange) {
        this.listenable = listenable;
        this.onChange = onChange;
    }

    public PropertyListListener(ObjectProperty<P> listenableProperty, BiConsumer<P, List<Property<?>>> onChange) {
        this.onChange = onChange;
        this.changed(listenableProperty, null, listenableProperty.get());
    }

    @Override
    public void changed(ObservableValue<? extends P> observable, P oldValue, P newValue) {
        if (oldValue != null) {
            oldValue.getProperties().removeListener(this);
            listenable = null;
        }
        if (newValue != null) {
            newValue.getProperties().addListener(this);
            listenable = newValue;
        }
    }

    @Override
    public void onChanged(Change<? extends Property<?>> c) {
        changed.clear();
        while (c.next()) {
            if (c.wasUpdated()) {
                changed.addAll(c.getList().subList(c.getFrom(), c.getTo()));
            }
        }
        if (!changed.isEmpty()) {
            onChange.accept(listenable, changed);
        }
    }
}