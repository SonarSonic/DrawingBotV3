package drawingbot.pfm;

import drawingbot.DrawingBotV3;
import drawingbot.files.ConfigFileHandler;
import drawingbot.pfm.legacy.LegacyPFMLoaders;
import drawingbot.utils.GenericSetting;
import drawingbot.utils.GenericFactory;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class PFMMasterRegistry {

    public static HashMap<Class<? extends IPFM>, ObservableList<GenericSetting<?, ?>>> pfmSettings = new LinkedHashMap<>();
    public static HashMap<Class<? extends IPFM>, GenericFactory<IPFM>> pfmFactories = new LinkedHashMap<>();
    public static HashMap<String, ObservableList<PFMPreset>> pfmPresets = new LinkedHashMap<>();

    static{
        registerPFMFactory(PFMSketch.class, "Sketch PFM", PFMSketch::new, false);
        registerPFMFactory(PFMSquares.class, "Squares PFM", PFMSquares::new, false);
        registerPFMFactory(PFMSpiral.class, "Spiral PFM", PFMSpiral::new, false);
        registerPFMFactory(PFMLines.class, "Lines PFM (Experimental)", PFMLines::new, true);
        registerPFMFactory(LegacyPFMLoaders.pfmSketchLegacyClass, "Sketch PFM (Legacy)", LegacyPFMLoaders.pfmSketchLegacy, true);
        registerPFMFactory(LegacyPFMLoaders.pfmSquaresLegacyClass, "Squares PFM (Legacy)", LegacyPFMLoaders.pfmSquaresLegacy, true);
        registerPFMFactory(LegacyPFMLoaders.pfmSpiralLegacyClass, "Spiral PFM (Legacy)", LegacyPFMLoaders.pfmSpiralLegacy, true);

        ////GENERAL
        registerSetting(GenericSetting.createRangedFloatSetting(AbstractPFM.class, "Plotting Resolution", 1.0F, 0.1F, 10F, (pfmSketch, value) -> pfmSketch.plottingResolution = value));
        registerSetting(GenericSetting.createRangedLongSetting(AbstractPFM.class, "Random Seed", 0L, Long.MIN_VALUE, Long.MAX_VALUE, (pfmSketch, value) -> pfmSketch.seed = value));

        ////ABSTRACT SKETCH PFM
        registerSetting(GenericSetting.createRangedFloatSetting(AbstractSketchPFM.class, "Desired Brightness", 250F, 0F, 255F, (pfmSketch, value) -> pfmSketch.desired_brightness = value));
        registerSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, "Squiggle Length", 500, 1, Integer.MAX_VALUE, (pfmSketch, value) -> pfmSketch.squiggle_length = value));
        registerSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, "Adjust Brightness", 50, 1, 255, (pfmSketch, value) -> pfmSketch.adjustbrightness = value));
        registerSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, "Neighbour Tests", 20, 1, 720, (pfmSketch, value) -> pfmSketch.tests = value));
        registerSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, "Min Line length", 20, 1, Integer.MAX_VALUE, (pfmSketch, value) -> pfmSketch.minLineLength = value));
        registerSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, "Max Line length", 40, 1, Integer.MAX_VALUE, (pfmSketch, value) -> pfmSketch.maxLineLength = value));
        registerSetting(GenericSetting.createBooleanSetting(AbstractSketchPFM.class, "Should Lift Pen", true, (pfmSketch, value) -> pfmSketch.shouldLiftPen = value));

        ////SKETCH PFM
        registerSetting(GenericSetting.createRangedIntSetting(PFMSketch.class, "Start Angle Min", -72, -360, 360, (pfmSketch, value) -> pfmSketch.startAngleMin = value));
        registerSetting(GenericSetting.createRangedIntSetting(PFMSketch.class, "Start Angle Max", -52, -360, 360, (pfmSketch, value) -> pfmSketch.startAngleMax = value));
        registerSetting(GenericSetting.createRangedFloatSetting(PFMSketch.class, "Drawing Delta Angle", 360F, -360F, 360F, (pfmSketch, value) -> pfmSketch.drawingDeltaAngle = value));
        registerSetting(GenericSetting.createRangedFloatSetting(PFMSketch.class, "Shading Delta Angle", 180F, -360F, 360F, (pfmSketch, value) -> pfmSketch.shadingDeltaAngle = value));
        registerSetting(GenericSetting.createBooleanSetting(PFMSketch.class, "Enable Shading", true, (pfmSketch, value) -> pfmSketch.enableShading = value));
        registerSetting(GenericSetting.createRangedIntSetting(PFMSketch.class, "Squiggles till shading", 190, 1, Integer.MAX_VALUE, (pfmSketch, value) -> pfmSketch.squigglesTillShading = value));

        ////SQUARES PFM

        ////SPIRAL PFM
        registerSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, "Distance between rings", 7F, 0F, Short.MAX_VALUE, (pfmSketch, value) -> pfmSketch.distBetweenRings = value));
        registerSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, "Density", 75F, 0F, Short.MAX_VALUE, (pfmSketch, value) -> pfmSketch.density = value));
        registerSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, "Amplitude", 4.5F, 0F, Short.MAX_VALUE, (pfmSketch, value) -> pfmSketch.ampScale = value));
    }

    //// PFM LOADERS \\\\

    public static void registerPFMFactory(Class<? extends IPFM> pfmClass, String name, Supplier<IPFM> create, boolean isHidden){
        pfmFactories.put(pfmClass, new GenericFactory(pfmClass, name, create, isHidden));
        registerPreset(new PFMPreset(name, "Default", false));
    }

    public static GenericFactory<IPFM> getDefaultPFMFactory(){
        return pfmFactories.get(PFMSketch.class);
    }

    //// PFM SETTING \\\\

    public static <C, V> void registerSetting(GenericSetting<C, V> setting){
        for(GenericFactory<IPFM> loader : PFMMasterRegistry.pfmFactories.values()){
            if(setting.isAssignableFrom(loader.getInstanceClass())){
                GenericSetting<C,V> copy = setting.copy();
                PFMMasterRegistry.pfmSettings.putIfAbsent(loader.getInstanceClass(), FXCollections.observableArrayList());
                PFMMasterRegistry.pfmSettings.get(loader.getInstanceClass()).add(copy);
                setting.value.addListener((observable, oldValue, newValue) -> PFMMasterRegistry.onSettingChanged(copy, observable, oldValue, newValue));
            }
        }
    }

    public static <V> void onSettingChanged(GenericSetting<?,V> setting, ObservableValue<?> observable, V oldValue, V newValue){
        ///not used at the moment, called whenever a setting's value is changed
    }

    public static <P extends IPFM> void applySettings(P pfm){
        for(GenericSetting<?, ?> setting : pfmSettings.get(pfm.getClass())){
            setting.applySetting(pfm);
        }
    }

    public static void resetSettingsToDefault(List<GenericSetting<?, ?>> settingList){
        for(GenericSetting<?, ?> setting : settingList){
            setting.resetSetting();
        }
    }

    public static void randomiseSettings(List<GenericSetting<?, ?>> settingList){
        for(GenericSetting<?, ?> setting : settingList){
            setting.randomiseSetting(ThreadLocalRandom.current());
        }
    }

    //// PFM PRESET \\\\

    public static PFMPreset getDefaultPFMPreset(){
        return getDefaultPFMPreset(DrawingBotV3.INSTANCE.pfmLoader.get());
    }

    public static PFMPreset getDefaultPFMPreset(GenericFactory<IPFM> loader){
        return pfmPresets.get(loader.getName()).stream().filter(p -> p.presetName.equals("Default")).findFirst().get();
    }

    public static void registerPreset(PFMPreset preset){
        PFMMasterRegistry.pfmPresets.putIfAbsent(preset.pfmName, FXCollections.observableArrayList());
        PFMMasterRegistry.pfmPresets.get(preset.pfmName).add(preset);
    }

    public static void savePreset(PFMPreset preset){
        registerPreset(preset);
        if(preset.userCreated){
            updatePresetJSON();
        }
    }

    public static void updatePreset(PFMPreset preset){
        updatePresetJSON();
    }

    public static void deletePreset(PFMPreset preset){
        PFMMasterRegistry.pfmPresets.get(preset.pfmName).remove(preset);
        updatePresetJSON();
    }

    public static void updatePresetJSON(){
        DrawingBotV3.INSTANCE.backgroundService.submit(ConfigFileHandler::updatePresetJSON);
    }

    public static void loadUserCreatedPresets(Map<String, List<PFMPreset>> presets){
        presets.values().forEach(p -> p.forEach(PFMMasterRegistry::registerPreset));
    }

    //// JAVA FX \\\\

    public static ObservableList<GenericFactory<IPFM>> getObservablePFMLoaderList(){
        ObservableList<GenericFactory<IPFM>> list = FXCollections.observableArrayList();
        for(GenericFactory<IPFM> loader : pfmFactories.values()){
            if(!loader.isHidden() || ConfigFileHandler.settings.isDeveloperMode){
                list.add(loader);
            }
        }
        return list;
    }

    public static ObservableList<GenericSetting<?, ?>> getObservablePFMSettingsList(){
        return getObservablePFMSettingsList(DrawingBotV3.INSTANCE.pfmLoader.get());
    }

    public static ObservableList<GenericSetting<?, ?>> getObservablePFMSettingsList(GenericFactory<IPFM> loader){
        return pfmSettings.get(loader.getInstanceClass());
    }

    public static ObservableList<PFMPreset> getObservablePFMPresetList(){
        return getObservablePFMPresetList(DrawingBotV3.INSTANCE.pfmLoader.get());
    }

    public static ObservableList<PFMPreset> getObservablePFMPresetList(GenericFactory<IPFM> loader){
        return pfmPresets.get(loader.getName());
    }


    public static class PFMHashMap<P extends IPFM> extends LinkedHashMap<Class<P>, ObservableList<GenericSetting<P, ?>>>{}
}