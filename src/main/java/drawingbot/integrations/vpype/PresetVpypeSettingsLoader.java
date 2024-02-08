package drawingbot.integrations.vpype;

import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.MasterRegistry;

import java.util.Optional;

public class PresetVpypeSettingsLoader extends AbstractPresetLoader<PresetVpypeSettings> {

    public PresetVpypeSettingsLoader(PresetType presetType) {
        super(PresetVpypeSettings.class, presetType, "user_vpype_presets.json");
    }

    @Override
    public GenericPreset<PresetVpypeSettings> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "Default");
    }

    @Override
    public PresetVpypeSettings createDataInstance(GenericPreset<PresetVpypeSettings> preset) {
        return new PresetVpypeSettings();
    }

    public static GenericPreset<PresetVpypeSettings> getPresetOrDefault(String presetName){
        Optional<GenericPreset<PresetVpypeSettings>> preset = VpypePlugin.PRESET_LOADER_VPYPE_SETTINGS.presets.stream().filter(p -> p.getPresetName().equals(presetName)).findFirst();
        return preset.orElseGet(() -> VpypePlugin.PRESET_LOADER_VPYPE_SETTINGS.getDefaultPreset());
    }
}
