package drawingbot.files.json.presets;

import drawingbot.api.IDrawingSet;
import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.drawing.IColorManagedDrawingSet;
import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.DefaultPresetEditor;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableDrawingSet;

public class PresetDrawingSetManager extends AbstractPresetManager<IDrawingSet, IDrawingSet> {

    public PresetDrawingSetManager(PresetDrawingSetLoader presetLoader) {
        super(presetLoader, IDrawingSet.class);
    }

    @Override
    public IDrawingSet getTargetFromContext(DBTaskContext context) {
        return context.project().getActiveDrawingSet();
    }

    @Override
    public void updatePreset(DBTaskContext context, IDrawingSet target, GenericPreset<IDrawingSet> preset) {
        if(target != null && preset.data instanceof DrawingSet set){
            set.pens.clear();
            set.colorHandler = null;
            set.colorSettings = null;
            target.getPens().forEach(p -> set.pens.add(new DrawingPen(p)));

            if(target instanceof IColorManagedDrawingSet colorManagedDrawingSet){
                if(colorManagedDrawingSet.getColorSeparationHandler() != null){
                    set.colorHandler = colorManagedDrawingSet.getColorSeparationHandler();
                }

                if(colorManagedDrawingSet.getColorSeparationSettings() != null){
                    set.colorSettings = colorManagedDrawingSet.getColorSeparationSettings().copy();
                }
            }
        }
    }

    @Override
    public void applyPreset(DBTaskContext context, IDrawingSet target, GenericPreset<IDrawingSet> preset, boolean changesOnly) {
        //TODO REMOVE ME!
        if(target instanceof ObservableDrawingSet set){
            set.loadDrawingSet(preset.data);
        }
    }

    @Override
    public DefaultPresetEditor<IDrawingSet, IDrawingSet> createPresetEditor() {
        return new PresetDrawingSetEditor(this);
    }
}
