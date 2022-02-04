package drawingbot.files.presets.types;

import drawingbot.DrawingBotV3;
import drawingbot.files.presets.AbstractSettingsLoader;
import drawingbot.files.presets.PresetType;
import drawingbot.image.DrawingArea;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.EnumScalingMode;
import drawingbot.utils.UnitsLength;

import java.util.HashMap;
import java.util.List;

public class PresetDrawingAreaLoader extends AbstractSettingsLoader<PresetDrawingArea> {

    public PresetDrawingAreaLoader(PresetType presetType) {
        super(PresetDrawingArea.class, presetType, "user_page_presets.json");
    }

    public void registerSettings(){
        registerSetting(GenericSetting.createBooleanSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "useOriginalSizing", true, false, (area, value) -> area.useOriginalSizing.set(value)).setGetter(app -> app.useOriginalSizing.get()));
        registerSetting(GenericSetting.createOptionSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "inputUnits", List.of(UnitsLength.values()), UnitsLength.MILLIMETRES, false, (area, value) -> area.inputUnits.set(value)).setGetter(app -> app.inputUnits.get()));
        registerSetting(GenericSetting.createOptionSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "scalingMode", List.of(EnumScalingMode.values()), EnumScalingMode.CROP_TO_FIT, false, (area, value) -> area.scalingMode.set(value)).setGetter(app -> app.scalingMode.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "drawingAreaWidth", 0F, 0F, Float.MAX_VALUE, false, (area, value) -> area.drawingAreaWidth.set(value)).setGetter(app -> app.drawingAreaWidth.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "drawingAreaHeight", 0F, 0F, Float.MAX_VALUE, false, (area, value) -> area.drawingAreaHeight.set(value)).setGetter(app -> app.drawingAreaHeight.get()));
        registerSetting(GenericSetting.createBooleanSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "drawingAreaGang", true, false, (area, value) -> DrawingBotV3.INSTANCE.controller.checkBoxGangPadding.setSelected(value)).setGetter(app -> DrawingBotV3.INSTANCE.controller.checkBoxGangPadding.isSelected()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "drawingAreaPaddingLeft", 0F, 0F, Float.MAX_VALUE, false, (area, value) -> area.drawingAreaPaddingLeft.set(value)).setGetter(app -> app.drawingAreaPaddingLeft.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "drawingAreaPaddingRight", 0F, 0F, Float.MAX_VALUE, false, (area, value) -> area.drawingAreaPaddingRight.set(value)).setGetter(app -> app.drawingAreaPaddingRight.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "drawingAreaPaddingTop", 0F, 0F, Float.MAX_VALUE, false, (area, value) -> area.drawingAreaPaddingTop.set(value)).setGetter(app -> app.drawingAreaPaddingTop.get()));
        registerSetting(GenericSetting.createRangedFloatSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "drawingAreaPaddingBottom", 0F, 0F, Float.MAX_VALUE, false, (area, value) -> area.drawingAreaPaddingBottom.set(value)).setGetter(app -> app.drawingAreaPaddingBottom.get()));
    }

    @Override
    public GenericPreset<PresetDrawingArea> updatePreset(GenericPreset<PresetDrawingArea> preset) {
        GenericSetting.updateSettingsFromInstance(settings, DrawingBotV3.INSTANCE.drawingArea);
        preset.data.settingList = GenericSetting.toJsonMap(settings, new HashMap<>(), false);
        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<PresetDrawingArea> preset) {
        GenericSetting.applySettings(preset.data.settingList, settings);
        GenericSetting.applySettingsToInstance(settings, DrawingBotV3.INSTANCE.drawingArea);
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
