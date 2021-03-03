package drawingbot.files.presets.types;

import drawingbot.DrawingBotV3;
import drawingbot.files.presets.AbstractPresetLoader;
import drawingbot.files.presets.AbstractSettingsLoader;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.utils.EnumJsonType;
import drawingbot.utils.EnumScalingMode;
import drawingbot.utils.Units;
import java.util.List;

public class PresetDrawingAreaLoader extends AbstractSettingsLoader<PresetDrawingArea> {

    public PresetDrawingAreaLoader() {
        super(PresetDrawingArea.class, EnumJsonType.DRAWING_AREA, "user_page_presets.json");
    }

    public void registerSettings(){
        registerSetting(GenericSetting.createBooleanSetting(DrawingBotV3.class, "useOriginalSizing", true, false, (app, value) -> app.useOriginalSizing.set(value)).setGetter(app -> app.useOriginalSizing.get()));
        registerSetting(GenericSetting.createOptionSetting(DrawingBotV3.class, "inputUnits", List.of(Units.values()), Units.MILLIMETRES, false, (app, value) -> app.inputUnits.set(value)).setGetter(app -> app.inputUnits.get()));
        registerSetting(GenericSetting.createOptionSetting(DrawingBotV3.class, "scalingMode", List.of(EnumScalingMode.values()), EnumScalingMode.CROP_TO_FIT, false, (app, value) -> app.scalingMode.set(value)).setGetter(app -> app.scalingMode.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "drawingAreaWidth", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.textFieldDrawingWidth.setText(String.valueOf(value))).setGetter(app -> app.drawingAreaWidth.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "drawingAreaHeight", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.textFieldDrawingHeight.setText(String.valueOf(value))).setGetter(app -> app.drawingAreaHeight.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "drawingAreaPaddingLeft", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.textFieldPaddingLeft.setText(String.valueOf(value))).setGetter(app -> app.drawingAreaPaddingLeft.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "drawingAreaPaddingRight", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.textFieldPaddingRight.setText(String.valueOf(value))).setGetter(app -> app.drawingAreaPaddingRight.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "drawingAreaPaddingTop", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.textFieldPaddingTop.setText(String.valueOf(value))).setGetter(app -> app.drawingAreaPaddingTop.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "drawingAreaPaddingBottom", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.textFieldPaddingBottom.setText(String.valueOf(value))).setGetter(app -> app.drawingAreaPaddingBottom.get()));
    }

    @Override
    public GenericPreset<PresetDrawingArea> getDefaultPreset() {
        return presets.stream().filter(p -> p.presetName.equals("Original Sizing")).findFirst().get();
    }

    @Override
    protected PresetDrawingArea getPresetInstance(GenericPreset<PresetDrawingArea> preset) {
        return new PresetDrawingArea();
    }
}
