package drawingbot.javafx.controls;

import drawingbot.files.json.IPresetLoader;
import drawingbot.files.json.IPresetManager;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.GenericPreset;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * UI control providing the {@link javafx.scene.control.MenuButton} for the configuring of presets, here for re-usability, it's also used in the {@link ControlPresetSelector} and {@link ControlPresetSelectorCategory} controls
 * @param <TARGET> the type of the instance to apply the preset to
 * @param <DATA> the data type stored in the preset e.g. {@link drawingbot.files.json.PresetData}
 */
public class ControlPresetButton<TARGET, DATA> extends Control {

    public ControlPresetButton(){}

    ////////////////////////////////////////////////////////

    public ObjectProperty<IPresetManager<TARGET, DATA>> presetManager = new SimpleObjectProperty<>();

    public IPresetManager<TARGET, DATA> getPresetManager() {
        return presetManager.get();
    }

    public ObjectProperty<IPresetManager<TARGET, DATA>> presetManagerProperty() {
        return presetManager;
    }

    public void setPresetManager(IPresetManager<TARGET, DATA> presetManager) {
        this.presetManager.set(presetManager);
    }

    public IPresetLoader<DATA> getPresetLoader() {
        return getPresetManager().getPresetLoader();
    }

    public PresetType getPresetType() {
        return getPresetManager().getPresetType();
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<GenericPreset<DATA>> activePreset = new SimpleObjectProperty<>();

    public GenericPreset<DATA> getActivePreset() {
        return activePreset.get();
    }

    public ObjectProperty<GenericPreset<DATA>> activePresetProperty() {
        return activePreset;
    }

    public void setActivePreset(GenericPreset<DATA> activePreset) {
        this.activePreset.set(activePreset);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<TARGET> target = new SimpleObjectProperty<>();

    public TARGET getTarget() {
        return target.get();
    }

    public ObjectProperty<TARGET> targetProperty() {
        return target;
    }

    public void setTarget(TARGET target) {
        this.target.set(target);
    }

    ////////////////////////////////////////////////////////

    /**
     * Should be called when the associated {@link IPresetLoader} is modified, it ensures changes are reflected in the UI
     */
    public void refresh(){

    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SkinPresetsButton<>(this);
    }
}
