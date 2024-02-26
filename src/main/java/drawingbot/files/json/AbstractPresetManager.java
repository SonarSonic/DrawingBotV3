package drawingbot.files.json;

import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;

public abstract class AbstractPresetManager<TARGET, DATA> implements IPresetManager<TARGET, DATA> {

    public final IPresetLoader<DATA> presetLoader;
    public final Class<TARGET> targetType;

    public AbstractPresetManager(IPresetLoader<DATA> presetLoader, Class<TARGET> targetType){
        this.presetLoader = presetLoader;
        this.targetType = targetType;
    }

    @Override
    public IPresetLoader<DATA> getPresetLoader() {
        return presetLoader;
    }

    @Override
    public Class<TARGET> getTargetType() {
        return targetType;
    }

    @Override
    public PresetType getPresetType(){
        return presetLoader.getPresetType();
    }

    /**
     * updates the presets settings with the ones currently configured
     */
    public abstract void updatePreset(DBTaskContext context, TARGET target, GenericPreset<DATA> preset);

    /**
     * applies the presets settings
     */
    public abstract void applyPreset(DBTaskContext context, TARGET target, GenericPreset<DATA> preset, boolean changesOnly);

    @Override
    public IPresetEditor<TARGET, DATA> createPresetEditor() {
        return new DefaultPresetEditor<>(this);
    }

}
