package drawingbot.files.json;

import drawingbot.javafx.GenericPreset;

public abstract class AbstractPresetManager<O extends IJsonData> {

    public AbstractJsonLoader<O> presetLoader;

    public AbstractPresetManager(AbstractJsonLoader<O> presetLoader){
        this.presetLoader = presetLoader;
    }

    public PresetType getPresetType(){
        return presetLoader.type;
    }

    /**
     * updates the presets settings with the ones currently configured
     * @return the preset or null if the settings couldn't be saved
     */
    public abstract GenericPreset<O> updatePreset(GenericPreset<O> preset);

    /**
     * applies the presets settings
     */
    public abstract void applyPreset(GenericPreset<O> preset);


    public final GenericPreset<O> tryUpdatePreset(GenericPreset<O> preset) {
        if (preset != null && preset.userCreated) {
            preset = updatePreset(preset);
            if (preset != null) {
                presetLoader.queueJsonUpdate();
                return preset;
            }
        }
        return null;
    }

    public final void tryApplyPreset(GenericPreset<O> preset) {
        applyPreset(preset);
    }
}
