package drawingbot.registry;

import com.jhlabs.image.*;
import drawingbot.api.IPlugin;
import drawingbot.drawing.*;
import drawingbot.plugins.CopicPenPlugin;
import drawingbot.plugins.SpecialPenPlugin;
import drawingbot.plugins.StaedtlerPenPlugin;
import drawingbot.files.DrawingExportHandler;
import drawingbot.files.FileUtils;
import drawingbot.files.exporters.*;
import drawingbot.files.presets.PresetType;
import drawingbot.files.presets.types.*;
import drawingbot.image.ImageTools;
import drawingbot.image.filters.*;
import drawingbot.integrations.vpype.PresetVpypeSettingsLoader;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.controls.DialogExportGCodeBegin;
import drawingbot.javafx.settings.DrawingStylesSetting;
import drawingbot.pfm.*;
import drawingbot.utils.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Register implements IPlugin {

    public static Register INSTANCE = new Register();

    //// PRESET LOADERS \\\\
    public static PresetType PRESET_TYPE_CONFIGS;
    public static ConfigJsonLoader PRESET_LOADER_CONFIGS;

    public static PresetType PRESET_TYPE_PROJECT;
    public static PresetProjectSettingsLoader PRESET_LOADER_PROJECT;

    public static PresetType PRESET_TYPE_PFM;
    public static PresetPFMSettingsLoader PRESET_LOADER_PFM;

    public static PresetType PRESET_TYPE_FILTERS;
    public static PresetImageFiltersLoader PRESET_LOADER_FILTERS;

    public static PresetType PRESET_TYPE_DRAWING_SET;
    public static PresetDrawingSetLoader PRESET_LOADER_DRAWING_SET;

    public static PresetType PRESET_TYPE_DRAWING_PENS;
    public static PresetDrawingPenLoader PRESET_LOADER_DRAWING_PENS;

    public static PresetType PRESET_TYPE_DRAWING_AREA;
    public static PresetDrawingAreaLoader PRESET_LOADER_DRAWING_AREA;

    public static PresetType PRESET_TYPE_GCODE_SETTINGS;
    public static PresetGCodeSettingsLoader PRESET_LOADER_GCODE_SETTINGS;

    public static PresetType PRESET_TYPE_VPYPE_SETTINGS;
    public static PresetVpypeSettingsLoader PRESET_LOADER_VPYPE_SETTINGS;
    @Override
    public String getPluginName() {
        return "Default";
    }

    @Override
    public void registerPlugins(List<IPlugin> newPlugins) {
        newPlugins.add(new CopicPenPlugin());
        newPlugins.add(new StaedtlerPenPlugin());
        newPlugins.add(new SpecialPenPlugin());
    }

    @Override
    public void preInit() {

        MasterRegistry.INSTANCE.registerPresetType(PRESET_TYPE_CONFIGS = new PresetType("config_settings"));
        MasterRegistry.INSTANCE.registerPresetType(PRESET_TYPE_PROJECT = new PresetType("project", new FileChooser.ExtensionFilter[]{FileUtils.FILTER_PROJECT}));
        MasterRegistry.INSTANCE.registerPresetType(PRESET_TYPE_PFM = new PresetType("pfm_settings").setDefaultsPerSubType(true));
        MasterRegistry.INSTANCE.registerPresetType(PRESET_TYPE_FILTERS = new PresetType("image_filters"));
        MasterRegistry.INSTANCE.registerPresetType(PRESET_TYPE_DRAWING_SET = new PresetType("drawing_set"));
        MasterRegistry.INSTANCE.registerPresetType(PRESET_TYPE_DRAWING_PENS = new PresetType("drawing_pen"));
        MasterRegistry.INSTANCE.registerPresetType(PRESET_TYPE_DRAWING_AREA = new PresetType("drawing_area"));
        MasterRegistry.INSTANCE.registerPresetType(PRESET_TYPE_GCODE_SETTINGS = new PresetType("gcode_settings"));
        MasterRegistry.INSTANCE.registerPresetType(PRESET_TYPE_VPYPE_SETTINGS = new PresetType("vpype_settings"));

        MasterRegistry.INSTANCE.registerPresetLoaders(PRESET_LOADER_CONFIGS = new ConfigJsonLoader(PRESET_TYPE_CONFIGS));
        MasterRegistry.INSTANCE.registerPresetLoaders(PRESET_LOADER_PROJECT = new PresetProjectSettingsLoader(PRESET_TYPE_PROJECT));
        MasterRegistry.INSTANCE.registerPresetLoaders(PRESET_LOADER_PFM = new PresetPFMSettingsLoader(PRESET_TYPE_PFM));
        MasterRegistry.INSTANCE.registerPresetLoaders(PRESET_LOADER_FILTERS = new PresetImageFiltersLoader(PRESET_TYPE_FILTERS));
        MasterRegistry.INSTANCE.registerPresetLoaders(PRESET_LOADER_DRAWING_SET = new PresetDrawingSetLoader(PRESET_TYPE_DRAWING_SET));
        MasterRegistry.INSTANCE.registerPresetLoaders(PRESET_LOADER_DRAWING_PENS = new PresetDrawingPenLoader(PRESET_TYPE_DRAWING_PENS));
        MasterRegistry.INSTANCE.registerPresetLoaders(PRESET_LOADER_DRAWING_AREA = new PresetDrawingAreaLoader(PRESET_TYPE_DRAWING_AREA));
        MasterRegistry.INSTANCE.registerPresetLoaders(PRESET_LOADER_GCODE_SETTINGS = new PresetGCodeSettingsLoader(PRESET_TYPE_GCODE_SETTINGS));
        MasterRegistry.INSTANCE.registerPresetLoaders(PRESET_LOADER_VPYPE_SETTINGS = new PresetVpypeSettingsLoader(PRESET_TYPE_VPYPE_SETTINGS));
    }

    @Override
    public void registerPFMS() {
        MasterRegistry.INSTANCE.registerPFM(PFMSketchLines.class, "Sketch Lines PFM", PFMSketchLines::new, false, true);
        MasterRegistry.INSTANCE.registerPFM(PFMSketchSquares.class, "Sketch Squares PFM", PFMSketchSquares::new, false, false);
        MasterRegistry.INSTANCE.registerPFM(PFMSpiral.class, "Spiral PFM", PFMSpiral::new, false, true).setTransparentCMYK(false);
        MasterRegistry.INSTANCE.registerPFM(PFMLayers.class, "Layers PFM", PFMLayers::new, true, true).setIsBeta(true);
        MasterRegistry.INSTANCE.registerPFM(PFMTest.class, "Test PFM", PFMTest::new, true, true).setDistributionType(EnumDistributionType.SINGLE_PEN);
    }

    @Override
    public void registerPFMSettings(){
        //// GENERAL \\\\
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(AbstractPFM.class, "Plotting Resolution", 1.0F, 0.1F, 10.0F, true, (pfmSketch, value) -> pfmSketch.pfmResolution = value).setSafeRange(0.1F, 1.0F));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(AbstractPFM.class, "Random Seed", 0, Integer.MIN_VALUE, Integer.MAX_VALUE, false, (pfmSketch, value) -> pfmSketch.seed = value));

        //// SKETCH LINES \\\\
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(PFMSketchLines.class, "Start Angle Min", -72, -360, 360, false, (pfmSketch, value) -> pfmSketch.startAngleMin = value));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(PFMSketchLines.class, "Start Angle Max", -52, -360, 360, false, (pfmSketch, value) -> pfmSketch.startAngleMax = value));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSketchLines.class, "Drawing Delta Angle", 360F, -360F, 360F, true, (pfmSketch, value) -> pfmSketch.drawingDeltaAngle = value));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createBooleanSetting(PFMSketchLines.class, "Shading", false, false, (pfmSketch, value) -> pfmSketch.enableShading = value));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSketchLines.class, "Shading Threshold", 50, 0, 100, false, (pfmSketch, value) -> pfmSketch.shadingThreshold = value/100));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSketchLines.class, "Shading Delta Angle", 180F, -360F, 360F, true, (pfmSketch, value) -> pfmSketch.shadingDeltaAngle = value));

        //// SKETCH SQUARES \\\\
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(PFMSketchSquares.class, "Start Angle", 45, -360, 360, false, (pfmSketch, value) -> pfmSketch.startAngle = value));

        //// SPIRAL \\\\
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, "Spiral Size", 100F, 0F, 100F, false, (pfmSketch, value) -> pfmSketch.fillPercentage = value/100));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, "Centre X", 50F, 0F, 100F, false, (pfmSketch, value) -> pfmSketch.centreXScale = value/100));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, "Centre Y", 50F, 0F, 100F, false, (pfmSketch, value) -> pfmSketch.centreYScale = value/100));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, "Ring Spacing", 7F, 0F, 100F, false, (pfmSketch, value) -> pfmSketch.distBetweenRings = value));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, "Amplitude", 4.5F, 0F, 50F, false, (pfmSketch, value) -> pfmSketch.ampScale = value));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, "Density", 75F, 0F, 1000F, false, (pfmSketch, value) -> pfmSketch.density = value));

        //// ABSTRACT SKETCH PFM \\\\
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(AbstractSketchPFM.class, "Line Density", 75F, 0F, 100F, true, (pfmSketch, value) -> pfmSketch.lineDensity = value/100));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, "Min Line length", 2, 2, Short.MAX_VALUE, false, (pfmSketch, value) -> pfmSketch.minLineLength = value).setSafeRange(2, 500));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, "Max Line length", 40, 2, Short.MAX_VALUE, false, (pfmSketch, value) -> pfmSketch.maxLineLength = value).setSafeRange(2, 500));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, "Max Line Limit", -1, -1, Integer.MAX_VALUE, true, (pfmSketch, value) -> pfmSketch.maxLines = value));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, "Squiggle Length", 500, 1, Short.MAX_VALUE, false, (pfmSketch, value) -> pfmSketch.squiggleLength = value).setSafeRange(1, 5000));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(AbstractSketchPFM.class, "Squiggle Max Deviation", 25, 0, 100, false, (pfmSketch, value) -> pfmSketch.squiggleDeviation = value/100F));

        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, "Adjust Brightness", 50, 1, 255, false, (pfmSketch, value) -> pfmSketch.adjustbrightness = value));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createBooleanSetting(PFMSketchLines.class, "Unlimited Tests", false, true, (pfmSketch, value) -> pfmSketch.unlimitedTests = value));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, "Neighbour Tests", 20, 1, 3200, false, (pfmSketch, value) -> pfmSketch.lineTests = value).setSafeRange(0, 360));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createBooleanSetting(AbstractSketchPFM.class, "Should Lift Pen", true, false, (pfmSketch, value) -> pfmSketch.shouldLiftPen = value));




        //// LAYERS PFM \\\\
        MasterRegistry.INSTANCE.registerPFMSetting(new DrawingStylesSetting<>(PFMLayers.class, "Drawing Styles", new DrawingStyleSet(new ArrayList<>()), true, (pfmSketch, value) -> pfmSketch.drawingStyles = value));
        MasterRegistry.INSTANCE.removePFMSettingByName(PFMLayers.class, "Plotting Resolution");
    }

    @Override
    public void registerDrawingTools(){


    }

    @Override
    public void registerImageFilters(){


        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BORDERS, SimpleBorderFilter.class, "Dirty Border", SimpleBorderFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SimpleBorderFilter.class, "Type", 1, 1, 13, true, (filter, value) -> filter.borderNumber = value));


        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, BoxBlurFilter.class, "Box Blur", BoxBlurFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(BoxBlurFilter.class, "H Radius", 0F, 0, 100F, false, BoxBlurFilter::setHRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(BoxBlurFilter.class, "V Radius", 0F, 0, 100F, false, BoxBlurFilter::setVRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(BoxBlurFilter.class, "Iterations", 1, 0, 10, false, BoxBlurFilter::setIterations));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(BoxBlurFilter.class, "Premultiply", true, false, BoxBlurFilter::setPremultiplyAlpha));

        //// MISSING: CONVOLVE

        //// MISSING: DESPECKLE

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, BumpFilter.class, "Emboss Edges", BumpFilter::new, false);

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, MotionBlurOp.class, "Motion Blur - Fast", MotionBlurOp::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MotionBlurOp.class, "Centre X", 0.5F, 0, 1, false, MotionBlurOp::setCentreX));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MotionBlurOp.class, "Centre Y", 0.5F, 0, 1, false, MotionBlurOp::setCentreY));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(MotionBlurOp.class, "Angle", 0, 0, 360, false, MotionBlurOp::setAngle).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MotionBlurOp.class, "Distance", 0, 0, 200, false, MotionBlurOp::setDistance));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(MotionBlurOp.class, "Rotation", 0, -180, 180, false, MotionBlurOp::setRotation).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MotionBlurOp.class, "Zoom", 0, 0, 100, false, MotionBlurOp::setZoom));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, GaussianFilter.class, "Gaussian Blur", GaussianFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(GaussianFilter.class, "Radius", 0, 0, 100, false, GaussianFilter::setRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(GaussianFilter.class, "Premultiply", true, false, GaussianFilter::setPremultiplyAlpha));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, GlowFilter.class, "Glow", GlowFilter::new, false); //extends Gaussian Blur
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(GlowFilter.class, "Premultiply", true, false, GlowFilter::setPremultiplyAlpha));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, HighPassFilter.class, "High Pass", HighPassFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(HighPassFilter.class, "Softness", 0, 0, 100, false, HighPassFilter::setRadius));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, LensBlurFilter.class, "Lens Blur", LensBlurFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(LensBlurFilter.class, "Radius", 10, 0, 50, false, LensBlurFilter::setRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(LensBlurFilter.class, "Sides", 5, 3, 12, false, LensBlurFilter::setSides));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(LensBlurFilter.class, "Bloom", 2F, 1.0F, 8.0F, false, LensBlurFilter::setBloom));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(LensBlurFilter.class, "Bloom Threshold", 255F, 0F, 255F, false, LensBlurFilter::setBloomThreshold));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, MaximumFilter.class, "Maximum", MaximumFilter::new, false);

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, MedianFilter.class, "Median", MedianFilter::new, false);

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, MinimumFilter.class, "Minimum", MinimumFilter::new, false);

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, MotionBlurFilter.class, "Motion Blur Slow", MotionBlurFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(MotionBlurFilter.class, "Wrap Edges", false, false, MotionBlurFilter::setWrapEdges));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(MotionBlurFilter.class, "Angle", 45, 0, 360, false, MotionBlurFilter::setAngle).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MotionBlurFilter.class, "Distance", 100, 0, 200, false, MotionBlurFilter::setDistance));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MotionBlurFilter.class, "Rotation", -180, 0, 180, false, MotionBlurFilter::setRotation));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MotionBlurFilter.class, "Zoom", 20, 0, 100, false, MotionBlurFilter::setZoom));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, SharpenFilter.class, "Sharpen", SharpenFilter::new, false);

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, BlurFilter.class, "Simple Blur", BlurFilter::new, false);

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, SmartBlurFilter.class, "Smart Blur", SmartBlurFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SmartBlurFilter.class, "H Radius", 5, 0, 100, false, SmartBlurFilter::setHRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SmartBlurFilter.class, "V Radius", 5, 0, 100, false, SmartBlurFilter::setVRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SmartBlurFilter.class, "Threshold", 10, 0, 255, false, SmartBlurFilter::setThreshold));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, HSBAdjustFilter.class, "Adjust HSB", HSBAdjustFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(HSBAdjustFilter.class, "Hue", 0F, -1F, 1F, false, HSBAdjustFilter::setHFactor));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(HSBAdjustFilter.class, "Saturation", 0F, -1F, 1F, false, HSBAdjustFilter::setSFactor));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(HSBAdjustFilter.class, "Brightness", 0F, -1F, 1F, false, HSBAdjustFilter::setBFactor));

        //// MISSING: SMOOTH FILTER

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, UnsharpFilter.class, "Unsharp Mask", UnsharpFilter::new, false); //extends Gaussian Blur
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(UnsharpFilter.class, "Amount", 0.5F, 0, 1, false, UnsharpFilter::setAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(UnsharpFilter.class, "Threshold", 1, 0, 255, false, UnsharpFilter::setThreshold));

        //// MISSING: VARIABLE BLUR

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, RGBAdjustFilter.class, "Adjust RGB", RGBAdjustFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RGBAdjustFilter.class, "Red", 0F, -1F, 1F, false, RGBAdjustFilter::setRFactor));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RGBAdjustFilter.class, "Green", 0F, -1F, 1F, false, RGBAdjustFilter::setGFactor));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RGBAdjustFilter.class, "Blue", 0F, -1F, 1F, false, RGBAdjustFilter::setBFactor));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, ContrastFilter.class, "Contrast", ContrastFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ContrastFilter.class, "Brightness", 1F, 0, 2F, false, ContrastFilter::setBrightness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ContrastFilter.class, "Contrast", 1F, 0, 2F, false, ContrastFilter::setContrast));

        //// MISSING: DIFFUSION DITHER

        //// MISSING: DITHER

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, ExposureFilter.class, "Exposure", ExposureFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ExposureFilter.class, "Exposure", 1F, 0, 5F, false, ExposureFilter::setExposure));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, GainFilter.class, "Gain", GainFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(GainFilter.class, "Gain", 0.5F, 0, 1F, false, GainFilter::setGain));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(GainFilter.class, "Bias", 0.5F, 0, 1F, false, GainFilter::setBias));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, GammaFilter.class, "Gamma", GammaFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(GammaFilter.class, "Gamma", 1F, 0, 3F, false, GammaFilter::setGamma));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, GrayscaleFilter.class, "Grayscale", GrayscaleFilter::new, false);

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, InvertFilter.class, "Invert", InvertFilter::new, false);

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, LevelsFilter.class, "Levels", LevelsFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(LevelsFilter.class, "Low Level", 1F, 0, 1F, false, (filter, value) -> filter.setLowLevel(Math.min(value, filter.getHighLevel()))));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(LevelsFilter.class, "High Level", 1F, 0, 1F, false, (filter, value) -> filter.setHighLevel(Math.max(value, filter.getLowLevel()))));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(LevelsFilter.class, "Low Output Level", 1F, 0, 1F, false, (filter, value) -> filter.setLowOutputLevel(Math.min(value, filter.getHighOutputLevel()))));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(LevelsFilter.class, "Low Output Level", 1F, 0, 1F, false, (filter, value) -> filter.setHighOutputLevel(Math.max(value, filter.getLowOutputLevel()))));

        /// MISSING: LOOKUP

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, ChannelMixFilter.class, "Mix Channels", ChannelMixFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Blue/Green", 0, 0, 255, false, ChannelMixFilter::setBlueGreen));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Into Red", 0, 0, 255, false, ChannelMixFilter::setIntoR));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Red/Blue", 0, 0, 255, false, ChannelMixFilter::setRedBlue));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Into Green", 0, 0, 255, false, ChannelMixFilter::setIntoG));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Green/Red", 0, 0, 255, false, ChannelMixFilter::setGreenRed));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Into Blue", 0, 0, 255, false, ChannelMixFilter::setIntoB));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, PosterizeFilter.class, "Posterize", PosterizeFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(PosterizeFilter.class, "Posterize", 6, 0, 255, false, PosterizeFilter::setNumLevels));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, QuantizeFilter.class, "Quantize", QuantizeFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(QuantizeFilter.class, "Number of Colours", 255, 0, 255, false, QuantizeFilter::setNumColors));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(QuantizeFilter.class, "Dither", false, false, QuantizeFilter::setDither));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(QuantizeFilter.class, "Serpentine", false, false, QuantizeFilter::setSerpentine));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, RescaleFilter.class, "Rescale", RescaleFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RescaleFilter.class, "Number of Colours", 1F, 0F, 5F, false, RescaleFilter::setScale));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, SolarizeFilter.class, "Solarize", SolarizeFilter::new, false);

        /// MISSING: TEMPERATURE FILTER

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, OpacityFilter.class, "Transparency", OpacityFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(OpacityFilter.class, "Opacity", 255, 0, 255, false, OpacityFilter::setOpacity));

        /// MISSING: TRITONE FILTER

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        /// MISSING: BORDER

        /// MISSING: CIRCLE

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.DISTORT, DiffuseFilter.class, "Diffuse", DiffuseFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(DiffuseFilter.class, "Scale", 4, 0, 100, false, DiffuseFilter::setScale));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(DiffuseFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.TRANSPARENT, false, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.DISTORT, DisplaceFilter.class, "Displace", DisplaceFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(DisplaceFilter.class, "Amount", 1, 0, 100, false, DisplaceFilter::setAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(DisplaceFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.TRANSPARENT, false, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.DISTORT, KaleidoscopeFilter.class, "Kaleidoscope", KaleidoscopeFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(KaleidoscopeFilter.class, "Centre X", 0.5F, 0F, 1F, false, KaleidoscopeFilter::setCentreX));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(KaleidoscopeFilter.class, "Centre Y", 0.5F, 0F, 1F, false, KaleidoscopeFilter::setCentreY));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(KaleidoscopeFilter.class, "Angle", -180, 0, 180, false, KaleidoscopeFilter::setAngle).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(KaleidoscopeFilter.class, "Angle 2", -180, 0, 180, false, KaleidoscopeFilter::setAngle2).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(KaleidoscopeFilter.class, "Radius", 0F, 0F, 200F, false, KaleidoscopeFilter::setRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(KaleidoscopeFilter.class, "Sides", 3, 0, 32, false, KaleidoscopeFilter::setSides));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(KaleidoscopeFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.CLAMP, false, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.DISTORT, MarbleFilter.class, "Marble", MarbleFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MarbleFilter.class, "Scale", 4, 0, 100, false, (filter, value) -> {
            filter.setXScale(value);
            filter.setYScale(value);
        }));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MarbleFilter.class, "Amount", 1, 0, 1, false, MarbleFilter::setAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MarbleFilter.class, "Turbulence", 1, 0, 15, false, MarbleFilter::setTurbulence));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(MarbleFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.CLAMP, false, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        /// MISSING: OFFSET - NOT REALLY NEEDED

        /// MISSING: PINCH - REQUIRES ADDITIONAL RENDERING

        /// MISSING: POLAR COORDINATES

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.DISTORT, RippleFilter.class, "Ripple", RippleFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RippleFilter.class, "X Amplitude", 5F, 0F, 100F, false, RippleFilter::setXAmplitude));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RippleFilter.class, "X Wavelength", 16F, 0F, 100F, false, RippleFilter::setXWavelength));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RippleFilter.class, "Y Amplitude", 0F, 0F, 100F, false, RippleFilter::setYAmplitude));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RippleFilter.class, "Y Wavelength", 16F, 0F, 100F, false, RippleFilter::setYWavelength));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(RippleFilter.class, "Shape", List.of(EnumWaveType.values()), EnumWaveType.SINE, false, (filter, value) -> filter.setWaveType(value.getWaveType())));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(RippleFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.TRANSPARENT, false, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.DISTORT, ShearFilter.class, "Shear", ShearFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ShearFilter.class, "X Angle", -60F, 0F, 60F, false, ShearFilter::setXAngle).setMajorTick(30F));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ShearFilter.class, "Y Angle", -60F, 0F, 60F, false, ShearFilter::setYAngle).setMajorTick(30F));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(ShearFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.TRANSPARENT, false, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        /// MISSING: SPHERE - REQUIRES ADDITIONAL RENDERING

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.DISTORT, SwimFilter.class, "Swim", SwimFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(SwimFilter.class, "Scale", 32F, 1F, 300F, false, SwimFilter::setScale));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SwimFilter.class, "Angle", 0, 0, 360, false, SwimFilter::setAngle).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(SwimFilter.class, "Stretch", 1F, 1F, 50F, false, SwimFilter::setStretch));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(SwimFilter.class, "Turbulence", 1F, 1F, 10F, false, SwimFilter::setTurbulence));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(SwimFilter.class, "Amount", 1F, 0F, 100F, false, SwimFilter::setAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(SwimFilter.class, "Time", 0F, 0F, 100F, false, SwimFilter::setTime));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(SwimFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.TRANSPARENT, false, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        /// MISSING: TWIRL - REQUIRES ADDITIONAL RENDERING

        /// MISSING: WATER RIPPLES - REQUIRES ADDITIONAL RENDERING

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.EDGES, EdgeFilter.class, "Detect Edges", EdgeFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(EdgeFilter.class, "Horizontal", List.of(EnumEdgeDetect.values()), EnumEdgeDetect.SOBEL, false, (filter, value) -> filter.setHEdgeMatrix(value.getHorizontalMatrix())));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(EdgeFilter.class, "Vertical", List.of(EnumEdgeDetect.values()), EnumEdgeDetect.SOBEL, false, (filter, value) -> filter.setVEdgeMatrix(value.getVerticalMatrix())));

        /// MISSING DIFFERENCE OF GAUSSIANS

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.EDGES, LaplaceFilter.class, "Laplace", LaplaceFilter::new, false);

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        /////EFFECTS

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.EFFECTS, ChromeFilter.class, "Chrome", ChromeFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ChromeFilter.class, "Softness", 5.0f, 0F, 50F, false, ChromeFilter::setBumpSoftness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ChromeFilter.class, "Height", 1.0f, 0F, 5F, false, ChromeFilter::setBumpHeight));
        //// MISSING SETTING, BUMP SOURCE
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ChromeFilter.class, "Amount", 0.5f, 0F, 1F, false, ChromeFilter::setAmount));//percentage
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ChromeFilter.class, "Exposure", 1.0f, 0F, 5F, false, ChromeFilter::setExposure));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(ChromeFilter.class, "Color", Color.BLACK, false, (filter, value) -> filter.setDiffuseColor(ImageTools.getARGBFromColor(value))));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.EFFECTS, FeedbackFilter.class, "Feedback", FeedbackFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "Centre X", 0.5F, 0F, 1F, false, FeedbackFilter::setCentreX));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "Centre Y", 0.5F, 0F, 1F, false, FeedbackFilter::setCentreY));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(FeedbackFilter.class, "Angle", 0, 0, 360, false, FeedbackFilter::setAngle).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "Distance", 0F, 0F, 200F, false, FeedbackFilter::setDistance));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(FeedbackFilter.class, "Rotation", -180, 0, 180, false, FeedbackFilter::setRotation).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "Zoom", 0F, -1, 1F, false, FeedbackFilter::setZoom));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(FeedbackFilter.class, "Iterations", 2, 2, 100, false, FeedbackFilter::setIterations));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "Start Alpha", 1F, 0F, 1F, false, FeedbackFilter::setStartAlpha));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "End Alpha", 1F, 0F, 1F, false, FeedbackFilter::setEndAlpha));

        ///GLINT
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.EFFECTS, GlintFilter.class, "Glint", GlintFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(GlintFilter.class, "Threshold", 1F, 0F, 1F, false, GlintFilter::setThreshold));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(GlintFilter.class, "Amount", 0.1F, 0F, 1F, false, GlintFilter::setAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(GlintFilter.class, "Length", 5, 0, 50, false, GlintFilter::setLength));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(GlintFilter.class, "Blur", 0F, 0F, 20F, false, GlintFilter::setBlur));
        ///MISSING SETTING COLOUR MAP
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(GlintFilter.class, "Glint Only", false, false, GlintFilter::setGlintOnly));

        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MirrorFilter.class, "Centre Y", 0.5F, 0F, 1F, false, MirrorFilter::setCentreY));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MirrorFilter.class, "Gap", 0F, 0F, 1F, false, MirrorFilter::setGap));


        ///INTERPOLATE

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.EFFECTS, MirrorFilter.class, "Mirror", MirrorFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MirrorFilter.class, "Opacity", 1F, 0F, 1F, false, MirrorFilter::setOpacity));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MirrorFilter.class, "Centre Y", 0.5F, 0F, 1F, false, MirrorFilter::setCentreY));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MirrorFilter.class, "Gap", 0F, 0F, 1F, false, MirrorFilter::setGap));


        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        /////KEYING

        ///CHROMA KEY
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.KEYING, ChromaKeyFilter.class, "Chroma Key", ChromaKeyFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ChromaKeyFilter.class, "H Tolerance", 0F, 0F, 1F, false, ChromaKeyFilter::setHTolerance));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ChromaKeyFilter.class, "S Tolerance", 0F, 0F, 1F, false, ChromaKeyFilter::setSTolerance));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ChromaKeyFilter.class, "B Tolerance", 0F, 0F, 1F, false, ChromaKeyFilter::setBTolerance));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(ChromaKeyFilter.class, "Color", new Color(23F/255F, 255/255F, 23/255F, 255/255F), false, (filter, value) -> filter.setColor(ImageTools.getARGBFromColor(value))));


        ///KEY

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        /////PIXELLATE

        ///COLOR HALFTONE
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.PIXELLATE, ColorHalftoneFilter.class, "Color Halftone", ColorHalftoneFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ColorHalftoneFilter.class, "Max Radius", 2F, 1F, 30F, false, ColorHalftoneFilter::setdotRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ColorHalftoneFilter.class, "Cyan Screen Angle", 108, 0, 360, false, (filter, value) -> filter.setCyanScreenAngle((float)Math.toRadians(value))).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ColorHalftoneFilter.class, "Magenta Screen Angle", 162, 0, 360, false, (filter, value) -> filter.setMagentaScreenAngle((float)Math.toRadians(value))).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ColorHalftoneFilter.class, "Yellow Screen Angle", 90, 0, 360, false, (filter, value) -> filter.setYellowScreenAngle((float)Math.toRadians(value))).setMajorTick(90));

        ///CRYSTALISE
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.PIXELLATE, CrystallizeFilter.class, "Crystallize", CrystallizeFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(CrystallizeFilter.class, "Size", 16F, 1F, 100F, false, CrystallizeFilter::setScale));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(CrystallizeFilter.class, "Angle", 0, 0, 360, false, CrystallizeFilter::setAngle).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(CrystallizeFilter.class, "Randomness", 0F, 0F, 1F, false, CrystallizeFilter::setRandomness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(CrystallizeFilter.class, "Edges", 0.4F, 0F, 1F, false, CrystallizeFilter::setEdgeThickness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(CrystallizeFilter.class, "Grid Type", List.of(EnumPixellateGridType.values()), EnumPixellateGridType.HEXAGONAL, false, (filter, value) -> filter.setGridType(value.getGridType())));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(CrystallizeFilter.class, "Fade Edges", false, false, CrystallizeFilter::setFadeEdges));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(CrystallizeFilter.class, "Edge Colour", Color.BLACK, false, (filter, value) -> filter.setEdgeColor(ImageTools.getARGBFromColor(value))));

        ///MOSIAC

        ///POINTILIZE
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.PIXELLATE, PointillizeFilter.class, "Pointillize", PointillizeFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(PointillizeFilter.class, "Size", 16F, 1F, 100F, false, PointillizeFilter::setScale));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(PointillizeFilter.class, "Angle", 0, 0, 360, false, PointillizeFilter::setAngle).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(PointillizeFilter.class, "Randomness", 0F, 0F, 1F, false, PointillizeFilter::setRandomness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(PointillizeFilter.class, "Dot Size", 0.4F, 0F, 1F, false, PointillizeFilter::setEdgeThickness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(PointillizeFilter.class, "Fuzziness", 0.1F, 0F, 1F, false, PointillizeFilter::setFuzziness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(PointillizeFilter.class, "Grid Type", List.of(EnumPixellateGridType.values()), EnumPixellateGridType.HEXAGONAL, false, (filter, value) -> filter.setGridType(value.getGridType())));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(PointillizeFilter.class, "Fill", false, false, PointillizeFilter::setFadeEdges));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(PointillizeFilter.class, "Edge Colour", Color.BLACK, false, (filter, value) -> filter.setEdgeColor(ImageTools.getARGBFromColor(value))));


        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        //////RENDER

        ///SCRATCHES
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.RENDER, ScratchFilter.class, "Scratches", ScratchFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ScratchFilter.class, "Density", 0.1F, 0F, 1F, false, ScratchFilter::setDensity));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ScratchFilter.class, "Length", 0.5F, 0F, 1F, false, ScratchFilter::setLength));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ScratchFilter.class, "Width", 0.5F, 0F, 1F, false, ScratchFilter::setWidth));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ScratchFilter.class, "Angle Variation", 1.0F, 0F, 1F, false, ScratchFilter::setAngleVariation));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ScratchFilter.class, "Seed", 0, 0, 100, false, ScratchFilter::setSeed));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(ScratchFilter.class, "Colour", Color.WHITE, false, (filter, value) -> filter.setColor(ImageTools.getARGBFromColor(value))));


        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        //////STYLIZE

        /// ADD NOISE
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, NoiseFilter.class, "Noise", NoiseFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(NoiseFilter.class, "Amount", 25, 0, 100, false, NoiseFilter::setAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(NoiseFilter.class, "Density", 1.0F, 0F, 1F, false, NoiseFilter::setDensity));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(NoiseFilter.class, "Grid Type", List.of(EnumNoiseDistribution.values()), EnumNoiseDistribution.GAUSSIAN, false, (filter, value) -> filter.setDistribution(value.getDistribution())));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(NoiseFilter.class, "Monochrome", false, false, NoiseFilter::setMonochrome));

        /// CONTOURS
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, ContourFilter.class, "Contours", ContourFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ContourFilter.class, "Levels", 5F, 0F, 30F, false, ContourFilter::setLevels));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ContourFilter.class, "Offset", 0F, 0F, 1F, false, ContourFilter::setOffset));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ContourFilter.class, "Scale", 1F, 0F, 1F, false, ContourFilter::setScale));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(ContourFilter.class, "Colour", Color.BLACK, false, (filter, value) -> filter.setContourColor(ImageTools.getARGBFromColor(value))));

        ///DISSOLVE
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, DissolveFilter.class, "Dissolve", DissolveFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(DissolveFilter.class, "Density", 1.0F, 0F, 1F, false, DissolveFilter::setDensity));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(DissolveFilter.class, "Softness", 0F, 0F, 1F, false, DissolveFilter::setSoftness));

        ///DROP SHADOW
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, ShadowFilter.class, "Drop Shadow", ShadowFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ShadowFilter.class, "Angle", (float)Math.PI*6/4, 0F, 360F, false, ShadowFilter::setAngle).setMajorTick(90F));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ShadowFilter.class, "Distance", 5F, 0F, 100F, false, ShadowFilter::setDistance));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ShadowFilter.class, "Softness", 5F, 0F, 100F, false, ShadowFilter::setRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ShadowFilter.class, "Opacity", 0.5F, 0F, 1F, false, ShadowFilter::setOpacity));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(ShadowFilter.class, "Shadow Colour", Color.BLACK, false, (filter, value) -> filter.setShadowColor(ImageTools.getARGBFromColor(value))));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(ShadowFilter.class, "Monochrome", false, false, ShadowFilter::setShadowOnly));

        ///EMBOSS
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, EmbossFilter.class, "Emboss", EmbossFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(EmbossFilter.class, "Light Direction", 135.0F * ImageMath.PI / 180.0F, 0F, 360F, false, EmbossFilter::setAzimuth).setMajorTick(90F));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(EmbossFilter.class, "Light Elevation", 30.0F * ImageMath.PI / 180.0F, 0F, 90F, false, EmbossFilter::setElevation).setMajorTick(90F));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(EmbossFilter.class, "Bump Height", 1F, 0F, 1F, false, EmbossFilter::setBumpHeight));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(EmbossFilter.class, "Texture", false, false, EmbossFilter::setEmboss));

        ///FLARE - ADDITIONAL RENDERING REQUIRED
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, FlareFilter.class, "Flare", FlareFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "X Position", 0.5F, 0F, 1F, false, (filter, value) -> filter.setCentre(new Point2D.Float(value, (float)filter.getCentre().getY()))));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Y Position", 0.5F, 0F, 1F, false, (filter, value) -> filter.setCentre(new Point2D.Float((float)filter.getCentre().getX(), value))));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Radius", 0F, 0F, 400F, false, FlareFilter::setRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Base", 1.0F, 0F, 1F, false, FlareFilter::setBaseAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Ring", 0.2f, 0F, 1F, false, FlareFilter::setRingAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Ring Width", 1.6f, 0F, 10F, false, FlareFilter::setRingWidth));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Ray", 0.1f, 0F, 1F, false, FlareFilter::setRayAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(FlareFilter.class, "Colour", Color.WHITE, false, (filter, value) -> filter.setColor(ImageTools.getARGBFromColor(value))));

        ///HALFTONE - MASK NEEDED
        //MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, HalftoneFilter.class, "Halftone", HalftoneFilter::new, false);
        //registerSetting(GenericSetting.createRangedFloatSetting(HalftoneFilter.class, "Softness", 0.1f, 0F, 1F, false, HalftoneFilter::setSoftness));

        ///LIGHT EFFECTS - COMPLICATED

        ///OIL
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, OilFilter.class, "Oil", OilFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(OilFilter.class, "Range", 3, 1, 5, false, OilFilter::setRange));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(OilFilter.class, "Levels", 256, 1, 256, false, OilFilter::setLevels));


        ///RAYS
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, RaysFilter.class, "Rays", RaysFilter::new, false); //extends MotionBlurOp
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RaysFilter.class, "Opacity", 1.0F, 0F, 1.0F, false, RaysFilter::setOpacity));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RaysFilter.class, "Strength", 0.5F, 0F, 5.0F, false, RaysFilter::setStrength));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RaysFilter.class, "Threshold", 0.0F, 0F, 1.0F, false, RaysFilter::setThreshold));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(RaysFilter.class, "Rays Only", false, false, RaysFilter::setRaysOnly));
        //missing color map

        ///SHAPEBURST
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, ShapeFilter.class, "Shape Burst", ShapeFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ShapeFilter.class, "Factor", 1.0F, 0F, 5.0F, false, ShapeFilter::setFactor));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(ShapeFilter.class, "Shape Type", List.of(EnumShapeFilter.values()), EnumShapeFilter.LINEAR, false, (filter, value) -> filter.setType(value.getShapeType())));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(ShapeFilter.class, "Invert", false, false, ShapeFilter::setInvert));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(ShapeFilter.class, "Merge", false, false, ShapeFilter::setMerge));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(ShapeFilter.class, "Use Alpha", false, false, ShapeFilter::setUseAlpha));
        //missing color map

        ///SPARKLE
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, SparkleFilter.class, "Sparkle", SparkleFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SparkleFilter.class, "Range", 50, 0, 300, false, SparkleFilter::setRays));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SparkleFilter.class, "Radius", 25, 0, 300, false, SparkleFilter::setRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SparkleFilter.class, "Shine", 50, 0, 100, false, SparkleFilter::setAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SparkleFilter.class, "Randomness", 25, 0, 50, false, SparkleFilter::setRandomness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(SparkleFilter.class, "Colour", Color.WHITE, false, (filter, value) -> filter.setColor(ImageTools.getARGBFromColor(value))));


        ///STAMP
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, StampFilter.class, "Stamp", StampFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(StampFilter.class, "Radius", 5.0F, 0F, 100F, false, StampFilter::setRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(StampFilter.class, "Threshold", 0.5F, 0F, 1.0F, false, StampFilter::setThreshold));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(StampFilter.class, "Softness", 0F, 0F, 1.0F, false, StampFilter::setSoftness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(StampFilter.class, "Lower Colour", Color.BLACK, false, (filter, value) -> filter.setBlack(ImageTools.getARGBFromColor(value))));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(StampFilter.class, "Upper Colour", Color.WHITE, false, (filter, value) -> filter.setWhite(ImageTools.getARGBFromColor(value))));


        ///THRESHOLD
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, ThresholdFilter.class, "Threshold", ThresholdFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ThresholdFilter.class, "Lower Threshold", 127, 0, 255, false, ThresholdFilter::setLowerThreshold));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ThresholdFilter.class, "Upper Threshold", 127, 0, 255, false, ThresholdFilter::setUpperThreshold));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(ThresholdFilter.class, "Lower Colour", Color.BLACK, false, (filter, value) -> filter.setBlack(ImageTools.getARGBFromColor(value))));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(ThresholdFilter.class, "Upper Colour", Color.WHITE, false, (filter, value) -> filter.setWhite(ImageTools.getARGBFromColor(value))));

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        ///TEXTURES - NOT FOR NOW

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        ///TRANSITION - NOT FOR NOW

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        ///VIDEO - NOT FOR NOW

        //MasterRegistry.INSTANCE.registerImageFilterKernelFactory(new AbstractKernelFactory());
    }

    public static DrawingExportHandler EXPORT_SVG, EXPORT_INKSCAPE_SVG, EXPORT_IMAGE, EXPORT_HPGL, EXPORT_PDF, EXPORT_GCODE, EXPORT_GCODE_TEST;

    @Override
    public void registerDrawingExportHandlers(){
        EXPORT_SVG = MasterRegistry.INSTANCE.registerDrawingExportHandler(new DrawingExportHandler(DrawingExportHandler.Category.SVG, "Export SVG (.svg)", true, SVGExporter::exportBasicSVG, FileUtils.FILTER_SVG));
        EXPORT_INKSCAPE_SVG = MasterRegistry.INSTANCE.registerDrawingExportHandler(new DrawingExportHandler(DrawingExportHandler.Category.SVG, "Export Inkscape SVG (.svg)", true, SVGExporter::exportInkscapeSVG, FileUtils.FILTER_SVG));
        EXPORT_IMAGE = MasterRegistry.INSTANCE.registerDrawingExportHandler(new DrawingExportHandler(DrawingExportHandler.Category.IMAGE, "Export Image File (.png, .jpg, etc.)", false, ImageExporter::exportImage, FileUtils.FILTER_PNG, FileUtils.FILTER_JPG, FileUtils.FILTER_TIF, FileUtils.FILTER_TGA));
        EXPORT_PDF = MasterRegistry.INSTANCE.registerDrawingExportHandler(new DrawingExportHandler(DrawingExportHandler.Category.VECTOR, "Export PDF (.pdf)", true, PDFExporter::exportPDF, FileUtils.FILTER_PDF));
        EXPORT_GCODE = MasterRegistry.INSTANCE.registerDrawingExportHandler(new DrawingExportHandler(DrawingExportHandler.Category.VECTOR, "Export GCode File (.gcode, .txt)", true, GCodeExporter::exportGCode, e -> new DialogExportGCodeBegin(), FileUtils.FILTER_GCODE, FileUtils.FILTER_TXT));
        EXPORT_GCODE_TEST = MasterRegistry.INSTANCE.registerDrawingExportHandler(new DrawingExportHandler(DrawingExportHandler.Category.VECTOR, "Export GCode Test Drawing (.gcode, .txt)", true, GCodeExporter::exportGCodeTest, e -> new DialogExportGCodeBegin(), FileUtils.FILTER_GCODE, FileUtils.FILTER_TXT));
     }

    public static ColourSplitterHandler DEFAULT_COLOUR_SPLITTER;

    @Override
    public void registerColourSplitterHandlers(){
        DEFAULT_COLOUR_SPLITTER = MasterRegistry.INSTANCE.registerColourSplitter(new ColourSplitterHandler("Default", List::of, ColourSplitterHandler::createDefaultDrawingSet, List.of("Original")));
     }

}
