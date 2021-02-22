package drawingbot.files.presets.types;

import drawingbot.api.IDrawingPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingRegistry;
import drawingbot.files.presets.IJsonData;
import drawingbot.utils.EnumJsonType;
import drawingbot.utils.GenericPreset;

public class PresetDrawingPen extends DrawingPen implements IJsonData {

    public transient GenericPreset<PresetDrawingPen> preset;

    public PresetDrawingPen(){}

    public PresetDrawingPen(GenericPreset<PresetDrawingPen> preset) {
        this.preset = preset;
        this.preset.data = this;
    }

    public PresetDrawingPen(IDrawingPen source, GenericPreset<PresetDrawingPen> preset) {
        super(source);
        this.preset = preset;
        this.preset.data = this;
    }

    @Override
    public void update(String type, String name, int argb, int distributionWeight, float strokeSize) {
        super.update(type, name, argb, distributionWeight, strokeSize);
        this.type = DrawingRegistry.userType;
    }

    @Override
    public void update(IDrawingPen pen) {
        super.update(pen);
        this.type = DrawingRegistry.userType;
    }

    @Override
    public EnumJsonType getJsonType() {
        return EnumJsonType.DRAWING_PEN;
    }
}
