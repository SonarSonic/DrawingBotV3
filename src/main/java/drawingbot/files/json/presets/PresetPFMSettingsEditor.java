package drawingbot.files.json.presets;

import com.google.gson.JsonElement;
import drawingbot.DrawingBotV3;
import drawingbot.files.json.DefaultPresetEditor;
import drawingbot.files.json.IPresetManager;
import drawingbot.files.json.PresetData;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.controls.ControlPFMSettingsEditor;
import drawingbot.javafx.editors.EditorContext;
import drawingbot.javafx.preferences.items.AbstractPropertyNode;
import drawingbot.javafx.preferences.items.TreeNode;
import drawingbot.javafx.util.JFXUtils;
import drawingbot.pfm.PFMSettings;
import drawingbot.registry.MasterRegistry;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;

import java.util.HashMap;

public class PresetPFMSettingsEditor extends DefaultPresetEditor<PFMSettings, PresetData> {

    public final PFMSettings pfmSettings = new PFMSettings();

    private ControlPFMSettingsEditor controlPFMSettingsEditor;

    public PresetPFMSettingsEditor(IPresetManager<PFMSettings, PresetData> manager) {
        super(manager);
    }

    @Override
    public void init(TreeNode editorNode) {
        super.init(editorNode);
        ChangeListener<String> subTypeChangeListener = (observable, oldValue, newValue) -> {
            //Save the current preset settings
            HashMap<String, JsonElement> map = GenericSetting.toJsonMap(pfmSettings.getSettings(), new HashMap<>(), false);

            pfmSettings.setPFMFactory(MasterRegistry.INSTANCE.getPFMFactory(newValue));

            //Reapply the preset settings to the new PFM, if settings are the same they will be caried across
            GenericSetting.applySettings(map, pfmSettings.getSettings());
        };

        JFXUtils.subscribeListener(editingPresetProperty(), (observable, oldValue, newValue) -> {
            if(oldValue != null){
                oldValue.presetSubTypeProperty().removeListener(subTypeChangeListener);
            }
            if (newValue != null) {
                manager.applyPreset(DrawingBotV3.context(), pfmSettings, newValue, false);
                newValue.presetSubTypeProperty().addListener(subTypeChangeListener);
            }
        });

        if (!isDetailed()) {
            return;
        }

        controlPFMSettingsEditor = new ControlPFMSettingsEditor();
        controlPFMSettingsEditor.pfmSettingsProperty().set(pfmSettings);
        controlPFMSettingsEditor.activePresetProperty().bind(selectedPresetProperty());

        editorNode.getChildren().add(new AbstractPropertyNode("PFM Settings") {
            @Override
            public Node getEditorNode(EditorContext context) {
                return controlPFMSettingsEditor;
            }

            @Override
            public void resetProperty() {
                controlPFMSettingsEditor.resetSettings();
            }

            @Override
            public String asString() {
                return pfmSettings.toString();
            }

            @Override
            public Observable[] getDependencies() {
                return new Observable[0];
            }
        });


    }

    @Override
    public void updatePreset() {
        super.updatePreset();
        manager.updatePreset(DrawingBotV3.context(), pfmSettings, getEditingPreset());
    }
}