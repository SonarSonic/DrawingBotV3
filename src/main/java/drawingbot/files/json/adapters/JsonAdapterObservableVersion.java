package drawingbot.files.json.adapters;

import com.google.gson.*;
import drawingbot.files.json.projects.PresetProjectSettings;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableVersion;

import java.lang.reflect.Type;

public class JsonAdapterObservableVersion implements JsonSerializer<ObservableVersion>, JsonDeserializer<ObservableVersion> {

    @Override
    public JsonElement serialize(ObservableVersion src, Type typeOfSrc, JsonSerializationContext context) {
        src.updatePreset();
        return context.serialize(src.getPreset(), GenericPreset.class);
    }
    @Override
    public ObservableVersion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        GenericPreset<PresetProjectSettings> preset = context.deserialize(json, GenericPreset.class);
        return new ObservableVersion(preset, true);
    }
}
