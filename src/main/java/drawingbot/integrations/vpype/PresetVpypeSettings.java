package drawingbot.integrations.vpype;

import com.google.gson.JsonElement;
import drawingbot.files.json.AbstractJsonData;
import drawingbot.files.json.JsonData;
import drawingbot.files.json.PresetType;
import drawingbot.registry.Register;

import java.util.HashMap;

@JsonData
public class PresetVpypeSettings extends AbstractJsonData {

    public PresetVpypeSettings() {
        super();
    }

    public PresetVpypeSettings(HashMap<String, JsonElement> settingList) {
        super(settingList);
    }

    @Override
    public PresetType getPresetType() {
        return Register.PRESET_TYPE_VPYPE_SETTINGS;
    }
}
