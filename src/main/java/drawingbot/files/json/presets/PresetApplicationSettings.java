package drawingbot.files.json.presets;

import com.google.gson.JsonElement;
import drawingbot.files.json.AbstractJsonData;
import drawingbot.files.json.PresetType;
import drawingbot.registry.Register;

import java.util.HashMap;

public class PresetApplicationSettings extends AbstractJsonData {

    public PresetApplicationSettings() {
        super();
    }

    public PresetApplicationSettings(HashMap<String, JsonElement> settingList) {
        super(settingList);
    }

    @Override
    public PresetType getPresetType() {
        return Register.PRESET_TYPE_APPLICATION_SETTINGS;
    }
}
