package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.DefaultPresetEditor;
import drawingbot.files.json.IPresetLoader;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;

public class PresetDrawingPenManager extends AbstractPresetManager<IDrawingPen, IDrawingPen> {

    public PresetDrawingPenManager(IPresetLoader<IDrawingPen> presetLoader) {
        super(presetLoader, IDrawingPen.class);
    }

    @Override
    public IDrawingPen getTargetFromContext(DBTaskContext context) {
        return null; //NOP TODO MAKE THE SELECTED PEN AVAILABLE IN DB?
    }

    @Override
    public void updatePreset(DBTaskContext context, IDrawingPen target, GenericPreset<IDrawingPen> preset) {
        if(target == null){
            return;
        }
        preset.setPresetSubType(target.getType());
        preset.setPresetName(target.getName());
        if(preset.data instanceof DrawingPen pen){
            pen.update(target);
        }else{
            DrawingBotV3.logger.severe("Unable to update Preset %s".formatted(preset.getPresetName()));
        }
    }

    @Override
    public void applyPreset(DBTaskContext context, IDrawingPen target, GenericPreset<IDrawingPen> preset, boolean changesOnly) {
        //TODO / NOP
    }

    @Override
    public DefaultPresetEditor<IDrawingPen, IDrawingPen> createPresetEditor() {
        return new PresetDrawingPenEditor(this);
    }

}
