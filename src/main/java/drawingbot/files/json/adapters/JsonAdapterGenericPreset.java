package drawingbot.files.json.adapters;

import com.google.gson.*;
import drawingbot.files.json.GsonHelper;
import drawingbot.files.json.IPresetLoader;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.MasterRegistry;

import java.lang.reflect.Type;

public class JsonAdapterGenericPreset implements JsonSerializer<GenericPreset<?>>, JsonDeserializer<GenericPreset<?>> {

    public static final String VERSION = "version";
    public static final String PRESET_TYPE = "presetType";
    public static final String PRESET_SUB_TYPE = "presetSubType";
    public static final String PRESET_NAME = "presetName";
    public static final String PRESET_ENABLED = "enabled";
    public static final String USER_CREATED = "userCreated";
    public static final String JSON_DATA = "data";

    @Override
    public JsonElement serialize(GenericPreset<?> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(PRESET_TYPE, src.presetType.registryName);
        jsonObject.addProperty(VERSION, src.version);
        jsonObject.addProperty(PRESET_SUB_TYPE, src.getPresetSubType());
        jsonObject.addProperty(PRESET_NAME, src.getPresetName());
        jsonObject.addProperty(PRESET_ENABLED, src.isEnabled());
        jsonObject.addProperty(USER_CREATED, src.userCreated); //Legacy: kept for compatibility with older versions but now all presets loaded from internal .json will be considered system, and all others user.

        Gson gson = JsonLoaderManager.createDefaultGson();
        IPresetLoader<?> manager = JsonLoaderManager.getJsonLoaderForPresetType(src);
        if(manager != null){
            jsonObject.add(JSON_DATA, manager.toJsonElement(gson, src));
            return jsonObject;
        }
        return null;
    }

    @Override
    public GenericPreset<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        if(!jsonObject.has(VERSION)){
            //pre-release presets will fail here
            return null;
        }
        GenericPreset<Object> preset = new GenericPreset<>();
        preset.presetType = MasterRegistry.INSTANCE.getPresetType(jsonObject.get(PRESET_TYPE).getAsString());
        preset.version = jsonObject.get(VERSION).getAsString();
        preset.setPresetSubType(jsonObject.get(PRESET_SUB_TYPE).getAsString());
        preset.setPresetName(jsonObject.get(PRESET_NAME).getAsString());
        preset.setEnabled(GsonHelper.getBooleanOrDefault(jsonObject, PRESET_ENABLED, true));
        preset.userCreated = jsonObject.get(USER_CREATED).getAsBoolean(); //Legacy: this should be overwritten after loading, depending on the source of the .json

        Gson gson = JsonLoaderManager.createDefaultGson();
        IPresetLoader<Object> manager = JsonLoaderManager.getJsonLoaderForPresetType(preset);
        if(manager != null){
            preset.data = manager.fromJsonElement(gson, preset, jsonObject.get(JSON_DATA));
            preset.presetLoader = manager;
            return preset;
        }
        return null;
    }
}
