package drawingbot.files.json;

import drawingbot.javafx.GenericPreset;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;

//TODO DOCUMENTATION

/**
 * A {@link IPresetEditor} is responsible for handling users edits to a given preset.
 * It provides all of the controls to edit the current editing preset.
 * Preset editors only need to be re-instanced when changing preset type, and should handle switching the editing preset
 */
public interface IPresetEditor<TARGET, DATA> {

    void init();

    void dispose();

    Class<DATA> getDataType();

    PresetType getPresetType();

    IPresetManager<TARGET, DATA> getPresetManager();

    IPresetLoader<DATA> getPresetLoader();


    Node getNode();

    ObjectProperty<Node> nodeProperty();

    void setNode(Node node);


    GenericPreset<DATA> getSelectedPreset();

    ObjectProperty<GenericPreset<DATA>> selectedPresetProperty();

    void setSelectedPreset(GenericPreset<DATA> selectedPreset);


    GenericPreset<DATA> getEditingPreset();

    ObjectProperty<GenericPreset<DATA>> editingPresetProperty();

    void setEditingPreset(GenericPreset<DATA> editingPreset);


    boolean isDetailed();

    BooleanProperty detailedProperty();

    void setDetailed(boolean detailed);

    GenericPreset<?> confirmEdit();

    void updatePreset();
}
