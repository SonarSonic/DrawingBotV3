package drawingbot.image;

import com.jhlabs.image.*;
import drawingbot.DrawingBotV3;
import drawingbot.files.presets.types.PresetImageFilters;
import drawingbot.image.filters.EnumEdgeAction;
import drawingbot.image.filters.EnumEdgeDetect;
import drawingbot.image.filters.EnumWaveType;
import drawingbot.javafx.controls.DialogImageFilter;
import drawingbot.utils.EnumFilterTypes;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Dialog;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

//TODO IMPROVE FILTERS!!!
public class ImageFilterRegistry {

    public static Map<EnumFilterTypes, ObservableList<GenericFactory<BufferedImageOp>>> filterFactories = FXCollections.observableHashMap();
    public static ObservableList<ObservableImageFilter> currentFilters = FXCollections.observableArrayList();
    public static HashMap<Class<? extends BufferedImageOp>, List<GenericSetting<?, ?>>> filterSettings = new LinkedHashMap<>();
    public static HashMap<Class<? extends BufferedImageOp>, Function<ObservableImageFilter, Dialog<ObservableImageFilter>>> dialogs = new LinkedHashMap<>();
    public static ObservableList<GenericPreset<PresetImageFilters>> imagePresets = FXCollections.observableArrayList();

    /// TODO BEING ABLE TO EDIT IMAGE FILTERS AFTER FIRST PLOT?
    /// TODO FIX BORDER FILTERS

