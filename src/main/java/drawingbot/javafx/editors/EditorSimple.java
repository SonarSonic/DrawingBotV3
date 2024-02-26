package drawingbot.javafx.editors;

import javafx.scene.Node;

/**
 * Basic extendable class for creating {@link IEditor} which only require only simple node to function
 */
public abstract class EditorSimple<V, N extends Node> extends EditorBase<V>{

    protected final N node;

    public EditorSimple(EditorContext context, IEditableProperty<V> property, N node){
        super(context, property);
        this.node = node;
    }

    @Override
    public final N getNode(){
        return node;
    }
}
