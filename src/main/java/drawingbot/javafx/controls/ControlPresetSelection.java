package drawingbot.javafx.controls;

import drawingbot.files.json.IPresetLoader;
import drawingbot.files.json.IPresetManager;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Skin;

import java.util.function.Supplier;

public class ControlPresetSelection<TARGET, DATA> extends ControlPresetButton<TARGET, DATA> {

    public ControlPresetSelection(){
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
        if(getSkin() instanceof SkinPresetSelection<?, ?> skin){
            skin.refresh();
        }
    }

    ////////////////////////////////////////////////////////

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SkinPresetSelection<>(this);
    }
}
