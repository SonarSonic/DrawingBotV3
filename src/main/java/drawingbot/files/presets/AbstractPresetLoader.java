package drawingbot.files.presets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import drawingbot.javafx.GenericPreset;

public abstract class AbstractPresetLoader<O extends IJsonData> extends AbstractJsonLoader<O>{

    public final Class<O> dataType;

    public AbstractPresetLoader(Class<O> dataType, PresetType type, String configFile) {
        super(type, configFile);
        this.dataType = dataType;
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
