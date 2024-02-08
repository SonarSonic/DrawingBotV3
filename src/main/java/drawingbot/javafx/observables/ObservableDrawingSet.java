package drawingbot.javafx.observables;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.api.IProperties;
import drawingbot.drawing.ColorSeparationHandler;
import drawingbot.drawing.ColorSeparationSettings;
import drawingbot.drawing.IColorManagedDrawingSet;
import drawingbot.files.json.adapters.JsonAdapterObservableDrawingSet;
import drawingbot.javafx.util.JFXUtils;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.registry.Register;
import drawingbot.utils.EnumDistributionOrder;
import drawingbot.utils.EnumDistributionType;
import drawingbot.utils.SpecialListenable;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonAdapter(JsonAdapterObservableDrawingSet.class)
public class ObservableDrawingSet extends SpecialListenable<ObservableDrawingSet.Listener> implements IDrawingSet, IProperties, IColorManagedDrawingSet {

    public final SimpleStringProperty type = new SimpleStringProperty();
    public final SimpleStringProperty name = new SimpleStringProperty("");
    public final ObservableList<ObservableDrawingPen> pens = FXCollections.observableArrayList();
    public final SimpleObjectProperty<EnumDistributionOrder> distributionOrder = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<EnumDistributionType> distributionType = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<ColorSeparationHandler> colorHandler = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<ColorSeparationSettings> colorSettings = new SimpleObjectProperty<>();
    public final SimpleBooleanProperty useColorSplitOpacity = new SimpleBooleanProperty(false);

    private transient int[] currentRenderOrder;

    public transient boolean loadingDrawingSet = false;

    public ObservableDrawingSet(){
        this.distributionOrder.set(EnumDistributionOrder.DARKEST_FIRST);
        this.distributionType.set(EnumDistributionType.EVEN_WEIGHTED);
        this.colorHandler.set(Register.DEFAULT_COLOUR_SPLITTER);
        init();
    }

    public ObservableDrawingSet(IDrawingSet source){
        this.distributionOrder.set(EnumDistributionOrder.DARKEST_FIRST);
        this.distributionType.set(EnumDistributionType.EVEN_WEIGHTED);
        this.colorHandler.set(Register.DEFAULT_COLOUR_SPLITTER);
        loadDrawingSet(source);
        init();
    }

    private void init(){
        InvalidationListener genericListener = observable -> sendListenerEvent(listener -> listener.onDrawingSetPropertyChanged(this, observable));
        getPropertyList().forEach(prop -> prop.addListener(genericListener));

        //Color Handler Opacity - Handles initial setup
        useColorSplitOpacity.set(colorHandler.get() != null && colorHandler.get().useColorSplitterOpacity());


        colorHandler.addListener((observable, oldValue, newValue) -> {
            sendListenerEvent(listener -> listener.onColourSeparatorChanged(this, oldValue, newValue));
            //Color Handler Opacity - Handles updates
            useColorSplitOpacity.set(newValue != null && newValue.useColorSplitterOpacity());
        });

        //Color Handler Opacity - Handles adding of new pens
        pens.addListener((ListChangeListener<? super ObservableDrawingPen>) c -> {
            while(c.next()){
                for(ObservableDrawingPen added : c.getAddedSubList()){
                    added.useColorSplitOpacity.set(useColorSplitOpacity.get());
                }
            }
        });

        //Color Splitter Opacity - Handles changes to the value, and setting up the pens properly intitially
        JFXUtils.subscribeListener(useColorSplitOpacity, (observable, oldValue, newValue) -> {
            pens.forEach(pen -> pen.useColorSplitOpacity.set(newValue));
        });

        PropertyUtil.addSpecialListenerWithSubList(this, pens, Listener::onDrawingPenAdded, Listener::onDrawingPenRemoved);
    }

