package drawingbot.files.presets.types;

import drawingbot.DrawingBotV3;
import drawingbot.files.presets.AbstractSettingsLoader;
import drawingbot.files.presets.PresetType;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.EnumScalingMode;
import drawingbot.utils.UnitsLength;
import java.util.List;

public class PresetDrawingAreaLoader extends AbstractSettingsLoader<PresetDrawingArea> {

    public PresetDrawingAreaLoader(PresetType presetType) {
        super(PresetDrawingArea.class, presetType, "user_page_presets.json");
    }

    public void registerSettings(){
        registerSetting(GenericSetting.createBooleanSetting(DrawingBotV3.class, "useOriginalSizing", true, false, (app, value) -> app.useOriginalSizing.set(value)).setGetter(app -> app.useOriginalSizing.get()));
        registerSetting(GenericSetting.createOptionSetting(DrawingBotV3.class, "inputUnits", List.of(UnitsLength.values()), UnitsLength.MILLIMETRES, false, (app, value) -> app.inputUnits.set(value)).setGetter(app -> app.inputUnits.get()));
        registerSetting(GenericSetting.createOptionSetting(DrawingBotV3.class, "scalingMode", List.of(EnumScalingMode.values()), EnumScalingMode.CROP_TO_FIT, false, (app, value) -> app.scalingMode.set(value)).setGetter(app -> app.scalingMode.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "drawingAreaWidth", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.textFieldDrawingWidth.setText(String.valueOf(value))).setGetter(app -> app.drawingAreaWidth.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "drawingAreaHeight", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.textFieldDrawingHeight.setText(String.valueOf(value))).setGetter(app -> app.drawingAreaHeight.get()));
        registerSetting(GenericSetting.createBooleanSetting(DrawingBotV3.class, "drawingAreaGang", true, false, (app, value) -> app.controller.checkBoxGangPadding.setSelected(value)).setGetter(app -> app.controller.checkBoxGangPadding.isSelected()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "drawingAreaPaddingLeft", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.textFieldPaddingLeft.setText(String.valueOf(value))).setGetter(app -> app.drawingAreaPaddingLeft.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "drawingAreaPaddingRight", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.textFieldPaddingRight.setText(String.valueOf(value))).setGetter(app -> app.drawingAreaPaddingRight.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "drawingAreaPaddingTop", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.textFieldPaddingTop.setText(String.valueOf(value))).setGetter(app -> app.drawingAreaPaddingTop.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "drawingAreaPaddingBottom", 0F, 0F, Float.MAX_VALUE, false, (app, value) -> app.controller.textFieldPaddingBottom.setText(String.valueOf(value))).setGetter(app -> app.drawingAreaPaddingBottom.get()));
    }

    @Override
    public GenericPreset<PresetDrawingArea> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "Original Sizing");
    }

    @Override
    protected PresetDrawingArea getPresetInstance(GenericPreset<PresetDrawingArea> preset) {
        return new PresetDrawingArea();
    }
}
