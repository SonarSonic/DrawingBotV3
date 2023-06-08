package drawingbot.files.json.presets;

import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.editors.TreeNode;
import drawingbot.pfm.PFMFactory;
import drawingbot.registry.MasterRegistry;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public abstract class PresetPFMSettingsManager extends AbstractPresetManager<PresetPFMSettings> {

    public PresetPFMSettingsManager(PresetPFMSettingsLoader presetLoader) {
        super(presetLoader);
    }

    public abstract Property<PFMFactory<?>> pfmProperty(DBTaskContext context);

    public abstract Property<ObservableList<GenericSetting<?, ?>>> settingProperty(DBTaskContext context);

    @Override
    public GenericPreset<PresetPFMSettings> updatePreset(DBTaskContext context, GenericPreset<PresetPFMSettings> preset) {
        PFMFactory<?> pfm = pfmProperty(context).getValue();
        ObservableList<GenericSetting<?, ?>> settings = settingProperty(context).getValue();
        if(pfm != null && settings != null) {
            preset.setPresetSubType(pfm.getRegistryName());
            preset.data.settingList = GenericSetting.toJsonMap(settings, new HashMap<>(), false);
        }
        return preset;
    }

    @Override
    public void applyPreset(DBTaskContext context, GenericPreset<PresetPFMSettings> preset) {
        pfmProperty(context).setValue(MasterRegistry.INSTANCE.getPFMFactory(preset.getPresetSubType()));
        Property<ObservableList<GenericSetting<?, ?>>> settings = settingProperty(context);
        GenericSetting.applySettings(preset.data.settingList, settings.getValue());
    }

    @Override
    public void addEditDialogElements(GenericPreset<PresetPFMSettings> preset, ObservableList<TreeNode> builder, List<Consumer<GenericPreset<PresetPFMSettings>>> callbacks) {
        super.addEditDialogElements(preset, builder, callbacks);

        /*

        builder.add(new LabelNode("Settings").setTitleStyling());
        List<GenericSetting<?, ?>> tempSettings = MasterRegistry.INSTANCE.getNewPFMSettingsList(MasterRegistry.INSTANCE.getPFMFactory(preset.getPresetSubType()));

        for(GenericSetting<?, ?> setting : tempSettings){
            if(setting.getBindingFactory() != null){
                setting.getBindingFactory().accept(setting, tempSettings);
            }
        }
        GenericSetting.applySettings(preset.data.settingList, tempSettings);
        for(GenericSetting<?, ?> setting :tempSettings){
            builder.add(new SettingNode(setting));
        }
        callbacks.add(save -> {
            save.data.settingList = GenericSetting.toJsonMap(tempSettings, new HashMap<>(), false);
        });

         */
    }
}