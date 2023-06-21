package drawingbot.javafx.editors;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;

/**
 * A {@link ElementNode} which wraps around a raw JavaFX {@link Property}
 * It's better to use {@link SettingNode} where possible
 */
public class PropertyNode extends ElementNode {

    public Property<?> property;
    public Class<?> type;
    public boolean editable = true;

    public PropertyNode(String name, Property<?> property, Class<?> type, TreeNode... children) {
        super(name, children);
        this.property = property;
        this.type = type;
    }

    public PropertyNode setEditable(boolean editable) {
        this.editable = editable;
        return this;
    }

    public Node createEditor() {
        return Editors.createEditor(property, type);
    }

    @Override
    public void addElement(PageBuilder builder) {
        Label label = new Label();
        label.textProperty().bind(nameProperty());
        label.getStyleClass().add(labelStyle);


        Node editor = null;
        if (editable) {
            editor = createEditor();
            editor.getStyleClass().add(labelStyle);
            if (editor instanceof Control) {
                Control control = (Control) editor;
                control.setPrefWidth(200);
            }
        } else {
            Label propertyLabel = new Label();
            propertyLabel.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(property.getValue()), property));
            editor = propertyLabel;
        }

        builder.addRow(label, editor);

        if (disabled != null) {
            label.disableProperty().bind(disabled);
            editor.disableProperty().bind(disabled);
        }
    }
}
