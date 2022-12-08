package drawingbot.javafx.util;

import drawingbot.api.IProperties;
import drawingbot.javafx.GenericSetting;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.function.BiConsumer;

public class PropertyUtil {

    public static ObservableList<Observable> createPropertiesList(Observable ...properties){
        return createPropertiesList(List.of(properties));
    }

    public static ObservableList<Observable> createPropertiesList(List<Observable> properties){
        return FXCollections.observableList(properties, E -> new Observable[]{E});
    }

    public static ObservableList<GenericSetting<?, ?>> createPropertiesListFromSettings(GenericSetting<?,?> ...settings) {
        return createPropertiesListFromSettings(List.of(settings));
    }

    public static ObservableList<GenericSetting<?, ?>> createPropertiesListFromSettings(List<GenericSetting<?,?>> settings) {
        return FXCollections.observableList(settings, E -> new Observable[]{E});
    }

    public static <P extends IProperties> PropertyListListener<P> addPropertyListListener(P listenable, BiConsumer<P, List<Observable>> onChange){
        PropertyListListener<P> listener = new PropertyListListener<>(listenable, onChange);
        listenable.getObservables().addListener(listener);
        return listener;
    }

    public static <P extends IProperties> void removePropertyListListener(P listenable, PropertyListListener<P> listener){
        listenable.getObservables().removeListener(listener);
    }

    public static <P extends IProperties> PropertyListListener<P> addPropertyListListener(ObjectProperty<P> listenable, BiConsumer<P, List<Observable>> onChange){
        PropertyListListener<P> listener = new PropertyListListener<>(listenable, onChange);
        listenable.addListener(listener);
        return listener;
    }

    public static <P extends IProperties> void removePropertyListListener(ObjectProperty<P> listenable, PropertyListListener<P> listener){
        listenable.removeListener(listener);
    }

}