package drawingbot.files.json.presets;

import com.google.gson.JsonElement;
import drawingbot.files.json.AbstractJsonData;
import drawingbot.files.json.JsonData;
import drawingbot.files.json.PresetType;
import drawingbot.registry.Register;

import java.util.HashMap;

@JsonData
public class PresetUISettings extends AbstractJsonData {

    public PresetUISettings() {
        super();
    }

    public PresetUISettings(HashMap<String, JsonElement> settingList) {
        super(settingList);
    }

    @Override
    public PresetType getPresetType() {
        return Register.PRESET_TYPE_DRAWING_AREA;
    }
}
