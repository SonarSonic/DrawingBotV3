package drawingbot.files.json.adapters;

import com.google.gson.*;
import drawingbot.drawing.ColourSeparationHandler;
import drawingbot.registry.MasterRegistry;

import java.lang.reflect.Type;

public class JsonAdapterColourSplitter implements JsonSerializer<ColourSeparationHandler>, JsonDeserializer<ColourSeparationHandler> {

    @Override
    public ColourSeparationHandler deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return MasterRegistry.INSTANCE.getColourSplitter(json.getAsString());
    }

    @Override
    public JsonElement serialize(ColourSeparationHandler src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.name);
    }

}