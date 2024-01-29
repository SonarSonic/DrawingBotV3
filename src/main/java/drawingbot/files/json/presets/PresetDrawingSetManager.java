package drawingbot.files.json.presets;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.registry.MasterRegistry;
import javafx.collections.ObservableList;

public abstract class PresetDrawingSetManager extends AbstractPresetManager<DrawingSet> {

    public PresetDrawingSetManager(PresetDrawingSetLoader presetLoader) {
        super(presetLoader);
    }

    public abstract ObservableDrawingSet getSelectedDrawingSet(DBTaskContext context);

    @Override
    public GenericPreset<DrawingSet> updatePreset(DBTaskContext context, GenericPreset<DrawingSet> preset, boolean loadingProject) {
        ObservableDrawingSet drawingSet = getSelectedDrawingSet(context);
        if(drawingSet != null){
            preset.data.pens.clear();
            preset.data.colorHandler = null;
            preset.data.colorSettings = null;
            drawingSet.getPens().forEach(p -> preset.data.pens.add(new DrawingPen(p)));

            if(drawingSet.getColorSeparationHandler() != null){
                preset.data.colorHandler = drawingSet.getColorSeparationHandler();
            }

            if(drawingSet.getColorSeparationSettings() != null){
                preset.data.colorSettings = drawingSet.getColorSeparationSettings().copy();
            }
        }
        return preset;
    }

    @Override
    public void applyPreset(DBTaskContext context, GenericPreset<DrawingSet> preset, boolean changesOnly, boolean loadingProject) {
        //TODO REMOVE ME!
        context.project().getDrawingSets().changeDrawingSet(preset.data);
    }

    @Override
    public boolean isSubTypeEditable() {
        return true;
    }

    @Override
    public ObservableList<String> getObservableCategoryList() {
        return MasterRegistry.INSTANCE.registeredDrawingSetCategories;
    }
}