    static {

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        registerImageFilter(EnumFilterTypes.BLUR, BoxBlurFilter.class, "Box Blur", BoxBlurFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(BoxBlurFilter.class, "H Radius", 0F, 0, 100F, false, BoxBlurFilter::setHRadius));
        registerSetting(GenericSetting.createRangedFloatSetting(BoxBlurFilter.class, "V Radius", 0F, 0, 100F, false, BoxBlurFilter::setVRadius));
        registerSetting(GenericSetting.createRangedIntSetting(BoxBlurFilter.class, "Iterations", 1, 0, 10, false, BoxBlurFilter::setIterations));
        registerSetting(GenericSetting.createBooleanSetting(BoxBlurFilter.class, "Premultiply", true, false, BoxBlurFilter::setPremultiplyAlpha));

        //// MISSING: CONVOLVE

        //// MISSING: DESPECKLE

        registerImageFilter(EnumFilterTypes.BLUR, BumpFilter.class, "Emboss Edges", BumpFilter::new, false);

        registerImageFilter(EnumFilterTypes.BLUR, MotionBlurOp.class, "Motion Blur - Fast", MotionBlurOp::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(MotionBlurOp.class, "Centre X", 0.5F, 0, 1, false, MotionBlurOp::setCentreX));
        registerSetting(GenericSetting.createRangedFloatSetting(MotionBlurOp.class, "Centre Y", 0.5F, 0, 1, false, MotionBlurOp::setCentreY));
        registerSetting(GenericSetting.createRangedFloatSetting(MotionBlurOp.class, "Angle", 0, 0, 360, false, MotionBlurOp::setAngle));
        registerSetting(GenericSetting.createRangedFloatSetting(MotionBlurOp.class, "Distance", 0, 0, 200, false, MotionBlurOp::setDistance));
        registerSetting(GenericSetting.createRangedFloatSetting(MotionBlurOp.class, "Rotation", 0, -180, 180, false, MotionBlurOp::setRotation));
        registerSetting(GenericSetting.createRangedFloatSetting(MotionBlurOp.class, "Zoom", 0, 0, 100, false, MotionBlurOp::setZoom));

        registerImageFilter(EnumFilterTypes.BLUR, GaussianFilter.class, "Gaussian Blur", GaussianFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(GaussianFilter.class, "Radius", 0, 0, 100, false, GaussianFilter::setRadius));
        registerSetting(GenericSetting.createBooleanSetting(GaussianFilter.class, "Premultiply", true, false, GaussianFilter::setPremultiplyAlpha));

        registerImageFilter(EnumFilterTypes.BLUR, GlowFilter.class, "Glow", GlowFilter::new, false); //extends Gaussian Blur
        registerSetting(GenericSetting.createBooleanSetting(GlowFilter.class, "Premultiply", true, false, GlowFilter::setPremultiplyAlpha));

        registerImageFilter(EnumFilterTypes.BLUR, HighPassFilter.class, "High Pass", HighPassFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(HighPassFilter.class, "Softness", 0, 0, 100, false, HighPassFilter::setRadius));


        registerImageFilter(EnumFilterTypes.BLUR, LensBlurFilter.class, "Lens Blur", LensBlurFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(LensBlurFilter.class, "Radius", 10, 0, 50, false, LensBlurFilter::setRadius));
        registerSetting(GenericSetting.createRangedIntSetting(LensBlurFilter.class, "Sides", 5, 3, 12, false, LensBlurFilter::setSides));
        registerSetting(GenericSetting.createRangedFloatSetting(LensBlurFilter.class, "Bloom", 2F, 1.0F, 8.0F, false, LensBlurFilter::setBloom));
        registerSetting(GenericSetting.createRangedFloatSetting(LensBlurFilter.class, "Bloom Threshold", 255F, 0F, 255F, false, LensBlurFilter::setBloomThreshold));

        registerImageFilter(EnumFilterTypes.BLUR, MaximumFilter.class, "Maximum", MaximumFilter::new, false);

        registerImageFilter(EnumFilterTypes.BLUR, MedianFilter.class, "Median", MedianFilter::new, false);

        registerImageFilter(EnumFilterTypes.BLUR, MinimumFilter.class, "Minimum", MinimumFilter::new, false);

        registerImageFilter(EnumFilterTypes.BLUR, MinimumFilter.class, "Minimum", MinimumFilter::new, false);

        registerImageFilter(EnumFilterTypes.BLUR, MotionBlurFilter.class, "Motion Blur Slow", MotionBlurFilter::new, false);
        registerSetting(GenericSetting.createBooleanSetting(MotionBlurFilter.class, "Wrap Edges", false, false, MotionBlurFilter::setWrapEdges));
        registerSetting(GenericSetting.createRangedFloatSetting(MotionBlurFilter.class, "Angle", 45, 0, 360, false, MotionBlurFilter::setAngle));
        registerSetting(GenericSetting.createRangedFloatSetting(MotionBlurFilter.class, "Distance", 100, 0, 200, false, MotionBlurFilter::setDistance));
        registerSetting(GenericSetting.createRangedFloatSetting(MotionBlurFilter.class, "Rotation", -180, 0, 180, false, MotionBlurFilter::setRotation));
        registerSetting(GenericSetting.createRangedFloatSetting(MotionBlurFilter.class, "Zoom", 20, 0, 100, false, MotionBlurFilter::setZoom));

        registerImageFilter(EnumFilterTypes.BLUR, SharpenFilter.class, "Sharpen", SharpenFilter::new, false);

        registerImageFilter(EnumFilterTypes.BLUR, BlurFilter.class, "Simple Blur", BlurFilter::new, false);

        registerImageFilter(EnumFilterTypes.BLUR, SmartBlurFilter.class, "Smart Blur", SmartBlurFilter::new, false);
        registerSetting(GenericSetting.createRangedIntSetting(SmartBlurFilter.class, "H Radius", 5, 0, 100, false, SmartBlurFilter::setHRadius));
        registerSetting(GenericSetting.createRangedIntSetting(SmartBlurFilter.class, "V Radius", 5, 0, 100, false, SmartBlurFilter::setVRadius));
        registerSetting(GenericSetting.createRangedIntSetting(SmartBlurFilter.class, "Threshold", 10, 0, 255, false, SmartBlurFilter::setThreshold));

        registerImageFilter(EnumFilterTypes.COLOURS, HSBAdjustFilter.class, "Adjust HSB", HSBAdjustFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(HSBAdjustFilter.class, "Hue", 0F, -1F, 1F, false, HSBAdjustFilter::setHFactor));
        registerSetting(GenericSetting.createRangedFloatSetting(HSBAdjustFilter.class, "Saturation", 0F, -1F, 1F, false, HSBAdjustFilter::setSFactor));
        registerSetting(GenericSetting.createRangedFloatSetting(HSBAdjustFilter.class, "Brightness", 0F, -1F, 1F, false, HSBAdjustFilter::setBFactor));

        //// MISSING: SMOOTH FILTER

        registerImageFilter(EnumFilterTypes.BLUR, UnsharpFilter.class, "Unsharp Mask", UnsharpFilter::new, false); //extends Gaussian Blur
        registerSetting(GenericSetting.createRangedFloatSetting(UnsharpFilter.class, "Amount", 0.5F, 0, 1, false, UnsharpFilter::setAmount));
        registerSetting(GenericSetting.createRangedIntSetting(UnsharpFilter.class, "Threshold", 1, 0, 255, false, UnsharpFilter::setThreshold));

        //// MISSING: VARIABLE BLUR

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        registerImageFilter(EnumFilterTypes.COLOURS, RGBAdjustFilter.class, "Adjust RGB", RGBAdjustFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(RGBAdjustFilter.class, "Red", 0F, -1F, 1F, false, RGBAdjustFilter::setRFactor));
        registerSetting(GenericSetting.createRangedFloatSetting(RGBAdjustFilter.class, "Green", 0F, -1F, 1F, false, RGBAdjustFilter::setGFactor));
        registerSetting(GenericSetting.createRangedFloatSetting(RGBAdjustFilter.class, "Blue", 0F, -1F, 1F, false, RGBAdjustFilter::setBFactor));

        registerImageFilter(EnumFilterTypes.COLOURS, ContrastFilter.class, "Contrast", ContrastFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(ContrastFilter.class, "Brightness", 1F, 0, 2F, false, ContrastFilter::setBrightness));
        registerSetting(GenericSetting.createRangedFloatSetting(ContrastFilter.class, "Contrast", 1F, 0, 2F, false, ContrastFilter::setContrast));

        //// MISSING: DIFFUSION DITHER

        //// MISSING: DITHER

        registerImageFilter(EnumFilterTypes.COLOURS, ExposureFilter.class, "Exposure", ExposureFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(ExposureFilter.class, "Exposure", 1F, 0, 5F, false, ExposureFilter::setExposure));

        registerImageFilter(EnumFilterTypes.COLOURS, GainFilter.class, "Gain", GainFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(GainFilter.class, "Gain", 0.5F, 0, 1F, false, GainFilter::setGain));
        registerSetting(GenericSetting.createRangedFloatSetting(GainFilter.class, "Bias", 0.5F, 0, 1F, false, GainFilter::setBias));

        registerImageFilter(EnumFilterTypes.COLOURS, GammaFilter.class, "Gamma", GammaFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(GammaFilter.class, "Gamma", 1F, 0, 3F, false, GammaFilter::setGamma));

        registerImageFilter(EnumFilterTypes.COLOURS, GrayFilter.class, "Gray Out", GrayFilter::new, false);

        registerImageFilter(EnumFilterTypes.COLOURS, InvertFilter.class, "Invert", InvertFilter::new, false);

        registerImageFilter(EnumFilterTypes.COLOURS, LevelsFilter.class, "Levels", LevelsFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(LevelsFilter.class, "Low Level", 1F, 0, 1F, false, (filter, value) -> filter.setLowLevel(Math.min(value, filter.getHighLevel()))));
        registerSetting(GenericSetting.createRangedFloatSetting(LevelsFilter.class, "High Level", 1F, 0, 1F, false, (filter, value) -> filter.setHighLevel(Math.max(value, filter.getLowLevel()))));
        registerSetting(GenericSetting.createRangedFloatSetting(LevelsFilter.class, "Low Output Level", 1F, 0, 1F, false, (filter, value) -> filter.setLowOutputLevel(Math.min(value, filter.getHighOutputLevel()))));
        registerSetting(GenericSetting.createRangedFloatSetting(LevelsFilter.class, "Low Output Level", 1F, 0, 1F, false, (filter, value) -> filter.setHighOutputLevel(Math.max(value, filter.getLowOutputLevel()))));

        /// MISSING: LOOKUP

        registerImageFilter(EnumFilterTypes.COLOURS, ChannelMixFilter.class, "Mix Channels", ChannelMixFilter::new, false);
        registerSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Blue/Green", 0, 0, 255, false, ChannelMixFilter::setBlueGreen));
        registerSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Into Red", 0, 0, 255, false, ChannelMixFilter::setIntoR));
        registerSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Red/Blue", 0, 0, 255, false, ChannelMixFilter::setRedBlue));
        registerSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Into Green", 0, 0, 255, false, ChannelMixFilter::setIntoG));
        registerSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Green/Red", 0, 0, 255, false, ChannelMixFilter::setGreenRed));
        registerSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Into Blue", 0, 0, 255, false, ChannelMixFilter::setIntoB));

        registerImageFilter(EnumFilterTypes.COLOURS, PosterizeFilter.class, "Posterize", PosterizeFilter::new, false);
        registerSetting(GenericSetting.createRangedIntSetting(PosterizeFilter.class, "Posterize", 6, 0, 255, false, PosterizeFilter::setNumLevels));

        registerImageFilter(EnumFilterTypes.COLOURS, QuantizeFilter.class, "Quantize", QuantizeFilter::new, false);
        registerSetting(GenericSetting.createRangedIntSetting(QuantizeFilter.class, "Number of Colours", 255, 0, 255, false, QuantizeFilter::setNumColors));
        registerSetting(GenericSetting.createBooleanSetting(QuantizeFilter.class, "Dither", false, false, QuantizeFilter::setDither));
        registerSetting(GenericSetting.createBooleanSetting(QuantizeFilter.class, "Serpentine", false, false, QuantizeFilter::setSerpentine));

        registerImageFilter(EnumFilterTypes.COLOURS, RescaleFilter.class, "Rescale", RescaleFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(RescaleFilter.class, "Number of Colours", 1F, 0F, 5F, false, RescaleFilter::setScale));

        registerImageFilter(EnumFilterTypes.COLOURS, SolarizeFilter.class, "Solarize", SolarizeFilter::new, false);

        /// MISSING: TEMPERATURE FILTER

        registerImageFilter(EnumFilterTypes.COLOURS, OpacityFilter.class, "Transparency", OpacityFilter::new, false);
        registerSetting(GenericSetting.createRangedIntSetting(OpacityFilter.class, "Opacity", 255, 0, 255, false, OpacityFilter::setOpacity));

        /// MISSING: TRITONE FILTER

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        /// MISSING: BORDER

        /// MISSING: CIRCLE

        registerImageFilter(EnumFilterTypes.DISTORT, DiffuseFilter.class, "Diffuse", DiffuseFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(DiffuseFilter.class, "Scale", 4, 0, 100, false, DiffuseFilter::setScale));
        registerSetting(GenericSetting.createOptionSetting(DiffuseFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.TRANSPARENT, false, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        registerImageFilter(EnumFilterTypes.DISTORT, DisplaceFilter.class, "Displace", DisplaceFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(DisplaceFilter.class, "Amount", 1, 0, 100, false, DisplaceFilter::setAmount));
        registerSetting(GenericSetting.createOptionSetting(DisplaceFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.TRANSPARENT, false, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        registerImageFilter(EnumFilterTypes.DISTORT, KaleidoscopeFilter.class, "Kaleidoscope", KaleidoscopeFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(KaleidoscopeFilter.class, "Centre X", 0.5F, 0F, 1F, false, KaleidoscopeFilter::setCentreX));
        registerSetting(GenericSetting.createRangedFloatSetting(KaleidoscopeFilter.class, "Centre Y", 0.5F, 0F, 1F, false, KaleidoscopeFilter::setCentreY));
        registerSetting(GenericSetting.createRangedFloatSetting(KaleidoscopeFilter.class, "Angle", -180F, 0F, 180F, false, KaleidoscopeFilter::setAngle));
        registerSetting(GenericSetting.createRangedFloatSetting(KaleidoscopeFilter.class, "Angle 2", -180F, 0F, 180F, false, KaleidoscopeFilter::setAngle2));
        registerSetting(GenericSetting.createRangedFloatSetting(KaleidoscopeFilter.class, "Radius", 0F, 0F, 200F, false, KaleidoscopeFilter::setRadius));
        registerSetting(GenericSetting.createRangedIntSetting(KaleidoscopeFilter.class, "Sides", 3, 0, 32, false, KaleidoscopeFilter::setSides));
        registerSetting(GenericSetting.createOptionSetting(KaleidoscopeFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.CLAMP, false, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        registerImageFilter(EnumFilterTypes.DISTORT, MarbleFilter.class, "Marble", MarbleFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(MarbleFilter.class, "Scale", 4, 0, 100, false, (filter, value) -> {
            filter.setXScale(value);
            filter.setYScale(value);
        }));
        registerSetting(GenericSetting.createRangedFloatSetting(MarbleFilter.class, "Amount", 1, 0, 1, false, MarbleFilter::setAmount));
        registerSetting(GenericSetting.createRangedFloatSetting(MarbleFilter.class, "Turbulence", 1, 0, 15, false, MarbleFilter::setTurbulence));
        registerSetting(GenericSetting.createOptionSetting(MarbleFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.CLAMP, false, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        /// MISSING: OFFSET - NOT REALLY NEEDED

        /// MISSING: PINCH - REQUIRES ADDITIONAL RENDERING

        /// MISSING: POLAR COORDINATES


        registerImageFilter(EnumFilterTypes.DISTORT, RippleFilter.class, "Ripple", RippleFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(RippleFilter.class, "X Amplitude", 5F, 0F, 100F, false, RippleFilter::setXAmplitude));
        registerSetting(GenericSetting.createRangedFloatSetting(RippleFilter.class, "X Wavelength", 16F, 0F, 100F, false, RippleFilter::setXWavelength));
        registerSetting(GenericSetting.createRangedFloatSetting(RippleFilter.class, "Y Amplitude", 0F, 0F, 100F, false, RippleFilter::setYAmplitude));
        registerSetting(GenericSetting.createRangedFloatSetting(RippleFilter.class, "Y Wavelength", 16F, 0F, 100F, false, RippleFilter::setYWavelength));
        registerSetting(GenericSetting.createOptionSetting(RippleFilter.class, "Shape", List.of(EnumWaveType.values()), EnumWaveType.SINE, false, (filter, value) -> filter.setWaveType(value.getWaveType())));
        registerSetting(GenericSetting.createOptionSetting(RippleFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.TRANSPARENT, false, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        registerImageFilter(EnumFilterTypes.DISTORT, ShearFilter.class, "Shear", ShearFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(ShearFilter.class, "X Angle", -60F, 0F, 60F, false, ShearFilter::setXAngle));
        registerSetting(GenericSetting.createRangedFloatSetting(ShearFilter.class, "Y Angle", -60F, 0F, 60F, false, ShearFilter::setYAngle));
        registerSetting(GenericSetting.createOptionSetting(ShearFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.TRANSPARENT, false, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        /// MISSING: SPHERE - REQUIRES ADDITIONAL RENDERING

        registerImageFilter(EnumFilterTypes.DISTORT, SwimFilter.class, "Swim", SwimFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(SwimFilter.class, "Scale", 32F, 1F, 300F, false, SwimFilter::setScale));
        registerSetting(GenericSetting.createRangedFloatSetting(SwimFilter.class, "Angle", 0F, 0F, 360F, false, SwimFilter::setAngle));
        registerSetting(GenericSetting.createRangedFloatSetting(SwimFilter.class, "Stretch", 1F, 1F, 50F, false, SwimFilter::setStretch));
        registerSetting(GenericSetting.createRangedFloatSetting(SwimFilter.class, "Turbulence", 1F, 1F, 10F, false, SwimFilter::setTurbulence));
        registerSetting(GenericSetting.createRangedFloatSetting(SwimFilter.class, "Amount", 1F, 0F, 100F, false, SwimFilter::setAmount));
        registerSetting(GenericSetting.createRangedFloatSetting(SwimFilter.class, "Time", 0F, 0F, 100F, false, SwimFilter::setTime));
        registerSetting(GenericSetting.createOptionSetting(SwimFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.TRANSPARENT, false, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        /// MISSING: TWIRL - REQUIRES ADDITIONAL RENDERING

        /// MISSING: WATER RIPPLES - REQUIRES ADDITIONAL RENDERING

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        registerImageFilter(EnumFilterTypes.EDGES, EdgeFilter.class, "Detect Edges", EdgeFilter::new, false);
        registerSetting(GenericSetting.createOptionSetting(EdgeFilter.class, "Horizontal", List.of(EnumEdgeDetect.values()), EnumEdgeDetect.SOBEL, false, (filter, value) -> filter.setHEdgeMatrix(value.getHorizontalMatrix())));
        registerSetting(GenericSetting.createOptionSetting(EdgeFilter.class, "Vertical", List.of(EnumEdgeDetect.values()), EnumEdgeDetect.SOBEL, false, (filter, value) -> filter.setVEdgeMatrix(value.getVerticalMatrix())));

        /// MISSING DIFFERENCE OF GAUSSIANS

        registerImageFilter(EnumFilterTypes.EDGES, LaplaceFilter.class, "Laplace", LaplaceFilter::new, false);

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        
        currentFilters.addListener((ListChangeListener<ObservableImageFilter>) c -> {
            DrawingBotV3.onImageFiltersChanged();
        });
    }

    public static <I extends BufferedImageOp> void registerImageFilter(EnumFilterTypes filterType, Class<I> filterClass, String name, Supplier<I> create, boolean isHidden){
        filterFactories.putIfAbsent(filterType, FXCollections.observableArrayList());
        filterFactories.get(filterType).add(new GenericFactory(filterClass, name, create, isHidden));
    }

    public static void registerSetting(GenericSetting<? extends BufferedImageOp, ?> setting){
        filterSettings.putIfAbsent(setting.clazz, new ArrayList<>());
        filterSettings.get(setting.clazz).add(setting);
    }

    public static void registerDialog(Class<BufferedImageOp> filterClass, Function<ObservableImageFilter, Dialog<ObservableImageFilter>> dialog){
        filterSettings.putIfAbsent(filterClass, new ArrayList<>());
        dialogs.put(filterClass, dialog);
    }

    public static EnumFilterTypes getDefaultFilterType(){
        return filterFactories.keySet().stream().findFirst().orElseGet(null);
    }

    public static GenericFactory<BufferedImageOp> getDefaultFilter(EnumFilterTypes type){
        return filterFactories.get(type).stream().findFirst().orElseGet(null);
    }

    public static GenericFactory<BufferedImageOp> getFilterFromName(String filter){
        for(ObservableList<GenericFactory<BufferedImageOp>> factories : filterFactories.values()){
            for(GenericFactory<BufferedImageOp> factory : factories){
                if(factory.getName().equals(filter)){
                    return factory;
                }
            }
        }
        return null;
    }

    public static GenericPreset<PresetImageFilters> getDefaultImageFilterPreset(){
        return imagePresets.stream().filter(p -> p.presetName.equals("Default")).findFirst().get();
    }

    public static void registerPreset(GenericPreset<PresetImageFilters> preset){
        imagePresets.add(preset);
    }

    public static <V> void onSettingChanged(GenericSetting<?,V> setting, ObservableValue<?> observable, V oldValue, V newValue){
        ///not used at the moment, called whenever a setting's value is changed
    }

    public static BufferedImage applyCurrentFilters(BufferedImage image){
        for(BufferedImageOp filter : ImageFilterRegistry.createFilters()){
            image = filter.filter(image, null);
        }
        return image;
    }

    public static List<BufferedImageOp> createFilters(){
        List<BufferedImageOp> filters = new ArrayList<>();
        for(ObservableImageFilter filter : currentFilters){
            if(filter.enable.get()){
                BufferedImageOp instance = filter.filterFactory.instance();
                filter.filterSettings.forEach(setting -> setting.applySetting(instance));
                filters.add(instance);
            }
        }
        return filters;
    }

    public static ObservableList<GenericSetting<?,?>> getNewObservableSettingsList(GenericFactory<BufferedImageOp> filterFactory){
        ObservableList<GenericSetting<?, ?>> settings = FXCollections.observableArrayList();
        for(Map.Entry<Class<? extends BufferedImageOp>, List<GenericSetting<?, ?>>> entry : filterSettings.entrySet()){
            if(entry.getKey().isAssignableFrom(filterFactory.getInstanceClass())){
                for(GenericSetting<?, ?> setting : entry.getValue()){
                    settings.add(setting.copy());
                }
            }
        }
        return settings;
    }

    public static Dialog<ObservableImageFilter> getDialogForFilter(ObservableImageFilter filter){
        Function<ObservableImageFilter, Dialog<ObservableImageFilter>> func = dialogs.get(filter.filterFactory.getInstanceClass());
        return func == null ? new DialogImageFilter(filter) : func.apply(filter);
    }

    public static class ObservableImageFilter {

        public SimpleBooleanProperty enable;
        public SimpleStringProperty name;
        public GenericFactory<BufferedImageOp> filterFactory;
        public ObservableList<GenericSetting<?, ?>> filterSettings;
        public SimpleStringProperty settingsString; //settings as a string

        public ObservableImageFilter(GenericFactory<BufferedImageOp> filterFactory) {
            this(true, filterFactory.getName(), filterFactory, getNewObservableSettingsList(filterFactory));
        }

        public ObservableImageFilter(ObservableImageFilter duplicate) {
            this(duplicate.enable.get(), duplicate.name.get(), duplicate.filterFactory, GenericSetting.copy(duplicate.filterSettings, FXCollections.observableArrayList()));
        }

        public ObservableImageFilter(boolean enable, String name, GenericFactory<BufferedImageOp> filterFactory, ObservableList<GenericSetting<?, ?>> filterSettings){
            this.enable = new SimpleBooleanProperty(enable);
            this.name = new SimpleStringProperty(name);
            this.filterFactory = filterFactory;
            this.filterSettings = filterSettings;
            this.settingsString = new SimpleStringProperty(filterSettings.toString());

            this.filterSettings.forEach(s -> s.addListener((observable, oldValue, newValue) -> onSettingChanged()));
            this.enable.addListener((observable, oldValue, newValue) -> DrawingBotV3.onImageFiltersChanged());
            //changes to filter settings are called from the FXController
        }

        public void onSettingChanged(){
            ///actually updating the image filter rendering is done elsewhere, this shouldn't always change as values do
            this.settingsString.set(filterSettings.toString());
        }
    }

}
