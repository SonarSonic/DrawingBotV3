package drawingbot.javafx.controls;

import drawingbot.api.IDrawingPen;
import drawingbot.drawing.DrawingSets;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.Register;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.skin.ComboBoxListViewSkin;

import java.util.function.Function;
import java.util.function.Supplier;

public class ControlPresetDrawingPen extends ControlPresetSelectionCategory<IDrawingPen, IDrawingPen> {

    public static Supplier<ComboBox<GenericPreset<IDrawingPen>>> defaultDrawingPenComboBoxFactory = () -> {
        ComboBox<GenericPreset<IDrawingPen>> comboBox =  new ComboBox<>();
        comboBox.setCellFactory(param -> new ComboCellPresetDrawingPen(null, false));
        comboBox.setButtonCell(new ComboCellPresetDrawingPen(null, false));
        return comboBox;
    };

    public static Supplier<ComboBox<String>> defaultCategoryComboBoxFactory = () -> {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setMaxWidth(124);
        return comboBox;
    };

    public static Function<Property<DrawingSets>, ComboBox<GenericPreset<IDrawingPen>>> selectableDrawingPenComboBoxFactory = (drawingSets) -> {
        ComboBox<GenericPreset<IDrawingPen>> comboBox =  new ComboBox<>();
        comboBox.setCellFactory(param -> new ComboCellPresetDrawingPen(drawingSets, true));
        comboBox.setButtonCell(new ComboCellPresetDrawingPen(drawingSets, false));

        ComboBoxListViewSkin<GenericPreset<IDrawingPen>> comboBoxDrawingPenSkin = new ComboBoxListViewSkin<>(comboBox);
        comboBoxDrawingPenSkin.hideOnClickProperty().set(false);
        comboBox.setSkin(comboBoxDrawingPenSkin);
        comboBox.setMaxWidth(180);
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
            if(drawingSets.get() == null){
                return defaultDrawingPenComboBoxFactory;
            }
            return () -> selectableDrawingPenComboBoxFactory.apply(drawingSets);
        }, drawingSets));
    }

    public ObjectProperty<DrawingSets> drawingSets = new SimpleObjectProperty<>();

    public DrawingSets getDrawingSets() {
        return drawingSets.get();
    }

    public ObjectProperty<DrawingSets> drawingSetsProperty() {
        return drawingSets;
    }

    public void setDrawingSets(DrawingSets drawingSets) {
        this.drawingSets.set(drawingSets);
    }
}
