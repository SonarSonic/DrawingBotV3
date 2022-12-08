package drawingbot.files.json.presets;

import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.MasterRegistry;

public class PresetUISettingsLoader extends AbstractPresetLoader<PresetUISettings> {

    public PresetUISettingsLoader(PresetType presetType) {
        super(PresetUISettings.class, presetType, "ui_presets.json");
        setDefaultManager(new PresetUISettingsManager(this));
    }

    @Override
    public GenericPreset<PresetUISettings> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "Default");
    }

    @Override
    protected PresetUISettings getPresetInstance(GenericPreset<PresetUISettings> preset) {
        return new PresetUISettings();
    }
}
