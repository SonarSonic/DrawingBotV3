package drawingbot.integrations.vpype;

import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.DefaultPresetManager;
import drawingbot.files.json.PresetType;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.registry.MasterRegistry;

import java.util.Optional;

public class PresetVpypeSettingsLoader extends AbstractPresetLoader<PresetVpypeSettings> {

    public PresetVpypeSettingsLoader(PresetType presetType) {
        super(PresetVpypeSettings.class, presetType, "user_vpype_presets.json");
        setDefaultManager(new DefaultPresetManager<>(this) {
            @Override
            public void registerDataLoaders() {
                registerSetting(GenericSetting.createStringSetting(VpypeSettings.class, "vPypeCommand", "show", (settings, value) -> settings.vpypeCommand.setValue(value)).setGetter(settings -> settings.vpypeCommand.getValue()));
                registerSetting(GenericSetting.createBooleanSetting(VpypeSettings.class, "vPypeBypassOptimisation", false, (settings, value) -> settings.vpypeBypassOptimisation.setValue(value)).setGetter(settings -> settings.vpypeBypassOptimisation.getValue()));
            }

            @Override
            public VpypeSettings getInstance(DBTaskContext context) {
                return VpypePlugin.INSTANCE.vpypeSettings;
            }
        });
    }

    @Override
    public GenericPreset<PresetVpypeSettings> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "Default");
    }

    @Override
    protected PresetVpypeSettings getPresetInstance(GenericPreset<PresetVpypeSettings> preset) {
        return new PresetVpypeSettings();
    }

    public static GenericPreset<PresetVpypeSettings> getPresetOrDefault(String presetName){
        Optional<GenericPreset<PresetVpypeSettings>> preset = VpypePlugin.PRESET_LOADER_VPYPE_SETTINGS.presets.stream().filter(p -> p.getPresetName().equals(presetName)).findFirst();
        return preset.orElseGet(() -> VpypePlugin.PRESET_LOADER_VPYPE_SETTINGS.getDefaultPreset());
    }
}
