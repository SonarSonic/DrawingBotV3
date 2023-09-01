package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetData;
import drawingbot.files.json.PresetType;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.pfm.PFMFactory;
import drawingbot.registry.MasterRegistry;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PresetPFMSettingsLoader extends AbstractPresetLoader<PresetData> {

    public PresetPFMSettingsLoader(PresetType presetType) {
        super(PresetData.class, presetType, "user_pfm_presets.json");
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
    public PresetData getPresetInstance(GenericPreset<PresetData> preset) {
        return new PresetData();
    }

    @Override
    public void registerPreset(GenericPreset<PresetData> preset) {
        DrawingBotV3.logger.finest("Registering PFM Preset: " + preset.getPresetName());
        super.registerPreset(preset);
    }

    @Override
    public void unregisterPreset(GenericPreset<PresetData> preset) {
        DrawingBotV3.logger.finest("Unregistering PFM Preset: " + preset.getPresetName());
        super.unregisterPreset(preset);
    }

    @Override
    public GenericPreset<PresetData> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, MasterRegistry.INSTANCE.getDefaultPFM().getRegistryName(), "Default");
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        List<GenericPreset<?>> userCreated = new ArrayList<>();
        for (Map.Entry<String, ObservableList<GenericPreset<PresetData>>> entry : presetsByType.entrySet()) {
            for (GenericPreset<PresetData> preset : entry.getValue()) {
                if (preset.userCreated) {
                    userCreated.add(preset);
                }
            }
        }
        return userCreated;
    }

}