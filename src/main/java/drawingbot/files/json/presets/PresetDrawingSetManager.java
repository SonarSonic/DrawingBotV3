package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.DrawingPen;
import drawingbot.files.json.AbstractJsonLoader;
import drawingbot.files.json.AbstractPresetManager;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableDrawingSet;

public abstract class PresetDrawingSetManager extends AbstractPresetManager<PresetDrawingSet> {

    public PresetDrawingSetManager(PresetDrawingSetLoader presetLoader) {
        super(presetLoader);
    }

    public abstract ObservableDrawingSet getSelectedDrawingSet();

    @Override
    public GenericPreset<PresetDrawingSet> updatePreset(GenericPreset<PresetDrawingSet> preset) {
        ObservableDrawingSet drawingSet = getSelectedDrawingSet();
        if(drawingSet != null){
            preset.data.pens.clear();
            drawingSet.getPens().forEach(p -> preset.data.pens.add(new DrawingPen(p)));
        }
        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<PresetDrawingSet> preset) {
        //TODO REMOVE ME!
        DrawingBotV3.INSTANCE.controller.drawingSetsController.changeDrawingSet(preset.data);
    }
}
