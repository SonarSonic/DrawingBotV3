package drawingbot.files.json;

import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.editors.LabelNode;
import drawingbot.javafx.editors.PropertyNode;
import drawingbot.javafx.editors.TreeNode;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;

import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractPresetManager<O extends IJsonData> implements IPresetManager<O> {

    public AbstractJsonLoader<O> presetLoader;

    public AbstractPresetManager(AbstractJsonLoader<O> presetLoader){
        this.presetLoader = presetLoader;
    }

    public PresetType getPresetType(){
        return presetLoader.type;
    }

    /**
     * updates the presets settings with the ones currently configured
     * @return the preset or null if the settings couldn't be saved
     */
    public abstract GenericPreset<O> updatePreset(DBTaskContext context, GenericPreset<O> preset, boolean loadingProject);

    /**
     * applies the presets settings
     */
    public abstract void applyPreset(DBTaskContext context, GenericPreset<O> preset, boolean loadingProject);


    public final GenericPreset<O> tryUpdatePreset(DBTaskContext context, GenericPreset<O> preset) {
        if (preset != null && preset.userCreated) {
            preset = updatePreset(context, preset, false);
            if (preset != null) {
                presetLoader.queueJsonUpdate();
                return preset;
            }
        }
        return null;
    }

    public final void tryApplyPreset(DBTaskContext context, GenericPreset<O> preset) {
        applyPreset(context, preset, false);
    }

    public boolean isSubTypeEditable(){
        return false;
    }

    public ObservableList<String> getObservableCategoryList(){
        return null;
    }

    public void addEditDialogElements(GenericPreset<O> preset, ObservableList<TreeNode> builder, List<Consumer<GenericPreset<O>>> callbacks) {
        builder.add(new LabelNode("Preset").setTitleStyling());
        builder.add(new PropertyNode("Category", preset.presetSubTypeProperty(), String.class){
            @Override
            public Node createEditor() {
                //Setup the special ComboBox to show a drop down for the Preset's Categories
                ObservableList<String> categoryList = getObservableCategoryList();
                if(categoryList != null && isSubTypeEditable()){
                    ComboBox<String> categoryComboBox = new ComboBox<>();
                    categoryComboBox.setEditable(true);
                    categoryComboBox.itemsProperty().set(categoryList);
                    categoryComboBox.valueProperty().bindBidirectional((Property<String>) property); //TODO WHY IS CATEGORY NOT SAVING?
                    return categoryComboBox;
                }
                return super.createEditor();
            }
        }.setEditable(isSubTypeEditable()));
        builder.add(new PropertyNode("Name", preset.presetNameProperty(), String.class));
    }

}
