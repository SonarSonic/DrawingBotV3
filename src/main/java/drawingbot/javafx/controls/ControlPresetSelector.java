package drawingbot.javafx.controls;

import drawingbot.files.json.IPresetLoader;
import drawingbot.files.json.IPresetManager;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Skin;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * UI control for the selection of {@link GenericPreset} from a list of available ones, it also provides the "presets" menu button
 * Use the {@link #quickSetup(IPresetManager)} method to configure the preset selector easily
 * @param <TARGET> the type of the instance to apply the preset to
 * @param <DATA> the data type stored in the preset e.g. {@link drawingbot.files.json.PresetData}
 */
public class ControlPresetSelector<TARGET, DATA> extends ControlPresetButton<TARGET, DATA> {

    public ControlPresetSelector(){
        //Create a special listener for the attached preset manager to update the displayed categories / presets
        IPresetLoader.Listener<DATA> loaderListener = new IPresetLoader.Listener<>() {
            @Override
            public void onMarkDirty() {
                refresh();
            }
        };
        presetManagerProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                oldValue.getPresetLoader().removeSpecialListener(loaderListener);
            }
            if(newValue != null){
                newValue.getPresetLoader().addSpecialListener(loaderListener);
            }
        });
        filteredPresetsProperty().bind(Bindings.createObjectBinding(() -> getAvailablePresets() == null ? null : getPresetFilter() == null ? getAvailablePresets() : new FilteredList<>(getAvailablePresets(), getPresetFilter()), availablePresetsProperty(), presetFilterProperty()));
    }

    public void quickSetup(IPresetManager<TARGET, DATA> manager){
        setPresetManager(manager);
        setAvailablePresets(manager.getPresetLoader().getPresets());
        setActivePreset(manager.getPresetLoader().getDefaultPreset());
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<ObservableList<GenericPreset<DATA>>> availablePresets = new SimpleObjectProperty<>();

    public ObservableList<GenericPreset<DATA>> getAvailablePresets() {
        return availablePresets.get();
    }

    public ObjectProperty<ObservableList<GenericPreset<DATA>>> availablePresetsProperty() {
        return availablePresets;
    }

    public void setAvailablePresets(ObservableList<GenericPreset<DATA>> availablePresets) {
        this.availablePresets.set(availablePresets);
    }

    ////////////////////////////////////////////////////////

    protected ObjectProperty<ObservableList<GenericPreset<DATA>>> filteredPresets = new SimpleObjectProperty<>();

    public ObservableList<GenericPreset<DATA>> getFilteredPresets() {
        return filteredPresets.get();
    }

    public ObjectProperty<ObservableList<GenericPreset<DATA>>> filteredPresetsProperty() {
        return filteredPresets;
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<Predicate<GenericPreset<DATA>>> presetFilter = new SimpleObjectProperty<>(GenericPreset::isEnabled);

    public Predicate<GenericPreset<DATA>> getPresetFilter() {
        return presetFilter.get();
    }

    public ObjectProperty<Predicate<GenericPreset<DATA>>> presetFilterProperty() {
        return presetFilter;
    }

    public void setPresetFilter(Predicate<GenericPreset<DATA>> presetFilter) {
        this.presetFilter.set(presetFilter);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<Supplier<ComboBox<GenericPreset<DATA>>>> comboBoxFactory = new SimpleObjectProperty<>();

    public Supplier<ComboBox<GenericPreset<DATA>>> getComboBoxFactory() {
        return comboBoxFactory.get();
    }

    public ObjectProperty<Supplier<ComboBox<GenericPreset<DATA>>>> comboBoxFactoryProperty() {
        return comboBoxFactory;
    }

    public void setComboBoxFactory(Supplier<ComboBox<GenericPreset<DATA>>> comboBoxFactory) {
        this.comboBoxFactory.set(comboBoxFactory);
    }

    ////////////////////////////////////////////////////////

    public BooleanProperty disablePresetMenu = new SimpleBooleanProperty(false);

    public boolean disablePresetMenu() {
        return disablePresetMenu.get();
    }

    public BooleanProperty disablePresetMenuProperty() {
        return disablePresetMenu;
    }

    public void setDisablePresetMenu(boolean disablePresetMenu) {
        this.disablePresetMenu.set(disablePresetMenu);
    }

    ////////////////////////////////////////////////////////

    public void applyPreset(DBTaskContext context){
        if(getActivePreset() != null){
            getPresetManager().applyPreset(context, getTarget(), getActivePreset(), false);
        }
    }

    @Override
    public void refresh(){
        if(getSkin() instanceof SkinPresetSelector<?, ?> skin){
            skin.refresh();
        }
    }

    ////////////////////////////////////////////////////////

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SkinPresetSelector<>(this);
    }
}
