package drawingbot.javafx.editors.custom;

import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.editors.EditorBase;
import drawingbot.javafx.editors.EditorContext;
import drawingbot.javafx.editors.IEditableProperty;
import drawingbot.javafx.editors.IEditor;
import drawingbot.javafx.util.JFXUtils;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Generic text input editor for any property type.
 * If the property is a {@link GenericSetting} the provided TextFormatter will be used
 */
public class EditorTextInputControl<V, T extends TextInputControl> extends EditorBase<V> {

    protected T node = null;
    protected ChangeListener<Boolean> focusListener = null;
    protected EventHandler<ActionEvent> onActionEvent = null;
    protected ChangeListener<V> valueListener = null;

    public EditorTextInputControl(EditorContext context, IEditableProperty<V> property, T node) {
        super(context, property);
        this.node = node;

        if(getProperty() instanceof GenericSetting<?, V> setting){
            node.setTextFormatter(setting.createTextFormatter());
            node.setText(setting.getValueAsString());

            node.addEventHandler(ActionEvent.ACTION, onActionEvent = e -> {
                setting.setValueFromString(node.getText());
                setting.sendUserEditedEvent();
            });
            node.focusedProperty().addListener(focusListener = (observable, oldValue, newValue) -> {
                //set the value when the text field is de-focused
                if(oldValue && !newValue){
                    setting.setValueFromString(node.getText());
                }
            });
            setting.valueProperty().addListener(valueListener = (observable, oldValue, newValue) -> node.setText(setting.getValueAsString()));
        }else{
            node.textProperty().bindBidirectional(property.valueProperty(), JFXUtils.getStringConverter(property.getType()));
            node.addEventHandler(ActionEvent.ACTION, onActionEvent = e -> {
                property.sendUserEditedEvent();
            });
        }

        if(!String.class.isAssignableFrom(property.getType())){
            node.setMaxWidth(200);
        }
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public void dispose() {
        if(getProperty() instanceof GenericSetting<?, V> setting){
            node.setTextFormatter(null);
            node.removeEventHandler(ActionEvent.ACTION, onActionEvent);
            node.focusedProperty().removeListener(focusListener);
            setting.valueProperty().removeListener(valueListener);
            onActionEvent = null;
            focusListener = null;
            valueListener = null;
        }else{
            node.textProperty().unbindBidirectional(property.valueProperty());
            node.removeEventHandler(ActionEvent.ACTION, onActionEvent);
            onActionEvent = null;
        }
    }

    public static class Field<V> extends EditorTextInputControl<V, TextField>{

        public Field(EditorContext context, IEditableProperty<V> property) {
            super(context, property, new TextField());
        }
    }

    public static class Area<V> extends EditorTextInputControl<V, TextArea>{

        public Area(EditorContext context, IEditableProperty<V> property) {
            super(context, property, new TextArea());
            VBox.setVgrow(node, Priority.ALWAYS);
            HBox.setHgrow(node, Priority.ALWAYS);
            node.setPrefRowCount(6);
            node.setMinHeight(100);
        }
    }
}
