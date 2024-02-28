package drawingbot.javafx.controls;

import drawingbot.files.json.IPresetEditor;
import drawingbot.files.json.IPresetManager;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.MasterRegistry;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * A UI Control for the universal editing of {@link GenericPreset}
 * It handles the creation and disposal of the required {@link IPresetEditor} for each preset
 *
 * It is only necessary to bind/set the selectedPreset and then calling the {@link #confirmEdit()} / {@link #resetEdit()}
 * The control works by first creating a duplicate of the selectedPreset which is stored in the {@link #editingPresetProperty()}
 */
public class ControlPresetEditor extends Control {

    public ControlPresetEditor(){
        this.editor.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                disposeEditor(oldValue);
            }
            if(newValue != null){
                initEditor(newValue);
            }
        });
        this.selectedPreset.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){

                //Prevent clashes with the existing editor
                setEditingPreset(null);

                if(!hasCustomEditor() && (getEditor() == null || getEditor().getPresetType() != newValue.getPresetType())){
                    IPresetManager<?, ?> manager = MasterRegistry.INSTANCE.getDefaultPresetManager(newValue);
                    IPresetEditor<?, ?> editor = manager.createPresetEditor();
                    setEditor(editor);
                }

                updateEditingPreset(newValue);
            }else{
                if(!hasCustomEditor()){
                    setEditor(null);
                }
                setEditingPreset(null);
            }
        });
    }

    public ControlPresetEditor(GenericPreset<?> preset){
        this();
        this.selectedPreset.set(preset);
    }

    public ControlPresetEditor(IPresetEditor<?, ?> editor){
        this();
        this.editor.set(editor);
    }

    private  <DATA> void updateEditingPreset(GenericPreset<DATA> selectedPreset) {
        if(selectedPreset == null){
            setEditingPreset(null);
            return;
        }
        setEditingPreset(new GenericPreset<>(selectedPreset));
    }

    private <DATA> void initEditor(IPresetEditor<?, DATA> editor){
        editor.selectedPresetProperty().bind(Bindings.createObjectBinding(() -> editor.getPresetManager().cast(selectedPreset.get()), selectedPreset));
        editor.editingPresetProperty().bind(Bindings.createObjectBinding(() -> editor.getPresetManager().cast(editingPreset.get()), editingPreset));
        editor.detailedProperty().bind(detailedProperty());

        if(!useTreeNode()){
            editor.init();
        }
    }

    private void disposeEditor(IPresetEditor<?, ?> editor){
        editor.selectedPresetProperty().unbind();
        editor.editingPresetProperty().unbind();
        editor.detailedProperty().unbind();
        editor.dispose();
    }

    public void resetEdit(){
        updateEditingPreset(getSelectedPreset());
    }

    public GenericPreset<?> confirmEdit(boolean newPreset){
        if(getEditor() != null){
            return getEditor().confirmEdit(newPreset);
        }
        return null;
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<GenericPreset<?>> selectedPreset = new SimpleObjectProperty<>();

    public GenericPreset<?> getSelectedPreset() {
        return selectedPreset.get();
    }

    public ObjectProperty<GenericPreset<?>> selectedPresetProperty() {
        return selectedPreset;
    }

    public void setSelectedPreset(GenericPreset<?> selectedPreset) {
        this.selectedPreset.set(selectedPreset);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<GenericPreset<?>> editingPreset = new SimpleObjectProperty<>();

    public GenericPreset<?> getEditingPreset() {
        return editingPreset.get();
    }

    public ObjectProperty<GenericPreset<?>> editingPresetProperty() {
        return editingPreset;
    }

    public void setEditingPreset(GenericPreset<?> editingPreset) {
        this.editingPreset.set(editingPreset);
    }

    ////////////////////////////////////////////////////////

    public BooleanProperty detailed = new SimpleBooleanProperty(false);

    public boolean isDetailed() {
        return detailed.get();
    }

    public BooleanProperty detailedProperty() {
        return detailed;
    }

    public void setDetailed(boolean detailed) {
        this.detailed.set(detailed);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<IPresetEditor<?, ?>> editor = new SimpleObjectProperty<>();

    public IPresetEditor<?, ?> getEditor() {
        return editor.get();
    }

    public ReadOnlyObjectProperty<IPresetEditor<?, ?>> editorProperty() {
        return editor;
    }

    private void setEditor(IPresetEditor<?, ?> editor) {
        this.editor.set(editor);
    }

    ////////////////////////////////////////////////////////

    public BooleanProperty customEditor = new SimpleBooleanProperty(false);

    public boolean hasCustomEditor() {
        return customEditor.get();
    }

    public BooleanProperty customEditorProperty() {
        return customEditor;
    }

    public void setCustomEditor(IPresetEditor<?, ?> customEditor) {
        this.customEditor.set(true);
        this.editor.set(customEditor);
    }

    ////////////////////////////////////////////////////////

    public BooleanProperty useTreeNode = new SimpleBooleanProperty(false);

    public boolean useTreeNode() {
        return useTreeNode.get();
    }

    public BooleanProperty useTreeNodeProperty() {
        return useTreeNode;
    }

    public void setUseTreeNode(boolean useTreeNode) {
        this.useTreeNode.set(useTreeNode);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SkinPresetEditor(this);
    }
}
