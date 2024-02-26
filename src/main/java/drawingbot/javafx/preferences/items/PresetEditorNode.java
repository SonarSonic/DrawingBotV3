package drawingbot.javafx.preferences.items;

import drawingbot.files.json.DefaultPresetEditor;
import drawingbot.javafx.controls.ControlPresetEditor;

public class PresetEditorNode extends GroupNode {

    public final ControlPresetEditor presetEditor;

    public PresetEditorNode(String name, TreeNode... children) {
        super(name, children);
        this.presetEditor = new ControlPresetEditor();
        this.presetEditor.setUseTreeNode(true);
        this.presetEditor.editorProperty().addListener((observable, oldValue, newValue) -> {
            getChildren().clear();
            if(newValue instanceof DefaultPresetEditor<?, ?> pageEditor){
                pageEditor.init(this);
            }
        });
    }

    public ControlPresetEditor getPresetEditor() {
        return presetEditor;
    }
}
