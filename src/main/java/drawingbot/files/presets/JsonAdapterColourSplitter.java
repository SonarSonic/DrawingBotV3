package drawingbot.files.presets;

import com.google.gson.*;
import drawingbot.drawing.ColourSplitterHandler;
import drawingbot.registry.MasterRegistry;

import java.lang.reflect.Type;

public class JsonAdapterColourSplitter implements JsonSerializer<ColourSplitterHandler>, JsonDeserializer<ColourSplitterHandler> {

    @Override
    public ColourSplitterHandler deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return MasterRegistry.INSTANCE.getColourSplitter(json.getAsString());
    }

    @Override
    public JsonElement serialize(ColourSplitterHandler src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.name);
    }

}
