package drawingbot.integrations.vpype;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.AbstractSettingsLoader;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;

import java.util.Optional;

public class PresetVpypeSettingsLoader extends AbstractSettingsLoader<PresetVpypeSettings> {

    public PresetVpypeSettingsLoader(PresetType presetType) {
        super(PresetVpypeSettings.class, presetType, "user_vpype_presets.json");
    }

    public void registerSettings(){
        registerSetting(GenericSetting.createStringSetting(DrawingBotV3.class, "vPypeCommand", "show", (app, value) -> app.vPypeCommand.setValue(value)).setGetter(app -> app.vPypeCommand.getValue()));
        registerSetting(GenericSetting.createBooleanSetting(DrawingBotV3.class, "vPypeBypassOptimisation", false, (app, value) -> app.vPypeBypassOptimisation.setValue(value)).setGetter(app -> app.vPypeBypassOptimisation.getValue()));
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
        Optional<GenericPreset<PresetVpypeSettings>> preset = Register.PRESET_LOADER_VPYPE_SETTINGS.presets.stream().filter(p -> p.presetName.equals(presetName)).findFirst();
        return preset.orElseGet(Register.PRESET_LOADER_VPYPE_SETTINGS::getDefaultPreset);
    }
}
