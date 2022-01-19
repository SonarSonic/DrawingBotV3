package drawingbot.files.presets.types;

import drawingbot.DrawingBotV3;
import drawingbot.files.presets.AbstractPresetLoader;
import drawingbot.files.presets.PresetType;
import drawingbot.registry.MasterRegistry;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PresetPFMSettingsLoader extends AbstractPresetLoader<PresetPFMSettings> {

    public PresetPFMSettingsLoader(PresetType presetType) {
        super(PresetPFMSettings.class, presetType, "user_pfm_presets.json");
    }

    @Override
    public PresetPFMSettings getPresetInstance(GenericPreset<PresetPFMSettings> preset) {
        return new PresetPFMSettings();
    }

    @Override
    public void registerPreset(GenericPreset<PresetPFMSettings> preset) {
        MasterRegistry.INSTANCE.registerPFMPreset(preset);
    }

    @Override
    public void unregisterPreset(GenericPreset<PresetPFMSettings> preset) {
        MasterRegistry.INSTANCE.pfmPresets.get(preset.presetSubType).remove(preset);
    }

    @Override
    public GenericPreset<PresetPFMSettings> updatePreset(GenericPreset<PresetPFMSettings> preset) {
        preset.presetSubType = DrawingBotV3.INSTANCE.pfmFactory.get().getName();
        preset.data.settingList = GenericSetting.toJsonMap(MasterRegistry.INSTANCE.getObservablePFMSettingsList(), new HashMap<>(), false);
        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<PresetPFMSettings> preset) {
        DrawingBotV3.INSTANCE.pfmFactory.set(MasterRegistry.INSTANCE.getPFMFactory(preset.presetSubType));
        GenericSetting.applySettings(preset.data.settingList, MasterRegistry.INSTANCE.getObservablePFMSettingsList());
    }

    @Override
    public GenericPreset<PresetPFMSettings> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPFMPreset();
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        List<GenericPreset<?>> userCreated = new ArrayList<>();
        for (Map.Entry<String, ObservableList<GenericPreset<PresetPFMSettings>>> entry : MasterRegistry.INSTANCE.pfmPresets.entrySet()) {
            for (GenericPreset<PresetPFMSettings> preset : entry.getValue()) {
                if (preset.userCreated) {
                    userCreated.add(preset);
                }
            }
        }
        return userCreated;
    }
}
