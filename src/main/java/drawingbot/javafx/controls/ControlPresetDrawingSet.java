package drawingbot.javafx.controls;

import drawingbot.api.IDrawingSet;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.Register;
import javafx.scene.control.ComboBox;

import java.util.function.Supplier;

public class ControlPresetDrawingSet extends ControlPresetSelectorCategory<IDrawingSet, IDrawingSet> {

    public static Supplier<ComboBox<GenericPreset<IDrawingSet>>> defaultDrawingSetComboBoxFactory = () -> {
        ComboBox<GenericPreset<IDrawingSet>> comboBox =  new ComboBox<>();
        comboBox.setCellFactory(param -> new ComboCellPresetDrawingSet());
        comboBox.setButtonCell(new ComboCellPresetDrawingSet());
        comboBox.setMaxWidth(222);
        return comboBox;
    };

    public static Supplier<ComboBox<String>> defaultCategoryComboBoxFactory = () -> {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setMaxWidth(80);
        return comboBox;
    };

    public ControlPresetDrawingSet() {
        super();
        setPresetManager(Register.PRESET_MANAGER_DRAWING_SET);
        setCategories(Register.PRESET_LOADER_DRAWING_SET.getPresetSubTypes());
        setAvailablePresets(Register.PRESET_LOADER_DRAWING_SET.getPresets());

        setComboBoxFactory(defaultDrawingSetComboBoxFactory);
        setCategoryComboBoxFactory(defaultCategoryComboBoxFactory);
        setActivePreset(Register.PRESET_LOADER_DRAWING_SET.getDefaultPreset());
    }
}
