package drawingbot.api_impl;

import drawingbot.api.API;
import drawingbot.api.IPathFindingModule;
import drawingbot.javafx.GenericSetting;
import drawingbot.registry.MasterRegistry;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class DrawingBotV3API implements API.IDrawingBotAPI {

    @Override
    public void registerPathFindingModule(Class<? extends IPathFindingModule> pfmClass, String name, Supplier<IPathFindingModule> create, boolean isHidden) {
        MasterRegistry.INSTANCE.registerPFM(pfmClass, name, create, isHidden);
    }

    @Override
    public <C> void createBooleanSetting(Class<C> pfmClass, String settingName, Boolean defaultValue, boolean shouldLock, BiConsumer<C, Boolean> setter) {
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createBooleanSetting(pfmClass, settingName, defaultValue, shouldLock, setter));
    }

    @Override
    public <C> void createStringSetting(Class<C> pfmClass, String settingName, String defaultValue, boolean shouldLock, BiConsumer<C, String> setter) {
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createStringSetting(pfmClass, settingName, defaultValue, shouldLock, setter));
    }

    @Override
    public <C> void createRangedFloatSetting(Class<C> pfmClass, String settingName, float defaultValue, float minValue, float maxValue, boolean shouldLock, BiConsumer<C, Float> setter) {
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(pfmClass, settingName, defaultValue, minValue, maxValue, shouldLock, setter));
    }

    @Override
    public <C> void createRangedLongSetting(Class<C> pfmClass, String settingName, long defaultValue, long minValue, long maxValue, boolean shouldLock, BiConsumer<C, Long> setter) {
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedLongSetting(pfmClass, settingName, defaultValue, minValue, maxValue, shouldLock, setter));
    }

    @Override
    public <C> void createRangedIntSetting(Class<C> pfmClass, String settingName, int defaultValue, int minValue, int maxValue, boolean shouldLock, BiConsumer<C, Integer> setter) {
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(pfmClass, settingName, defaultValue, minValue, maxValue, shouldLock, setter));
    }

}
