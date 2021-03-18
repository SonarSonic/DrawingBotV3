package drawingbot.files.presets.types;

import drawingbot.DrawingBotV3;
import drawingbot.files.presets.AbstractSettingsLoader;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.utils.EnumJsonType;

public class PresetGCodeSettingsLoader extends AbstractSettingsLoader<PresetGCodeSettings> {

    public PresetGCodeSettingsLoader() {
        super(PresetGCodeSettings.class, EnumJsonType.GCODE_SETTINGS, "user_gcode_presets.json");
    }

    public void registerSettings(){
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "gcodeOffsetX", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.exportController.textFieldOffsetX.setText(String.valueOf(value))).setGetter(app -> app.gcodeOffsetX.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "gcodeOffsetY", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.exportController.textFieldOffsetY.setText(String.valueOf(value))).setGetter(app -> app.gcodeOffsetY.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "penUpZ", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.exportController.textFieldPenUpZ.setText(String.valueOf(value))).setGetter(app -> app.penUpZ.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "penDownZ", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.exportController.textFieldPenDownZ.setText(String.valueOf(value))).setGetter(app -> app.penDownZ.get()));
        registerSetting(GenericSetting.createBooleanSetting(DrawingBotV3.class, "enableAutoHome", false, false, (app, value) -> app.enableAutoHome.set(value)).setGetter(app -> app.enableAutoHome.get()));
    }

    @Override
    public GenericPreset<PresetGCodeSettings> getDefaultPreset() {
        return presets.stream().filter(p -> p.presetName.equals("Default")).findFirst().get();
    }


    @Override
    protected PresetGCodeSettings getPresetInstance(GenericPreset<PresetGCodeSettings> preset) {
        return new PresetGCodeSettings();
    }
}
