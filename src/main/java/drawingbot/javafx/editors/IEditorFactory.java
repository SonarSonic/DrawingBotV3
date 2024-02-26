package drawingbot.javafx.editors;

/**
 * Editor factories are responsible for providing {@link IEditor} instances for {@link IEditableProperty}s
 * @param <V> the properties data type
 */
public interface IEditorFactory<V> {

    /**
     * Creates a new instance of an {@link IEditor} for a given property
     * @param context the context to generate the {@link IEditor} with
     * @param property the property to bind the {@link IEditor}  to
     * @return a new instance of a {@link IEditor}, should not be null
     */
    IEditor<V> createEditor(EditorContext context, IEditableProperty<V> property);

}
