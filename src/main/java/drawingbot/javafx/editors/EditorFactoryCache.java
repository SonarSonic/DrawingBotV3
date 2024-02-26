package drawingbot.javafx.editors;

import java.util.LinkedHashMap;

/**
 * Provides a simple way to keep track of multiple {@link IEditor} instances and their linked {@link IEditableProperty}s
 * Editors shouldn't be created unnecessarily as this can cause many bindings to individual {@link IEditableProperty} which can cause eventual slow down / memory leaks.
 *
 * Instead this will manage editor instances and also handle their disposal when needed, by calling {@link #disposeCache()}
 */
public class EditorFactoryCache {

    public final EditorContext context;
    public EditorFactoryCache(EditorContext context){
        this.context = context;
    }

    public LinkedHashMap<IEditableProperty<?>, IEditor<?>> editors = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    public <V> IEditor<V> getOrCreateEditor(IEditableProperty<V> property){
        return (IEditor<V>) editors.computeIfAbsent(property, property1 -> property1.createEditor(context));
    }

    public <V> IEditorFactory<V> asEditorFactory(IEditableProperty<V> property){
        return (c, p) -> getOrCreateEditor(property);
    }

    public void disposeCache(){
        editors.values().forEach(IEditor::dispose);
        editors.clear();
    }
}
