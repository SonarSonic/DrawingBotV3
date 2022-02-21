package drawingbot.api_impl;

import drawingbot.api.API;
import drawingbot.api.IPathFindingModule;
import drawingbot.javafx.GenericSetting;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class DrawingBotV3API implements API.IDrawingBotAPI {

    @Override
    public void registerPathFindingModule(Class<IPathFindingModule> pfmClass, String name, Supplier<IPathFindingModule> create, boolean isHidden, boolean registerDefaultPreset) {
        MasterRegistry.INSTANCE.registerPFM(pfmClass, name, create, isHidden, registerDefaultPreset);
    }

    @Override
    public <C> void createBooleanSetting(Class<C> pfmClass, String category, String settingName, Boolean defaultValue, BiConsumer<C, Boolean> setter) {
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createBooleanSetting(pfmClass, category, settingName, defaultValue, setter));
    }

    @Override
    public <C> void createStringSetting(Class<C> pfmClass, String category, String settingName, String defaultValue, BiConsumer<C, String> setter) {
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createStringSetting(pfmClass, category, settingName, defaultValue, setter));
    }

    @Override
    public <C> void createRangedFloatSetting(Class<C> pfmClass, String category, String settingName, float defaultValue, float minValue, float maxValue, BiConsumer<C, Float> setter) {
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(pfmClass, category, settingName, defaultValue, minValue, maxValue, setter));
    }

    @Override
    public <C> void createRangedLongSetting(Class<C> pfmClass, String category, String settingName, long defaultValue, long minValue, long maxValue, BiConsumer<C, Long> setter) {
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedLongSetting(pfmClass, category, settingName, defaultValue, minValue, maxValue, setter));
    }

    @Override
    public <C> void createRangedIntSetting(Class<C> pfmClass, String category, String settingName, int defaultValue, int minValue, int maxValue, BiConsumer<C, Integer> setter) {
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(pfmClass, category, settingName, defaultValue, minValue, maxValue, setter));
    }

}
