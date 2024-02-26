package drawingbot.files.json.presets;

import drawingbot.files.exporters.GCodeExporter;
import drawingbot.files.exporters.GCodeSettings;
import drawingbot.files.json.DefaultPresetEditor;
import drawingbot.files.json.IPresetManager;
import drawingbot.files.json.PresetData;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.preferences.items.*;
import drawingbot.registry.Register;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.fxmisc.easybind.EasyBind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PresetGCodeSettingsEditor extends DefaultPresetEditor<GCodeSettings, PresetData> {

    public List<GenericSetting<?, ?>> editingSettings;
    public ObjectProperty<GCodeSettings> target = new SimpleObjectProperty<>();
    
    public PresetGCodeSettingsEditor(IPresetManager<GCodeSettings, PresetData> manager) {
        super(manager);
    }

    @Override
    public void init(TreeNode editorNode) {
        super.init(editorNode);

        editingSettings = new ArrayList<>();

        editorNode.getChildren().add(new LabelNode("Layout").setTitleStyling());
        editorNode.getChildren().add(new SettingNode<>("Units", collectSetting(Register.PRESET_MANAGER_GCODE_SETTINGS.gcodeUnits.copy())));
        editorNode.getChildren().add(new SettingNode<>("X Offset", collectSetting(Register.PRESET_MANAGER_GCODE_SETTINGS.gcodeOffsetX.copy())));
        editorNode.getChildren().add(new SettingNode<>("Y Offset", collectSetting(Register.PRESET_MANAGER_GCODE_SETTINGS.gcodeOffsetY.copy())));
        editorNode.getChildren().add(new SettingNode<>("Curve Flattening", collectSetting(Register.PRESET_MANAGER_GCODE_SETTINGS.gcodeEnableFlattening.copy())));
        editorNode.getChildren().add(new SettingNode<>( "Curve Flatness", collectSetting(Register.PRESET_MANAGER_GCODE_SETTINGS.gcodeCurveFlatness.copy())));
        editorNode.getChildren().add(new SettingNode<>("Center Zero Point", collectSetting(Register.PRESET_MANAGER_GCODE_SETTINGS.gcodeCenterZeroPoint.copy())));
        editorNode.getChildren().add(new SettingNode<>("Comment Type", collectSetting(Register.PRESET_MANAGER_GCODE_SETTINGS.gcodeCommentType.copy())));
        editorNode.getChildren().add(new LabelNode("Custom GCode").setTitleStyling());
        editorNode.getChildren().add(new SettingNode<>("Start", collectSetting(Register.PRESET_MANAGER_GCODE_SETTINGS.gcodeStartCode.copy())));
        editorNode.getChildren().add(new SettingNode<>("End", collectSetting(Register.PRESET_MANAGER_GCODE_SETTINGS.gcodeEndCode.copy())));
        editorNode.getChildren().add(new SettingNode<>("Pen Down", collectSetting(Register.PRESET_MANAGER_GCODE_SETTINGS.gcodePenDownCode.copy())));
        editorNode.getChildren().add(new SettingNode<>("Pen Up", collectSetting(Register.PRESET_MANAGER_GCODE_SETTINGS.gcodePenUpCode.copy())));
        editorNode.getChildren().add(new LabelNode("").setTitleStyling());
        editorNode.getChildren().add(new LabelNode("With wildcards " + GCodeExporter.wildcards).setSubtitleStyling());
        editorNode.getChildren().add(new SettingNode<>("Start Pen Layer", collectSetting(Register.PRESET_MANAGER_GCODE_SETTINGS.gcodeStartLayerCode.copy())));
        editorNode.getChildren().add(new LabelNode("").setTitleStyling());
        editorNode.getChildren().add(new LabelNode("With wildcards " + GCodeExporter.wildcards).setSubtitleStyling());
        editorNode.getChildren().add(new SettingNode<>("End Pen Layer", collectSetting(Register.PRESET_MANAGER_GCODE_SETTINGS.gcodeEndLayerCode.copy())));

        EasyBind.subscribe(target, settings -> {
            if(settings != null){
                GenericSetting.updateSettingsFromInstance(editingSettings, settings);
            }
        });

        EasyBind.subscribe(editingPresetProperty(), preset -> {
            if(preset != null){
                GenericSetting.applySettings(preset.data.settings, editingSettings);
            }
        });


        InvalidationListener genericListener = observable -> {
            if(observable instanceof GenericSetting<?, ?> setting){
                setting.applySetting(target.get());
            }
        };
        editingSettings.forEach(prop -> prop.addListener(genericListener));

        GenericSetting.addBindings(editingSettings);

    }

    public <C,V> GenericSetting<C, V> collectSetting(GenericSetting<C, V> setting){
        editingSettings.add(setting);
        return setting;
    }

    @Override
    public void updatePreset() {
        super.updatePreset();
        getEditingPreset().data.settings = GenericSetting.toJsonMap(editingSettings, new HashMap<>(), false);
    }


}
