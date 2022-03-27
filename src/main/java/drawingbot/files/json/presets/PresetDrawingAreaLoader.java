package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.AbstractSettingsLoader;
import drawingbot.files.json.PresetType;
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
        registerSetting(GenericSetting.createBooleanSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "useOriginalSizing", true, (area, value) -> area.useOriginalSizing.set(value)).setGetter(app -> app.useOriginalSizing.get()).setDisplayName("Use Original Sizing"));
        registerSetting(GenericSetting.createOptionSetting(DrawingArea.class, UnitsLength.class, Register.CATEGORY_UNIQUE, "inputUnits", List.of(UnitsLength.values()), UnitsLength.MILLIMETRES, (area, value) -> area.inputUnits.set(value)).setGetter(app -> app.inputUnits.get()).setDisplayName("Input Units"));
        registerSetting(GenericSetting.createOptionSetting(DrawingArea.class, EnumScalingMode.class, Register.CATEGORY_UNIQUE, "scalingMode", List.of(EnumScalingMode.values()), EnumScalingMode.CROP_TO_FIT, (area, value) -> area.scalingMode.set(value)).setGetter(app -> app.scalingMode.get()).setDisplayName("Scaling Mode"));
        registerSetting(GenericSetting.createFloatSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "drawingAreaWidth", 0F, (area, value) -> area.drawingAreaWidth.set(value)).setGetter(app -> app.drawingAreaWidth.get()).setValidator(Math::abs).setDisplayName("Width"));
        registerSetting(GenericSetting.createFloatSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "drawingAreaHeight", 0F, (area, value) -> area.drawingAreaHeight.set(value)).setGetter(app -> app.drawingAreaHeight.get()).setValidator(Math::abs).setDisplayName("Height"));
        registerSetting(GenericSetting.createBooleanSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "drawingAreaGang", true, (area, value) -> DrawingBotV3.INSTANCE.controller.checkBoxGangPadding.setSelected(value)).setGetter(app -> DrawingBotV3.INSTANCE.controller.checkBoxGangPadding.isSelected()).setDisplayName("Gang Padding"));
        registerSetting(GenericSetting.createFloatSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "drawingAreaPaddingLeft", 0F, (area, value) -> area.drawingAreaPaddingLeft.set(value)).setGetter(app -> app.drawingAreaPaddingLeft.get()).setValidator(Math::abs).setDisplayName("Padding Left"));
        registerSetting(GenericSetting.createFloatSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "drawingAreaPaddingRight", 0F, (area, value) -> area.drawingAreaPaddingRight.set(value)).setGetter(app -> app.drawingAreaPaddingRight.get()).setValidator(Math::abs).setDisplayName("Padding Right"));
        registerSetting(GenericSetting.createFloatSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "drawingAreaPaddingTop", 0F, (area, value) -> area.drawingAreaPaddingTop.set(value)).setGetter(app -> app.drawingAreaPaddingTop.get()).setValidator(Math::abs).setDisplayName("Padding Top"));
        registerSetting(GenericSetting.createFloatSetting(DrawingArea.class, Register.CATEGORY_UNIQUE, "drawingAreaPaddingBottom", 0F, (area, value) -> area.drawingAreaPaddingBottom.set(value)).setGetter(app -> app.drawingAreaPaddingBottom.get()).setValidator(Math::abs).setDisplayName("Padding Bottom"));
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
