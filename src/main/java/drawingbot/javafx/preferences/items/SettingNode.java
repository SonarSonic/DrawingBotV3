package drawingbot.javafx.preferences.items;

import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.editors.EditorContext;
import drawingbot.javafx.editors.IEditableProperty;
import drawingbot.javafx.editors.IEditor;
import javafx.beans.Observable;
import javafx.scene.Node;

/**
 * A {@link ElementNode} which wraps around a DrawingBotV3 {@link GenericSetting}
 * It will take the settings names and editors which edit the values live (e.g. sliders) will notify the setting when the value is changing with {@link GenericSetting#setValueChanging(boolean)}
 */
public class SettingNode<V> extends AbstractPropertyNode {

    public IEditableProperty<V> property;
    public IEditor<V> editor;

    public SettingNode(IEditableProperty<V> property, TreeNode... children) {
        this("", property, children);
    }

    public SettingNode(String overrideName, IEditableProperty<V> property, TreeNode... children) {
        super(overrideName.isEmpty() ? property.getDisplayName() : overrideName, children);
        this.property = property;

        this.propertyDisabledProperty().bind(property.disabledProperty());
        if(overrideName.isEmpty() && property instanceof GenericSetting<?, ?> setting){
            this.nameProperty().bind(setting.displayNameProperty());
        }
    }

    @Override
    public void resetProperty() {
        property.resetValue();
    }

    @Override
    public Node getEditorNode(EditorContext context) {
        if(editor == null){
            editor = property.createEditor(context);
        }
        return editor.getNode();
    }

    @Override
    public String asString() {
        return property.getValueAsString();
    }

    @Override
    public Observable[] getDependencies() {
        return new Observable[]{property.valueProperty()};
    }

}
