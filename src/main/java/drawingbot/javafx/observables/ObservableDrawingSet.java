package drawingbot.javafx.observables;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.drawing.ColourSeperationHandler;
import drawingbot.files.presets.JsonAdapterObservableDrawingSet;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.javafx.FXController;
import drawingbot.registry.Register;
import drawingbot.utils.EnumDistributionOrder;
import drawingbot.utils.EnumDistributionType;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;

import java.util.ArrayList;
import java.util.List;

@JsonAdapter(JsonAdapterObservableDrawingSet.class)
public class ObservableDrawingSet implements IDrawingSet<ObservableDrawingPen> {

    public final SimpleStringProperty type = new SimpleStringProperty();
    public final SimpleStringProperty name = new SimpleStringProperty();
    public final ObservableList<ObservableDrawingPen> pens = FXCollections.observableArrayList();
    public final SimpleObjectProperty<EnumDistributionOrder> distributionOrder = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<EnumDistributionType> distributionType = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<ColourSeperationHandler> colourSeperator = new SimpleObjectProperty<>();

    private transient int[] currentRenderOrder;

    public transient boolean loadingDrawingSet = false;

    public ObservableDrawingSet(){}

    public ObservableDrawingSet(IDrawingSet<?> source){
        this.distributionOrder.set(EnumDistributionOrder.DARKEST_FIRST);
        this.distributionType.set(EnumDistributionType.getRecommendedType());
        this.colourSeperator.set(Register.DEFAULT_COLOUR_SPLITTER);

        loadDrawingSet(source);
    }

    public void loadDrawingSet(IDrawingSet<?> source){
        loadingDrawingSet = true;

        if(source instanceof ObservableDrawingSet){
            ObservableDrawingSet drawingSet = (ObservableDrawingSet)source;
            this.distributionOrder.set(drawingSet.distributionOrder.get());
            this.distributionType.set(drawingSet.distributionType.get());
            this.colourSeperator.set(drawingSet.colourSeperator.get());
        }

        this.pens.clear();
        this.type.set(source.getType());
        this.name.set(source.getName());
        for(IDrawingPen pen : source.getPens()){
            pens.add(new ObservableDrawingPen(pens.size(), pen));
        }
        this.currentRenderOrder = calculateRenderOrder();

        loadingDrawingSet = false;
        Platform.runLater(() -> DrawingBotV3.INSTANCE.onDrawingSetChanged());
    }

    public void addNewPen(IDrawingPen pen){
        pens.add(new ObservableDrawingPen(pens.size(), pen));
    }

    public int[] calculateRenderOrder(){
        List<ObservableDrawingPen> sortedList = new ArrayList<>(pens);
        sortedList.sort(distributionOrder.get().comparator);
        currentRenderOrder = new int[sortedList.size()];
        for(int i = 0; i < sortedList.size(); i++){
            currentRenderOrder[i] = sortedList.get(i).penNumber.get();
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

    public int getDrawingSetSlot(){
        return DrawingBotV3.INSTANCE.drawingSetSlots.indexOf(this);
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


    public static ObservableDrawingSet getDrawingSetForSlot(int slot){
        if(DrawingBotV3.INSTANCE.drawingSetSlots.size() > slot){
            return DrawingBotV3.INSTANCE.drawingSetSlots.get(slot);
        }
        return DrawingBotV3.INSTANCE.activeDrawingSet.get();
    }
}
