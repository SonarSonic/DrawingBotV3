package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.IPresetLoader;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.image.ImageTools;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.editors.PropertyNode;
import drawingbot.javafx.editors.TreeNode;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.function.Consumer;

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
    public void addEditDialogElements(GenericPreset<IDrawingPen> preset, ObservableList<TreeNode> builder, List<Consumer<GenericPreset<IDrawingPen>>> callbacks) {
        super.addEditDialogElements(preset, builder, callbacks);
        SimpleObjectProperty<Color> penColour = new SimpleObjectProperty<>();
        penColour.set(ImageTools.getColorFromARGB(preset.data.getARGB()));
        penColour.addListener((observable, oldValue, newValue) -> {
            if(preset.data instanceof DrawingPen pen){

                pen.argb = ImageTools.getARGBFromColor(newValue);
            }
        });
        builder.add(new PropertyNode("Colour", penColour, Color.class));
    }

    @Override
    public boolean isSubTypeEditable() {
        return true;
    }
}
