package drawingbot.javafx.util;

import drawingbot.api.IProperties;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.function.BiConsumer;

public class PropertyUtil {

    public static ObservableList<Property<?>> createPropertiesList(Property<?> ...properties){
        return createPropertiesList(List.of(properties));
    }

    public static ObservableList<Property<?>> createPropertiesList(List<Property<?>> properties){
        return FXCollections.observableList(properties, E -> new Observable[]{E});
    }

    public static <P extends IProperties> PropertyListListener<P> addPropertyListListener(P listenable, BiConsumer<P, List<Property<?>>> onChange){
        PropertyListListener<P> listener = new PropertyListListener<>(listenable, onChange);
        listenable.getProperties().addListener(listener);
        return listener;
    }

    public static <P extends IProperties> PropertyListListener<P> addPropertyListListener(ObjectProperty<P> listenable, BiConsumer<P, List<Property<?>>> onChange){
        PropertyListListener<P> listener = new PropertyListListener<>(listenable, onChange);
        listenable.addListener(listener);
        return listener;
    }

}
