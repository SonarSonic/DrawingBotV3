package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.IPresetLoader;
import drawingbot.files.json.IPresetManager;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.List;

/**
 * UI control providing the {@link javafx.scene.MenuButton} for the configuring of presets, here for re-usability, it's also used in the {@link ControlPresetSelector} and {@link ControlPresetSelectorCategory} controls
 * @param <TARGET> the type of the instance to apply the preset to
 * @param <DATA> the data type stored in the preset e.g. {@link drawingbot.files.json.PresetData}
 */
public class ControlPresetButton<TARGET, DATA> extends Control {

    public ControlPresetButton(){}

    ////////////////////////////////////////////////////////

    /**
     * @return The preset to use as the starting point when hitting the new preset button, can be null
     */
    public GenericPreset<DATA> createNewPresetInstance(){
        return null;
    }

    public void actionNewPreset(){
        GenericPreset<DATA> result = FXHelper.actionNewPreset(getPresetManager(), createNewPresetInstance(), getTarget(), false);
        if(result != null){
            refresh();
            setActivePreset(result);
            setActivePreset(result); //FORCE: if new sub types are created FIXME?
        }
    }

    public void actionUpdatePreset(){
        GenericPreset<DATA>  result = FXHelper.actionUpdatePreset(getPresetManager(), getActivePreset(), getTarget());
        if(result != null){
            refresh();
            setActivePreset(result);
            setActivePreset(result); //FORCE: if new sub types are created FIXME?
        }
    }

    public void actionEditPreset(){
        GenericPreset<DATA>  result = FXHelper.actionEditPreset(getPresetManager(), getActivePreset(), getTarget(), false);
        if(result != null){
            refresh();
            setActivePreset(result);
            setActivePreset(result); //FORCE: if new sub types are created FIXME?
        }
    }
    
    public void actionDeletePreset(){
        GenericPreset<DATA>  preset = getActivePreset();
        if(preset == null){
            return;
        }

        String subType = preset.getPresetSubType();
        int oldIndex = getPresetType().getSubTypeBehaviour().isIgnored() ? getPresetLoader().getPresets().indexOf(preset) : getPresetLoader().getPresetsForSubType(subType).indexOf(preset);

        GenericPreset<DATA>  result = FXHelper.actionDeletePreset(getPresetLoader(), getActivePreset());
        if(result == null){
            refresh();

            //1. Try to set the preset to the next one down in the list in the sub type / preset list
            int nextIndex = Math.max(0, oldIndex-1);
            List<GenericPreset<DATA>> targetList = getPresetType().getSubTypeBehaviour().isIgnored() ? getPresetLoader().getPresets() : getPresetLoader().getPresetsForSubType(subType);
            if(!targetList.isEmpty() && nextIndex < targetList.size()){
                GenericPreset<DATA> nextPreset = targetList.get(nextIndex);
                if(nextPreset != null){
                    setActivePreset(nextPreset);
                    return;
                }
            }
            //2. If we failed to find it use the default for the sub type
            GenericPreset<DATA> defaultPreset = getPresetLoader().getDefaultPresetForSubType(subType);
            if(defaultPreset != null){
                setActivePreset(defaultPreset);
                return;
            }
            //3. If we still found nothing, switch to the generic default preset
            setActivePreset(getPresetLoader().getDefaultPreset());
        }
    }
    
    public void actionImportPreset(){
        FXHelper.importPreset(DrawingBotV3.context(), getPresetType(), false, true);
    }
    
    public void actionExportPreset(){
        GenericPreset<DATA> current = getActivePreset();
        if(current != null){
            FXHelper.exportPreset(DrawingBotV3.context(), current, DrawingBotV3.project().getExportDirectory(), current.getPresetName(), true);
        }
    }
    
    public void actionSetAsDefault(){
        GenericPreset<DATA> current = getActivePreset();
        if(current != null){
            FXHelper.actionSetDefaultPreset(getPresetLoader(), current);
            refresh();
        }
    }
    
    public void actionShowPresetManager(){
        GenericPreset<DATA> current = getActivePreset();
        if(current != null){
            DrawingBotV3.INSTANCE.controller.presetManagerController.type.set(current.getPresetType());
            if(!current.getPresetType().getSubTypeBehaviour().isIgnored()){
                DrawingBotV3.INSTANCE.controller.presetManagerController.category.set(current.getPresetSubType());
            }
            DrawingBotV3.INSTANCE.controller.presetManagerController.selectPreset(current);
        }
        DrawingBotV3.INSTANCE.controller.presetManagerStage.show();
    }

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
