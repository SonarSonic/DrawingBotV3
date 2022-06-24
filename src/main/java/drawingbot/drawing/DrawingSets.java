package drawingbot.drawing;

import drawingbot.api.IProperties;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.util.PropertyUtil;
import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DrawingSets implements IProperties {

    public SimpleObjectProperty<ObservableDrawingSet> activeDrawingSet = new SimpleObjectProperty<>();
    public SimpleObjectProperty<ObservableList<ObservableDrawingSet>> drawingSetSlots = new SimpleObjectProperty<>(FXCollections.observableArrayList());
    {
        drawingSetSlots.get().addListener((InvalidationListener) observable -> {
            if(!drawingSetSlots.get().contains(activeDrawingSet.get())){
                if(!drawingSetSlots.get().isEmpty()){
                    activeDrawingSet.set(drawingSetSlots.get().get(0));
                }
            }
        });
    }

    public final ObservableList<Property<?>> observables = PropertyUtil.createPropertiesList(activeDrawingSet, drawingSetSlots);

    public DrawingSets(){}

    public DrawingSets(List<ObservableDrawingSet> sets){
        if(sets.size() > 0){
            drawingSetSlots.get().addAll(sets);
            activeDrawingSet.set(sets.get(0));
        }
    }

    public int getActiveSetSlot(){
        return getDrawingSetSlot(activeDrawingSet.get());
    }

    public int getDrawingSetSlot(ObservableDrawingSet drawingSet){
        return drawingSetSlots.get().indexOf(drawingSet);
    }

    public ObservableDrawingSet getDrawingSetForSlot(int slot){
        if(drawingSetSlots.get().size() > slot){
            return drawingSetSlots.get().get(slot);
        }
        return activeDrawingSet.get();
    }

    @Nullable
    public ObservableDrawingSet getDrawingSetForName(String name){
        for(ObservableDrawingSet set : drawingSetSlots.get()){
            if(set.getName().equals(name)){
                return set;
            }
        }
        return null;
    }

    public DrawingSets copy(){
        DrawingSets copy = new DrawingSets();
        drawingSetSlots.get().forEach(set -> {
            ObservableDrawingSet setCopy = new ObservableDrawingSet(set);
            copy.drawingSetSlots.get().add(setCopy);
            if(set.equals(activeDrawingSet.get())){
                copy.activeDrawingSet.set(setCopy);
            }
        });
        return copy;
    }

    @Override
    public ObservableList<Property<?>> getProperties() {
        return observables;
    }
}
