package drawingbot.javafx.preferences.items;

import drawingbot.javafx.editors.EditableProperty;
import drawingbot.javafx.editors.Editors;
import javafx.beans.property.Property;

import java.util.function.Supplier;

/**
 * A {@link ElementNode} which wraps around a raw JavaFX {@link Property}
 * It's better to use {@link SettingPresetTargetNode} where possible
 */
public class PropertyNode<V> extends SettingNode<V> {

    public PropertyNode(String name, Property<V> property, Class<V> type, TreeNode... children) {
        super(name, new EditableProperty<>(type, name, property, Editors.getDefaultEditorFactory(type)), children);
        this.setCanReset(false);
    }

    public PropertyNode(String name, Property<V> property, V defaultValue, Class<V> type, TreeNode... children) {
        super(name, new EditableProperty<>(type, name, property, Editors.getDefaultEditorFactory(type)).setDefaultSupplier(() -> defaultValue), children);
    }

    public PropertyNode(String name, Property<V> property, Supplier<V> defaultValueSupplier, Class<V> type, TreeNode... children) {
        super(name, new EditableProperty<>(type, name, property, Editors.getDefaultEditorFactory(type)).setDefaultSupplier(defaultValueSupplier), children);
    }

    public PropertyNode<V> setEditable(boolean editable) {
        return (PropertyNode<V>) super.setEditable(editable);
    }

}
