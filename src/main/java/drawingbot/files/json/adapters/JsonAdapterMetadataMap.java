package drawingbot.files.json.adapters;

import com.google.gson.*;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.Metadata;
import drawingbot.utils.MetadataMap;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonAdapterMetadataMap implements JsonSerializer<MetadataMap>, JsonDeserializer<MetadataMap> {

    @Override
    public JsonElement serialize(MetadataMap src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        JsonObject metadata = new JsonObject();
        for(Map.Entry<Metadata<?>, Object> entry : src.data.entrySet()){
            if(entry.getKey().serialize){
                metadata.add(entry.getKey().key, context.serialize(entry.getValue(), entry.getKey().type));
            }
        }
        jsonObject.add("metadata", metadata);
        return jsonObject;
    }

    @Override
    public MetadataMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        MetadataMap map = new MetadataMap(new LinkedHashMap<>());
        JsonObject jsonObject = json.getAsJsonObject();
        JsonObject metadata = jsonObject.getAsJsonObject("metadata");
        for(String key : metadata.keySet()){
            Metadata metadataType = MasterRegistry.INSTANCE.metadataTypes.get(key);
            if(metadataType == null) {
                continue;
            }
            Object value = context.deserialize(metadata.get(key), metadataType.type);
            if(value == null){
                continue;
            }
            map.setMetadata(metadataType, value);
        }
        return map;
    }
}
