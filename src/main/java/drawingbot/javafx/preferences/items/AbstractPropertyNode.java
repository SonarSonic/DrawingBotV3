package drawingbot.javafx.preferences.items;

import drawingbot.javafx.editors.EditorContext;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import org.controlsfx.glyphfont.Glyph;

public abstract class AbstractPropertyNode extends ElementNode {

    public AbstractPropertyNode(String name, TreeNode... children) {
        super(name, children);
    }

    ////////////////////////////////////////

    public BooleanProperty canReset = new SimpleBooleanProperty(true);

    public boolean canReset() {
        return canReset.get();
    }

    public BooleanProperty canResetProperty() {
        return canReset;
    }

    public void setCanReset(boolean canReset) {
        this.canReset.set(canReset);
    }

    ////////////////////////////////////////

    public BooleanProperty editable = new SimpleBooleanProperty(true);

    public boolean isEditable() {
        return editable.get();
    }

    public BooleanProperty editableProperty() {
        return editable;
    }

    public AbstractPropertyNode setEditable(boolean editable) {
        this.editable.set(editable);
        return this;
    }

    ////////////////////////////////////////

    public BooleanProperty propertyDisabled = new SimpleBooleanProperty(false);

    public boolean isPropertyDisabled() {
        return propertyDisabled.get();
    }

    public BooleanProperty propertyDisabledProperty() {
        return propertyDisabled;
    }

    public void setPropertyDisabled(boolean propertyDisabled) {
        this.propertyDisabled.set(propertyDisabled);
    }

    ////////////////////////////////////////

    public abstract void resetProperty();

    public abstract Node getEditorNode(EditorContext context);

    public abstract String asString();

    public abstract Observable[] getDependencies();


    public void addElement(PageBuilder builder) {
        Label label = new Label();
        label.textProperty().bind(nameProperty());
        label.getStyleClass().add(labelStyle);
        label.disableProperty().bind(disabled.or(propertyDisabled));

        Node editor = null;
        if(isEditable()){
            editor = getEditorNode(builder.context);
            editor.getStyleClass().add(labelStyle);
            editor.disableProperty().bind(disabled.or(propertyDisabled));
            if (editor instanceof Control control) {
                control.setPrefWidth(200);
            }
        }else{
            Label propertyLabel = new Label();
            propertyLabel.textProperty().bind(Bindings.createStringBinding(this::asString, getDependencies()));
            editor = propertyLabel;
        }

        Button resetButton = new Button("", new Glyph("FontAwesome", "ROTATE_LEFT"));
        resetButton.getStyleClass().add("preference-reset-button");
        resetButton.setOnAction(e -> {
            if(canReset()){
                resetProperty();
            }
        });
        resetButton.disableProperty().bind(disabled.or(propertyDisabled));
        resetButton.visibleProperty().bind(canReset);
        builder.addRow(label, editor, resetButton);

    }

}
