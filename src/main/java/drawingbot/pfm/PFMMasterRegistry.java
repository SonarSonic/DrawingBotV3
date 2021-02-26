package drawingbot.pfm;

import drawingbot.DrawingBotV3;
import drawingbot.api.IPathFindingModule;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.files.presets.types.PresetPFMSettings;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.GenericPreset;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class PFMMasterRegistry {

    public static HashMap<Class<? extends IPathFindingModule>, ObservableList<GenericSetting<?, ?>>> pfmSettings = new LinkedHashMap<>();
    public static HashMap<Class<? extends IPathFindingModule>, GenericFactory<IPathFindingModule>> pfmFactories = new LinkedHashMap<>();
    public static HashMap<String, ObservableList<GenericPreset<PresetPFMSettings>>> pfmPresets = new LinkedHashMap<>();

    static{
        registerPFMFactory(PFMSketch.class, "Sketch PFM", PFMSketch::new, false);
        registerPFMFactory(PFMSquares.class, "Squares PFM", PFMSquares::new, false);
        registerPFMFactory(PFMSpiral.class, "Spiral PFM", PFMSpiral::new, false);
        registerPFMFactory(PFMLines.class, "Lines PFM (Experimental)", PFMLines::new, true);
        registerPFMFactory(PFMSquiggleDraw.class, "Squiggle Draw PFM (Experimental)", PFMSquiggleDraw::new, true);

        ////GENERAL
        registerSetting(GenericSetting.createRangedFloatSetting(AbstractPFM.class, "Plotting Resolution", 1.0F, 0.1F, 1.0F, true, (pfmSketch, value) -> pfmSketch.pfmResolution = value));
        registerSetting(GenericSetting.createRangedIntSetting(AbstractPFM.class, "Random Seed", 0, Integer.MIN_VALUE, Integer.MAX_VALUE, false, (pfmSketch, value) -> pfmSketch.seed = value));

        ////ABSTRACT SKETCH PFM
        registerSetting(GenericSetting.createRangedFloatSetting(AbstractSketchPFM.class, "Desired Brightness", 250F, 0F, 255F, true, (pfmSketch, value) -> pfmSketch.desired_brightness = value));
        registerSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, "Squiggle Length", 500, 1, Short.MAX_VALUE, false, (pfmSketch, value) -> pfmSketch.squiggle_length = value));
        registerSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, "Adjust Brightness", 50, 1, 255, false, (pfmSketch, value) -> pfmSketch.adjustbrightness = value));
        registerSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, "Neighbour Tests", 20, 1, 720, false, (pfmSketch, value) -> pfmSketch.tests = value));
        registerSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, "Min Line length", 20, 1, Short.MAX_VALUE, false, (pfmSketch, value) -> pfmSketch.minLineLength = value));
        registerSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, "Max Line length", 40, 1, Short.MAX_VALUE, false, (pfmSketch, value) -> pfmSketch.maxLineLength = value));
        registerSetting(GenericSetting.createBooleanSetting(AbstractSketchPFM.class, "Should Lift Pen", true, false, (pfmSketch, value) -> pfmSketch.shouldLiftPen = value));

        ////SKETCH PFM
        registerSetting(GenericSetting.createRangedIntSetting(PFMSketch.class, "Start Angle Min", -72, -360, 360, false, (pfmSketch, value) -> pfmSketch.startAngleMin = value));
        registerSetting(GenericSetting.createRangedIntSetting(PFMSketch.class, "Start Angle Max", -52, -360, 360, false, (pfmSketch, value) -> pfmSketch.startAngleMax = value));
        registerSetting(GenericSetting.createRangedFloatSetting(PFMSketch.class, "Drawing Delta Angle", 360F, -360F, 360F, true, (pfmSketch, value) -> pfmSketch.drawingDeltaAngle = value));
        registerSetting(GenericSetting.createRangedFloatSetting(PFMSketch.class, "Shading Delta Angle", 180F, -360F, 360F, true, (pfmSketch, value) -> pfmSketch.shadingDeltaAngle = value));
        registerSetting(GenericSetting.createBooleanSetting(PFMSketch.class, "Enable Shading", true, true, (pfmSketch, value) -> pfmSketch.enableShading = value));
        registerSetting(GenericSetting.createRangedIntSetting(PFMSketch.class, "Squiggles till shading", 190, 1, Short.MAX_VALUE, false, (pfmSketch, value) -> pfmSketch.squigglesTillShading = value));

        ////SQUARES PFM

        ////SPIRAL PFM
        registerSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, "Distance between rings", 7F, 0F, Short.MAX_VALUE, false, (pfmSketch, value) -> pfmSketch.distBetweenRings = value));
        registerSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, "Density", 75F, 0F, Short.MAX_VALUE, false, (pfmSketch, value) -> pfmSketch.density = value));
        registerSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, "Amplitude", 4.5F, 0F, Short.MAX_VALUE, false, (pfmSketch, value) -> pfmSketch.ampScale = value));
    }

    //// PFM LOADERS \\\\

    public static void registerPFMFactory(Class<? extends IPathFindingModule> pfmClass, String name, Supplier<IPathFindingModule> create, boolean isHidden){
        pfmFactories.put(pfmClass, new GenericFactory(pfmClass, name, create, isHidden));
        registerPreset(JsonLoaderManager.PFM.createNewPreset(name, "Default", false));
    }

    public static GenericFactory<IPathFindingModule> getDefaultPFMFactory(){
        return pfmFactories.get(PFMSketch.class);
    }

    //// PFM SETTING \\\\

    public static <C, V> void registerSetting(GenericSetting<C, V> setting){
        for(GenericFactory<IPathFindingModule> loader : PFMMasterRegistry.pfmFactories.values()){
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

    public static <P extends IPathFindingModule> void applySettings(P pfm){
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

    public static GenericPreset<PresetPFMSettings> getDefaultPFMPreset(){
        return getDefaultPFMPreset(DrawingBotV3.pfmFactory.get());
    }

    public static GenericPreset<PresetPFMSettings> getDefaultPFMPreset(GenericFactory<IPathFindingModule> loader){
        return pfmPresets.get(loader.getName()).stream().filter(p -> p.presetName.equals("Default")).findFirst().get();
    }

    public static void registerPreset(GenericPreset<PresetPFMSettings> preset){
        PFMMasterRegistry.pfmPresets.putIfAbsent(preset.presetSubType, FXCollections.observableArrayList());
        PFMMasterRegistry.pfmPresets.get(preset.presetSubType).add(preset);
    }

    //// JAVA FX \\\\

    public static ObservableList<GenericFactory<IPathFindingModule>> getObservablePFMLoaderList(){
        ObservableList<GenericFactory<IPathFindingModule>> list = FXCollections.observableArrayList();
        for(GenericFactory<IPathFindingModule> loader : pfmFactories.values()){
            if(!loader.isHidden() || ConfigFileHandler.getApplicationSettings().isDeveloperMode){
                list.add(loader);
            }
        }
        return list;
    }

    /**the current settings for the PFM*/
    public static ObservableList<GenericSetting<?, ?>> getObservablePFMSettingsList(){
        return getObservablePFMSettingsList(DrawingBotV3.pfmFactory.get());
    }

    public static ObservableList<GenericSetting<?, ?>> getObservablePFMSettingsList(GenericFactory<IPathFindingModule> loader){
        return pfmSettings.get(loader.getInstanceClass());
    }

    public static ObservableList<GenericPreset<PresetPFMSettings>> getObservablePFMPresetList(){
        return getObservablePFMPresetList(DrawingBotV3.pfmFactory.get());
    }

    public static ObservableList<GenericPreset<PresetPFMSettings>> getObservablePFMPresetList(GenericFactory<IPathFindingModule> loader){
        return pfmPresets.get(loader.getName());
    }


    public static class PFMHashMap<P extends IPathFindingModule> extends LinkedHashMap<Class<P>, ObservableList<GenericSetting<P, ?>>>{}
}