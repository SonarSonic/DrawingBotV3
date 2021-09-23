package drawingbot.integrations.vpype;

import com.google.gson.JsonElement;
import drawingbot.files.presets.AbstractJsonData;
import drawingbot.utils.EnumJsonType;

import java.util.HashMap;

public class PresetVpypeSettings extends AbstractJsonData {

    public PresetVpypeSettings() {
        super();
    }

    public PresetVpypeSettings(HashMap<String, JsonElement> settingList) {
        super(settingList);
    }

    @Override
    public EnumJsonType getJsonType() {
        return EnumJsonType.VPYPE_SETTINGS;
    }
}
