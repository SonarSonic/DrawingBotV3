package drawingbot.javafx.controls;

import drawingbot.api.IDrawingPen;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.registry.Register;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.skin.ComboBoxListViewSkin;

import java.util.function.Function;
import java.util.function.Supplier;

public class ControlPresetDrawingPen extends ControlPresetSelectorCategory<IDrawingPen, IDrawingPen> {

    public static Supplier<ComboBox<GenericPreset<IDrawingPen>>> defaultDrawingPenComboBoxFactory = () -> {
        ComboBox<GenericPreset<IDrawingPen>> comboBox =  new ComboBox<>();
        comboBox.setCellFactory(param -> new ComboCellPresetDrawingPen(null, false));
        comboBox.setButtonCell(new ComboCellPresetDrawingPen(null, false));
        comboBox.setVisibleRowCount(16);
        return comboBox;
    };

    public static Supplier<ComboBox<String>> defaultCategoryComboBoxFactory = () -> {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setMaxWidth(124);
        comboBox.setVisibleRowCount(16);
        return comboBox;
    };

    public static Function<Property<ObservableDrawingSet>, ComboBox<GenericPreset<IDrawingPen>>> selectableDrawingPenComboBoxFactory = (drawingSets) -> {
        ComboBox<GenericPreset<IDrawingPen>> comboBox =  new ComboBox<>();
        comboBox.setCellFactory(param -> new ComboCellPresetDrawingPen(drawingSets, true));
        comboBox.setButtonCell(new ComboCellPresetDrawingPen(drawingSets, false));
        comboBox.setVisibleRowCount(16);
        comboBox.setMaxWidth(180);

        ComboBoxListViewSkin<GenericPreset<IDrawingPen>> comboBoxDrawingPenSkin = new ComboBoxListViewSkin<>(comboBox);
        comboBoxDrawingPenSkin.hideOnClickProperty().set(false);
        comboBox.setSkin(comboBoxDrawingPenSkin);
        return comboBox;
    };

    public ControlPresetDrawingPen() {
        super();
        setPresetManager(Register.PRESET_MANAGER_DRAWING_PENS);
        setCategories(Register.PRESET_LOADER_DRAWING_PENS.getPresetSubTypes());
        setAvailablePresets(Register.PRESET_LOADER_DRAWING_PENS.getPresets());
        setComboBoxFactory(defaultDrawingPenComboBoxFactory);
        setCategoryComboBoxFactory(defaultCategoryComboBoxFactory);
        setActivePreset(Register.PRESET_LOADER_DRAWING_PENS.getDefaultPreset());
        comboBoxFactoryProperty().bind(Bindings.createObjectBinding(() -> {
            if(drawingSet.get() == null){
                return defaultDrawingPenComboBoxFactory;
            }
            return () -> selectableDrawingPenComboBoxFactory.apply(drawingSet);
        }, drawingSet));
    }

    @Override
    public GenericPreset<IDrawingPen> createNewPresetInstance() {
        return Register.PRESET_MANAGER_DRAWING_PENS.getPresetLoader().createNewPreset(getActiveCategory(), "Custom Pen", true);
    }

    public ObjectProperty<ObservableDrawingSet> drawingSet = new SimpleObjectProperty<>();

    public ObservableDrawingSet getDrawingSet() {
        return drawingSet.get();
    }

    public ObjectProperty<ObservableDrawingSet> drawingSetProperty() {
        return drawingSet;
    }

    public void setDrawingSet(ObservableDrawingSet drawingSet) {
        this.drawingSet.set(drawingSet);
    }
}
