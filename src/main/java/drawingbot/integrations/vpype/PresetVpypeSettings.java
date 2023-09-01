package drawingbot.integrations.vpype;

import com.google.gson.JsonElement;
import drawingbot.files.json.JsonData;
import drawingbot.files.json.PresetData;

import java.util.HashMap;

@JsonData
public class PresetVpypeSettings extends PresetData {

    public PresetVpypeSettings() {
        super();
    }

    public PresetVpypeSettings(HashMap<String, JsonElement> settingList) {
        super(settingList);
    }

}
