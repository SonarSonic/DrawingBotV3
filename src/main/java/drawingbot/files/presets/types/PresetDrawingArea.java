package drawingbot.files.presets.types;

import drawingbot.files.presets.AbstractJsonData;
import drawingbot.utils.EnumJsonType;

import java.util.HashMap;

public class PresetDrawingArea extends AbstractJsonData {

    public PresetDrawingArea() {
        super();
    }

    public PresetDrawingArea(HashMap<String, String> settingList) {
        super(settingList);
    }

    @Override
    public EnumJsonType getJsonType() {
        return EnumJsonType.DRAWING_AREA;
    }
}
