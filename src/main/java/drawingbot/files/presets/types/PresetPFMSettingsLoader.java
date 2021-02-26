package drawingbot.files.presets.types;

import drawingbot.DrawingBotV3;
import drawingbot.files.presets.AbstractPresetLoader;
import drawingbot.pfm.PFMMasterRegistry;
import drawingbot.utils.EnumJsonType;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PresetPFMSettingsLoader extends AbstractPresetLoader<PresetPFMSettings> {

    public PresetPFMSettingsLoader() {
        super(PresetPFMSettings.class, EnumJsonType.PFM_PRESET, "user_pfm_presets.json");
    }

    @Override
    public PresetPFMSettings getPresetInstance(GenericPreset<PresetPFMSettings> preset) {
        return new PresetPFMSettings();
    }

    @Override
    public void registerPreset(GenericPreset<PresetPFMSettings> preset) {
        PFMMasterRegistry.registerPreset(preset);
    }

    @Override
    public void unregisterPreset(GenericPreset<PresetPFMSettings> preset) {
        PFMMasterRegistry.pfmPresets.get(preset.presetSubType).remove(preset);
    }

    @Override
    public GenericPreset<PresetPFMSettings> updatePreset(GenericPreset<PresetPFMSettings> preset) {
        preset.presetSubType = DrawingBotV3.pfmFactory.get().getName();
        preset.data.settingList = GenericSetting.toJsonMap(PFMMasterRegistry.getObservablePFMSettingsList(), new HashMap<>());
        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<PresetPFMSettings> preset) {
        GenericSetting.applySettings(preset.data.settingList, PFMMasterRegistry.getObservablePFMSettingsList());
    }

    @Override
    public GenericPreset<PresetPFMSettings> getDefaultPreset() {
        return PFMMasterRegistry.getDefaultPFMPreset();
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        List<GenericPreset<?>> userCreated = new ArrayList<>();
        for (Map.Entry<String, ObservableList<GenericPreset<PresetPFMSettings>>> entry : PFMMasterRegistry.pfmPresets.entrySet()) {
            for (GenericPreset<PresetPFMSettings> preset : entry.getValue()) {
                if (preset.userCreated) {
                    userCreated.add(preset);
                }
            }
        }
        return userCreated;
    }
}
