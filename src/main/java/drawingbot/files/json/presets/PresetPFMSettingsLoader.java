package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetType;
import drawingbot.pfm.PFMFactory;
import drawingbot.registry.MasterRegistry;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;

import java.util.*;

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
        DrawingBotV3.logger.finest("Registering PFM Preset: " + preset.presetName);
        super.registerPreset(preset);
    }

    @Override
    public void unregisterPreset(GenericPreset<PresetPFMSettings> preset) {
        DrawingBotV3.logger.finest("Unregistering PFM Preset: " + preset.presetName);
        super.unregisterPreset(preset);
    }

    @Override
    public GenericPreset<PresetPFMSettings> updatePreset(GenericPreset<PresetPFMSettings> preset) {
        preset.presetSubType = DrawingBotV3.INSTANCE.pfmFactory.get().getName();
        preset.data.settingList = GenericSetting.toJsonMap(MasterRegistry.INSTANCE.getObservablePFMSettingsList(), new HashMap<>(), false);
        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<PresetPFMSettings> preset) {
        applyPreset(preset, DrawingBotV3.INSTANCE.pfmFactory, MasterRegistry.INSTANCE.getObservablePFMSettingsList());
    }

    public void applyPreset(GenericPreset<PresetPFMSettings> preset, Property<PFMFactory<?>> factoryProperty, ObservableList<GenericSetting<?, ?>> settingsList) {
        factoryProperty.setValue(MasterRegistry.INSTANCE.getPFMFactory(preset.presetSubType));
        GenericSetting.applySettings(preset.data.settingList, settingsList);
    }

    @Override
    public GenericPreset<PresetPFMSettings> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, DrawingBotV3.INSTANCE.pfmFactory.get().getName(), "Default");
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        List<GenericPreset<?>> userCreated = new ArrayList<>();
        for (Map.Entry<String, ObservableList<GenericPreset<PresetPFMSettings>>> entry : presetsByType.entrySet()) {
            for (GenericPreset<PresetPFMSettings> preset : entry.getValue()) {
                if (preset.userCreated) {
                    userCreated.add(preset);
                }
            }
        }
        return userCreated;
    }


}
