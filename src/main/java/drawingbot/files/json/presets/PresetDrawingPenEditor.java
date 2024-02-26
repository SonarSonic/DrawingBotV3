package drawingbot.files.json.presets;

import drawingbot.api.IDrawingPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.files.json.DefaultPresetEditor;
import drawingbot.files.json.IPresetManager;
import drawingbot.image.ImageTools;
import drawingbot.javafx.preferences.items.PropertyNode;
import drawingbot.javafx.preferences.items.TreeNode;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import org.fxmisc.easybind.EasyBind;

public class PresetDrawingPenEditor extends DefaultPresetEditor<IDrawingPen, IDrawingPen> {

    public SimpleObjectProperty<Color> penColour;

    public PresetDrawingPenEditor(IPresetManager<IDrawingPen, IDrawingPen> manager) {
        super(manager);
    }

    @Override
    public void init(TreeNode editorNode) {
        super.init(editorNode);
        penColour = new SimpleObjectProperty<>();
        penColour.set(Color.WHITE);
        EasyBind.subscribe(editingPresetProperty(), preset -> {
            if(preset != null){
                penColour.set(ImageTools.getColorFromARGB(preset.data.getARGB()));
            }
        });
        editorNode.getChildren().add(new PropertyNode<Color>("Colour", penColour, () -> ImageTools.getColorFromARGB(getSelectedPreset().data.getARGB()), Color.class));
    }

    @Override
    public void updatePreset() {
        super.updatePreset();
        if(editingPreset.get().data instanceof DrawingPen pen){
            pen.argb = ImageTools.getARGBFromColor(penColour.get());
        }
    }
}
