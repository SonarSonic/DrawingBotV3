package drawingbot.javafx.observables;

import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.utils.EnumDistributionOrder;
import drawingbot.utils.EnumDistributionType;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;

import java.util.List;

public class ObservableDrawingSet implements IDrawingSet<ObservableDrawingPen> {

    public final IDrawingSet<?> source;
    public final SimpleStringProperty type = new SimpleStringProperty();
    public final SimpleStringProperty name = new SimpleStringProperty();
    public final ObservableList<ObservableDrawingPen> pens = FXCollections.observableArrayList();
    public final SimpleObjectProperty<EnumDistributionOrder> distributionOrder = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<EnumDistributionType> distributionType = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<EnumBlendMode> blendMode = new SimpleObjectProperty<>(EnumBlendMode.NORMAL);
    public int[] currentRenderOrder;

    public ObservableDrawingSet(IDrawingSet<?> source){
        this.source = source;
        this.distributionOrder.set(EnumDistributionOrder.DARKEST_FIRST);
        this.distributionType.set(EnumDistributionType.EVEN_WEIGHTED);
        this.blendMode.set(EnumBlendMode.NORMAL);

        initListeners();
        loadDrawingSet(source);
    }

    public void initListeners(){
        this.pens.addListener((ListChangeListener<ObservableDrawingPen>) c -> DrawingBotV3.INSTANCE.onDrawingSetChanged());
        this.distributionOrder.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingSetChanged());
        this.distributionType.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingSetChanged());
        this.blendMode.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingSetChanged());
    }

    public void loadDrawingSet(IDrawingSet<?> source){

        if(source instanceof ObservableDrawingSet){
            ObservableDrawingSet drawingSet = (ObservableDrawingSet)source;
            this.distributionOrder.set(drawingSet.distributionOrder.get());
            this.distributionType.set(drawingSet.distributionType.get());
            this.blendMode.set(drawingSet.blendMode.get());
        }

        this.pens.clear();
        this.type.set(source.getType());
        this.name.set(source.getName());
        for(IDrawingPen pen : source.getPens()){
            pens.add(new ObservableDrawingPen(pens.size(), pen));
        }
        this.currentRenderOrder = calculateRenderOrder();
    }

    public void addNewPen(IDrawingPen pen){
        pens.add(new ObservableDrawingPen(pens.size(), pen));
    }

    public int[] calculateRenderOrder(){
        SortedList<ObservableDrawingPen> sortedList = pens.sorted();
        sortedList.setComparator(distributionOrder.get().comparator);
        currentRenderOrder = new int[sortedList.size()];
        for(int i = 0; i < sortedList.size(); i++){
            currentRenderOrder[i] = sortedList.getSourceIndex(i);
        }
        return currentRenderOrder;
    }

    public int getIndexOfPen(int penNumber){
        int index = 0;
        for(ObservableDrawingPen pen : pens){
            if(pen.penNumber.get() == penNumber){
                return index;
            }
            index++;
        }
        return -1;
    }

    public ObservableDrawingPen getPen(int penNumber){
        for(ObservableDrawingPen pen : pens){
            if(pen.penNumber.get() == penNumber){
                return pen;
            }
        }
        return DrawingBotV3.INSTANCE.invisibleDrawingPen;
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
