package drawingbot.files.json.adapters;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import drawingbot.files.json.PresetContainerJsonFile;
import drawingbot.javafx.GenericPreset;

import java.lang.reflect.Type;
import java.util.List;

public class JsonAdapterGenericPresetContainer  implements JsonSerializer<PresetContainerJsonFile>, JsonDeserializer<PresetContainerJsonFile> {

    public static final Type presetsType = TypeToken.getParameterized(List.class, GenericPreset.class).getType();

    @Override
    public JsonElement serialize(PresetContainerJsonFile src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("jsonMap", context.serialize(src.jsonMap, presetsType));
        return jsonObject;
    }
    @Override
    public PresetContainerJsonFile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        PresetContainerJsonFile containerJsonFile = new PresetContainerJsonFile();
        JsonObject jsonObject = json.getAsJsonObject();
        if(jsonObject.has(JsonAdapterGenericPreset.PRESET_TYPE) && jsonObject.has(JsonAdapterGenericPreset.PRESET_NAME)){
            //If we identify the json as a Single Preset, import it as if it was a container file, to allow importing both file types at the same time
            GenericPreset<?> preset = context.deserialize(json, GenericPreset.class);
            if(preset != null){
                containerJsonFile.jsonMap = List.of(preset);
            }
        }else if(jsonObject.has("jsonMap")){
            containerJsonFile.jsonMap = context.deserialize(jsonObject.get("jsonMap"), presetsType);
        }
        return containerJsonFile;
    }
}
