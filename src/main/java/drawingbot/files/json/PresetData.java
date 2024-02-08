package drawingbot.files.json;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Objects;

/**
 * A generic implementation of PresetData, which just contains a list of settings.
 * Used by most Presets / as this is all that's required generally.
 */
@JsonData
public class PresetData {

    @SerializedName(value = "settings", alternate = "settingList")
    public HashMap<String, JsonElement> settings;

    public PresetData(){
        settings = new HashMap<>();
    }

    public PresetData(HashMap<String, JsonElement> settings){
        this.settings = settings;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PresetData other){
            return settings.equals(other.settings);
        }
        return super.equals(obj);
    }
}
