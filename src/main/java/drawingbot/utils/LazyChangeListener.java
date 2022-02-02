package drawingbot.utils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public abstract class LazyChangeListener<T> implements ChangeListener<T> {

    public abstract void onChange(ObservableValue<? extends T> observable, T oldValue, T newValue);

    @Override
    public final void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
        onChange(observable, oldValue, newValue);
        observable.removeListener(this);
    }
}
