package drawingbot.files.json.adapters;

import com.google.gson.*;
import drawingbot.drawing.ColorSeparationHandler;
import drawingbot.registry.MasterRegistry;

import java.lang.reflect.Type;

public class JsonAdapterColorSeparationHandler implements JsonSerializer<ColorSeparationHandler>, JsonDeserializer<ColorSeparationHandler> {

    @Override
    public ColorSeparationHandler deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return MasterRegistry.INSTANCE.getColourSplitter(json.getAsString());
    }

    @Override
    public JsonElement serialize(ColorSeparationHandler src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.name);
    }

}