package drawingbot.drawing;

import com.sun.javafx.collections.ObservableListWrapper;
import drawingbot.DrawingBotV3;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class ObservableDrawingSet implements IDrawingSet<ObservableDrawingPen> {

    public final SimpleStringProperty name;
    public final ObservableList<ObservableDrawingPen> pens;

    public ObservableDrawingSet(IDrawingSet<?> source){
        this.name = new SimpleStringProperty();
        this.pens = new ObservableListWrapper<>(new ArrayList<>());
        this.pens.addListener((ListChangeListener<ObservableDrawingPen>) c -> onPropertiesChanged());
        loadDrawingSet(source);
    }

    public void loadDrawingSet(IDrawingSet<?> source){
        this.pens.clear();
        this.name.set(source.getName());
        for(IDrawingPen pen : source.getPens()){
            pens.add(new ObservableDrawingPen(pen));
        }
    }

    public void onPropertiesChanged(){
        DrawingBotV3.INSTANCE.reRender();
        System.out.println("CHANGED DRAWING SET!");
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public List<ObservableDrawingPen> getPens() {
        return pens;
    }

    @Override
    public String toString(){
        return getName();
    }
}
