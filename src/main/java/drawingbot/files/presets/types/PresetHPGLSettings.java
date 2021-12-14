package drawingbot.files.presets.types;

import com.google.gson.JsonElement;
import drawingbot.files.presets.AbstractJsonData;
import drawingbot.utils.EnumJsonType;

import java.util.HashMap;

public class PresetHPGLSettings extends AbstractJsonData {

    public PresetHPGLSettings() {
        super();
    }

    public PresetHPGLSettings(HashMap<String, JsonElement> settingList) {
        super(settingList);
    }

    @Override
    public EnumJsonType getJsonType() {
        return EnumJsonType.HPGL_SETTINGS;
    }
}
