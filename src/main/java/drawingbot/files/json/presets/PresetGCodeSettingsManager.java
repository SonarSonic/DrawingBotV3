package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.files.exporters.GCodeBuilder;
import drawingbot.files.exporters.GCodeExporter;
import drawingbot.files.exporters.GCodeSettings;
import drawingbot.files.json.DefaultPresetManager;
import drawingbot.javafx.GenericSetting;
import drawingbot.utils.UnitsLength;

import java.util.List;

public class PresetGCodeSettingsManager extends DefaultPresetManager<PresetGCodeSettings, GCodeSettings> {

    public PresetGCodeSettingsManager(PresetGCodeSettingsLoader presetLoader) {
        super(presetLoader);
    }

    @Override
    public void registerSettings() {
        registerSetting(GenericSetting.createRangedFloatSetting(GCodeSettings.class, "gcodeOffsetX", 0F, 0F, Float.MAX_VALUE, (settings, value) -> settings.gcodeOffsetX.set(value)).setGetter(settings -> settings.gcodeOffsetX.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(GCodeSettings.class, "gcodeOffsetY", 0F, 0F, Float.MAX_VALUE, (settings, value) -> settings.gcodeOffsetY.set(value)).setGetter(settings -> settings.gcodeOffsetY.get()));
        registerSetting(GenericSetting.createOptionSetting(GCodeSettings.class, UnitsLength.class, "gcodeUnits", List.of(UnitsLength.values()), UnitsLength.MILLIMETRES, (settings, value) -> settings.gcodeUnits.set(value)).setGetter(settings -> settings.gcodeUnits.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(GCodeSettings.class, "gcodeCurveFlatness", 0.1F, 0F, Float.MAX_VALUE, (settings, value) -> settings.gcodeCurveFlatness.set(value)).setGetter(settings -> settings.gcodeCurveFlatness.get()));
        registerSetting(GenericSetting.createBooleanSetting(GCodeSettings.class, "gcodeEnableFlattening", true, (settings, value) -> settings.gcodeEnableFlattening.set(value)).setGetter(settings -> settings.gcodeEnableFlattening.get()));
        registerSetting(GenericSetting.createBooleanSetting(GCodeSettings.class, "gcodeCenterZeroPoint", false, (settings, value) -> settings.gcodeCenterZeroPoint.set(value)).setGetter(settings -> settings.gcodeCenterZeroPoint.get()));
        registerSetting(GenericSetting.createOptionSetting(GCodeSettings.class, GCodeBuilder.CommentType.class, "gcodeCommentType", List.of(GCodeBuilder.CommentType.values()), GCodeBuilder.CommentType.BRACKETS, (settings, value) -> settings.gcodeCommentType.set(value)).setGetter(settings -> settings.gcodeCommentType.get()));
        registerSetting(GenericSetting.createStringSetting(GCodeSettings.class, "gcodeStartCode", GCodeExporter.defaultStartCode, (settings, value) -> settings.gcodeStartCode.set(value)).setGetter(settings -> settings.gcodeStartCode.get()));
        registerSetting(GenericSetting.createStringSetting(GCodeSettings.class, "gcodeEndCode", GCodeExporter.defaultEndCode, (settings, value) -> settings.gcodeEndCode.set(value)).setGetter(settings -> settings.gcodeEndCode.get()));
        registerSetting(GenericSetting.createStringSetting(GCodeSettings.class, "gcodePenDownCode", GCodeExporter.defaultPenDownCode, (settings, value) -> settings.gcodePenDownCode.set(value)).setGetter(settings -> settings.gcodePenDownCode.get()));
        registerSetting(GenericSetting.createStringSetting(GCodeSettings.class, "gcodePenUpCode", GCodeExporter.defaultPenUpCode, (settings, value) -> settings.gcodePenUpCode.set(value)).setGetter(settings -> settings.gcodePenUpCode.get()));
        registerSetting(GenericSetting.createStringSetting(GCodeSettings.class, "gcodeStartLayerCode", GCodeExporter.defaultStartLayerCode, (settings, value) -> settings.gcodeStartLayerCode.set(value)).setGetter(settings -> settings.gcodeStartLayerCode.get()));
        registerSetting(GenericSetting.createStringSetting(GCodeSettings.class, "gcodeEndLayerCode", GCodeExporter.defaultEndLayerCode, (settings, value) -> settings.gcodeEndLayerCode.set(value)).setGetter(settings -> settings.gcodeEndLayerCode.get()));
    }

    @Override
    public GCodeSettings getInstance() {
        return DrawingBotV3.INSTANCE.gcodeSettings;
    }
}
