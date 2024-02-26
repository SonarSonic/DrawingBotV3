package drawingbot.files.json;

import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;

/**
 * A preset manager handles applying preset settings to a given target or creating a preset from the target.
 * They are independent of the {@link IPresetLoader} to allow for different implementations of applying the preset
 *
 * @param <TARGET> See {@link #getTargetType()}
 * @param <DATA> See {@link #getDataType()} ()}
 *
 */
public interface IPresetManager<TARGET, DATA> {

    /**
     * @return the {@link IPresetLoader} which is responsible for loading this preset manager preset/data type
     */
    IPresetLoader<DATA> getPresetLoader();

    /**
     * @return the type of target this preset can be applied to
     */
    Class<TARGET> getTargetType();

    /**
     * @return see {@link IPresetLoader#getDataType()}
     */
    default Class<DATA> getDataType(){
        return getPresetLoader().getDataType();
    }

    /**
     * @return see {@link IPresetLoader#getPresetType()}
     */
    default PresetType getPresetType(){
        return getPresetLoader().getPresetType();
    }

    /**
     * Typically presets can be applied to a given {@link DBTaskContext}, if the {@link IPresetManager} doesn't support this, null should be returned instead
     * @param context the context to extract the target from
     * @return the target which matches the {@link #getTargetType()}
     */
    TARGET getTargetFromContext(DBTaskContext context);

    /**
     * Updates the given {@link GenericPreset} to match the settings from in the current target
     * @param context the context for the update task
     * @param target the current target
     * @param preset the preset to update
     */
    void updatePreset(DBTaskContext context, TARGET target, GenericPreset<DATA> preset);

    /**
     * applies the presets settings
     */
    void applyPreset(DBTaskContext context, TARGET target, GenericPreset<DATA> preset, boolean changesOnly);


    IPresetEditor<TARGET, DATA> createPresetEditor();



    default GenericPreset<DATA> cast(GenericPreset<?> preset){
        return (GenericPreset<DATA>) preset;
    }

    /////////////////////////////////////////

    /**
     * Convenience method, to create a new preset from the given target
     */
    default GenericPreset<DATA> createPresetFromTarget(DBTaskContext context, TARGET target){
        GenericPreset<DATA> preset = getPresetLoader().createNewPreset();
        updatePreset(context, target, preset);
        return preset;
    }
}
