package drawingbot.files.json;

import drawingbot.files.json.projects.DBTaskContext;
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
    public abstract GenericPreset<O> updatePreset(DBTaskContext context, GenericPreset<O> preset);

    /**
     * applies the presets settings
     */
    public abstract void applyPreset(DBTaskContext context, GenericPreset<O> preset);


    public final GenericPreset<O> tryUpdatePreset(DBTaskContext context, GenericPreset<O> preset) {
        if (preset != null && preset.userCreated) {
            preset = updatePreset(context, preset);
            if (preset != null) {
                presetLoader.queueJsonUpdate();
                return preset;
            }
        }
        return null;
    }

    public final void tryApplyPreset(DBTaskContext context, GenericPreset<O> preset) {
        applyPreset(context, preset);
    }
}
