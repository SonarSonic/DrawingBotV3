package drawingbot.javafx.editors;

/**
 * Simple base class for creating {@link IEditor} classes from
 */
public abstract class EditorBase<V> implements IEditor<V>{

    protected final EditorContext context;
    protected final IEditableProperty<V> property;

    public EditorBase(EditorContext context, IEditableProperty<V> property){
        this.context = context;
        this.property = property;
    }

    @Override
    public final EditorContext context() {
        return context;
    }

    @Override
    public final IEditableProperty<V> getProperty() {
        return property;
    }
}
