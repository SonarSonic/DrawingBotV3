package drawingbot.files.presets;

import com.google.gson.*;
import drawingbot.drawing.ColourSeperationHandler;
import drawingbot.registry.MasterRegistry;

import java.lang.reflect.Type;

public class JsonAdapterColourSplitter implements JsonSerializer<ColourSeperationHandler>, JsonDeserializer<ColourSeperationHandler> {

    @Override
    public ColourSeperationHandler deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return MasterRegistry.INSTANCE.getColourSplitter(json.getAsString());
    }

    @Override
    public JsonElement serialize(ColourSeperationHandler src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.name);
    }

}
