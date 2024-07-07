package drawingbot.files.json.adapters;

import com.google.gson.*;

import java.io.File;
import java.lang.reflect.Type;

public class JsonAdapterFile implements JsonSerializer<File>, JsonDeserializer<File> {

    @Override
    public File deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new File(json.getAsString());
    }

    @Override
    public JsonElement serialize(File src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getAbsolutePath());
    }
}
