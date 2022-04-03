package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.AbstractSettingsLoader;
import drawingbot.files.json.PresetType;
import drawingbot.plotting.canvas.ObservableCanvas;
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
        registerSetting(GenericSetting.createBooleanSetting(ObservableCanvas.class, Register.CATEGORY_UNIQUE, "useOriginalSizing", true, (area, value) -> area.useOriginalSizing.set(value)).setGetter(app -> app.useOriginalSizing.get()).setDisplayName("Use Original Sizing"));
        registerSetting(GenericSetting.createOptionSetting(ObservableCanvas.class, UnitsLength.class, Register.CATEGORY_UNIQUE, "inputUnits", List.of(UnitsLength.values()), UnitsLength.MILLIMETRES, (area, value) -> area.inputUnits.set(value)).setGetter(app -> app.inputUnits.get()).setDisplayName("Input Units"));
        registerSetting(GenericSetting.createOptionSetting(ObservableCanvas.class, EnumScalingMode.class, Register.CATEGORY_UNIQUE, "scalingMode", List.of(EnumScalingMode.values()), EnumScalingMode.CROP_TO_FIT, (area, value) -> area.scalingMode.set(value)).setGetter(app -> app.scalingMode.get()).setDisplayName("Scaling Mode"));
        registerSetting(GenericSetting.createFloatSetting(ObservableCanvas.class, Register.CATEGORY_UNIQUE, "drawingAreaWidth", 0F, (area, value) -> area.width.set(value)).setGetter(app -> app.width.get()).setValidator(Math::abs).setDisplayName("Width"));
        registerSetting(GenericSetting.createFloatSetting(ObservableCanvas.class, Register.CATEGORY_UNIQUE, "drawingAreaHeight", 0F, (area, value) -> area.height.set(value)).setGetter(app -> app.height.get()).setValidator(Math::abs).setDisplayName("Height"));
        registerSetting(GenericSetting.createBooleanSetting(ObservableCanvas.class, Register.CATEGORY_UNIQUE, "drawingAreaGang", true, (area, value) -> DrawingBotV3.INSTANCE.controller.checkBoxGangPadding.setSelected(value)).setGetter(app -> DrawingBotV3.INSTANCE.controller.checkBoxGangPadding.isSelected()).setDisplayName("Gang Padding"));
        registerSetting(GenericSetting.createFloatSetting(ObservableCanvas.class, Register.CATEGORY_UNIQUE, "drawingAreaPaddingLeft", 0F, (area, value) -> area.drawingAreaPaddingLeft.set(value)).setGetter(app -> app.drawingAreaPaddingLeft.get()).setValidator(Math::abs).setDisplayName("Padding Left"));
        registerSetting(GenericSetting.createFloatSetting(ObservableCanvas.class, Register.CATEGORY_UNIQUE, "drawingAreaPaddingRight", 0F, (area, value) -> area.drawingAreaPaddingRight.set(value)).setGetter(app -> app.drawingAreaPaddingRight.get()).setValidator(Math::abs).setDisplayName("Padding Right"));
        registerSetting(GenericSetting.createFloatSetting(ObservableCanvas.class, Register.CATEGORY_UNIQUE, "drawingAreaPaddingTop", 0F, (area, value) -> area.drawingAreaPaddingTop.set(value)).setGetter(app -> app.drawingAreaPaddingTop.get()).setValidator(Math::abs).setDisplayName("Padding Top"));
        registerSetting(GenericSetting.createFloatSetting(ObservableCanvas.class, Register.CATEGORY_UNIQUE, "drawingAreaPaddingBottom", 0F, (area, value) -> area.drawingAreaPaddingBottom.set(value)).setGetter(app -> app.drawingAreaPaddingBottom.get()).setValidator(Math::abs).setDisplayName("Padding Bottom"));
        registerSetting(GenericSetting.createBooleanSetting(ObservableCanvas.class, Register.CATEGORY_UNIQUE, "optimiseForPrint", true, (area, value) -> area.optimiseForPrint.set(value)).setGetter(app -> app.optimiseForPrint.get()).setDisplayName("Optimise for Print"));
        registerSetting(GenericSetting.createFloatSetting(ObservableCanvas.class, Register.CATEGORY_UNIQUE, "targetPenWidth", 0.3F, (area, value) -> area.targetPenWidth.set(value)).setGetter(app -> app.targetPenWidth.get()).setValidator(Math::abs).setDisplayName("Target pen width"));

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
