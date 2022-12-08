package drawingbot.api;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class API {

    public static IDrawingBotAPI INSTANCE;

    public interface IDrawingBotAPI {

        /**
         * @param pfmClass the {@link IPFM} class
         * @param name the name of the pfm, to appear in user interfaces
         * @param create supplier to create a new instance of the {@link IPFM}
         * @param isHidden true if the PFM should be only be shown in developer mode
         * @param registerDefaultPreset
         */
        void registerPathFindingModule(Class<IPFM> pfmClass, String name, String category, Supplier<IPFM> create, boolean isHidden, boolean registerDefaultPreset);

        /**
         * Create a boolean setting for a specific {@link IPFM}
         * @param pfmClass the {@link IPFM} class
         * @param settingName the settings name
         * @param defaultValue the default value
         * @param setter function which applies the setting to the {@link IPFM}
         */
        <C> void createBooleanSetting(Class<C> pfmClass, String category, String settingName, Boolean defaultValue, BiConsumer<C, Boolean> setter);

        /**
         * Create a string setting for a specific {@link IPFM}
         * @param pfmClass the {@link IPFM} class
         * @param settingName the settings name
         * @param defaultValue the default value
         * @param setter function which applies the setting to the {@link IPFM}
         */
        <C> void createStringSetting(Class<C> pfmClass, String category, String settingName, String defaultValue, BiConsumer<C, String> setter);

        /**
         * Create a float setting for a specific {@link IPFM}
         * @param pfmClass the {@link IPFM} class
         * @param settingName the settings name
         * @param defaultValue the default value
         * @param setter function which applies the setting to the {@link IPFM}
         */
        <C> void createRangedFloatSetting(Class<C> pfmClass, String category, String settingName, float defaultValue, float minValue, float maxValue, BiConsumer<C, Float> setter);

        /**
         * Create a long setting for a specific {@link IPFM}
         * @param pfmClass the {@link IPFM} class
         * @param settingName the settings name
         * @param defaultValue the default value
         * @param setter function which applies the setting to the {@link IPFM}
         */
        <C> void createRangedLongSetting(Class<C> pfmClass, String category, String settingName, long defaultValue, long minValue, long maxValue, BiConsumer<C, Long> setter);

        /**
         * Create a integer setting for a specific {@link IPFM}
         * @param pfmClass the {@link IPFM} class
         * @param settingName the settings name
         * @param defaultValue the default value
         * @param setter function which applies the setting to the {@link IPFM}
         */
        <C> void createRangedIntSetting(Class<C> pfmClass, String category, String settingName, int defaultValue, int minValue, int maxValue, BiConsumer<C, Integer> setter);
    }

}
