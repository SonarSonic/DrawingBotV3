package drawingbot.files.presets;

import com.google.gson.*;
import drawingbot.pfm.PFMFactory;
import drawingbot.registry.MasterRegistry;

import java.lang.reflect.Type;

public class JsonAdapterPFMFactory implements JsonSerializer<PFMFactory<?>>, JsonDeserializer<PFMFactory<?>> {

    @Override
    public PFMFactory<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return MasterRegistry.INSTANCE.getPFMFactory(json.getAsString());
    }

    @Override
    public JsonElement serialize(PFMFactory<?> src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getName());
    }
}
