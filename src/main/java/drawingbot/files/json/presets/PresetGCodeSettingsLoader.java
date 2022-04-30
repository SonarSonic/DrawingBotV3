package drawingbot.files.json.presets;

import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.MasterRegistry;

public class PresetGCodeSettingsLoader extends AbstractPresetLoader<PresetGCodeSettings> {

    public PresetGCodeSettingsLoader(PresetType presetType) {
        super(PresetGCodeSettings.class, presetType, "user_gcode_presets.json");
        setDefaultManager(new PresetGCodeSettingsManager(this));
    }

    @Override
    public GenericPreset<PresetGCodeSettings> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "Default");
    }

    @Override
    protected PresetGCodeSettings getPresetInstance(GenericPreset<PresetGCodeSettings> preset) {
        return new PresetGCodeSettings();
    }
}
