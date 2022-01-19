package drawingbot.integrations.vpype;

import com.google.gson.JsonElement;
import drawingbot.files.presets.AbstractJsonData;
import drawingbot.files.presets.PresetType;
import drawingbot.registry.Register;

import java.util.HashMap;

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
