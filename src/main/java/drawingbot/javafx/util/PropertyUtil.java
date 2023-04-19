package drawingbot.javafx.util;

import drawingbot.api.IProperties;
import drawingbot.javafx.GenericSetting;
import drawingbot.utils.SpecialListenable;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PropertyUtil {

    public static <E> ObservableList<E> createPropertiesList(){
        return FXCollections.observableList(new ArrayList<>(), (Callback<E, Observable[]>) createExtractor());
    }

    public static <E extends Observable> ObservableList<E> createPropertiesList(E ...properties){
        return createPropertiesList(List.of(properties));
    }

    public static <E extends Observable> ObservableList<E> createPropertiesList(List<E> properties){
        return FXCollections.observableList(properties, createExtractor());
    }

    public static ObservableList<GenericSetting<?, ?>> createPropertiesListFromSettings(GenericSetting<?,?> ...settings) {
        return createPropertiesListFromSettings(List.of(settings));
    }

    public static ObservableList<GenericSetting<?, ?>> createPropertiesListFromSettings(List<GenericSetting<?,?>> settings) {
        return FXCollections.observableList(settings, createExtractor());
    }

    public static <E extends Observable> Callback<E, Observable[]> createExtractor(){
        return o -> {
            if(o instanceof ObjectProperty<?>){
                ObjectProperty<?> prop = (ObjectProperty<?>) o;
                if(prop.get() instanceof IProperties){
                    return ((IProperties) prop.get()).getPropertyList().toArray(new Observable[0]);
                }else if(prop.get() instanceof Observable){
                    return new Observable[]{(Observable) prop.get()};
                }
            }
         return new Observable[]{o};
        };
    }


    public static <E> void addSimpleListListener(ObservableValue<ObservableList<E>> prop, ListChangeListener<E> listener){
        if(prop.getValue() != null){
            prop.getValue().addListener(listener);
        }
        prop.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                oldValue.removeListener(listener);
            }
            if(newValue != null){
                newValue.addListener(listener);
            }
        });
    }

    public static <L, P extends SpecialListenable<L>> void addSpecialListener(ObservableValue<P> prop, L listener){
        if(prop.getValue() != null){
            prop.getValue().addSpecialListener(listener);
        }
        prop.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                oldValue.removeSpecialListener(listener);
            }
            if(newValue != null){
                newValue.addSpecialListener(listener);
            }
        });
    }

    /**
     * If a listeners overrides another listener type and that has a list of another property which is compatible, then it will add the special listeners to that object too.
     */
    public static <SUPER, E extends SpecialListenable<SUPER>, MASTER extends SUPER> void addSpecialListenerWithSubList(SpecialListenable<MASTER> host, ObservableValue<ObservableList<E>> prop, BiConsumer<MASTER, E> add, BiConsumer<MASTER, E> remove){
        ListChangeListener<E> listListener =  c -> {
            while(c.next()){
                for (E removed : c.getRemoved()) {
                    host.sendListenerEvent(l -> remove.accept(l, removed));
                    host.listeners().forEach(removed::removeSpecialListener);
                }

                for(E added : c.getAddedSubList()){
                    host.sendListenerEvent(l -> add.accept(l, added));
                    host.listeners().forEach(added::addSpecialListener);
                }
            }
        };

        if(prop.getValue() != null){
            prop.getValue().addListener(listListener);
            prop.getValue().forEach(filter -> host.listeners().forEach(filter::addSpecialListener));
        }

        prop.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                oldValue.removeListener(listListener);
                oldValue.forEach(filter -> host.listeners().forEach(filter::removeSpecialListener));
            }
            if(newValue != null){
                newValue.addListener(listListener);
                newValue.forEach(filter -> host.listeners().forEach(filter::addSpecialListener));
            }
        });

        host.listeners().addListener((ListChangeListener<SUPER>) c -> {
            while(c.next()) {
                for (E filter : prop.getValue()) {
                    c.getRemoved().forEach(filter::removeSpecialListener);
                    c.getAddedSubList().forEach(filter::addSpecialListener);
                }
            }
        });
    }

    /**
     * If a listeners overrides another listener type and that has a list of another property which is compatible, then it will add the special listeners to that object too.
     */
    public static <SUPER, E extends SpecialListenable<SUPER>, MASTER extends SUPER> void addSpecialListenerWithSubList(SpecialListenable<MASTER> host, ObservableList<E> prop, BiConsumer<MASTER, E> add, BiConsumer<MASTER, E> remove){
        prop.addListener((ListChangeListener<? super E>) c -> {
            while(c.next()){
                for (E removed : c.getRemoved()) {
                    host.sendListenerEvent(l -> remove.accept(l, removed));
                    host.listeners().forEach(removed::removeSpecialListener);
                }

                for(E added : c.getAddedSubList()){
                    host.sendListenerEvent(l -> add.accept(l, added));
                    host.listeners().forEach(added::addSpecialListener);
                }
            }
        });
        host.listeners().addListener((ListChangeListener<SUPER>) c -> {
            while(c.next()){
                for(E filter : prop){
                    c.getRemoved().forEach(filter::removeSpecialListener);
                    c.getAddedSubList().forEach(filter::addSpecialListener);
                }
            }
        });
        prop.forEach(filter -> host.listeners().forEach(filter::addSpecialListener));
    }
}