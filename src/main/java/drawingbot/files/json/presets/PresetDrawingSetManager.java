package drawingbot.files.json.presets;

import drawingbot.drawing.DrawingPen;
import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableDrawingSet;

public abstract class PresetDrawingSetManager extends AbstractPresetManager<PresetDrawingSet> {

    public PresetDrawingSetManager(PresetDrawingSetLoader presetLoader) {
        super(presetLoader);
    }

    public abstract ObservableDrawingSet getSelectedDrawingSet(DBTaskContext context);

    @Override
    public GenericPreset<PresetDrawingSet> updatePreset(DBTaskContext context, GenericPreset<PresetDrawingSet> preset) {
        ObservableDrawingSet drawingSet = getSelectedDrawingSet(context);
        if(drawingSet != null){
            preset.data.pens.clear();
            drawingSet.getPens().forEach(p -> preset.data.pens.add(new DrawingPen(p)));
        }
        return preset;
    }

    @Override
    public void applyPreset(DBTaskContext context, GenericPreset<PresetDrawingSet> preset) {
        //TODO REMOVE ME!
        context.project().getDrawingSets().changeDrawingSet(preset.data);
    }
}
