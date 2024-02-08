package drawingbot.integrations.vpype;

import drawingbot.files.json.DefaultPresetManager;
import drawingbot.files.json.IPresetLoader;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericSetting;

public class PresetVpypeSettingsManager extends DefaultPresetManager<VpypeSettings, PresetVpypeSettings> {

    public PresetVpypeSettingsManager(IPresetLoader<PresetVpypeSettings> presetLoader) {
        super(presetLoader, VpypeSettings.class);
    }

    @Override
    public void registerDataLoaders() {
        registerSetting(GenericSetting.createStringSetting(VpypeSettings.class, "vPypeCommand", "show", (settings, value) -> settings.vpypeCommand.setValue(value)).setGetter(settings -> settings.vpypeCommand.getValue()));
        registerSetting(GenericSetting.createBooleanSetting(VpypeSettings.class, "vPypeBypassOptimisation", false, (settings, value) -> settings.vpypeBypassOptimisation.setValue(value)).setGetter(settings -> settings.vpypeBypassOptimisation.getValue()));
    }

    @Override
    public VpypeSettings getTargetFromContext(DBTaskContext context) {
        return VpypePlugin.INSTANCE.vpypeSettings;
    }
}
