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

public abstract class AbstractPresetManager<TARGET, DATA> implements IPresetManager<TARGET, DATA> {

    public final IPresetLoader<DATA> presetLoader;
    public final Class<TARGET> targetType;

    public AbstractPresetManager(IPresetLoader<DATA> presetLoader, Class<TARGET> targetType){
        this.presetLoader = presetLoader;
        this.targetType = targetType;
    }

    @Override
    public IPresetLoader<DATA> getPresetLoader() {
        return presetLoader;
    }

    @Override
    public Class<TARGET> getTargetType() {
        return targetType;
    }

    public PresetType getPresetType(){
        return presetLoader.getPresetType();
    }

    /**
     * updates the presets settings with the ones currently configured
     */
    public abstract void updatePreset(DBTaskContext context, TARGET target, GenericPreset<DATA> preset);

    /**
     * applies the presets settings
     */
    public abstract void applyPreset(DBTaskContext context, TARGET target, GenericPreset<DATA> preset, boolean changesOnly);


    //@Override TODO DELETE ME
    public final void tryApplyPreset(DBTaskContext context, TARGET target, GenericPreset<DATA> preset) {
        applyPreset(context, target, preset, false);
    }

    public boolean isSubTypeEditable(){
        return false;
    }

    public void addEditDialogElements(GenericPreset<DATA> preset, ObservableList<TreeNode> builder, List<Consumer<GenericPreset<DATA>>> callbacks) {
        builder.add(new LabelNode("Preset").setTitleStyling());
        builder.add(new PropertyNode("Category", preset.presetSubTypeProperty(), String.class){
            @Override
            public Node createEditor() {
                //Setup the special ComboBox to show a drop down for the Preset's Categories
                ObservableList<String> categoryList = getPresetLoader().getPresetSubTypes();
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
