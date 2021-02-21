package drawingbot.drawing;

import drawingbot.api.IDrawingPen;
import drawingbot.utils.GenericPreset;

public class UserDrawingPen extends DrawingPen {

    public GenericPreset preset;

    public UserDrawingPen(IDrawingPen source, GenericPreset preset) {
        super(source);
        this.type = DrawingRegistry.userType;
        this.preset = preset;
        this.preset.object = this;
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
}
