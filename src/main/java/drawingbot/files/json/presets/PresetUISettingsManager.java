package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.files.exporters.GCodeBuilder;
import drawingbot.files.exporters.GCodeExporter;
import drawingbot.files.exporters.GCodeSettings;
import drawingbot.files.json.DefaultPresetManager;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.plotting.canvas.ObservableCanvas;
import drawingbot.registry.Register;
import drawingbot.utils.EnumOrientation;
import drawingbot.utils.EnumScalingMode;
import drawingbot.utils.UnitsLength;
import javafx.scene.paint.Color;

import java.util.List;

public class PresetUISettingsManager extends DefaultPresetManager<PresetUISettings, DrawingBotV3> {

    public PresetUISettingsManager(PresetUISettingsLoader presetLoader) {
        super(presetLoader);
    }

    @Override
    public void registerSettings() {
        registerSetting(GenericSetting.createOptionSetting(DrawingBotV3.class, EnumBlendMode.class, "blendMode", List.of(EnumBlendMode.values()), EnumBlendMode.NORMAL, (settings, value) -> settings.blendMode.set(value)).setGetter(settings -> settings.blendMode.get()));
        registerSetting(GenericSetting.createColourSetting(DrawingBotV3.class, "canvasColor", Color.WHITE, (settings, value) -> settings.drawingArea.canvasColor.set(value)).setGetter(settings -> settings.drawingArea.canvasColor.get()));
        registerSetting(GenericSetting.createColourSetting(DrawingBotV3.class, "backgroundColor", ObservableCanvas.backgroundColourDefault, (settings, value) -> settings.drawingArea.backgroundColor.set(value)).setGetter(settings -> settings.drawingArea.backgroundColor.get()));
        
    }

    @Override
    public DrawingBotV3 getInstance() {
        return DrawingBotV3.INSTANCE;
    }
}