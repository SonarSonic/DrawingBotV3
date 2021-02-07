package drawingbot.drawing;

import com.sun.javafx.collections.ObservableListWrapper;
import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.utils.EnumBlendMode;
import drawingbot.utils.EnumDistributionOrder;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;

import java.util.ArrayList;
import java.util.List;

public class ObservableDrawingSet implements IDrawingSet<ObservableDrawingPen> {

    public final SimpleStringProperty name;
    public final ObservableList<ObservableDrawingPen> pens;
    public final SimpleObjectProperty<EnumDistributionOrder> renderOrder;
    public final SimpleObjectProperty<EnumBlendMode> blendMode;
    public int[] currentRenderOrder;

    public ObservableDrawingSet(IDrawingSet<?> source){
        this.name = new SimpleStringProperty();
        this.pens = new ObservableListWrapper<>(new ArrayList<>());
        this.pens.addListener((ListChangeListener<ObservableDrawingPen>) c -> DrawingBotV3.INSTANCE.onDrawingSetChanged());
        this.renderOrder = new SimpleObjectProperty<>(EnumDistributionOrder.DARKEST_FIRST);
        this.blendMode = new SimpleObjectProperty<>(EnumBlendMode.NONE);
        this.renderOrder.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingSetChanged());
        this.blendMode.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingSetChanged());
        loadDrawingSet(source);
    }

    public void loadDrawingSet(IDrawingSet<?> source){
        this.pens.clear();
        this.name.set(source.getName());
        for(IDrawingPen pen : source.getPens()){
            pens.add(new ObservableDrawingPen(pens.size(), pen));
        }
    }

    public void addNewPen(IDrawingPen pen){
        pens.add(new ObservableDrawingPen(pens.size(), pen));
    }

    public int[] getCurrentRenderOrder(){
        SortedList<ObservableDrawingPen> sortedList = pens.sorted();
        sortedList.setComparator(renderOrder.get().comparator);
        currentRenderOrder = new int[sortedList.size()];
        for(int i = 0; i < sortedList.size(); i++){
            currentRenderOrder[i] = sortedList.getSourceIndex(i);
        }
        return currentRenderOrder;
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
