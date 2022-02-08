package drawingbot.files.presets.types;

import drawingbot.DrawingBotV3;
import drawingbot.files.exporters.GCodeBuilder;
import drawingbot.files.exporters.GCodeExporter;
import drawingbot.files.presets.AbstractSettingsLoader;
import drawingbot.files.presets.PresetType;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.UnitsLength;

import java.util.List;

public class PresetGCodeSettingsLoader extends AbstractSettingsLoader<PresetGCodeSettings> {

    public PresetGCodeSettingsLoader(PresetType presetType) {
        super(PresetGCodeSettings.class, presetType, "user_gcode_presets.json");
    }

    public void registerSettings(){
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "gcodeOffsetX", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.gcodeOffsetX.set(value)).setGetter(app -> app.gcodeOffsetX.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "gcodeOffsetY", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.gcodeOffsetY.set(value)).setGetter(app -> app.gcodeOffsetY.get()));
        registerSetting(GenericSetting.createOptionSetting(DrawingBotV3.class, "gcodeUnits", List.of(UnitsLength.values()), UnitsLength.MILLIMETRES, false, (app, value) -> app.gcodeUnits.set(value)).setGetter(app -> app.gcodeUnits.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "gcodeCurveFlatness", 0.1F, 0F, Float.MAX_VALUE, false, (app, value) -> app.gcodeCurveFlatness.set(value)).setGetter(app -> app.gcodeCurveFlatness.get()));
        registerSetting(GenericSetting.createBooleanSetting(DrawingBotV3.class, "gcodeEnableFlattening", true, false, (app, value) -> app.gcodeEnableFlattening.set(value)).setGetter(app -> app.gcodeEnableFlattening.get()));
        registerSetting(GenericSetting.createBooleanSetting(DrawingBotV3.class, "gcodeCenterZeroPoint", false, false, (app, value) -> app.gcodeCenterZeroPoint.set(value)).setGetter(app -> app.gcodeCenterZeroPoint.get()));
        registerSetting(GenericSetting.createOptionSetting(DrawingBotV3.class, "gcodeCommentType", List.of(GCodeBuilder.CommentType.values()), GCodeBuilder.CommentType.BRACKETS, false, (app, value) -> app.gcodeCommentType.set(value)).setGetter(app -> app.gcodeCommentType.get()));
        registerSetting(GenericSetting.createStringSetting(DrawingBotV3.class, "gcodeStartCode", GCodeExporter.defaultStartCode, false, (app, value) -> app.controller.exportController.textAreaGCodeStart.setText(value)).setGetter(app -> app.gcodeStartCode.get()));
        registerSetting(GenericSetting.createStringSetting(DrawingBotV3.class, "gcodeEndCode", GCodeExporter.defaultEndCode, false, (app, value) -> app.controller.exportController.textAreaGCodeEnd.setText(value)).setGetter(app -> app.gcodeEndCode.get()));
        registerSetting(GenericSetting.createStringSetting(DrawingBotV3.class, "gcodePenDownCode", GCodeExporter.defaultPenDownCode, false, (app, value) -> app.controller.exportController.textAreaGCodePenDown.setText(value)).setGetter(app -> app.gcodePenDownCode.get()));
        registerSetting(GenericSetting.createStringSetting(DrawingBotV3.class, "gcodePenUpCode", GCodeExporter.defaultPenUpCode, false, (app, value) -> app.controller.exportController.textAreaGCodePenUp.setText(value)).setGetter(app -> app.gcodePenUpCode.get()));
        registerSetting(GenericSetting.createStringSetting(DrawingBotV3.class, "gcodeStartLayerCode", GCodeExporter.defaultStartLayerCode, false, (app, value) -> app.controller.exportController.textAreaGCodeStartLayer.setText(value)).setGetter(app -> app.gcodeStartLayerCode.get()));
        registerSetting(GenericSetting.createStringSetting(DrawingBotV3.class, "gcodeEndLayerCode", GCodeExporter.defaultEndLayerCode, false, (app, value) -> app.controller.exportController.textAreaGCodeEndLayer.setText(value)).setGetter(app -> app.gcodeEndLayerCode.get()));
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
