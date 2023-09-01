package drawingbot.files.json.adapters;

import com.google.gson.*;
import drawingbot.files.json.AbstractJsonLoader;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.MasterRegistry;

import java.lang.reflect.Type;

public class JsonAdapterGenericPreset implements JsonSerializer<GenericPreset<?>>, JsonDeserializer<GenericPreset<?>> {

    public static final String VERSION = "version";
    public static final String PRESET_TYPE = "presetType";
    public static final String PRESET_SUB_TYPE = "presetSubType";
    public static final String PRESET_NAME = "presetName";
    public static final String USER_CREATED = "userCreated";
    public static final String JSON_DATA = "data";

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
        preset.userCreated = jsonObject.get(USER_CREATED).getAsBoolean();

        Gson gson = JsonLoaderManager.createDefaultGson();
        AbstractJsonLoader<Object> manager = JsonLoaderManager.getJsonLoaderForPresetType(preset);
        if(manager != null){
            preset.data = manager.fromJsonElement(gson, preset, jsonObject.get(JSON_DATA));
            preset.presetLoader = manager;
            return preset;
        }
        return null;
    }

    @Override
    public JsonElement serialize(GenericPreset<?> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(PRESET_TYPE, src.presetType.id);
        jsonObject.addProperty(VERSION, src.version);
        jsonObject.addProperty(PRESET_SUB_TYPE, src.getPresetSubType());
        jsonObject.addProperty(PRESET_NAME, src.getPresetName());
        jsonObject.addProperty(USER_CREATED, src.userCreated);

        Gson gson = JsonLoaderManager.createDefaultGson();
        AbstractJsonLoader<?> manager = JsonLoaderManager.getJsonLoaderForPresetType(src);
        if(manager != null){
            jsonObject.add(JSON_DATA, manager.toJsonElement(gson, src));
            return jsonObject;
        }
        return null;
    }
}
