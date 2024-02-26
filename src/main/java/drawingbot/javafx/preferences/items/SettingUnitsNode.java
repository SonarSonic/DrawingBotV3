package drawingbot.javafx.preferences.items;

import drawingbot.javafx.editors.EditorContext;
import drawingbot.javafx.editors.IEditableProperty;
import drawingbot.javafx.editors.IEditor;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class SettingUnitsNode<V, U> extends SettingNode<V> {

    public IEditableProperty<U> units;
    public IEditor<U> unitsEditor;

    public SettingUnitsNode(IEditableProperty<V> property, IEditableProperty<U> units, TreeNode... children) {
        super(property, children);
        this.units = units;
    }

    public SettingUnitsNode(String overrideName, IEditableProperty<V> property, IEditableProperty<U> units, TreeNode... children) {
        super(overrideName, property, children);
        this.units = units;
    }

    @Override
    public Node getEditorNode(EditorContext context) {
        if(editor == null){
            editor = property.createEditor(context);
            HBox.setHgrow(editor.getNode(), Priority.ALWAYS);

            unitsEditor = units.createEditor(context);
            HBox.setHgrow(unitsEditor.getNode(), Priority.SOMETIMES);
        }
        return new HBox(6, editor.getNode(), unitsEditor.getNode());
    }

}
