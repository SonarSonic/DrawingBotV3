package drawingbot.files.json.presets;

import drawingbot.api.IDrawingPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.image.ImageTools;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.editors.PropertyNode;
import drawingbot.javafx.editors.TreeNode;
import drawingbot.registry.MasterRegistry;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.function.Consumer;

public abstract class PresetDrawingPenManager extends AbstractPresetManager<DrawingPen> {

    public PresetDrawingPenManager(PresetDrawingPenLoader presetLoader) {
        super(presetLoader);
    }

    public abstract IDrawingPen getSelectedDrawingPen();

    @Override
    public GenericPreset<DrawingPen> updatePreset(DBTaskContext context, GenericPreset<DrawingPen> preset, boolean loadingProject) {
        IDrawingPen selectedPen = getSelectedDrawingPen();
        if (selectedPen != null) {
            DrawingPen pen = new DrawingPen(selectedPen);
            preset.setPresetSubType(pen.getType());
            preset.setPresetName(pen.getName());
            preset.data.update(pen);
            return preset;
        }else{
            return preset;
        }
    }

    @Override
    public void applyPreset(DBTaskContext context, GenericPreset<DrawingPen> preset, boolean loadingProject) {
        //nothing to apply
    }

    @Override
    public void addEditDialogElements(GenericPreset<DrawingPen> preset, ObservableList<TreeNode> builder, List<Consumer<GenericPreset<DrawingPen>>> callbacks) {
        super.addEditDialogElements(preset, builder, callbacks);
        SimpleObjectProperty<Color> penColour = new SimpleObjectProperty<>();
        penColour.set(ImageTools.getColorFromARGB(preset.data.argb));
        penColour.addListener((observable, oldValue, newValue) -> preset.data.argb = ImageTools.getARGBFromColor(newValue));
        builder.add(new PropertyNode("Colour", penColour, Color.class));
    }

    @Override
    public boolean isSubTypeEditable() {
        return true;
    }

    @Override
    public ObservableList<String> getObservableCategoryList() {
        return MasterRegistry.INSTANCE.registeredPenCategories;
    }
}
