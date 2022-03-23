package drawingbot.registry;

import com.jhlabs.image.*;
import drawingbot.api.IPlugin;
import drawingbot.files.json.presets.*;
import drawingbot.utils.EnumDistributionType;
import drawingbot.utils.EnumFilterTypes;
import drawingbot.drawing.*;
import drawingbot.geom.shapes.*;
import drawingbot.plugins.CopicPenPlugin;
import drawingbot.plugins.DiamineInkPlugin;
import drawingbot.plugins.SpecialPenPlugin;
import drawingbot.plugins.StaedtlerPenPlugin;
import drawingbot.files.DrawingExportHandler;
import drawingbot.files.FileUtils;
import drawingbot.files.exporters.*;
import drawingbot.files.json.PresetType;
import drawingbot.image.ImageTools;
import drawingbot.image.filters.*;
import drawingbot.integrations.vpype.PresetVpypeSettingsLoader;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.controls.DialogExportGCodeBegin;
import drawingbot.pfm.*;
import drawingbot.render.IDisplayMode;
import drawingbot.render.modes.DrawingJFXDisplayMode;
import drawingbot.render.modes.ImageJFXDisplayMode;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.awt.geom.Point2D;
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

    //// SETTINGS CATEGORIES \\\\

    public static final String CATEGORY_DEFAULT = "Default"; // Priority = 10
    public static final String CATEGORY_UNIQUE = "Unique"; // Priority = 5
    public static final String CATEGORY_GENERIC = "Generic"; // Priority = 0

    //// DISPLAY MODES \\\\
    public IDisplayMode DISPLAY_MODE_IMAGE;
    public IDisplayMode DISPLAY_MODE_DRAWING;
    public IDisplayMode DISPLAY_MODE_ORIGINAL;
    public IDisplayMode DISPLAY_MODE_REFERENCE;
    public IDisplayMode DISPLAY_MODE_LIGHTENED;
    public IDisplayMode DISPLAY_MODE_SELECTED_PEN;

    @Override
    public String getPluginName() {
        return "Default";
    }

    @Override
    public void registerPlugins(List<IPlugin> newPlugins) {
        newPlugins.add(new CopicPenPlugin());
        newPlugins.add(new DiamineInkPlugin());
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

        MasterRegistry.INSTANCE.registerGeometryType("line", GLine.class, GLine::new);
        MasterRegistry.INSTANCE.registerGeometryType("path", GPath.class, GPath::new);
        MasterRegistry.INSTANCE.registerGeometryType("cubic", GCubicCurve.class, GCubicCurve::new);
        MasterRegistry.INSTANCE.registerGeometryType("quad", GQuadCurve.class, GQuadCurve::new);
        MasterRegistry.INSTANCE.registerGeometryType("rect", GRectangle.class, GRectangle::new);
        MasterRegistry.INSTANCE.registerGeometryType("ellipse", GEllipse.class, GEllipse::new);

        MasterRegistry.INSTANCE.registerSettingCategory(CATEGORY_DEFAULT, 10);
        MasterRegistry.INSTANCE.registerSettingCategory(CATEGORY_UNIQUE, 5);
        MasterRegistry.INSTANCE.registerSettingCategory(CATEGORY_GENERIC, 0);

        MasterRegistry.INSTANCE.registerDisplayMode(DISPLAY_MODE_IMAGE = new ImageJFXDisplayMode.Image());
        MasterRegistry.INSTANCE.registerDisplayMode(DISPLAY_MODE_DRAWING = new DrawingJFXDisplayMode.Drawing());
        MasterRegistry.INSTANCE.registerDisplayMode(DISPLAY_MODE_ORIGINAL = new ImageJFXDisplayMode.Original());
        MasterRegistry.INSTANCE.registerDisplayMode(DISPLAY_MODE_REFERENCE = new ImageJFXDisplayMode.Reference());
        MasterRegistry.INSTANCE.registerDisplayMode(DISPLAY_MODE_LIGHTENED = new ImageJFXDisplayMode.Lightened());
        MasterRegistry.INSTANCE.registerDisplayMode(DISPLAY_MODE_SELECTED_PEN = new DrawingJFXDisplayMode.SelectedPen());
    }

    @Override
    public void registerPFMS() {
        MasterRegistry.INSTANCE.registerPFM(PFMSketchLines.class, "Sketch Lines PFM", PFMSketchLines::new, false, true).hasSampledARGB(true);
        MasterRegistry.INSTANCE.registerPFM(PFMSketchSquares.class, "Sketch Squares PFM", PFMSketchSquares::new, false, false).hasSampledARGB(true);
        MasterRegistry.INSTANCE.registerPFM(PFMSpiral.class, "Spiral PFM", PFMSpiral::new, false, true).setTransparentCMYK(false);
        MasterRegistry.INSTANCE.registerPFM(PFMTest.class, "Test PFM", PFMTest::new, true, true).setDistributionType(EnumDistributionType.SINGLE_PEN);
    }

    @Override
    public void registerPFMSettings(){
        //// GENERAL \\\\
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(AbstractPFM.class, CATEGORY_DEFAULT, "Plotting Resolution", 1.0F, 0.1F, 10.0F, (pfm, value) -> pfm.pfmResolution = value).setSafeRange(0.1F, 1.0F).setRandomiseExclude(true));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(AbstractPFM.class, CATEGORY_DEFAULT, "Random Seed", 0, Integer.MIN_VALUE, Integer.MAX_VALUE, (pfm, value) -> pfm.seed = value));

        //// SKETCH LINES \\\\
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(PFMSketchLines.class, CATEGORY_GENERIC, "Start Angle Min", -72, -360, 360, (pfm, value) -> pfm.startAngleMin = value));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(PFMSketchLines.class, CATEGORY_GENERIC, "Start Angle Max", -52, -360, 360, (pfm, value) -> pfm.startAngleMax = value));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSketchLines.class, CATEGORY_GENERIC, "Drawing Delta Angle", 360F, -360F, 360F, (pfm, value) -> pfm.drawingDeltaAngle = value).setRandomiseExclude(true));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createBooleanSetting(PFMSketchLines.class, CATEGORY_GENERIC, "Shading", false, (pfm, value) -> pfm.enableShading = value));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSketchLines.class, CATEGORY_GENERIC, "Shading Threshold", 50, 0, 100, (pfm, value) -> pfm.shadingThreshold = value/100));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSketchLines.class, CATEGORY_GENERIC, "Shading Delta Angle", 180F, -360F, 360F, (pfm, value) -> pfm.shadingDeltaAngle = value).setRandomiseExclude(true));

        //// SKETCH SQUARES \\\\
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(PFMSketchSquares.class, CATEGORY_UNIQUE, "Start Angle", 45, -360, 360, (pfm, value) -> pfm.startAngle = value));

        //// SPIRAL \\\\
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, CATEGORY_UNIQUE, "Spiral Size", 100F, 0F, 100F, (pfm, value) -> pfm.fillPercentage = value/100));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, CATEGORY_UNIQUE, "Centre X", 50F, 0F, 100F, (pfm, value) -> pfm.centreXScale = value/100));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, CATEGORY_UNIQUE, "Centre Y", 50F, 0F, 100F, (pfm, value) -> pfm.centreYScale = value/100));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, CATEGORY_UNIQUE, "Ring Spacing", 7F, 0F, 100F, (pfm, value) -> pfm.distBetweenRings = value));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, CATEGORY_UNIQUE, "Amplitude", 4.5F, 0F, 50F, (pfm, value) -> pfm.ampScale = value));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(PFMSpiral.class, CATEGORY_UNIQUE, "Density", 75F, 0F, 1000F, (pfm, value) -> pfm.density = value));

        //// ABSTRACT SKETCH PFM \\\\
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(AbstractSketchPFM.class, CATEGORY_GENERIC, "Line Density", 75F, 0F, 100F, (pfm, value) -> pfm.lineDensity = value/100).setRandomiseExclude(true));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, CATEGORY_GENERIC, "Min Line length", 2, 2, Short.MAX_VALUE, (pfm, value) -> pfm.minLineLength = value).setSafeRange(2, 500));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, CATEGORY_GENERIC, "Max Line length", 40, 2, Short.MAX_VALUE, (pfm, value) -> pfm.maxLineLength = value).setSafeRange(2, 500));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, CATEGORY_GENERIC, "Max Line Limit", -1, -1, Integer.MAX_VALUE, (pfm, value) -> pfm.maxLines = value).setRandomiseExclude(true));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, CATEGORY_GENERIC, "Squiggle Length", 500, 1, Short.MAX_VALUE, (pfm, value) -> pfm.squiggleLength = value).setSafeRange(1, 5000));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedFloatSetting(AbstractSketchPFM.class, CATEGORY_GENERIC, "Squiggle Max Deviation", 25, 0, 100, (pfm, value) -> pfm.squiggleDeviation = value/100F));

        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, CATEGORY_GENERIC, "Adjust Brightness", 50, 1, 255, (pfm, value) -> pfm.adjustbrightness = value));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createBooleanSetting(PFMSketchLines.class, CATEGORY_GENERIC, "Unlimited Tests", false, (pfm, value) -> pfm.unlimitedTests = value).setRandomiseExclude(true));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createRangedIntSetting(AbstractSketchPFM.class, CATEGORY_GENERIC, "Neighbour Tests", 20, 1, 3200, (pfm, value) -> pfm.lineTests = value).setSafeRange(0, 360));
        MasterRegistry.INSTANCE.registerPFMSetting(GenericSetting.createBooleanSetting(AbstractSketchPFM.class, CATEGORY_GENERIC, "Should Lift Pen", true, (pfm, value) -> pfm.shouldLiftPen = value).setRandomiseExclude(true));

    }

    @Override
    public void registerDrawingTools(){


    }

    @Override
    public void registerImageFilters(){


        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BORDERS, SimpleBorderFilter.class, "Dirty Border", SimpleBorderFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SimpleBorderFilter.class, "Type", 1, 1, 13, (filter, value) -> filter.borderNumber = value).setRandomiseExclude(true));


        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, BoxBlurFilter.class, "Box Blur", BoxBlurFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(BoxBlurFilter.class, "H Radius", 0F, 0, 100F, BoxBlurFilter::setHRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(BoxBlurFilter.class, "V Radius", 0F, 0, 100F, BoxBlurFilter::setVRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(BoxBlurFilter.class, "Iterations", 1, 0, 10, BoxBlurFilter::setIterations));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(BoxBlurFilter.class, "Premultiply", true,  BoxBlurFilter::setPremultiplyAlpha));

        //// MISSING: CONVOLVE

        //// MISSING: DESPECKLE

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, BumpFilter.class, "Emboss Edges", BumpFilter::new, false);

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, MotionBlurOp.class, "Motion Blur - Fast", MotionBlurOp::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MotionBlurOp.class, "Centre X", 0.5F, 0, 1, MotionBlurOp::setCentreX));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MotionBlurOp.class, "Centre Y", 0.5F, 0, 1, MotionBlurOp::setCentreY));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(MotionBlurOp.class, "Angle", 0, 0, 360, MotionBlurOp::setAngle).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MotionBlurOp.class, "Distance", 0, 0, 200, MotionBlurOp::setDistance));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(MotionBlurOp.class, "Rotation", 0, -180, 180, MotionBlurOp::setRotation).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MotionBlurOp.class, "Zoom", 0, 0, 100, MotionBlurOp::setZoom));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, GaussianFilter.class, "Gaussian Blur", GaussianFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(GaussianFilter.class, "Radius", 0, 0, 100, GaussianFilter::setRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(GaussianFilter.class, "Premultiply", true, GaussianFilter::setPremultiplyAlpha));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, GlowFilter.class, "Glow", GlowFilter::new, false); //extends Gaussian Blur
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(GlowFilter.class, "Premultiply", true, GlowFilter::setPremultiplyAlpha));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, HighPassFilter.class, "High Pass", HighPassFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(HighPassFilter.class, "Softness", 0, 0, 100, HighPassFilter::setRadius));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, LensBlurFilter.class, "Lens Blur", LensBlurFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(LensBlurFilter.class, "Radius", 10, 0, 50, LensBlurFilter::setRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(LensBlurFilter.class, "Sides", 5, 3, 12, LensBlurFilter::setSides));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(LensBlurFilter.class, "Bloom", 2F, 1.0F, 8.0F, LensBlurFilter::setBloom));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(LensBlurFilter.class, "Bloom Threshold", 255F, 0F, 255F, LensBlurFilter::setBloomThreshold));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, MaximumFilter.class, "Maximum", MaximumFilter::new, false);

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, MedianFilter.class, "Median", MedianFilter::new, false);

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, MinimumFilter.class, "Minimum", MinimumFilter::new, false);

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, MotionBlurFilter.class, "Motion Blur Slow", MotionBlurFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(MotionBlurFilter.class, "Wrap Edges", false, MotionBlurFilter::setWrapEdges));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(MotionBlurFilter.class, "Angle", 45, 0, 360, MotionBlurFilter::setAngle).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MotionBlurFilter.class, "Distance", 100, 0, 200, MotionBlurFilter::setDistance));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MotionBlurFilter.class, "Rotation", -180, 0, 180, MotionBlurFilter::setRotation));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MotionBlurFilter.class, "Zoom", 20, 0, 100, MotionBlurFilter::setZoom));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, SharpenFilter.class, "Sharpen", SharpenFilter::new, false);

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, BlurFilter.class, "Simple Blur", BlurFilter::new, false);

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, SmartBlurFilter.class, "Smart Blur", SmartBlurFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SmartBlurFilter.class, "H Radius", 5, 0, 100, SmartBlurFilter::setHRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SmartBlurFilter.class, "V Radius", 5, 0, 100, SmartBlurFilter::setVRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SmartBlurFilter.class, "Threshold", 10, 0, 255, SmartBlurFilter::setThreshold));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, HSBAdjustFilter.class, "Adjust HSB", HSBAdjustFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(HSBAdjustFilter.class, "Hue", 0F, -1F, 1F, HSBAdjustFilter::setHFactor));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(HSBAdjustFilter.class, "Saturation", 0F, -1F, 1F, HSBAdjustFilter::setSFactor));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(HSBAdjustFilter.class, "Brightness", 0F, -1F, 1F, HSBAdjustFilter::setBFactor));

        //// MISSING: SMOOTH FILTER

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.BLUR, UnsharpFilter.class, "Unsharp Mask", UnsharpFilter::new, false); //extends Gaussian Blur
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(UnsharpFilter.class, "Amount", 0.5F, 0, 1, UnsharpFilter::setAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(UnsharpFilter.class, "Threshold", 1, 0, 255, UnsharpFilter::setThreshold));

        //// MISSING: VARIABLE BLUR

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, RGBAdjustFilter.class, "Adjust RGB", RGBAdjustFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RGBAdjustFilter.class, "Red", 0F, -1F, 1F, RGBAdjustFilter::setRFactor));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RGBAdjustFilter.class, "Green", 0F, -1F, 1F, RGBAdjustFilter::setGFactor));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RGBAdjustFilter.class, "Blue", 0F, -1F, 1F, RGBAdjustFilter::setBFactor));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, ContrastFilter.class, "Contrast", ContrastFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ContrastFilter.class, "Brightness", 1F, 0, 2F, ContrastFilter::setBrightness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ContrastFilter.class, "Contrast", 1F, 0, 2F, ContrastFilter::setContrast));

        //// MISSING: DIFFUSION DITHER

        //// MISSING: DITHER

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, ExposureFilter.class, "Exposure", ExposureFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ExposureFilter.class, "Exposure", 1F, 0, 5F, ExposureFilter::setExposure));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, GainFilter.class, "Gain", GainFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(GainFilter.class, "Gain", 0.5F, 0, 1F, GainFilter::setGain));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(GainFilter.class, "Bias", 0.5F, 0, 1F, GainFilter::setBias));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, GammaFilter.class, "Gamma", GammaFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(GammaFilter.class, "Gamma", 1F, 0, 3F, GammaFilter::setGamma));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, GrayscaleFilter.class, "Grayscale", GrayscaleFilter::new, false);

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, InvertFilter.class, "Invert", InvertFilter::new, false);

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, LevelsFilter.class, "Levels", LevelsFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(LevelsFilter.class, "Low Level", 1F, 0, 1F, (filter, value) -> filter.setLowLevel(Math.min(value, filter.getHighLevel()))));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(LevelsFilter.class, "High Level", 1F, 0, 1F, (filter, value) -> filter.setHighLevel(Math.max(value, filter.getLowLevel()))));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(LevelsFilter.class, "Low Output Level", 1F, 0, 1F, (filter, value) -> filter.setLowOutputLevel(Math.min(value, filter.getHighOutputLevel()))));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(LevelsFilter.class, "Low Output Level", 1F, 0, 1F, (filter, value) -> filter.setHighOutputLevel(Math.max(value, filter.getLowOutputLevel()))));

        /// MISSING: LOOKUP

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, ChannelMixFilter.class, "Mix Channels", ChannelMixFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Blue/Green", 0, 0, 255, ChannelMixFilter::setBlueGreen));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Into Red", 0, 0, 255, ChannelMixFilter::setIntoR));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Red/Blue", 0, 0, 255, ChannelMixFilter::setRedBlue));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Into Green", 0, 0, 255, ChannelMixFilter::setIntoG));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Green/Red", 0, 0, 255, ChannelMixFilter::setGreenRed));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ChannelMixFilter.class, "Into Blue", 0, 0, 255, ChannelMixFilter::setIntoB));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, PosterizeFilter.class, "Posterize", PosterizeFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(PosterizeFilter.class, "Posterize", 6, 0, 255, PosterizeFilter::setNumLevels));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, QuantizeFilter.class, "Quantize", QuantizeFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(QuantizeFilter.class, "Number of Colours", 255, 0, 255, QuantizeFilter::setNumColors));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(QuantizeFilter.class, "Dither", false, QuantizeFilter::setDither));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(QuantizeFilter.class, "Serpentine", false, QuantizeFilter::setSerpentine));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, RescaleFilter.class, "Rescale", RescaleFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RescaleFilter.class, "Number of Colours", 1F, 0F, 5F, RescaleFilter::setScale));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, SolarizeFilter.class, "Solarize", SolarizeFilter::new, false);

        /// MISSING: TEMPERATURE FILTER

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.COLOURS, OpacityFilter.class, "Transparency", OpacityFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(OpacityFilter.class, "Opacity", 255, 0, 255, OpacityFilter::setOpacity));

        /// MISSING: TRITONE FILTER

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        /// MISSING: BORDER

        /// MISSING: CIRCLE

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.DISTORT, DiffuseFilter.class, "Diffuse", DiffuseFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(DiffuseFilter.class, "Scale", 4, 0, 100, DiffuseFilter::setScale));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(DiffuseFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.TRANSPARENT, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.DISTORT, DisplaceFilter.class, "Displace", DisplaceFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(DisplaceFilter.class, "Amount", 1, 0, 100, DisplaceFilter::setAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(DisplaceFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.TRANSPARENT, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.DISTORT, KaleidoscopeFilter.class, "Kaleidoscope", KaleidoscopeFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(KaleidoscopeFilter.class, "Centre X", 0.5F, 0F, 1F, KaleidoscopeFilter::setCentreX));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(KaleidoscopeFilter.class, "Centre Y", 0.5F, 0F, 1F, KaleidoscopeFilter::setCentreY));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(KaleidoscopeFilter.class, "Angle", -180, 0, 180, KaleidoscopeFilter::setAngle).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(KaleidoscopeFilter.class, "Angle 2", -180, 0, 180, KaleidoscopeFilter::setAngle2).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(KaleidoscopeFilter.class, "Radius", 0F, 0F, 200F, KaleidoscopeFilter::setRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(KaleidoscopeFilter.class, "Sides", 3, 0, 32, KaleidoscopeFilter::setSides));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(KaleidoscopeFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.CLAMP, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.DISTORT, MarbleFilter.class, "Marble", MarbleFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MarbleFilter.class, "Scale", 4, 0, 100, (filter, value) -> {
            filter.setXScale(value);
            filter.setYScale(value);
        }));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MarbleFilter.class, "Amount", 1, 0, 1, MarbleFilter::setAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MarbleFilter.class, "Turbulence", 1, 0, 15, MarbleFilter::setTurbulence));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(MarbleFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.CLAMP, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        /// MISSING: OFFSET - NOT REALLY NEEDED

        /// MISSING: PINCH - REQUIRES ADDITIONAL RENDERING

        /// MISSING: POLAR COORDINATES

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.DISTORT, RippleFilter.class, "Ripple", RippleFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RippleFilter.class, "X Amplitude", 5F, 0F, 100F, RippleFilter::setXAmplitude));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RippleFilter.class, "X Wavelength", 16F, 0F, 100F, RippleFilter::setXWavelength));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RippleFilter.class, "Y Amplitude", 0F, 0F, 100F, RippleFilter::setYAmplitude));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RippleFilter.class, "Y Wavelength", 16F, 0F, 100F, RippleFilter::setYWavelength));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(RippleFilter.class, "Shape", List.of(EnumWaveType.values()), EnumWaveType.SINE, (filter, value) -> filter.setWaveType(value.getWaveType())));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(RippleFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.TRANSPARENT, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.DISTORT, ShearFilter.class, "Shear", ShearFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ShearFilter.class, "X Angle", -60F, 0F, 60F, ShearFilter::setXAngle).setMajorTick(30F));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ShearFilter.class, "Y Angle", -60F, 0F, 60F, ShearFilter::setYAngle).setMajorTick(30F));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(ShearFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.TRANSPARENT, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        /// MISSING: SPHERE - REQUIRES ADDITIONAL RENDERING

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.DISTORT, SwimFilter.class, "Swim", SwimFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(SwimFilter.class, "Scale", 32F, 1F, 300F, SwimFilter::setScale));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SwimFilter.class, "Angle", 0, 0, 360, SwimFilter::setAngle).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(SwimFilter.class, "Stretch", 1F, 1F, 50F, SwimFilter::setStretch));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(SwimFilter.class, "Turbulence", 1F, 1F, 10F, SwimFilter::setTurbulence));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(SwimFilter.class, "Amount", 1F, 0F, 100F, SwimFilter::setAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(SwimFilter.class, "Time", 0F, 0F, 100F, SwimFilter::setTime));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(SwimFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.TRANSPARENT, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        /// MISSING: TWIRL - REQUIRES ADDITIONAL RENDERING

        /// MISSING: WATER RIPPLES - REQUIRES ADDITIONAL RENDERING

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.EDGES, EdgeFilter.class, "Detect Edges", EdgeFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(EdgeFilter.class, "Horizontal", List.of(EnumEdgeDetect.values()), EnumEdgeDetect.SOBEL, (filter, value) -> filter.setHEdgeMatrix(value.getHorizontalMatrix())));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(EdgeFilter.class, "Vertical", List.of(EnumEdgeDetect.values()), EnumEdgeDetect.SOBEL, (filter, value) -> filter.setVEdgeMatrix(value.getVerticalMatrix())));

        /// MISSING DIFFERENCE OF GAUSSIANS

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.EDGES, LaplaceFilter.class, "Laplace", LaplaceFilter::new, false);

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        /////EFFECTS

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.EFFECTS, ChromeFilter.class, "Chrome", ChromeFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ChromeFilter.class, "Softness", 5.0f, 0F, 50F, ChromeFilter::setBumpSoftness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ChromeFilter.class, "Height", 1.0f, 0F, 5F, ChromeFilter::setBumpHeight));
        //// MISSING SETTING, BUMP SOURCE
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ChromeFilter.class, "Amount", 0.5f, 0F, 1F, ChromeFilter::setAmount));//percentage
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ChromeFilter.class, "Exposure", 1.0f, 0F, 5F, ChromeFilter::setExposure));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(ChromeFilter.class, "Color", Color.BLACK, (filter, value) -> filter.setDiffuseColor(ImageTools.getARGBFromColor(value))));

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.EFFECTS, FeedbackFilter.class, "Feedback", FeedbackFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "Centre X", 0.5F, 0F, 1F, FeedbackFilter::setCentreX));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "Centre Y", 0.5F, 0F, 1F, FeedbackFilter::setCentreY));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(FeedbackFilter.class, "Angle", 0, 0, 360, FeedbackFilter::setAngle).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "Distance", 0F, 0F, 200F, FeedbackFilter::setDistance));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(FeedbackFilter.class, "Rotation", -180, 0, 180, FeedbackFilter::setRotation).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "Zoom", 0F, -1, 1F, FeedbackFilter::setZoom));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(FeedbackFilter.class, "Iterations", 2, 2, 100, FeedbackFilter::setIterations));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "Start Alpha", 1F, 0F, 1F, FeedbackFilter::setStartAlpha));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "End Alpha", 1F, 0F, 1F, FeedbackFilter::setEndAlpha));

        ///GLINT
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.EFFECTS, GlintFilter.class, "Glint", GlintFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(GlintFilter.class, "Threshold", 1F, 0F, 1F, GlintFilter::setThreshold));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(GlintFilter.class, "Amount", 0.1F, 0F, 1F, GlintFilter::setAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(GlintFilter.class, "Length", 5, 0, 50, GlintFilter::setLength));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(GlintFilter.class, "Blur", 0F, 0F, 20F, GlintFilter::setBlur));
        ///MISSING SETTING COLOUR MAP
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(GlintFilter.class, "Glint Only", false, GlintFilter::setGlintOnly));

        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MirrorFilter.class, "Centre Y", 0.5F, 0F, 1F, MirrorFilter::setCentreY));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MirrorFilter.class, "Gap", 0F, 0F, 1F, MirrorFilter::setGap));


        ///INTERPOLATE

        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.EFFECTS, MirrorFilter.class, "Mirror", MirrorFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MirrorFilter.class, "Opacity", 1F, 0F, 1F, MirrorFilter::setOpacity));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MirrorFilter.class, "Centre Y", 0.5F, 0F, 1F, MirrorFilter::setCentreY));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(MirrorFilter.class, "Gap", 0F, 0F, 1F, MirrorFilter::setGap));


        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        /////KEYING

        ///CHROMA KEY
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.KEYING, ChromaKeyFilter.class, "Chroma Key", ChromaKeyFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ChromaKeyFilter.class, "H Tolerance", 0F, 0F, 1F, ChromaKeyFilter::setHTolerance));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ChromaKeyFilter.class, "S Tolerance", 0F, 0F, 1F, ChromaKeyFilter::setSTolerance));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ChromaKeyFilter.class, "B Tolerance", 0F, 0F, 1F, ChromaKeyFilter::setBTolerance));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(ChromaKeyFilter.class, "Color", new Color(23F/255F, 255/255F, 23/255F, 255/255F), (filter, value) -> filter.setColor(ImageTools.getARGBFromColor(value))));


        ///KEY

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        /////PIXELLATE

        ///COLOR HALFTONE
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.PIXELLATE, ColorHalftoneFilter.class, "Color Halftone", ColorHalftoneFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ColorHalftoneFilter.class, "Max Radius", 2F, 1F, 30F, ColorHalftoneFilter::setdotRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ColorHalftoneFilter.class, "Cyan Screen Angle", 108, 0, 360, (filter, value) -> filter.setCyanScreenAngle((float)Math.toRadians(value))).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ColorHalftoneFilter.class, "Magenta Screen Angle", 162, 0, 360, (filter, value) -> filter.setMagentaScreenAngle((float)Math.toRadians(value))).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ColorHalftoneFilter.class, "Yellow Screen Angle", 90, 0, 360, (filter, value) -> filter.setYellowScreenAngle((float)Math.toRadians(value))).setMajorTick(90));

        ///CRYSTALISE
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.PIXELLATE, CrystallizeFilter.class, "Crystallize", CrystallizeFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(CrystallizeFilter.class, "Size", 16F, 1F, 100F, CrystallizeFilter::setScale));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(CrystallizeFilter.class, "Angle", 0, 0, 360, CrystallizeFilter::setAngle).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(CrystallizeFilter.class, "Randomness", 0F, 0F, 1F, CrystallizeFilter::setRandomness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(CrystallizeFilter.class, "Edges", 0.4F, 0F, 1F, CrystallizeFilter::setEdgeThickness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(CrystallizeFilter.class, "Grid Type", List.of(EnumPixellateGridType.values()), EnumPixellateGridType.HEXAGONAL, (filter, value) -> filter.setGridType(value.getGridType())));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(CrystallizeFilter.class, "Fade Edges", false, CrystallizeFilter::setFadeEdges));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(CrystallizeFilter.class, "Edge Colour", Color.BLACK, (filter, value) -> filter.setEdgeColor(ImageTools.getARGBFromColor(value))));

        ///MOSIAC

        ///POINTILIZE
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.PIXELLATE, PointillizeFilter.class, "Pointillize", PointillizeFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(PointillizeFilter.class, "Size", 16F, 1F, 100F, PointillizeFilter::setScale));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(PointillizeFilter.class, "Angle", 0, 0, 360, PointillizeFilter::setAngle).setMajorTick(90));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(PointillizeFilter.class, "Randomness", 0F, 0F, 1F, PointillizeFilter::setRandomness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(PointillizeFilter.class, "Dot Size", 0.4F, 0F, 1F, PointillizeFilter::setEdgeThickness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(PointillizeFilter.class, "Fuzziness", 0.1F, 0F, 1F, PointillizeFilter::setFuzziness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(PointillizeFilter.class, "Grid Type", List.of(EnumPixellateGridType.values()), EnumPixellateGridType.HEXAGONAL, (filter, value) -> filter.setGridType(value.getGridType())));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(PointillizeFilter.class, "Fill", false, PointillizeFilter::setFadeEdges));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(PointillizeFilter.class, "Edge Colour", Color.BLACK, (filter, value) -> filter.setEdgeColor(ImageTools.getARGBFromColor(value))));


        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        //////RENDER

        ///SCRATCHES
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.RENDER, ScratchFilter.class, "Scratches", ScratchFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ScratchFilter.class, "Density", 0.1F, 0F, 1F, ScratchFilter::setDensity));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ScratchFilter.class, "Length", 0.5F, 0F, 1F, ScratchFilter::setLength));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ScratchFilter.class, "Width", 0.5F, 0F, 1F, ScratchFilter::setWidth));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ScratchFilter.class, "Angle Variation", 1.0F, 0F, 1F, ScratchFilter::setAngleVariation));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ScratchFilter.class, "Seed", 0, 0, 100, ScratchFilter::setSeed));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(ScratchFilter.class, "Colour", Color.WHITE, (filter, value) -> filter.setColor(ImageTools.getARGBFromColor(value))));


        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        //////STYLIZE

        /// ADD NOISE
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, NoiseFilter.class, "Noise", NoiseFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(NoiseFilter.class, "Amount", 25, 0, 100, NoiseFilter::setAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(NoiseFilter.class, "Density", 1.0F, 0F, 1F, NoiseFilter::setDensity));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(NoiseFilter.class, "Grid Type", List.of(EnumNoiseDistribution.values()), EnumNoiseDistribution.GAUSSIAN, (filter, value) -> filter.setDistribution(value.getDistribution())));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(NoiseFilter.class, "Monochrome", false, NoiseFilter::setMonochrome));

        /// CONTOURS
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, ContourFilter.class, "Contours", ContourFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ContourFilter.class, "Levels", 5F, 0F, 30F, ContourFilter::setLevels));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ContourFilter.class, "Offset", 0F, 0F, 1F, ContourFilter::setOffset));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ContourFilter.class, "Scale", 1F, 0F, 1F, ContourFilter::setScale));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(ContourFilter.class, "Colour", Color.BLACK, (filter, value) -> filter.setContourColor(ImageTools.getARGBFromColor(value))));

        ///DISSOLVE
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, DissolveFilter.class, "Dissolve", DissolveFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(DissolveFilter.class, "Density", 1.0F, 0F, 1F, DissolveFilter::setDensity));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(DissolveFilter.class, "Softness", 0F, 0F, 1F, DissolveFilter::setSoftness));

        ///DROP SHADOW
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, ShadowFilter.class, "Drop Shadow", ShadowFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ShadowFilter.class, "Angle", (float)Math.PI*6/4, 0F, 360F, ShadowFilter::setAngle).setMajorTick(90F));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ShadowFilter.class, "Distance", 5F, 0F, 100F, ShadowFilter::setDistance));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ShadowFilter.class, "Softness", 5F, 0F, 100F, ShadowFilter::setRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ShadowFilter.class, "Opacity", 0.5F, 0F, 1F, ShadowFilter::setOpacity));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(ShadowFilter.class, "Shadow Colour", Color.BLACK, (filter, value) -> filter.setShadowColor(ImageTools.getARGBFromColor(value))));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(ShadowFilter.class, "Monochrome", false, ShadowFilter::setShadowOnly));

        ///EMBOSS
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, EmbossFilter.class, "Emboss", EmbossFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(EmbossFilter.class, "Light Direction", 135.0F * ImageMath.PI / 180.0F, 0F, 360F, EmbossFilter::setAzimuth).setMajorTick(90F));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(EmbossFilter.class, "Light Elevation", 30.0F * ImageMath.PI / 180.0F, 0F, 90F, EmbossFilter::setElevation).setMajorTick(90F));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(EmbossFilter.class, "Bump Height", 1F, 0F, 1F, EmbossFilter::setBumpHeight));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(EmbossFilter.class, "Texture", false, EmbossFilter::setEmboss));

        ///FLARE - ADDITIONAL RENDERING REQUIRED
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, FlareFilter.class, "Flare", FlareFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "X Position", 0.5F, 0F, 1F, (filter, value) -> filter.setCentre(new Point2D.Float(value, (float)filter.getCentre().getY()))));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Y Position", 0.5F, 0F, 1F, (filter, value) -> filter.setCentre(new Point2D.Float((float)filter.getCentre().getX(), value))));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Radius", 0F, 0F, 400F, FlareFilter::setRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Base", 1.0F, 0F, 1F, FlareFilter::setBaseAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Ring", 0.2f, 0F, 1F, FlareFilter::setRingAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Ring Width", 1.6f, 0F, 10F, FlareFilter::setRingWidth));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Ray", 0.1f, 0F, 1F, FlareFilter::setRayAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(FlareFilter.class, "Colour", Color.WHITE, (filter, value) -> filter.setColor(ImageTools.getARGBFromColor(value))));

        ///HALFTONE - MASK NEEDED
        //MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, HalftoneFilter.class, "Halftone", HalftoneFilter::new, false);
        //registerSetting(GenericSetting.createRangedFloatSetting(HalftoneFilter.class, "Softness", 0.1f, 0F, 1F, HalftoneFilter::setSoftness));

        ///LIGHT EFFECTS - COMPLICATED

        ///OIL
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, OilFilter.class, "Oil", OilFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(OilFilter.class, "Range", 3, 1, 5, OilFilter::setRange));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(OilFilter.class, "Levels", 256, 1, 256, OilFilter::setLevels));


        ///RAYS
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, RaysFilter.class, "Rays", RaysFilter::new, false); //extends MotionBlurOp
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RaysFilter.class, "Opacity", 1.0F, 0F, 1.0F, RaysFilter::setOpacity));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RaysFilter.class, "Strength", 0.5F, 0F, 5.0F, RaysFilter::setStrength));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(RaysFilter.class, "Threshold", 0.0F, 0F, 1.0F, RaysFilter::setThreshold));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(RaysFilter.class, "Rays Only", false, RaysFilter::setRaysOnly));
        //missing color map

        ///SHAPEBURST
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, ShapeFilter.class, "Shape Burst", ShapeFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(ShapeFilter.class, "Factor", 1.0F, 0F, 5.0F, ShapeFilter::setFactor));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createOptionSetting(ShapeFilter.class, "Shape Type", List.of(EnumShapeFilter.values()), EnumShapeFilter.LINEAR, (filter, value) -> filter.setType(value.getShapeType())));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(ShapeFilter.class, "Invert", false, ShapeFilter::setInvert));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(ShapeFilter.class, "Merge", false, ShapeFilter::setMerge));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createBooleanSetting(ShapeFilter.class, "Use Alpha", false, ShapeFilter::setUseAlpha));
        //missing color map

        ///SPARKLE
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, SparkleFilter.class, "Sparkle", SparkleFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SparkleFilter.class, "Range", 50, 0, 300, SparkleFilter::setRays));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SparkleFilter.class, "Radius", 25, 0, 300, SparkleFilter::setRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SparkleFilter.class, "Shine", 50, 0, 100, SparkleFilter::setAmount));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(SparkleFilter.class, "Randomness", 25, 0, 50, SparkleFilter::setRandomness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(SparkleFilter.class, "Colour", Color.WHITE, (filter, value) -> filter.setColor(ImageTools.getARGBFromColor(value))));


        ///STAMP
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, StampFilter.class, "Stamp", StampFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(StampFilter.class, "Radius", 5.0F, 0F, 100F, StampFilter::setRadius));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(StampFilter.class, "Threshold", 0.5F, 0F, 1.0F, StampFilter::setThreshold));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedFloatSetting(StampFilter.class, "Softness", 0F, 0F, 1.0F, StampFilter::setSoftness));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(StampFilter.class, "Lower Colour", Color.BLACK, (filter, value) -> filter.setBlack(ImageTools.getARGBFromColor(value))));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(StampFilter.class, "Upper Colour", Color.WHITE, (filter, value) -> filter.setWhite(ImageTools.getARGBFromColor(value))));


        ///THRESHOLD
        MasterRegistry.INSTANCE.registerImageFilter(EnumFilterTypes.STYLIZE, ThresholdFilter.class, "Threshold", ThresholdFilter::new, false);
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ThresholdFilter.class, "Lower Threshold", 127, 0, 255, ThresholdFilter::setLowerThreshold));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createRangedIntSetting(ThresholdFilter.class, "Upper Threshold", 127, 0, 255, ThresholdFilter::setUpperThreshold));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(ThresholdFilter.class, "Lower Colour", Color.BLACK, (filter, value) -> filter.setBlack(ImageTools.getARGBFromColor(value))));
        MasterRegistry.INSTANCE.registerImageFilterSetting(GenericSetting.createColourSetting(ThresholdFilter.class, "Upper Colour", Color.WHITE, (filter, value) -> filter.setWhite(ImageTools.getARGBFromColor(value))));

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

    public static ColourSeperationHandler DEFAULT_COLOUR_SPLITTER;

    @Override
    public void registerColourSplitterHandlers(){
        DEFAULT_COLOUR_SPLITTER = MasterRegistry.INSTANCE.registerColourSplitter(new ColourSeperationHandler("Default"));
     }

}