    public void loadDrawingSet(IDrawingSet source){
        loadingDrawingSet = true;

        this.pens.clear();
        this.type.set(source.getType());
        this.name.set(source.getName());
        for(IDrawingPen pen : source.getPens()){
            pens.add(new ObservableDrawingPen(pens.size(), pen));
        }

        if(source instanceof ObservableDrawingSet){
            ObservableDrawingSet drawingSet = (ObservableDrawingSet)source;
            this.distributionOrder.set(drawingSet.distributionOrder.get());
            this.distributionType.set(drawingSet.distributionType.get());
        }

        if(source instanceof IColorManagedDrawingSet){
            IColorManagedDrawingSet cmDrawingSet = (IColorManagedDrawingSet) source;
            ColorSeparationHandler handler = cmDrawingSet.getColorSeparationHandler();
            ColorSeparationSettings settings = cmDrawingSet.getColorSeparationSettings();

            //If the drawing set doesn't have a handler, just ignore it and keep the user selected one.
            if(handler != null){
                this.colorHandler.set(handler);
                this.colorSettings.set(settings != null ? settings.copy() : handler.getDefaultSettings());
            }
        }
        if(this.colorHandler.get() == null){
            this.colorHandler.set(Register.DEFAULT_COLOUR_SPLITTER);
            this.colorSettings.set(null);
        }else if(this.colorSettings.get() == null){
            this.colorSettings.set(this.colorHandler.get().getDefaultSettings());
        }


        this.currentRenderOrder = calculateRenderOrder();

        loadingDrawingSet = false;
    }

    public void mergePens(List<ObservableDrawingPen> pens){
        for(IDrawingPen pen : pens){
            if(!containsPen(pen)){
                pens.add(new ObservableDrawingPen(pens.size(), pen));
            }
        }
    }

    public ObservableDrawingPen addNewPen(IDrawingPen pen, boolean copy){
        if(!copy && pen instanceof ObservableDrawingPen){
            pens.add((ObservableDrawingPen) pen);
            return (ObservableDrawingPen) pen;
        }
        ObservableDrawingPen newPen = new ObservableDrawingPen(pens.size(), pen);
        pens.add(newPen);
        return newPen;
    }

    public List<ObservableDrawingPen> getRenderOrder(){
        return distributionOrder.get().getSortedPens(pens);
    }

    public List<ObservableDrawingPen> getRenderOrderEnabled(){
        return distributionOrder.get().getSortedPens(pens).stream().filter(ObservableDrawingPen::isEnabled).collect(Collectors.toList());
    }

    public int[] calculateRenderOrder(){
        List<ObservableDrawingPen> sortedList = getRenderOrder();
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
        return Register.INSTANCE.INVISIBLE_DRAWING_PEN;
    }

    public boolean containsPen(IDrawingPen pen){
        if(pen == null){
            return false;
        }
        return pens.stream().anyMatch(p -> p.getCodeName().equals(pen.getCodeName()));
    }

    public ObservableDrawingPen getFirstMatchingPen(IDrawingPen pen){
        if(pen == null){
            return null;
        }
        return pens.stream().filter(p -> p.getCodeName().equals(pen.getCodeName())).findFirst().orElse(null);
    }

    public List<ObservableDrawingPen> getMatchingPens(IDrawingPen pen){
        if(pen == null){
            return new ArrayList<>();
        }
        return pens.stream().filter(p -> p.getCodeName().equals(pen.getCodeName())).collect(Collectors.toList());
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
    public boolean isUserCreated() {
        return false;
    }

    @Override
    public String toString(){
        return getName();
    }

    ///////////////////////////

    private ObservableList<Observable> propertyList = null;

    @Override
    public ObservableList<Observable> getPropertyList() {
        if(propertyList == null){
            propertyList = PropertyUtil.createPropertiesList(type, name, pens, distributionOrder, distributionType, colorHandler, colorSettings);
        }
        return propertyList;
    }

    @Override
    public ColorSeparationHandler getColorSeparationHandler() {
        return colorHandler.get();
    }

    @Override
    public ColorSeparationSettings getColorSeparationSettings() {
        return colorSettings.get();
    }

    @Override
    public void setColorSeparation(ColorSeparationHandler handler, ColorSeparationSettings settings) {
        if(handler == null){
            this.colorHandler.set(Register.DEFAULT_COLOUR_SPLITTER);
            this.colorSettings.set(null);
        }else{
            this.colorHandler.set(handler);
            this.colorSettings.set(settings == null ? handler.getDefaultSettings() : settings);
        }
    }

    ///////////////////////////

    public interface Listener extends ObservableDrawingPen.Listener {

        default void onDrawingSetPropertyChanged(ObservableDrawingSet set, Observable property) {}

        default void onColourSeparatorChanged(ObservableDrawingSet set, ColorSeparationHandler oldValue, ColorSeparationHandler newValue) {}

        default void onDrawingPenAdded(ObservableDrawingPen pen) {}

        default void onDrawingPenRemoved(ObservableDrawingPen pen) {}

    }
}
