package drawingbot.files.json;

import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.controls.DialogSystemPresetDuplicate;
import drawingbot.javafx.editors.EditorContext;
import drawingbot.javafx.preferences.items.*;
import drawingbot.javafx.util.JFXUtils;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class DefaultPresetEditor<TARGET, DATA> implements IPresetEditor<TARGET, DATA> {

    public final IPresetManager<TARGET, DATA> manager;

    public DefaultPresetEditor(IPresetManager<TARGET, DATA> manager){
        this.manager = manager;
    }

    @Override
    public Class<DATA> getDataType(){
        return manager.getDataType();
    }

    @Override
    public PresetType getPresetType(){
        return manager.getPresetType();
    }

    @Override
    public IPresetManager<TARGET, DATA> getPresetManager(){
        return manager;
    }

    @Override
    public IPresetLoader<DATA> getPresetLoader(){
        return manager.getPresetLoader();
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<Node> node = new SimpleObjectProperty<>();

    public Node getNode() {
        return node.get();
    }

    public ObjectProperty<Node> nodeProperty() {
        return node;
    }

    public void setNode(Node node) {
        this.node.set(node);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<GenericPreset<DATA>> selectedPreset = new SimpleObjectProperty<>();

    @Override
    public GenericPreset<DATA> getSelectedPreset() {
        return selectedPreset.get();
    }

    @Override
    public ObjectProperty<GenericPreset<DATA>> selectedPresetProperty() {
        return selectedPreset;
    }

    @Override
    public void setSelectedPreset(GenericPreset<DATA> selectedPreset) {
        this.selectedPreset.set(selectedPreset);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<GenericPreset<DATA>> editingPreset = new SimpleObjectProperty<>();

    @Override
    public GenericPreset<DATA> getEditingPreset() {
        return editingPreset.get();
    }

    @Override
    public ObjectProperty<GenericPreset<DATA>> editingPresetProperty() {
        return editingPreset;
    }

    @Override
    public void setEditingPreset(GenericPreset<DATA> editingPreset) {
        this.editingPreset.set(editingPreset);
    }

    ////////////////////////////////////////////////////////

    public BooleanProperty detailed = new SimpleBooleanProperty(false);

    @Override
    public boolean isDetailed() {
        return detailed.get();
    }

    @Override
    public BooleanProperty detailedProperty() {
        return detailed;
    }

    @Override
    public void setDetailed(boolean detailed) {
        this.detailed.set(detailed);
    }

    ////////////////////////////////////////////////////////

    @Override
    public final void init(){
        PageNode pageNode = EditorSheet.page("editor");

        init(pageNode);

        Node content = pageNode.getContent();
        setNode(content);
        HBox.setHgrow(content, Priority.ALWAYS);
    }

    public void init(TreeNode editorNode){
        editorNode.getChildren().add(new LabelNode("Preset").setTitleStyling());
        if(!getPresetType().getSubTypeBehaviour().isIgnored()){
            SimpleStringProperty presetSubType = new SimpleStringProperty("");
            JFXUtils.createBiDirectionalSelectBinding(presetSubType, editingPreset, GenericPreset::presetSubTypeProperty);
            editorNode.getChildren().add(new PropertyNode<>("Category", presetSubType, String.class){
                @Override
                public Node getEditorNode(EditorContext context) {
                    //Setup the special ComboBox to show a drop down for the Preset's Categories
                    ObservableList<String> categoryList = getPresetLoader().getPresetSubTypes();
                    if(categoryList == null){
                        return super.getEditorNode(context);
                    }

                    PresetType.SubTypeBehaviour behaviour = getPresetType().getSubTypeBehaviour();
                    switch (behaviour){
                        case FIXED -> {
                            if(isDetailed()){ //only allow switching sub type when creating presets in the inspector
                                ChoiceBox<String> categoryChoiceBox = new ChoiceBox<>();
                                categoryChoiceBox.itemsProperty().set(categoryList);
                                categoryChoiceBox.valueProperty().bindBidirectional(property.valueProperty());
                                return categoryChoiceBox;
                            }
                        }
                        case EDITABLE -> {
                            ComboBox<String> categoryComboBox = new ComboBox<>();
                            categoryComboBox.setEditable(true);
                            categoryComboBox.itemsProperty().set(categoryList);
                            categoryComboBox.valueProperty().bindBidirectional(property.valueProperty());
                            return categoryComboBox;
                        }
                    }
                    return super.getEditorNode(context);
                }
            }.setEditable(isDetailed() || getPresetType().getSubTypeBehaviour().isEditable()));
        }

        //Preset Name
        SimpleStringProperty presetName = new SimpleStringProperty("");
        JFXUtils.createBiDirectionalSelectBinding(presetName, editingPreset, GenericPreset::presetNameProperty);
        editorNode.getChildren().add(new PropertyNode<>("Name", presetName, String.class));
    }

    @Override
    public final GenericPreset<?> confirmEdit(){
        updatePreset();
        if(selectedPreset.get().isSystemPreset()){
            DialogSystemPresetDuplicate.Result result = DialogSystemPresetDuplicate.openSystemPresetDialog(selectedPreset.get());
            GenericPreset<DATA> resultPreset = null;
            switch (result){
                case OVERRIDE -> {
                    resultPreset = manager.getPresetLoader().createOverridePreset(editingPreset.get());
                }
                case DUPLICATE -> {
                    resultPreset = manager.getPresetLoader().createEditablePreset(editingPreset.get());
                }
            }
            if(resultPreset != null){
                manager.getPresetLoader().addPreset(resultPreset);
                FXHelper.logPresetAction(resultPreset, "Created");
                return resultPreset;
            }
            return null;
        }
        GenericPreset<?> resultPreset = manager.getPresetLoader().editPreset(selectedPreset.get(), new GenericPreset<>(editingPreset.get()));
        FXHelper.logPresetAction(resultPreset, "Edited");
        return resultPreset;
    }

    @Override
    public void updatePreset(){
        //TODO
    }

    @Override
    public void dispose() {
        //TODO
    }
}
