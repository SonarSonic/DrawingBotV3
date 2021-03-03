package drawingbot.files.presets.types;

import drawingbot.files.presets.AbstractJsonData;
import drawingbot.utils.EnumJsonType;

import java.util.HashMap;

public class PresetGCodeSettings extends AbstractJsonData {

    public PresetGCodeSettings() {
        super();
    }

    public PresetGCodeSettings(HashMap<String, String> settingList) {
        super(settingList);
    }

    @Override
    public EnumJsonType getJsonType() {
        return EnumJsonType.GCODE_SETTINGS;
    }
}
