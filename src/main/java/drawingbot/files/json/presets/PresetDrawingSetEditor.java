package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingSet;
import drawingbot.files.json.DefaultPresetEditor;
import drawingbot.files.json.IPresetManager;
import drawingbot.javafx.controls.ControlDrawingSetEditor;
import drawingbot.javafx.editors.EditorContext;
import drawingbot.javafx.preferences.items.AbstractPropertyNode;
import drawingbot.javafx.preferences.items.TreeNode;
import drawingbot.javafx.observables.ObservableDrawingSet;
import javafx.beans.Observable;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class PresetDrawingSetEditor extends DefaultPresetEditor<IDrawingSet, IDrawingSet> {

    public final ObservableDrawingSet drawingSet = new ObservableDrawingSet();

    private ControlDrawingSetEditor controlDrawingSetEditor;

    public PresetDrawingSetEditor(IPresetManager<IDrawingSet, IDrawingSet> manager) {
        super(manager);
    }

    @Override
    public void init(TreeNode editorNode) {
        super.init(editorNode);
        if (!isDetailed()) {
            return;
        }
        editingPresetProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                manager.applyPreset(DrawingBotV3.context(), drawingSet, newValue, false);
            }
        });
        controlDrawingSetEditor = new ControlDrawingSetEditor();
        controlDrawingSetEditor.setDrawingSet(drawingSet);
        controlDrawingSetEditor.penTableView.prefHeight(-1);
        VBox.setVgrow(controlDrawingSetEditor, Priority.ALWAYS);

        editorNode.getChildren().add(new AbstractPropertyNode("Pen Settings") {
            @Override
            public Node getEditorNode(EditorContext context) {
                return controlDrawingSetEditor;
            }

            @Override
            public void resetProperty() {
                manager.applyPreset(DrawingBotV3.context(), drawingSet, getSelectedPreset(), false);
            }

            @Override
            public String asString() {
                return drawingSet.toString();
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
        manager.updatePreset(DrawingBotV3.context(), drawingSet, getEditingPreset());
    }
}