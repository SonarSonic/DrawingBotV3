package drawingbot.files.json.adapters;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import drawingbot.files.VersionControl;
import drawingbot.javafx.observables.ObservableVersion;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonAdapterVersionControl implements JsonSerializer<VersionControl>, JsonDeserializer<VersionControl> {

    public static final Type versionsType = TypeToken.getParameterized(ArrayList.class, ObservableVersion.class).getType();

    @Override
    public JsonElement serialize(VersionControl src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        if(!src.getProjectVersions().isEmpty()){
            src.getProjectVersions().forEach(JsonAdapterVersionControl::stripDuplicateVersionData);
            jsonObject.add("versions", context.serialize(new ArrayList<>(src.getProjectVersions()), versionsType));
        }

        if(src.getLastRun() != null){
            src.getLastRun().updatePreset();
            jsonObject.add("lastRun", context.serialize(src.getLastRun(), ObservableVersion.class));
        }

        return jsonObject;
    }

    @Override
    public VersionControl deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        VersionControl dst = new VersionControl();
        JsonObject jsonObject = json.getAsJsonObject();

        if(jsonObject.has("versions")){
            List<ObservableVersion> versions = context.deserialize(jsonObject.get("versions"), versionsType);
            versions.forEach(JsonAdapterVersionControl::stripDuplicateVersionData);
            dst.getProjectVersions().setAll(versions);
        }

        if(jsonObject.has("lastRun")){
            dst.setLastRun(context.deserialize(jsonObject.get("lastRun"), ObservableVersion.class));
        }

        return dst;
    }

    //Fix: Duplicate version data may still exist in projects created prior to the fix in 1.6.17-stable, this data will never be loaded or used we remove the entry
    private static void stripDuplicateVersionData(ObservableVersion version){
        version.getPreset().data.settings.remove("versions");
    }
}
