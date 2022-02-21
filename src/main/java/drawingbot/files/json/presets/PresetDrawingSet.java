package drawingbot.files.json.presets;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.files.json.PresetType;
import drawingbot.files.json.IJsonData;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.Register;

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
    public PresetType getPresetType() {
        return Register.PRESET_TYPE_DRAWING_SET;
    }
}
