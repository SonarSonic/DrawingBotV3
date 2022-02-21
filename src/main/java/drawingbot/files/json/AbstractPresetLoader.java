package drawingbot.files.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import drawingbot.files.json.adapters.JsonAdapterGenericPreset;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.MasterRegistry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public abstract class AbstractPresetLoader<O extends IJsonData> extends AbstractJsonLoader<O>{

    public final Class<O> dataType;
    public final ObservableList<GenericPreset<O>> presets = FXCollections.observableArrayList();
    public final HashMap<String, ObservableList<GenericPreset<O>>> presetsByType = new LinkedHashMap<>();

    public AbstractPresetLoader(Class<O> dataType, PresetType type, String configFile) {
        super(type, configFile);
        this.dataType = dataType;
    }

    @Override
    public void loadDefaults() {
        GenericPreset<O> defaultPreset = getDefaultPreset();
        if(defaultPreset != null){
            applyPreset(defaultPreset);
        }
    }

    @Override
    public void registerPreset(GenericPreset<O> preset) {
        presets.add(preset);
        if(preset.presetSubType != null && !preset.presetSubType.isEmpty()){
            presetsByType.putIfAbsent(preset.presetSubType, FXCollections.observableArrayList());
            presetsByType.get(preset.presetSubType).add(preset);
        }
    }

    @Override
    public void unregisterPreset(GenericPreset<O> preset) {
        presets.remove(preset);
        if(preset.presetSubType != null && !preset.presetSubType.isEmpty()) {
            presetsByType.get(preset.presetSubType).remove(preset);
        }
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        List<GenericPreset<?>> userCreated = new ArrayList<>();
        for (GenericPreset<O> preset : presets) {
            if (preset.userCreated) {
                userCreated.add(preset);
            }
        }
        return userCreated;
    }

    @Override
    public Collection<GenericPreset<O>> getAllPresets() {
        return presets;
    }

    @Override
    public List<String> getPresetSubTypes(){
        return new ArrayList<>(presetsByType.keySet());
    }

    @Override
    public List<GenericPreset<O>> getPresetsForSubType(String subType){
        return presetsByType.getOrDefault(subType, FXCollections.observableArrayList());
    }

    @Override
    public GenericPreset<O> getDefaultPresetForSubType(String subType){
        return MasterRegistry.INSTANCE.getDefaultPreset(this, subType, "Default");
    }

    /**
     * @return the data type for the preset loader
     */
    public Class<O> getType(){
        return dataType;
    }

    /**
     * @return the default preset combo box's will be set to, can be null
     */
    public abstract GenericPreset<O> getDefaultPreset();

    /**
     * @return a default method for creating a new preset
     */
    public GenericPreset<O> createNewPreset(){
        return createNewPreset("User", "New Preset", true);
    }

    /**
     * called by {@link JsonAdapterGenericPreset}
     * @return a json element to represent the presets data type
     */
    @Override
    public JsonElement toJsonElement(Gson gson, GenericPreset<?> preset){
        return gson.toJsonTree(preset.data, getType());
    }

    /**
     * called by {@link JsonAdapterGenericPreset}
     * @return the presets data, from the given json element
     */
    @Override
    public O fromJsonElement(Gson gson,  GenericPreset<?> preset, JsonElement element){
        return gson.fromJson(element, getType());
    }

}
