package drawingbot.javafx.util;

import javafx.beans.value.WritableValue;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class PropertyAccessorProp<C, T> extends PropertyAccessor<C, T> {

    public Class<T> type;
    public Function<C, ? extends WritableValue<T>> accessor;

    public PropertyAccessorProp(Class<C> clazz, String key, Class<T> type) {
        this(clazz, key, type, null);
    }

    public PropertyAccessorProp(Class<C> clazz, String key, Class<T> type, Function<C, ? extends WritableValue<T>> accessor) {
        super(clazz, type, key);
        this.type = type;
        this.accessor = accessor;
    }
    @Nullable
    @Override
    public T getData(C c) {
        return accessor.apply(c).getValue();
    }

    @Override
    public void setData(C c, @Nullable T t) {
        accessor.apply(c).setValue(t);
    }
}
