package drawingbot.files.json.presets;

import com.google.gson.JsonElement;
import drawingbot.files.json.AbstractJsonData;
import drawingbot.files.json.PresetType;
import drawingbot.registry.Register;

import java.util.HashMap;

public class PresetDrawingArea extends AbstractJsonData {

    public PresetDrawingArea() {
        super();
    }

    public PresetDrawingArea(HashMap<String, JsonElement> settingList) {
        super(settingList);
    }

    @Override
    public PresetType getPresetType() {
        return Register.PRESET_TYPE_DRAWING_AREA;
    }
}
