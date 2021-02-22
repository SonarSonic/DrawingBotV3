package drawingbot.files.presets.types;

import drawingbot.api.IDrawingPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.files.presets.IJsonData;
import drawingbot.utils.EnumJsonType;
import drawingbot.utils.GenericPreset;

import java.util.List;

public class PresetDrawingSet extends DrawingSet implements IJsonData {

    public transient GenericPreset<PresetDrawingSet> preset;

    public PresetDrawingSet(){}

    public PresetDrawingSet(String type, String name, List<DrawingPen> pens, GenericPreset<PresetDrawingSet> preset) {
        super(type, name, pens);
        this.preset = preset;
        this.preset.data = this;
    }

    @Override
    public EnumJsonType getJsonType() {
        return EnumJsonType.DRAWING_SET;
    }
}
