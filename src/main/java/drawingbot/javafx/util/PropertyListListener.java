package drawingbot.javafx.util;

import drawingbot.api.IProperties;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PropertyListListener<P extends IProperties> implements ChangeListener<P>, ListChangeListener<Observable> {

    public P listenable;
    public BiConsumer<P, List<Observable>> onChange;
    public List<Observable> changed = new ArrayList<>();

    public PropertyListListener(P listenable, BiConsumer<P, List<Observable>> onChange) {
        this.listenable = listenable;
        this.onChange = onChange;
    }

    public PropertyListListener(ObjectProperty<P> listenableProperty, BiConsumer<P, List<Observable>> onChange) {
        this.onChange = onChange;
        this.changed(listenableProperty, null, listenableProperty.get());
    }

    @Override
    public void changed(ObservableValue<? extends P> observable, P oldValue, P newValue) {
        if (oldValue != null) {
            oldValue.getObservables().removeListener(this);
            listenable = null;
        }
        if (newValue != null) {
            newValue.getObservables().addListener(this);
            listenable = newValue;
        }
    }

    @Override
    public void onChanged(Change<? extends Observable> c) {
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