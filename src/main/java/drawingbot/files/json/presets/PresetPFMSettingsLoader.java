package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetType;
import drawingbot.files.json.projects.DBTaskContext;
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
        setDefaultManager(new PresetPFMSettingsManager(this) {
            @Override
            public Property<PFMFactory<?>> pfmProperty(DBTaskContext context) {
                return context.project().getPFMSettings().factoryProperty();
            }

            @Override
            public Property<ObservableList<GenericSetting<?, ?>>> settingProperty(DBTaskContext context) {
                return context.project().getPFMSettings().settingsProperty();
            }
        });
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
    public GenericPreset<PresetPFMSettings> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, MasterRegistry.INSTANCE.getDefaultPFM().getRegistryName(), "Default");
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
