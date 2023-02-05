package drawingbot.drawing;

import drawingbot.api.Hooks;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.api.IProperties;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.SpecialListenable;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DrawingSets extends SpecialListenable<DrawingSets.Listener> implements IProperties {

    ///////////////////////////////////////////////

    public SimpleObjectProperty<ObservableDrawingSet> activeDrawingSet = new SimpleObjectProperty<>(null);

    public ObservableDrawingSet getActiveDrawingSet() {
        return activeDrawingSet.get();
    }

    public SimpleObjectProperty<ObservableDrawingSet> activeDrawingSetProperty() {
        return activeDrawingSet;
    }

    public void setActiveDrawingSet(ObservableDrawingSet activeDrawingSet) {
        this.activeDrawingSet.set(activeDrawingSet);
    }

    ///////////////////////////////////////////////

    public final ObservableList<ObservableDrawingSet> drawingSetSlots = FXCollections.observableArrayList();

    public ObservableList<ObservableDrawingSet> getDrawingSetSlots() {
        return drawingSetSlots;
    }

    ///////////////////////////////////////////////

    public DrawingSets(){
        init();
    }

    public DrawingSets(List<ObservableDrawingSet> sets){
        if(sets.size() > 0){
            drawingSetSlots.addAll(sets);
            activeDrawingSet.set(sets.get(0));
        }
        init();
    }

    public void init(){
        PropertyUtil.addSpecialListenerWithSubList(this, drawingSetSlots, Listener::onDrawingSetAdded, Listener::onDrawingSetRemoved);
        activeDrawingSet.addListener((observable, oldValue, newValue) -> sendListenerEvent(l -> l.onActiveSlotChanged(newValue)));
        drawingSetSlots.addListener((InvalidationListener) observable -> {
            if(activeDrawingSet.get() == null || !drawingSetSlots.contains(activeDrawingSet.get())){
                if(!drawingSetSlots.isEmpty()){
                    activeDrawingSet.set(drawingSetSlots.get(0));
                }
            }
        });
    }

    public int getActiveSetSlot(){
        return getDrawingSetSlot(activeDrawingSet.get());
    }

    public int getDrawingSetSlot(ObservableDrawingSet drawingSet){
        return drawingSetSlots.indexOf(drawingSet);
    }

    public ObservableDrawingSet getDrawingSetForSlot(int slot){
        if(drawingSetSlots.size() > slot){
            return drawingSetSlots.get(slot);
        }
        return activeDrawingSet.get();
    }

    @Nullable
    public ObservableDrawingSet getDrawingSetForName(String name){
        for(ObservableDrawingSet set : drawingSetSlots){
            if(set.getName().equals(name)){
                return set;
            }
        }
        return null;
    }

    public void changeDrawingSet(IDrawingSet<IDrawingPen> set){
        if(set != null){
            activeDrawingSet.get().loadDrawingSet(set);
            Hooks.runHook(Hooks.CHANGE_DRAWING_SET, set, this);
        }
    }

    public DrawingSets copy(){
        DrawingSets copy = new DrawingSets();
        drawingSetSlots.forEach(set -> {
            ObservableDrawingSet setCopy = new ObservableDrawingSet(set);
            copy.drawingSetSlots.add(setCopy);
            if(set.equals(activeDrawingSet.get())){
                copy.activeDrawingSet.set(setCopy);
            }
        });
        return copy;
    }


    ///////////////////////////////////////////////

    private ObservableList<Observable> propertyList = null;

    @Override
    public ObservableList<Observable> getPropertyList() {
        if(propertyList == null){
            propertyList = PropertyUtil.createPropertiesList(activeDrawingSet, drawingSetSlots);
        }
        return propertyList;
    }

    ///////////////////////////////////////////////

    public interface Listener extends ObservableDrawingSet.Listener {

        default void onActiveSlotChanged(ObservableDrawingSet activeSet){}

        default void onDrawingSetAdded(ObservableDrawingSet set){}

        default void onDrawingSetRemoved(ObservableDrawingSet set){}

    }

}
