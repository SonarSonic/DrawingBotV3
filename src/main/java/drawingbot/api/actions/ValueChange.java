package drawingbot.api.actions;

public class ValueChange<T> {

    private final T oldValue;
    private final T newValue;

    public ValueChange(T oldValue, T newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public T getOldValue() {
        return oldValue;
    }

    public T getNewValue() {
        return newValue;
    }

    public ValueChange<T> invert(){
        return new ValueChange<>(newValue, oldValue);
    }
}