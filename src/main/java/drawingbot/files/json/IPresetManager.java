package drawingbot.files.json;

import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.editors.TreeNode;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    /**
     * Controls the additional of extra elements to the "Edit Preset" pop-up
     * @param preset the preset which is being displayed
     * @param builder list of nodes for elements to be displayed in the "Edit Preset" pop-up
     * @param callbacks list of callbacks to be run after the preset has been edited
     */
    void addEditDialogElements(GenericPreset<DATA> preset, ObservableList<TreeNode> builder, List<Consumer<GenericPreset<DATA>>> callbacks);


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
