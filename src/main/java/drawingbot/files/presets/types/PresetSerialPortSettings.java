package drawingbot.files.presets.types;

import com.google.gson.JsonElement;
import drawingbot.files.presets.AbstractJsonData;
import drawingbot.utils.EnumJsonType;

import java.util.HashMap;

public class PresetSerialPortSettings extends AbstractJsonData {

    public PresetSerialPortSettings() {
        super();
    }

    public PresetSerialPortSettings(HashMap<String, JsonElement> settingList) {
        super(settingList);
    }

    @Override
    public EnumJsonType getJsonType() {
        return EnumJsonType.SERIAL_PORT_CONFIG;
    }
}
