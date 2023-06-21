package drawingbot.javafx.controls;

import drawingbot.javafx.observables.ObservableDrawingPen;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;

import java.util.HashMap;
import java.util.Map;

/**
 * Class which displays a "live" colour palette for a specific observable pen list
 */
public class ControlPenPalette extends Control {

    public HBox hBox;
    public Map<ObservableDrawingPen, ControlPenSwatch> swatchMap;

    public ListChangeListener<ObservableDrawingPen> LIST_CHANGE_LISTENER = this::onPenListChanged;

    public ControlPenPalette(ObservableList<ObservableDrawingPen> penList, ObservableDoubleValue width){
        this(penList);
        paletteWidth.bind(width);
    }

    public ControlPenPalette(ObservableList<ObservableDrawingPen> penList){
        init();
        penListProperty().set(penList);
    }

    public void init(){
        hBox = new HBox();
        swatchMap = new HashMap<>();

        minWidthProperty().bind(paletteWidth);
        minHeightProperty().bind(paletteHeight);
        prefWidthProperty().bind(hBox.prefWidthProperty());
        prefHeightProperty().bind(hBox.prefHeightProperty());

        penList.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                oldValue.removeListener(LIST_CHANGE_LISTENER);
                oldValue.forEach(this::onPenRemoved);
                listLength.set(0);
            }
            if(newValue != null){
                newValue.addListener(LIST_CHANGE_LISTENER);
                newValue.forEach(this::onPenAdded);
                listLength.set(newValue.size());
            }
        });
        paletteHeight.set(12);
        swatchWidth.bind(Bindings.createDoubleBinding(() -> {
            return paletteWidth.get() / listLength.get();
        }, listLength, paletteWidth));

        getChildren().add(hBox);
    }

    public void onPenListChanged(ListChangeListener.Change<? extends ObservableDrawingPen> change){
        while (change.next()){
            if(change.wasAdded()){
                change.getAddedSubList().forEach(this::onPenAdded);
            }
            if(change.wasRemoved()){
                change.getRemoved().forEach(this::onPenRemoved);
            }
        }
        refreshSwatches();
        listLength.set(change.getList().size());
    }

    public void onPenAdded(ObservableDrawingPen pen){
        ControlPenSwatch swatch = new ControlPenSwatch(pen, 12, 12);

        swatch.widthProperty().bind(swatchWidth);
        swatch.heightProperty().bind(paletteHeight);

        swatchMap.put(pen, swatch);
        hBox.getChildren().add(swatch);
    }

    public void onPenRemoved(ObservableDrawingPen pen){
        ControlPenSwatch swatch = swatchMap.remove(pen);
        if(swatch != null){
            swatch.fillProperty().unbind();
            swatch.widthProperty().unbind();
            swatch.heightProperty().unbind();
            hBox.getChildren().remove(swatch);
        }
    }

    public void refreshSwatches(){
        hBox.getChildren().clear();
        for(ObservableDrawingPen pen : penList.get()){
            hBox.getChildren().add(swatchMap.get(pen));
        }
    }

    public final SimpleIntegerProperty listLength = new SimpleIntegerProperty(0);

    public int getListLength() {
        return listLength.get();
    }

    public SimpleIntegerProperty listLengthProperty() {
        return listLength;
    }

    public void setListLength(int listLength) {
        this.listLength.set(listLength);
    }

    ///////////

    public final SimpleDoubleProperty paletteWidth = new SimpleDoubleProperty(60);

    public double getPaletteWidth() {
        return paletteWidth.get();
    }

    public SimpleDoubleProperty paletteWidthProperty() {
        return paletteWidth;
    }

    public void setPaletteWidth(double paletteWidth) {
        this.paletteWidth.set(paletteWidth);
    }

    ///////////

    public final SimpleDoubleProperty paletteHeight = new SimpleDoubleProperty();

    public double getPaletteHeight() {
        return paletteHeight.get();
    }

    public SimpleDoubleProperty paletteHeightProperty() {
        return paletteHeight;
    }

    public void setPaletteHeight(double paletteHeight) {
        this.paletteHeight.set(paletteHeight);
    }

    ///////////

    public final SimpleDoubleProperty swatchWidth = new SimpleDoubleProperty();

    public double getSwatchWidth() {
        return swatchWidth.get();
    }

    public SimpleDoubleProperty swatchWidthProperty() {
        return swatchWidth;
    }

    public void setSwatchWidth(double swatchWidth) {
        this.swatchWidth.set(swatchWidth);
    }

    ///////////

    public final SimpleObjectProperty<ObservableList<ObservableDrawingPen>> penList = new SimpleObjectProperty<>();

    public ObservableList<ObservableDrawingPen> getPenList() {
        return penList.get();
    }

    public SimpleObjectProperty<ObservableList<ObservableDrawingPen>> penListProperty() {
        return penList;
    }

    public void setPenList(ObservableList<ObservableDrawingPen> penList) {
        this.penList.set(penList);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SkinBase<>(this){};
    }
}
