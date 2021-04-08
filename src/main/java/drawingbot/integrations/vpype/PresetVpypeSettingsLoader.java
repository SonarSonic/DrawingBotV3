package drawingbot.integrations.vpype;

import drawingbot.DrawingBotV3;
import drawingbot.files.presets.AbstractSettingsLoader;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.utils.EnumJsonType;

import java.util.Optional;

public class PresetVpypeSettingsLoader extends AbstractSettingsLoader<PresetVpypeSettings> {

    public PresetVpypeSettingsLoader() {
        super(PresetVpypeSettings.class, EnumJsonType.VPYPE_SETTINGS, "user_vpype_presets.json");
    }

    public void registerSettings(){
        registerSetting(GenericSetting.createStringSetting(DrawingBotV3.class, "vPypeCommand", "show", false, (app, value) -> app.vPypeCommand.setValue(value)).setGetter(app -> app.vPypeCommand.getValue()));
        registerSetting(GenericSetting.createBooleanSetting(DrawingBotV3.class, "vPypeBypassOptimisation", false, false, (app, value) -> app.vPypeBypassOptimisation.setValue(value)).setGetter(app -> app.vPypeBypassOptimisation.getValue()));
    }

    @Override
    public GenericPreset<PresetVpypeSettings> getDefaultPreset() {
        return presets.stream().filter(p -> p.presetName.equals("Default")).findFirst().orElse(null);
    }

    @Override
    protected PresetVpypeSettings getPresetInstance(GenericPreset<PresetVpypeSettings> preset) {
        return new PresetVpypeSettings();
    }

    public static GenericPreset<PresetVpypeSettings> getPresetOrDefault(String presetName){
        Optional<GenericPreset<PresetVpypeSettings>> preset = JsonLoaderManager.VPYPE_SETTINGS.presets.stream().filter(p -> p.presetName.equals(presetName)).findFirst();
        return preset.orElseGet(JsonLoaderManager.VPYPE_SETTINGS::getDefaultPreset);
    }
}
