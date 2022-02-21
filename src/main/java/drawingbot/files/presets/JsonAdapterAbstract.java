package drawingbot.files.presets;

import com.google.gson.*;
import drawingbot.javafx.GenericSetting;

import java.lang.reflect.Type;
import java.util.List;

public abstract class JsonAdapterAbstract<O> implements JsonSerializer<O>, JsonDeserializer<O> {

    protected boolean updateSettings = false;
    protected boolean changesOnly = false;

    public abstract List<GenericSetting<?, ?>> getSettings();

    public abstract O getInstance();

    @Override
    public O deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return GenericSetting.fromJsonObject(json.getAsJsonObject(), getSettings(), getInstance(), updateSettings);
    }

    @Override
    public JsonElement serialize(O src, Type typeOfSrc, JsonSerializationContext context) {
        return GenericSetting.toJsonObject(getSettings(), src, changesOnly);
    }

}
