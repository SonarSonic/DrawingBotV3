package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.files.exporters.GCodeBuilder;
import drawingbot.files.exporters.GCodeExporter;
import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.DefaultPresetManager;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.plotting.canvas.ObservableCanvas;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.EnumScalingMode;
import drawingbot.utils.UnitsLength;

import java.util.List;

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
