package drawingbot.files.json.presets;

import drawingbot.api.IDrawingPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;

public abstract class PresetDrawingPenManager extends AbstractPresetManager<PresetDrawingPen> {

    public PresetDrawingPenManager(PresetDrawingPenLoader presetLoader) {
        super(presetLoader);
    }

    public abstract IDrawingPen getSelectedDrawingPen();

    @Override
    public GenericPreset<PresetDrawingPen> updatePreset(DBTaskContext context, GenericPreset<PresetDrawingPen> preset) {
        IDrawingPen selectedPen = getSelectedDrawingPen();
        if (selectedPen == null) {
            return null; // can't save the preset
        }
        DrawingPen pen = new DrawingPen(selectedPen);
        preset.presetSubType = pen.getType();
        preset.presetName = pen.getName();
        preset.data.update(pen);
        return preset;
    }

    @Override
    public void applyPreset(DBTaskContext context, GenericPreset<PresetDrawingPen> preset) {
        //nothing to apply
    }
}
