package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.DefaultPresetEditor;
import drawingbot.files.json.IPresetManager;
import drawingbot.image.ImageFilterSettings;
import drawingbot.javafx.controls.ControlImageFiltersEditor;
import drawingbot.javafx.editors.EditorContext;
import drawingbot.javafx.preferences.items.AbstractPropertyNode;
import drawingbot.javafx.preferences.items.TreeNode;
import javafx.beans.Observable;
import javafx.scene.Node;

public class PresetImageFiltersEditor extends DefaultPresetEditor<ImageFilterSettings, PresetImageFilters> {

    public final ImageFilterSettings imageFilterSettings = new ImageFilterSettings();

    private ControlImageFiltersEditor imageFiltersControl;

    public PresetImageFiltersEditor(IPresetManager<ImageFilterSettings, PresetImageFilters> manager) {
        super(manager);
    }

    @Override
    public void init(TreeNode editorNode) {
        super.init(editorNode);
        if(!isDetailed()){
            return;
        }
        editingPresetProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                manager.applyPreset(DrawingBotV3.context(), imageFilterSettings, newValue, false);
            }
        });
        imageFiltersControl = new ControlImageFiltersEditor();
        imageFiltersControl.settings.set(imageFilterSettings);

        editorNode.getChildren().add(new AbstractPropertyNode("Image Filters") {
            @Override
            public Node getEditorNode(EditorContext context) {
                return imageFiltersControl;
            }

            @Override
            public void resetProperty() {
                manager.applyPreset(DrawingBotV3.context(), imageFilterSettings, getSelectedPreset(), false);
            }

            @Override
            public String asString() {
                return imageFilterSettings.toString();
            }

            @Override
            public Observable[] getDependencies() {
                return new Observable[0];
            }
        });


    }

    @Override
    public void updatePreset() {
        super.updatePreset();
        manager.updatePreset(DrawingBotV3.context(), imageFilterSettings, getEditingPreset());
    }
}
