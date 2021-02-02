package drawingbot.pfm;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class PFMSettingsRegistry<P> {

    public static HashMap<Class<? extends IPFM>, List<PFMSetting<? extends IPFM, ?>>> settingsMap = new HashMap<>();

    static{
        ////GENERAL
        registerSetting(PFMSetting.createRangedFloatSetting(AbstractPFM.class, "Plotting Resolution", 1.0F, 0.1F, 10F, (pfmSketch, value) -> pfmSketch.plottingResolution = value));
        registerSetting(PFMSetting.createRangedLongSetting(AbstractPFM.class, "Random Seed", 0L, Long.MIN_VALUE, Long.MAX_VALUE, (pfmSketch, value) -> pfmSketch.seed = value));

        ////ABSTRACT SKETCH PFM
        registerSetting(PFMSetting.createRangedFloatSetting(AbstractSketchPFM.class, "Desired Brightness", 250F, 0F, 255F, (pfmSketch, value) -> pfmSketch.desired_brightness = value));
        registerSetting(PFMSetting.createRangedIntSetting(AbstractSketchPFM.class, "Squiggle Length", 500, 1, Integer.MAX_VALUE, (pfmSketch, value) -> pfmSketch.squiggle_length = value));
        registerSetting(PFMSetting.createRangedIntSetting(AbstractSketchPFM.class, "Adjust Brightness", 50, 1, 255, (pfmSketch, value) -> pfmSketch.adjustbrightness = value));
        registerSetting(PFMSetting.createRangedIntSetting(AbstractSketchPFM.class, "Neighbour Tests", 20, 1, 720, (pfmSketch, value) -> pfmSketch.tests = value));
        registerSetting(PFMSetting.createRangedIntSetting(AbstractSketchPFM.class, "Min Line length", 20, 1, Integer.MAX_VALUE, (pfmSketch, value) -> pfmSketch.minLineLength = value));
        registerSetting(PFMSetting.createRangedIntSetting(AbstractSketchPFM.class, "Max Line length", 40, 1, Integer.MAX_VALUE, (pfmSketch, value) -> pfmSketch.maxLineLength = value));
        registerSetting(PFMSetting.createBooleanSetting(AbstractSketchPFM.class, "Should Lift Pen", true, (pfmSketch, value) -> pfmSketch.shouldLiftPen = value));

        ////SKETCH PFM
        registerSetting(PFMSetting.createRangedIntSetting(PFMSketch.class, "Start Angle Min", -72, -360, 360, (pfmSketch, value) -> pfmSketch.startAngleMin = value));
        registerSetting(PFMSetting.createRangedIntSetting(PFMSketch.class, "Start Angle Max", -52, -360, 360, (pfmSketch, value) -> pfmSketch.startAngleMax = value));
        registerSetting(PFMSetting.createRangedFloatSetting(PFMSketch.class, "Drawing Delta Angle", 360F, -360F, 360F, (pfmSketch, value) -> pfmSketch.drawingDeltaAngle = value));
        registerSetting(PFMSetting.createRangedFloatSetting(PFMSketch.class, "Shading Delta Angle", 180F, -360F, 360F, (pfmSketch, value) -> pfmSketch.shadingDeltaAngle = value));
        registerSetting(PFMSetting.createBooleanSetting(PFMSketch.class, "Enable Shading", true, (pfmSketch, value) -> pfmSketch.enableShading = value));
        registerSetting(PFMSetting.createRangedIntSetting(PFMSketch.class, "Squiggles till shading", 190, 1, Integer.MAX_VALUE, (pfmSketch, value) -> pfmSketch.squigglesTillShading = value));

        ////SQUARES PFM

        ////SPIRAL PFM
        registerSetting(PFMSetting.createRangedFloatSetting(PFMSpiral.class, "Distance between rings", 7F, 0F, Short.MAX_VALUE, (pfmSketch, value) -> pfmSketch.distBetweenRings = value));
        registerSetting(PFMSetting.createRangedFloatSetting(PFMSpiral.class, "Density", 75F, 0F, Short.MAX_VALUE, (pfmSketch, value) -> pfmSketch.density = value));
        registerSetting(PFMSetting.createRangedFloatSetting(PFMSpiral.class, "Amplitude", 4.5F, 0F, Short.MAX_VALUE, (pfmSketch, value) -> pfmSketch.ampScale = value));


    }

    public static void registerSetting(PFMSetting<?, ?> setting){
        setting.registerSetting();
    }

    public static <V> void onSettingChanged(PFMSetting<?,V> setting, ObservableValue<?> observable, V oldValue, V newValue){
        ///update setter // getter
    }

    public static <P extends IPFM> void applySettings(P pfm){
        for(Map.Entry<Class<? extends IPFM>, List<PFMSetting<? extends IPFM, ?>>> entry : settingsMap.entrySet()){
            if(entry.getKey().isAssignableFrom(pfm.getClass())){
                for(PFMSetting<?, ?> setting : entry.getValue()){
                    setting.applySetting(pfm);
                }
            }
        }
    }

    public static ObservableList<PFMSetting<? extends IPFM, ?>> getSettingsFromLoader(PFMLoaders loader){
        ObservableList<PFMSetting<? extends IPFM, ?>> list = FXCollections.observableArrayList();
        for(Map.Entry<Class<? extends IPFM>, List<PFMSetting<? extends IPFM, ?>>> entry : settingsMap.entrySet()){
            if(entry.getKey().isAssignableFrom(loader.getPFMClass())){
                list.addAll(entry.getValue());
            }
        }
        return list;
    }

    public static void resetSettings(List<PFMSetting<?, ?>> settingList){
        for(PFMSetting<?, ?> setting : settingList){
            setting.resetSetting();
        }
    }

    public static void randomiseSettings(List<PFMSetting<?, ?>> settingList){
        for(PFMSetting<?, ?> setting : settingList){
            setting.randomiseSetting(ThreadLocalRandom.current());
        }
    }

}
