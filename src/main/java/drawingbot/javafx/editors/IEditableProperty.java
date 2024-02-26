package drawingbot.javafx.editors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;

/**
 * Represents a property which can be edited by an {@link IEditor}, typically will be a {@link drawingbot.javafx.GenericSetting}
 * @param <V> the type of value stored in the property
 */
public interface IEditableProperty<V> {

    /**
     * @return the type of the value stored in the property
     */
    Class<V> getType();

    Property<V> valueProperty();

    V getDefaultValue();

    String getValueAsString();

    void resetValue();

    void randomiseValue();

    String getDisplayName();

    String getDescription();

    BooleanProperty disabledProperty();

    /**
     * @return the editor factory associated with this {@link IEditableProperty}, which can provide the required {@link IEditor} instances
     */
    IEditorFactory<V> getEditorFactory();

    default void sendUserEditedEvent(){

    }

    default IEditor<V> createEditor(EditorContext context){
        return getEditorFactory().createEditor(context, this);
    }
}
