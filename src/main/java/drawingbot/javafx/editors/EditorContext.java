package drawingbot.javafx.editors;

/**
 * Provides useful context information such as styling hints and owner information to the {@link IEditor}
 * Will be extended in the future as needed.
 */
public class EditorContext {

    public Object owner;
    public EditorStyle style;

    public EditorContext(Object owner, EditorStyle style) {
        this.owner = owner;
        this.style = style;
    }
}
