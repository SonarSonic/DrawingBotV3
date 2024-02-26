package drawingbot.javafx.editors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

public class EditableProperty<V> implements IEditableProperty<V> {

    private final Property<V> property;
    private final Class<V> type;
    private final BooleanProperty disabled = new SimpleBooleanProperty();
    private final String name;
    private final V initialValue;
    private final IEditorFactory<V> factory;

    public EditableProperty(Class<V> type, String name, Property<V> property, IEditorFactory<V> factory){
        this.name = name;
        this.property = property;
        this.type = type;
        this.initialValue = property.getValue();
        this.factory = factory;
    }

    @Override
    public Class<V> getType() {
        return type;
    }

    @Override
    public Property<V> valueProperty() {
        return property;
    }

    @Override
    public V getDefaultValue() {
        if(defaultSupplier != null){
            return defaultSupplier.get();
        }
        return initialValue;
    }

    @Override
    public String getValueAsString() {
        return String.valueOf(property.getValue());
    }

    ////////////////////////////////

    private Supplier<V> defaultSupplier;

    public EditableProperty<V> setDefaultSupplier(Supplier<V> defaultSupplier){
        this.defaultSupplier = defaultSupplier;
        return this;
    }

    @Override
    public void resetValue() {
        if(defaultSupplier != null){
            valueProperty().setValue(defaultSupplier.get());
        }
    }

    ////////////////////////////////

    private Function<ThreadLocalRandom, V> randomiser; //optional: the randomiser returns a valid random value

    public EditableProperty<V> setRandomiser(Function<ThreadLocalRandom, V> randomiser){
        this.randomiser = randomiser;
        return this;
    }

    @Override
    public void randomiseValue() {
        if(randomiser != null) {
            randomiser.apply(ThreadLocalRandom.current());
        }
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public BooleanProperty disabledProperty() {
        return disabled;
    }

    @Override
    public IEditorFactory<V> getEditorFactory() {
        return factory;
    }
}
