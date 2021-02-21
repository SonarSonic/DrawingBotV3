package drawingbot.api;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class API {

    public static IDrawingBotAPI INSTANCE;

    public interface IDrawingBotAPI {

        /**
         * @param pfmClass the {@link IPathFindingModule} class
         * @param name the name of the pfm, to appear in user interfaces
         * @param create supplier to create a new instance of the {@link IPathFindingModule}
         * @param isHidden true if the PFM should be only be shown in developer mode
         */
        void registerPathFindingModule(Class<? extends IPathFindingModule> pfmClass, String name, Supplier<IPathFindingModule> create, boolean isHidden);

        /**
         * Create a boolean setting for a specific {@link IPathFindingModule}
         * @param pfmClass the {@link IPathFindingModule} class
         * @param settingName the settings name
         * @param defaultValue the default value
         * @param shouldLock if the setting should be locked by default
         * @param setter function which applies the setting to the {@link IPathFindingModule}
         */
        <C> void createBooleanSetting(Class<C> pfmClass, String settingName, Boolean defaultValue, boolean shouldLock, BiConsumer<C, Boolean> setter);

        /**
         * Create a string setting for a specific {@link IPathFindingModule}
         * @param pfmClass the {@link IPathFindingModule} class
         * @param settingName the settings name
         * @param defaultValue the default value
         * @param shouldLock if the setting should be locked by default
         * @param setter function which applies the setting to the {@link IPathFindingModule}
         */
        <C> void createStringSetting(Class<C> pfmClass, String settingName, String defaultValue, boolean shouldLock, BiConsumer<C, String> setter);

        /**
         * Create a float setting for a specific {@link IPathFindingModule}
         * @param pfmClass the {@link IPathFindingModule} class
         * @param settingName the settings name
         * @param defaultValue the default value
         * @param shouldLock if the setting should be locked by default
         * @param setter function which applies the setting to the {@link IPathFindingModule}
         */
        <C> void createRangedFloatSetting(Class<C> pfmClass, String settingName, float defaultValue, float minValue, float maxValue, boolean shouldLock, BiConsumer<C, Float> setter);

        /**
         * Create a long setting for a specific {@link IPathFindingModule}
         * @param pfmClass the {@link IPathFindingModule} class
         * @param settingName the settings name
         * @param defaultValue the default value
         * @param shouldLock if the setting should be locked by default
         * @param setter function which applies the setting to the {@link IPathFindingModule}
         */
        <C> void createRangedLongSetting(Class<C> pfmClass, String settingName, long defaultValue, long minValue, long maxValue, boolean shouldLock, BiConsumer<C, Long> setter);

        /**
         * Create a integer setting for a specific {@link IPathFindingModule}
         * @param pfmClass the {@link IPathFindingModule} class
         * @param settingName the settings name
         * @param defaultValue the default value
         * @param shouldLock if the setting should be locked by default
         * @param setter function which applies the setting to the {@link IPathFindingModule}
         */
        <C> void createRangedIntSetting(Class<C> pfmClass, String settingName, int defaultValue, int minValue, int maxValue, boolean shouldLock, BiConsumer<C, Integer> setter);
    }

}
