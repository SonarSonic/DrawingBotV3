package drawingbot.integrations.vpype;

import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetData;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.MasterRegistry;

import java.util.Optional;

public class PresetVpypeSettingsLoader extends AbstractPresetLoader<PresetData> {

    public PresetVpypeSettingsLoader(PresetType presetType) {
        super(PresetData.class, presetType, "user_vpype_presets.json");
    }

    @Override
    public GenericPreset<PresetData> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "Default");
    }

    @Override
    public PresetData createDataInstance(GenericPreset<PresetData> preset) {
        return new PresetData();
    }

    public static GenericPreset<PresetData> getPresetOrDefault(String presetName){
        Optional<GenericPreset<PresetData>> preset = VpypePlugin.PRESET_LOADER_VPYPE_SETTINGS.presets.stream().filter(p -> p.getPresetName().equals(presetName)).findFirst();
        return preset.orElseGet(() -> VpypePlugin.PRESET_LOADER_VPYPE_SETTINGS.getDefaultPreset());
    }
}
