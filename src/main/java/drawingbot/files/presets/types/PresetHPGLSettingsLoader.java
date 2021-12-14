package drawingbot.files.presets.types;

import drawingbot.DrawingBotV3;
import drawingbot.files.presets.AbstractSettingsLoader;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.utils.*;

import java.util.List;

public class PresetHPGLSettingsLoader extends AbstractSettingsLoader<PresetHPGLSettings> {

    public PresetHPGLSettingsLoader() {
        super(PresetHPGLSettings.class, EnumJsonType.HPGL_SETTINGS, "user_hpgl_presets.json");
    }

    public void registerSettings(){
        registerSetting(GenericSetting.createRangedIntSetting(DrawingBotV3.class, "hpglXMin", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE, false, (app, value) -> app.controller.exportController.textFieldHPGLXMin.setText(String.valueOf(value))).setGetter(app -> app.hpglXMin.get()));
        registerSetting(GenericSetting.createRangedIntSetting(DrawingBotV3.class, "hpglXMax", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE, false, (app, value) -> app.controller.exportController.textFieldHPGLXMax.setText(String.valueOf(value))).setGetter(app -> app.hpglXMax.get()));
        registerSetting(GenericSetting.createRangedIntSetting(DrawingBotV3.class, "hpglYMin", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE, false, (app, value) -> app.controller.exportController.textFieldHPGLYMin.setText(String.valueOf(value))).setGetter(app -> app.hpglYMin.get()));
        registerSetting(GenericSetting.createRangedIntSetting(DrawingBotV3.class, "hpglYMax", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE, false, (app, value) -> app.controller.exportController.textFieldHPGLYMax.setText(String.valueOf(value))).setGetter(app -> app.hpglYMax.get()));
        registerSetting(GenericSetting.createBooleanSetting(DrawingBotV3.class, "hpglXAxisMirror", false, false, (app, value) -> app.hpglXAxisMirror.set(value)).setGetter(app -> app.hpglXAxisMirror.get()));
        registerSetting(GenericSetting.createBooleanSetting(DrawingBotV3.class, "hpglYAxisMirror", true, false, (app, value) -> app.hpglYAxisMirror.set(value)).setGetter(app -> app.hpglYAxisMirror.get()));
        /*
        registerSetting(GenericSetting.createOptionSetting(DrawingBotV3.class, "hpglAlignX", List.of(EnumAlignment.values()), EnumAlignment.CENTER, false, (app, value) -> app.hpglAlignX.set(value)).setGetter(app -> app.hpglAlignX.get()));
        registerSetting(GenericSetting.createOptionSetting(DrawingBotV3.class, "hpglAlignY", List.of(EnumAlignment.values()), EnumAlignment.CENTER, false, (app, value) -> app.hpglAlignY.set(value)).setGetter(app -> app.hpglAlignY.get()));
        registerSetting(GenericSetting.createOptionSetting(DrawingBotV3.class, "hpglRotation", List.of(EnumRotation.values()), EnumRotation.AUTO, false, (app, value) -> app.hpglRotation.set(value)).setGetter(app -> app.hpglRotation.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "hpglCurveFlatness", 6F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.exportController.textFieldHPGLCurveFlatness.setText(String.valueOf(value))).setGetter(app -> app.hpglCurveFlatness.get()));
        registerSetting(GenericSetting.createRangedIntSetting(DrawingBotV3.class, "hpglPenSpeed", 0, 0, Integer.MAX_VALUE, false, (app, value) -> app.controller.exportController.textFieldHPGLPenSpeed.setText(String.valueOf(value))).setGetter(app -> app.hpglPenSpeed.get()));
        */
    }

    @Override
    public GenericPreset<PresetHPGLSettings> getDefaultPreset() {
        return presets.stream().filter(p -> p.presetName.equals("Default")).findFirst().orElse(null);
    }

    @Override
    protected PresetHPGLSettings getPresetInstance(GenericPreset<PresetHPGLSettings> preset) {
        return new PresetHPGLSettings();
    }
}
