package drawingbot.integrations.vpype;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.DefaultPresetManager;
import drawingbot.files.json.PresetType;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;

import java.util.Optional;

public class PresetVpypeSettingsLoader extends AbstractPresetLoader<PresetVpypeSettings> {

    public PresetVpypeSettingsLoader(PresetType presetType) {
        super(PresetVpypeSettings.class, presetType, "user_vpype_presets.json");
        setDefaultManager(new DefaultPresetManager<>(this) {
            @Override
            public void registerDataLoaders() {
                registerSetting(GenericSetting.createStringSetting(VpypeSettings.class, "vPypeCommand", "show", (settings, value) -> settings.vPypeCommand.setValue(value)).setGetter(settings -> settings.vPypeCommand.getValue()));
                registerSetting(GenericSetting.createBooleanSetting(VpypeSettings.class, "vPypeBypassOptimisation", false, (settings, value) -> settings.vPypeBypassOptimisation.setValue(value)).setGetter(settings -> settings.vPypeBypassOptimisation.getValue()));
            }

            @Override
            public VpypeSettings getInstance(DBTaskContext context) {
                return DrawingBotV3.INSTANCE.vpypeSettings;
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
        Optional<GenericPreset<PresetVpypeSettings>> preset = Register.PRESET_LOADER_VPYPE_SETTINGS.presets.stream().filter(p -> p.getPresetName().equals(presetName)).findFirst();
        return preset.orElseGet(() -> Register.PRESET_LOADER_VPYPE_SETTINGS.getDefaultPreset());
    }
}
