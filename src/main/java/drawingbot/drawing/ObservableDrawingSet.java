package drawingbot.drawing;

import com.sun.javafx.collections.ObservableListWrapper;
import drawingbot.DrawingBotV3;
import drawingbot.helpers.ImageTools;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import processing.core.PConstants;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//TODO SETUP UPDATES On "CHANGED DRAWING SET" - DO PEN DISTIBUTION
public class ObservableDrawingSet implements IDrawingSet<ObservableDrawingPen> {

    public final SimpleStringProperty name;
    public final ObservableList<ObservableDrawingPen> pens;
    public int[] renderOrder;

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

    public int[] getRenderOrder(){ //TODO ALLOW CHANGING OF ORDER, OTHER THAN BRIGHTEST TO DARKEST
        SortedList<ObservableDrawingPen> sortedList = pens.sorted();
        sortedList.setComparator(Comparator.comparingInt(pen -> -ImageTools.getBrightness(pen.getRGBColour())));
        renderOrder = new int[sortedList.size()];
        for(int i = 0; i < sortedList.size(); i++){
            renderOrder[i] = sortedList.getSourceIndex(i);
        }
        return renderOrder;
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
