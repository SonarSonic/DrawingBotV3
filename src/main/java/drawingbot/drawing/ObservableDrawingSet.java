package drawingbot.drawing;

import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.utils.EnumDistributionOrder;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;

import java.util.List;

public class ObservableDrawingSet implements IDrawingSet<ObservableDrawingPen> {

    public final SimpleStringProperty type;
    public final SimpleStringProperty name;
    public final ObservableList<ObservableDrawingPen> pens;
    public final SimpleObjectProperty<EnumDistributionOrder> distributionOrder;
    public final SimpleObjectProperty<EnumBlendMode> blendMode;
    public int[] currentRenderOrder;

    public ObservableDrawingSet(IDrawingSet<?> source){
        this.type = new SimpleStringProperty();
        this.name = new SimpleStringProperty();
        this.pens = FXCollections.observableArrayList();
        this.distributionOrder = new SimpleObjectProperty<>(EnumDistributionOrder.DARKEST_FIRST);
        this.blendMode = new SimpleObjectProperty<>(EnumBlendMode.NORMAL);

        this.pens.addListener((ListChangeListener<ObservableDrawingPen>) c -> DrawingBotV3.INSTANCE.onDrawingSetChanged());
        this.distributionOrder.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingSetChanged());
        this.blendMode.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingSetChanged());
        loadDrawingSet(source);
    }

    public void loadDrawingSet(IDrawingSet<?> source){
        this.pens.clear();
        this.type.set(source.getType());
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
        sortedList.setComparator(distributionOrder.get().comparator);
        currentRenderOrder = new int[sortedList.size()];
        for(int i = 0; i < sortedList.size(); i++){
            currentRenderOrder[i] = sortedList.getSourceIndex(i);
        }
        return currentRenderOrder;
    }

    public ObservableDrawingPen getPen(int penNumber){
        for(ObservableDrawingPen pen : pens){
            if(pen.penNumber.get() == penNumber){
                return pen;
            }
        }
        return null;
    }

    public boolean containsPen(IDrawingPen pen){
        if(pen == null){
            return false;
        }
        return pens.stream().anyMatch(p -> p.getCodeName().equals(pen.getCodeName()));
    }

    @Override
    public String getType() {
        return type.get();
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
