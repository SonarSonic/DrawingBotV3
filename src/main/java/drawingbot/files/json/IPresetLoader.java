package drawingbot.files.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.utils.ISpecialListenable;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * A {@link IPresetLoader} is responsible or loading/saving presets and keeping track of the preset which have been loaded
 * @param <DATA> see {@link #getDataType()}
 */
public interface IPresetLoader<DATA> extends ISpecialListenable<IPresetLoader.Listener<DATA>> {

    /**
     * @return the version of this preset loader and the presets it loads and generates
     */
    String getVersion();

    /**
     * @return the type of the data held in the preset typically {@link PresetData}, this data must be able to be serialized/deserialized in GSON
     */
    Class<DATA> getDataType();

    /**
     * @return the registered preset type this {@link IPresetLoader} can load/save, important to make sure when a given preset is loaded the correct {@link IPresetLoader} is used
     */
    PresetType getPresetType();

    /**
     * Registers the preset, called when a preset is created or loaded from a json or when a preset is created in the software
     */
    void addPreset(GenericPreset<DATA> preset);

    /**
     * Unregisters the preset, called when a preset is deleted, typically when a preset is deleted by the user
     */
    void removePreset(GenericPreset<DATA> preset);

    /**
     * Allows the user to reorganise the order of presets, it supports re-organising system and user presets combined
     * @param preset the preset to move
     * @param displayedList the presets currently visible to the user, the reorder should be faithful to this orderering even if not complete
     * @param shift negative or positive displacement in the displayedList
     * @return true if the preset was moved, false if the preset couldn't be moved
     */
    boolean reorderPreset(GenericPreset<?> preset, List<GenericPreset<?>> displayedList, int shift);

    /**
     * Should be called after edit operations to confirm them, by passing the original preset and the edited version
     * @param oldPreset the original preset which is being edited, must be unchanged during the edit process
     * @param editPreset a copy of the oldPreset which has been altered with the intended edits
     * @return the resulting preset which is still registered, this will return the editPreset if the sub type is changed otherwise it will be the oldPreset
     */
    GenericPreset<DATA> editPreset(GenericPreset<DATA> oldPreset, GenericPreset<DATA> editPreset);

    /**
     * @return a JavaFX {@link ObservableList} of all loaded presets, which is safe for use in UI Components
     */
    ObservableList<GenericPreset<DATA>> getPresets();

    /**
     * @return a JavaFX {@link ObservableList} of all presets which were created by the user, which is safe for use in UI Components, and for saving the presets to GSON
     */
    ObservableList<GenericPreset<DATA>> getUserCreatedPresets();

    /**
     * @return a list of preset which should be saved into the json, typically this is just the {@link #getUserCreatedPresets()}
     */
    default List<GenericPreset<DATA>> getSaveablePresets(){
        return getUserCreatedPresets();
    }

    /**
     * @return a JavaFX {@link ObservableList} of all loaded presets sub types, which is safe for use in UI Components
     */
    ObservableList<String> getPresetSubTypes();

    /**
     * @param subType the preset sub type to retrieve the list for
     * @return a JAVAFX list of all the loaded presets for the given sub type, which is safe for use in UI Components
     */
    ObservableList<GenericPreset<DATA>> getPresetsForSubType(String subType);


    /**
     * INTERNAL USE: Marks the presets as changed. Called by the {@link #addPreset(GenericPreset)}, {@link #removePreset(GenericPreset)} and {@link #editPreset(GenericPreset, GenericPreset)} methods.
     * Avoid calling from outside of the preset manager to prevent excessive json updates     *
     */
    void markDirty();

    /**
     * @return true if the presets have unsaved changes
     */
    boolean isDirty();

    /**
     * @return a property for monitoring if presets have unsaved changes
     */
    BooleanProperty dirtyProperty();

    ////////////////////////////

    /**
     * @return true if the preset loader is still being initialized, during this type json updates will be disabled and listener events won't send
     */
    boolean isLoading();

    /**
     * @return a property to monitor when the preset loader is loaded
     */
    BooleanProperty loadingProperty();

    /**
     * INTERNAL USE: Used during application initializaton to mark when all presets have been loaded, once this point has reached json updates and listener events are activated
     * @param isLoading mark if the loader is loaded
     */
    void setLoading(boolean isLoading);

    ////////////////////////////

    /**
     * @param preset the preset to create the instance for
     * @return an default instance of the DATA which can be stored in this preset
     */
    DATA createDataInstance(GenericPreset<DATA> preset);

    /**
     * @return a new {@link GenericPreset} which can be then be updated
     */
    GenericPreset<DATA> createNewPreset();

    /**
     * @param presetSubType the presets sub type
     * @param presetName the presets name
     * @param userCreated true, if the preset should be saved with other user presets
     * @return  a new {@link GenericPreset} with the given parameters
     */
    GenericPreset<DATA> createNewPreset(String presetSubType, String presetName, boolean userCreated);

    /**
     * Used to create a 'safe' preset to perform editing on, which can be disposed of later or used to confirm edits with the {@link #editPreset(GenericPreset, GenericPreset)} method
     * @param preset the preset you wish to edit
     * @return an editable version of the preset
     */
    GenericPreset<DATA> createEditablePreset(GenericPreset<DATA> preset);

    /**
     * Used to create a 'overriding' preset, which will take the place of an existing System Preset
     * @param systemPreset the system preset which will be overridden
     * @return an editable preset which can override the system one
     */
    GenericPreset<DATA> createOverridePreset(GenericPreset<DATA> systemPreset);


    ////////////////////////////
    /**
     * @return the current 'global' default preset, e.g. if a control provides the means to select the preset, this preset should be selected by default
     */
    GenericPreset<DATA> getDefaultPreset();

    /**
     * @param subType the preset sub type to retrieve the default for
     * @return the current default preset for the given sub type e.g. when switching between sub types this preset should be selected first
     */
    GenericPreset<DATA> getDefaultPresetForSubType(String subType);

    /**
     * @param preset the preset to use as the new 'global' default preset
     */
    void setDefaultPreset(GenericPreset<DATA> preset);

    /**
     * @param preset the preset to use as the new 'sub type' default preset
     */
    void setDefaultPresetSubType(GenericPreset<DATA> preset);

    /**
     * Reset the 'global' default preset to the one configured by the software, typically the first in the list
     */
    void resetDefaultPreset();

    /**
     * Reset the 'sub type' default preset to the one configured by the software, typically the first in the list
     */
    void resetDefaultPresetSubType(String subType);

    ////////////////////////////

    /**
     * INTERNAL USED: Loads any defaults associated with this {@link IPresetLoader} into the given project
     * NON FINAL API - TODO REMOVE ME ?
     * @param project
     */
    void loadDefaults(DBTaskContext project);

    void restoreDefaultOrder(@Nullable List<GenericPreset<?>> displayedList);

    void sortPresets(Comparator<GenericPreset<?>> comparator, @Nullable List<GenericPreset<?>> displayedList);

    ////////////////////////////

    void loadFromJSON();

    void updateJSON();

    void saveToJSON();

    DATA fromJsonElement(Gson gson, GenericPreset<?> preset, JsonElement jsonElement);

    JsonElement toJsonElement(Gson gson, GenericPreset<?> src);

    default DATA duplicateData(Gson gson, GenericPreset<DATA> preset){
        return fromJsonElement(gson, preset, toJsonElement(gson, preset));
    }

    ////////////////////////////

    default boolean canLoadPreset(GenericPreset<?> preset){
        return preset != null && preset.presetType == getPresetType();
    }

    default boolean isSystemPreset(GenericPreset<DATA> preset){
        return preset != null && !preset.userCreated;
    }

    default boolean isUserPreset(GenericPreset<DATA> preset){
        return preset != null && preset.userCreated;
    }

    ////////////////////////////

    /**
     * Convenience method to cast the preset into the preset type / data type assocaited with this {@link IPresetLoader}
     * It used it's assumed the necessary checks have been already performed to safely cast this preset
     */
    @SuppressWarnings("unchecked")
    default GenericPreset<DATA> cast(GenericPreset<?> preset){
        return (GenericPreset<DATA>) preset;
    }

    /**
     * Convenience method to return a preset with a given name
     * @param presetName the preset name to search for
     * @return the found preset with the exact name or null
     */
    default GenericPreset<DATA> findPreset(String presetName){
        for(GenericPreset<DATA> preset : getPresets()){
            if(preset.getPresetName().equals(presetName)){
                return preset;
            }
        }
        return null;
    }

    /**
     * Convenience method to return a preset with a given presetID
     * @param presetID the preset id to search for
     * @return the found preset with the exact preset id or null
     */
    default GenericPreset<DATA> findPresetFromID(String presetID){
        String[] parts = presetID.split(":");
        if(parts.length != 2){
            return null;
        }
        return findPreset(parts[0], parts[1]);
    }

    /**
     * Convenience method to return a preset with a given name
     * @param subType the preset sub type to search for
     * @param presetName the preset name to search for
     * @return the found preset with the exact name or null
     */
    default GenericPreset<DATA> findPreset(String subType, String presetName){
        for(GenericPreset<DATA> preset : getPresets()){
            if(preset.getPresetSubType().equals(subType) && preset.getPresetName().equals(presetName)){
                return preset;
            }
        }
        return null;
    }

    interface Listener<DATA>{

        default void onPresetAdded(GenericPreset<DATA> preset){}

        default void onPresetRemoved(GenericPreset<DATA> preset){}

        default void onPresetEdited(GenericPreset<DATA> preset){}

        default void onMarkDirty(){}
    }
}
