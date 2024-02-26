package drawingbot.javafx.editors;

import javafx.scene.Node;

/**
 * Represents a control to edit a {@link IEditableProperty} which will typically be a {@link drawingbot.javafx.GenericPreset}
 * Editors should be instanced uniquely and support multiple instances, they should not be re-used.
 *
 * Editors are created by an {@link IEditorFactory} which can be provided by the {@link IEditableProperty#createEditor(EditorContext)} method
 * @param <V>
 */
public interface IEditor<V> {

    /**
     * @return the {@link EditorContext} the editor was created by, which provides hints for styling and links back to the owner of the editor, if further custom styling is required
     */
    EditorContext context();

    /**
     * @return the {@link IEditableProperty} this {@link IEditor} is bound to
     */
    IEditableProperty<V> getProperty();

    /**
     * @return the JFX node representation of this {@link IEditor}, which will be used in the UI
     */
    Node getNode();

    /**
     * Called when the {@link IEditor} is no longer required, all bindings and JFX nodes should be unbound and destroyed here to avoid memory leaks
     */
    void dispose();
}
