package drawingbot.files.json.presets;

import drawingbot.files.json.*;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.MasterRegistry;

public class PresetPFMSettingsLoader extends AbstractPresetLoader<PresetData> {

    public PresetPFMSettingsLoader(PresetType presetType) {
        super(PresetData.class, presetType, "user_pfm_presets.json");
    }

    @Override
    public PresetData createDataInstance(GenericPreset<PresetData> preset) {
        return new PresetData();
    }

    @Override
    public GenericPreset<PresetData> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPresetWithFallback(this, MasterRegistry.INSTANCE.getDefaultPFM().getRegistryName(), "Default", true);
    }

}