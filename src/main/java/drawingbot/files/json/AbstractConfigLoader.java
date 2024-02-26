package drawingbot.files.json;

import drawingbot.javafx.GenericPreset;
import javafx.beans.property.SimpleObjectProperty;

import java.util.List;

public abstract class AbstractConfigLoader<CONFIG, DATA> extends AbstractPresetLoader<DATA> {

    private SimpleObjectProperty<GenericPreset<DATA>> configPreset = new SimpleObjectProperty<>();
    
    public AbstractConfigLoader(PresetType type, Class<DATA> dataType, String configFile) {
        super(dataType, type, configFile);
    }
    
    public abstract IPresetManager<CONFIG, DATA> getDefaultConfigManager();
    
    public abstract CONFIG getConfig();

    @Override
    protected void onJSONLoaded() {
        super.onJSONLoaded();

        if(configPreset.get() != null){
            return;
        }

        GenericPreset<DATA> preset = findPreset("config");
        if(preset == null){
            //If no preset is found then create a new default one
            preset = createNewPreset(presetType.registryName, "config", false);
            getDefaultConfigManager().updatePreset(null, getConfig(), preset);

            this.addPreset(preset);
            saveToJSON();
        }else{
            getDefaultConfigManager().applyPreset(null, getConfig(), preset, false);
        }
        configPreset.set(preset);
    }

    public void updateConfigs(){
        if(isLoading()){
            return;
        }
        getDefaultConfigManager().updatePreset(null, getConfig(), configPreset.get());
        markDirty();
    }

    public void applyConfigs(){
        getDefaultConfigManager().applyPreset(null, getConfig(), configPreset.get(), false);
    }


    /**
     * This won't wait for a background thread which could be cancelled, save the json on the main thread.
     */
    public void onShutdown(){
        getDefaultConfigManager().updatePreset(null, getConfig(), configPreset.get());
        saveToJSON();
    }

    @Override
    public List<GenericPreset<DATA>> getSaveablePresets() {
        return presets;
    }
}
