package drawingbot.drawing;

import drawingbot.api.IDrawingPen;
import drawingbot.utils.GenericPreset;

import java.util.List;

public class UserDrawingSet extends DrawingSet {

    public GenericPreset preset;

    public UserDrawingSet(String type, String name, List<IDrawingPen> pens, GenericPreset preset) {
        super(type, name, pens);
        this.preset = preset;
        this.preset.binding = this;
    }

}
