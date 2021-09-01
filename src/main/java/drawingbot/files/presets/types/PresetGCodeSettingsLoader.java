package drawingbot.files.presets.types;

import drawingbot.DrawingBotV3;
import drawingbot.files.exporters.GCodeExporter;
import drawingbot.files.presets.AbstractSettingsLoader;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.utils.EnumDirection;
import drawingbot.utils.EnumJsonType;

import java.util.List;

public class PresetGCodeSettingsLoader extends AbstractSettingsLoader<PresetGCodeSettings> {

    public PresetGCodeSettingsLoader() {
        super(PresetGCodeSettings.class, EnumJsonType.GCODE_SETTINGS, "user_gcode_presets.json");
    }

    public void registerSettings(){
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "gcodeOffsetX", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.exportController.textFieldOffsetX.setText(String.valueOf(value))).setGetter(app -> app.gcodeOffsetX.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "gcodeOffsetY", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.exportController.textFieldOffsetY.setText(String.valueOf(value))).setGetter(app -> app.gcodeOffsetY.get()));
        //registerSetting(GenericSetting.createOptionSetting(DrawingBotV3.class, "gcodeXDir", List.of(EnumDirection.values()), EnumDirection.POSITIVE, false, (app, value) -> app.controller.exportController.choiceBoxGCodeXDir.setValue(value)).setGetter(app -> app.gcodeXDirection.get()));
        //registerSetting(GenericSetting.createOptionSetting(DrawingBotV3.class, "gcodeYDir", List.of(EnumDirection.values()), EnumDirection.POSITIVE, false, (app, value) -> app.controller.exportController.choiceBoxGCodeYDir.setValue(value)).setGetter(app -> app.gcodeYDirection.get()));
        registerSetting(GenericSetting.createStringSetting(DrawingBotV3.class, "gcodeStartCode", GCodeExporter.defaultStartCode, false, (app, value) -> app.controller.exportController.textAreaGCodeStart.setText(value)).setGetter(app -> app.gcodeStartCode.get()));
        registerSetting(GenericSetting.createStringSetting(DrawingBotV3.class, "gcodeEndCode", GCodeExporter.defaultEndCode, false, (app, value) -> app.controller.exportController.textAreaGCodeEnd.setText(value)).setGetter(app -> app.gcodeEndCode.get()));
        registerSetting(GenericSetting.createStringSetting(DrawingBotV3.class, "gcodePenDownCode", GCodeExporter.defaultPenDownCode, false, (app, value) -> app.controller.exportController.textAreaGCodePenDown.setText(value)).setGetter(app -> app.gcodePenDownCode.get()));
        registerSetting(GenericSetting.createStringSetting(DrawingBotV3.class, "gcodePenUpCode", GCodeExporter.defaultPenUpCode, false, (app, value) -> app.controller.exportController.textAreaGCodePenUp.setText(value)).setGetter(app -> app.gcodePenUpCode.get()));
        registerSetting(GenericSetting.createStringSetting(DrawingBotV3.class, "gcodeStartLayerCode", GCodeExporter.defaultStartLayerCode, false, (app, value) -> app.controller.exportController.textAreaGCodeStartLayer.setText(value)).setGetter(app -> app.gcodeStartLayerCode.get()));
        registerSetting(GenericSetting.createStringSetting(DrawingBotV3.class, "gcodeEndLayerCode", GCodeExporter.defaultEndLayerCode, false, (app, value) -> app.controller.exportController.textAreaGCodeEndLayer.setText(value)).setGetter(app -> app.gcodeEndLayerCode.get()));
    }

    @Override
    public GenericPreset<PresetGCodeSettings> getDefaultPreset() {
        return presets.stream().filter(p -> p.presetName.equals("Default")).findFirst().orElse(null);
    }


    @Override
    protected PresetGCodeSettings getPresetInstance(GenericPreset<PresetGCodeSettings> preset) {
        return new PresetGCodeSettings();
    }
}
