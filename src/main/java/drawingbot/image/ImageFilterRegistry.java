package drawingbot.image;

import com.jhlabs.image.*;
import drawingbot.DrawingBotV3;
import drawingbot.files.presets.types.PresetImageFilters;
import drawingbot.image.filters.*;
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
import javafx.scene.paint.Color;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ImageFilterRegistry {

    public static Map<EnumFilterTypes, ObservableList<GenericFactory<BufferedImageOp>>> filterFactories = FXCollections.observableMap(new LinkedHashMap<>());
    public static ObservableList<ObservableImageFilter> currentFilters = FXCollections.observableArrayList();
    public static HashMap<Class<? extends BufferedImageOp>, List<GenericSetting<?, ?>>> filterSettings = new LinkedHashMap<>();
    public static HashMap<Class<? extends BufferedImageOp>, Function<ObservableImageFilter, Dialog<ObservableImageFilter>>> dialogs = new LinkedHashMap<>();
    public static ObservableList<GenericPreset<PresetImageFilters>> imagePresets = FXCollections.observableArrayList();

    static {

        registerImageFilter(EnumFilterTypes.BORDERS, SimpleBorderFilter.class, "Dirty Border", SimpleBorderFilter::new, false);
        registerSetting(GenericSetting.createRangedIntSetting(SimpleBorderFilter.class, "Type", 1, 1, 13, true, (filter, value) -> filter.borderNumber = value));


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
        registerSetting(GenericSetting.createRangedIntSetting(MotionBlurOp.class, "Angle", 0, 0, 360, false, MotionBlurOp::setAngle).setMajorTick(90));
        registerSetting(GenericSetting.createRangedFloatSetting(MotionBlurOp.class, "Distance", 0, 0, 200, false, MotionBlurOp::setDistance));
        registerSetting(GenericSetting.createRangedIntSetting(MotionBlurOp.class, "Rotation", 0, -180, 180, false, MotionBlurOp::setRotation).setMajorTick(90));
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

        registerImageFilter(EnumFilterTypes.BLUR, MotionBlurFilter.class, "Motion Blur Slow", MotionBlurFilter::new, false);
        registerSetting(GenericSetting.createBooleanSetting(MotionBlurFilter.class, "Wrap Edges", false, false, MotionBlurFilter::setWrapEdges));
        registerSetting(GenericSetting.createRangedIntSetting(MotionBlurFilter.class, "Angle", 45, 0, 360, false, MotionBlurFilter::setAngle).setMajorTick(90));
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
        registerSetting(GenericSetting.createRangedIntSetting(KaleidoscopeFilter.class, "Angle", -180, 0, 180, false, KaleidoscopeFilter::setAngle).setMajorTick(90));
        registerSetting(GenericSetting.createRangedIntSetting(KaleidoscopeFilter.class, "Angle 2", -180, 0, 180, false, KaleidoscopeFilter::setAngle2).setMajorTick(90));
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
        registerSetting(GenericSetting.createRangedFloatSetting(ShearFilter.class, "X Angle", -60F, 0F, 60F, false, ShearFilter::setXAngle).setMajorTick(30F));
        registerSetting(GenericSetting.createRangedFloatSetting(ShearFilter.class, "Y Angle", -60F, 0F, 60F, false, ShearFilter::setYAngle).setMajorTick(30F));
        registerSetting(GenericSetting.createOptionSetting(ShearFilter.class, "Edges", List.of(EnumEdgeAction.values()), EnumEdgeAction.TRANSPARENT, false, (filter, value) -> filter.setEdgeAction(value.getEdgeAction())));

        /// MISSING: SPHERE - REQUIRES ADDITIONAL RENDERING

        registerImageFilter(EnumFilterTypes.DISTORT, SwimFilter.class, "Swim", SwimFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(SwimFilter.class, "Scale", 32F, 1F, 300F, false, SwimFilter::setScale));
        registerSetting(GenericSetting.createRangedIntSetting(SwimFilter.class, "Angle", 0, 0, 360, false, SwimFilter::setAngle).setMajorTick(90));
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

        /////EFFECTS

        registerImageFilter(EnumFilterTypes.EFFECTS, ChromeFilter.class, "Chrome", ChromeFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(ChromeFilter.class, "Softness", 5.0f, 0F, 50F, false, ChromeFilter::setBumpSoftness));
        registerSetting(GenericSetting.createRangedFloatSetting(ChromeFilter.class, "Height", 1.0f, 0F, 5F, false, ChromeFilter::setBumpHeight));
        //// MISSING SETTING, BUMP SOURCE
        registerSetting(GenericSetting.createRangedFloatSetting(ChromeFilter.class, "Amount", 0.5f, 0F, 1F, false, ChromeFilter::setAmount));//percentage
        registerSetting(GenericSetting.createRangedFloatSetting(ChromeFilter.class, "Exposure", 1.0f, 0F, 5F, false, ChromeFilter::setExposure));
        registerSetting(GenericSetting.createColourSetting(ChromeFilter.class, "Color", Color.BLACK, false, (filter, value) -> filter.setDiffuseColor(ImageTools.getARGBFromColor(value))));

        registerImageFilter(EnumFilterTypes.EFFECTS, FeedbackFilter.class, "Feedback", FeedbackFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "Centre X", 0.5F, 0F, 1F, false, FeedbackFilter::setCentreX));
        registerSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "Centre Y", 0.5F, 0F, 1F, false, FeedbackFilter::setCentreY));
        registerSetting(GenericSetting.createRangedIntSetting(FeedbackFilter.class, "Angle", 0, 0, 360, false, FeedbackFilter::setAngle).setMajorTick(90));
        registerSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "Distance", 0F, 0F, 200F, false, FeedbackFilter::setDistance));
        registerSetting(GenericSetting.createRangedIntSetting(FeedbackFilter.class, "Rotation", -180, 0, 180, false, FeedbackFilter::setRotation).setMajorTick(90));
        registerSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "Zoom", 0F, -1, 1F, false, FeedbackFilter::setZoom));
        registerSetting(GenericSetting.createRangedIntSetting(FeedbackFilter.class, "Iterations", 2, 2, 100, false, FeedbackFilter::setIterations));
        registerSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "Start Alpha", 1F, 0F, 1F, false, FeedbackFilter::setStartAlpha));
        registerSetting(GenericSetting.createRangedFloatSetting(FeedbackFilter.class, "End Alpha", 1F, 0F, 1F, false, FeedbackFilter::setEndAlpha));

        ///GLINT
        registerImageFilter(EnumFilterTypes.EFFECTS, GlintFilter.class, "Glint", GlintFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(GlintFilter.class, "Threshold", 1F, 0F, 1F, false, GlintFilter::setThreshold));
        registerSetting(GenericSetting.createRangedFloatSetting(GlintFilter.class, "Amount", 0.1F, 0F, 1F, false, GlintFilter::setAmount));
        registerSetting(GenericSetting.createRangedIntSetting(GlintFilter.class, "Length", 5, 0, 50, false, GlintFilter::setLength));
        registerSetting(GenericSetting.createRangedFloatSetting(GlintFilter.class, "Blur", 0F, 0F, 20F, false, GlintFilter::setBlur));
        ///MISSING SETTING COLOUR MAP
        registerSetting(GenericSetting.createBooleanSetting(GlintFilter.class, "Glint Only", false, false, GlintFilter::setGlintOnly));

        registerSetting(GenericSetting.createRangedFloatSetting(MirrorFilter.class, "Centre Y", 0.5F, 0F, 1F, false, MirrorFilter::setCentreY));
        registerSetting(GenericSetting.createRangedFloatSetting(MirrorFilter.class, "Gap", 0F, 0F, 1F, false, MirrorFilter::setGap));


        ///INTERPOLATE

        registerImageFilter(EnumFilterTypes.EFFECTS, MirrorFilter.class, "Mirror", MirrorFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(MirrorFilter.class, "Opacity", 1F, 0F, 1F, false, MirrorFilter::setOpacity));
        registerSetting(GenericSetting.createRangedFloatSetting(MirrorFilter.class, "Centre Y", 0.5F, 0F, 1F, false, MirrorFilter::setCentreY));
        registerSetting(GenericSetting.createRangedFloatSetting(MirrorFilter.class, "Gap", 0F, 0F, 1F, false, MirrorFilter::setGap));


        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        /////KEYING

        ///CHROMA KEY
        registerImageFilter(EnumFilterTypes.KEYING, ChromaKeyFilter.class, "Chroma Key", ChromaKeyFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(ChromaKeyFilter.class, "H Tolerance", 0F, 0F, 1F, false, ChromaKeyFilter::setHTolerance));
        registerSetting(GenericSetting.createRangedFloatSetting(ChromaKeyFilter.class, "S Tolerance", 0F, 0F, 1F, false, ChromaKeyFilter::setSTolerance));
        registerSetting(GenericSetting.createRangedFloatSetting(ChromaKeyFilter.class, "B Tolerance", 0F, 0F, 1F, false, ChromaKeyFilter::setBTolerance));
        registerSetting(GenericSetting.createColourSetting(ChromaKeyFilter.class, "Color", new Color(23F/255F, 255/255F, 23/255F, 255/255F), false, (filter, value) -> filter.setColor(ImageTools.getARGBFromColor(value))));


        ///KEY

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        /////PIXELLATE

        ///COLOR HALFTONE
        registerImageFilter(EnumFilterTypes.PIXELLATE, ColorHalftoneFilter.class, "Color Halftone", ColorHalftoneFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(ColorHalftoneFilter.class, "Max Radius", 2F, 1F, 30F, false, ColorHalftoneFilter::setdotRadius));
        registerSetting(GenericSetting.createRangedIntSetting(ColorHalftoneFilter.class, "Cyan Screen Angle", 108, 0, 360, false, (filter, value) -> filter.setCyanScreenAngle((float)Math.toRadians(value))).setMajorTick(90));
        registerSetting(GenericSetting.createRangedIntSetting(ColorHalftoneFilter.class, "Magenta Screen Angle", 162, 0, 360, false, (filter, value) -> filter.setMagentaScreenAngle((float)Math.toRadians(value))).setMajorTick(90));
        registerSetting(GenericSetting.createRangedIntSetting(ColorHalftoneFilter.class, "Yellow Screen Angle", 90, 0, 360, false, (filter, value) -> filter.setYellowScreenAngle((float)Math.toRadians(value))).setMajorTick(90));

        ///CRYSTALISE
        registerImageFilter(EnumFilterTypes.PIXELLATE, CrystallizeFilter.class, "Crystallize", CrystallizeFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(CrystallizeFilter.class, "Size", 16F, 1F, 100F, false, CrystallizeFilter::setScale));
        registerSetting(GenericSetting.createRangedIntSetting(CrystallizeFilter.class, "Angle", 0, 0, 360, false, CrystallizeFilter::setAngle).setMajorTick(90));
        registerSetting(GenericSetting.createRangedFloatSetting(CrystallizeFilter.class, "Randomness", 0F, 0F, 1F, false, CrystallizeFilter::setRandomness));
        registerSetting(GenericSetting.createRangedFloatSetting(CrystallizeFilter.class, "Edges", 0.4F, 0F, 1F, false, CrystallizeFilter::setEdgeThickness));
        registerSetting(GenericSetting.createOptionSetting(CrystallizeFilter.class, "Grid Type", List.of(EnumPixellateGridType.values()), EnumPixellateGridType.HEXAGONAL, false, (filter, value) -> filter.setGridType(value.getGridType())));
        registerSetting(GenericSetting.createBooleanSetting(CrystallizeFilter.class, "Fade Edges", false, false, CrystallizeFilter::setFadeEdges));
        registerSetting(GenericSetting.createColourSetting(CrystallizeFilter.class, "Edge Colour", Color.BLACK, false, (filter, value) -> filter.setEdgeColor(ImageTools.getARGBFromColor(value))));

        ///MOSIAC

        ///POINTILIZE
        registerImageFilter(EnumFilterTypes.PIXELLATE, PointillizeFilter.class, "Pointillize", PointillizeFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(PointillizeFilter.class, "Size", 16F, 1F, 100F, false, PointillizeFilter::setScale));
        registerSetting(GenericSetting.createRangedIntSetting(PointillizeFilter.class, "Angle", 0, 0, 360, false, PointillizeFilter::setAngle).setMajorTick(90));
        registerSetting(GenericSetting.createRangedFloatSetting(PointillizeFilter.class, "Randomness", 0F, 0F, 1F, false, PointillizeFilter::setRandomness));
        registerSetting(GenericSetting.createRangedFloatSetting(PointillizeFilter.class, "Dot Size", 0.4F, 0F, 1F, false, PointillizeFilter::setEdgeThickness));
        registerSetting(GenericSetting.createRangedFloatSetting(PointillizeFilter.class, "Fuzziness", 0.1F, 0F, 1F, false, PointillizeFilter::setFuzziness));
        registerSetting(GenericSetting.createOptionSetting(PointillizeFilter.class, "Grid Type", List.of(EnumPixellateGridType.values()), EnumPixellateGridType.HEXAGONAL, false, (filter, value) -> filter.setGridType(value.getGridType())));
        registerSetting(GenericSetting.createBooleanSetting(PointillizeFilter.class, "Fill", false, false, PointillizeFilter::setFadeEdges));
        registerSetting(GenericSetting.createColourSetting(PointillizeFilter.class, "Edge Colour", Color.BLACK, false, (filter, value) -> filter.setEdgeColor(ImageTools.getARGBFromColor(value))));


        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        //////RENDER

        ///SCRATCHES
        registerImageFilter(EnumFilterTypes.RENDER, ScratchFilter.class, "Scratches", ScratchFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(ScratchFilter.class, "Density", 0.1F, 0F, 1F, false, ScratchFilter::setDensity));
        registerSetting(GenericSetting.createRangedFloatSetting(ScratchFilter.class, "Length", 0.5F, 0F, 1F, false, ScratchFilter::setLength));
        registerSetting(GenericSetting.createRangedFloatSetting(ScratchFilter.class, "Width", 0.5F, 0F, 1F, false, ScratchFilter::setWidth));
        registerSetting(GenericSetting.createRangedFloatSetting(ScratchFilter.class, "Angle Variation", 1.0F, 0F, 1F, false, ScratchFilter::setAngleVariation));
        registerSetting(GenericSetting.createRangedIntSetting(ScratchFilter.class, "Seed", 0, 0, 100, false, ScratchFilter::setSeed));
        registerSetting(GenericSetting.createColourSetting(ScratchFilter.class, "Colour", Color.WHITE, false, (filter, value) -> filter.setColor(ImageTools.getARGBFromColor(value))));


        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        //////STYLIZE

        /// ADD NOISE
        registerImageFilter(EnumFilterTypes.STYLIZE, NoiseFilter.class, "Noise", NoiseFilter::new, false);
        registerSetting(GenericSetting.createRangedIntSetting(NoiseFilter.class, "Amount", 25, 0, 100, false, NoiseFilter::setAmount));
        registerSetting(GenericSetting.createRangedFloatSetting(NoiseFilter.class, "Density", 1.0F, 0F, 1F, false, NoiseFilter::setDensity));
        registerSetting(GenericSetting.createOptionSetting(NoiseFilter.class, "Grid Type", List.of(EnumNoiseDistribution.values()), EnumNoiseDistribution.GAUSSIAN, false, (filter, value) -> filter.setDistribution(value.getDistribution())));
        registerSetting(GenericSetting.createBooleanSetting(NoiseFilter.class, "Monochrome", false, false, NoiseFilter::setMonochrome));

        /// CONTOURS
        registerImageFilter(EnumFilterTypes.STYLIZE, ContourFilter.class, "Coutours", ContourFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(ContourFilter.class, "Levels", 5F, 0F, 30F, false, ContourFilter::setLevels));
        registerSetting(GenericSetting.createRangedFloatSetting(ContourFilter.class, "Offset", 0F, 0F, 1F, false, ContourFilter::setOffset));
        registerSetting(GenericSetting.createRangedFloatSetting(ContourFilter.class, "Scale", 1F, 0F, 1F, false, ContourFilter::setScale));
        registerSetting(GenericSetting.createColourSetting(ContourFilter.class, "Colour", Color.BLACK, false, (filter, value) -> filter.setContourColor(ImageTools.getARGBFromColor(value))));

        ///DISSOLVE
        registerImageFilter(EnumFilterTypes.STYLIZE, DissolveFilter.class, "Dissolve", DissolveFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(DissolveFilter.class, "Density", 1.0F, 0F, 1F, false, DissolveFilter::setDensity));
        registerSetting(GenericSetting.createRangedFloatSetting(DissolveFilter.class, "Softness", 0F, 0F, 1F, false, DissolveFilter::setSoftness));

        ///DROP SHADOW
        registerImageFilter(EnumFilterTypes.STYLIZE, ShadowFilter.class, "Drop Shadow", ShadowFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(ShadowFilter.class, "Angle", (float)Math.PI*6/4, 0F, 360F, false, ShadowFilter::setAngle).setMajorTick(90F));
        registerSetting(GenericSetting.createRangedFloatSetting(ShadowFilter.class, "Distance", 5F, 0F, 100F, false, ShadowFilter::setDistance));
        registerSetting(GenericSetting.createRangedFloatSetting(ShadowFilter.class, "Softness", 5F, 0F, 100F, false, ShadowFilter::setRadius));
        registerSetting(GenericSetting.createRangedFloatSetting(ShadowFilter.class, "Opacity", 0.5F, 0F, 1F, false, ShadowFilter::setOpacity));
        registerSetting(GenericSetting.createColourSetting(ShadowFilter.class, "Shadow Colour", Color.BLACK, false, (filter, value) -> filter.setShadowColor(ImageTools.getARGBFromColor(value))));
        registerSetting(GenericSetting.createBooleanSetting(ShadowFilter.class, "Monochrome", false, false, ShadowFilter::setShadowOnly));

        ///EMBOSS
        registerImageFilter(EnumFilterTypes.STYLIZE, EmbossFilter.class, "Emboss", EmbossFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(EmbossFilter.class, "Light Direction", 135.0F * ImageMath.PI / 180.0F, 0F, 360F, false, EmbossFilter::setAzimuth).setMajorTick(90F));
        registerSetting(GenericSetting.createRangedFloatSetting(EmbossFilter.class, "Light Elevation", 30.0F * ImageMath.PI / 180.0F, 0F, 90F, false, EmbossFilter::setElevation).setMajorTick(90F));
        registerSetting(GenericSetting.createRangedFloatSetting(EmbossFilter.class, "Bump Height", 1F, 0F, 1F, false, EmbossFilter::setBumpHeight));
        registerSetting(GenericSetting.createBooleanSetting(EmbossFilter.class, "Texture", false, false, EmbossFilter::setEmboss));

        ///FLARE - ADDITIONAL RENDERING REQUIRED
        registerImageFilter(EnumFilterTypes.STYLIZE, FlareFilter.class, "Flare", FlareFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "X Position", 0.5F, 0F, 1F, false, (filter, value) -> filter.setCentre(new Point2D.Float(value, (float)filter.getCentre().getY()))));
        registerSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Y Position", 0.5F, 0F, 1F, false, (filter, value) -> filter.setCentre(new Point2D.Float((float)filter.getCentre().getX(), value))));
        registerSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Radius", 0F, 0F, 400F, false, FlareFilter::setRadius));
        registerSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Base", 1.0F, 0F, 1F, false, FlareFilter::setBaseAmount));
        registerSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Ring", 0.2f, 0F, 1F, false, FlareFilter::setRingAmount));
        registerSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Ring Width", 1.6f, 0F, 10F, false, FlareFilter::setRingWidth));
        registerSetting(GenericSetting.createRangedFloatSetting(FlareFilter.class, "Ray", 0.1f, 0F, 1F, false, FlareFilter::setRayAmount));
        registerSetting(GenericSetting.createColourSetting(FlareFilter.class, "Colour", Color.WHITE, false, (filter, value) -> filter.setColor(ImageTools.getARGBFromColor(value))));

        ///HALFTONE - MASK NEEDED
        //registerImageFilter(EnumFilterTypes.STYLIZE, HalftoneFilter.class, "Halftone", HalftoneFilter::new, false);
        //registerSetting(GenericSetting.createRangedFloatSetting(HalftoneFilter.class, "Softness", 0.1f, 0F, 1F, false, HalftoneFilter::setSoftness));

        ///LIGHT EFFECTS - COMPLICATED

        ///OIL
        registerImageFilter(EnumFilterTypes.STYLIZE, OilFilter.class, "Oil", OilFilter::new, false);
        registerSetting(GenericSetting.createRangedIntSetting(OilFilter.class, "Range", 3, 1, 5, false, OilFilter::setRange));
        registerSetting(GenericSetting.createRangedIntSetting(OilFilter.class, "Levels", 256, 1, 256, false, OilFilter::setLevels));


        ///RAYS
        registerImageFilter(EnumFilterTypes.STYLIZE, RaysFilter.class, "Rays", RaysFilter::new, false); //extends MotionBlurOp
        registerSetting(GenericSetting.createRangedFloatSetting(RaysFilter.class, "Opacity", 1.0F, 0F, 1.0F, false, RaysFilter::setOpacity));
        registerSetting(GenericSetting.createRangedFloatSetting(RaysFilter.class, "Strength", 0.5F, 0F, 5.0F, false, RaysFilter::setStrength));
        registerSetting(GenericSetting.createRangedFloatSetting(RaysFilter.class, "Threshold", 0.0F, 0F, 1.0F, false, RaysFilter::setThreshold));
        registerSetting(GenericSetting.createBooleanSetting(RaysFilter.class, "Rays Only", false, false, RaysFilter::setRaysOnly));
        //missing color map

        ///SHAPEBURST
        registerImageFilter(EnumFilterTypes.STYLIZE, ShapeFilter.class, "Shape Burst", ShapeFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(ShapeFilter.class, "Factor", 1.0F, 0F, 5.0F, false, ShapeFilter::setFactor));
        registerSetting(GenericSetting.createOptionSetting(ShapeFilter.class, "Shape Type", List.of(EnumShapeFilter.values()), EnumShapeFilter.LINEAR, false, (filter, value) -> filter.setType(value.getShapeType())));
        registerSetting(GenericSetting.createBooleanSetting(ShapeFilter.class, "Invert", false, false, ShapeFilter::setInvert));
        registerSetting(GenericSetting.createBooleanSetting(ShapeFilter.class, "Merge", false, false, ShapeFilter::setMerge));
        registerSetting(GenericSetting.createBooleanSetting(ShapeFilter.class, "Use Alpha", false, false, ShapeFilter::setUseAlpha));
        //missing color map

        ///SPARKLE
        registerImageFilter(EnumFilterTypes.STYLIZE, SparkleFilter.class, "Sparkle", SparkleFilter::new, false);
        registerSetting(GenericSetting.createRangedIntSetting(SparkleFilter.class, "Range", 50, 0, 300, false, SparkleFilter::setRays));
        registerSetting(GenericSetting.createRangedIntSetting(SparkleFilter.class, "Radius", 25, 0, 300, false, SparkleFilter::setRadius));
        registerSetting(GenericSetting.createRangedIntSetting(SparkleFilter.class, "Shine", 50, 0, 100, false, SparkleFilter::setAmount));
        registerSetting(GenericSetting.createRangedIntSetting(SparkleFilter.class, "Randomness", 25, 0, 50, false, SparkleFilter::setRandomness));
        registerSetting(GenericSetting.createColourSetting(SparkleFilter.class, "Colour", Color.WHITE, false, (filter, value) -> filter.setColor(ImageTools.getARGBFromColor(value))));


        ///STAMP
        registerImageFilter(EnumFilterTypes.STYLIZE, StampFilter.class, "Stamp", StampFilter::new, false);
        registerSetting(GenericSetting.createRangedFloatSetting(StampFilter.class, "Radius", 5.0F, 0F, 100F, false, StampFilter::setRadius));
        registerSetting(GenericSetting.createRangedFloatSetting(StampFilter.class, "Threshold", 0.5F, 0F, 1.0F, false, StampFilter::setThreshold));
        registerSetting(GenericSetting.createRangedFloatSetting(StampFilter.class, "Softness", 0F, 0F, 1.0F, false, StampFilter::setSoftness));
        registerSetting(GenericSetting.createColourSetting(StampFilter.class, "Lower Colour", Color.BLACK, false, (filter, value) -> filter.setBlack(ImageTools.getARGBFromColor(value))));
        registerSetting(GenericSetting.createColourSetting(StampFilter.class, "Upper Colour", Color.WHITE, false, (filter, value) -> filter.setWhite(ImageTools.getARGBFromColor(value))));


        ///THRESHOLD
        registerImageFilter(EnumFilterTypes.STYLIZE, ThresholdFilter.class, "Threshold", ThresholdFilter::new, false);
        registerSetting(GenericSetting.createRangedIntSetting(ThresholdFilter.class, "Lower Threshold", 127, 0, 255, false, ThresholdFilter::setLowerThreshold));
        registerSetting(GenericSetting.createRangedIntSetting(ThresholdFilter.class, "Upper Threshold", 127, 0, 255, false, ThresholdFilter::setUpperThreshold));
        registerSetting(GenericSetting.createColourSetting(ThresholdFilter.class, "Lower Colour", Color.BLACK, false, (filter, value) -> filter.setBlack(ImageTools.getARGBFromColor(value))));
        registerSetting(GenericSetting.createColourSetting(ThresholdFilter.class, "Upper Colour", Color.WHITE, false, (filter, value) -> filter.setWhite(ImageTools.getARGBFromColor(value))));

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        ///TEXTURES - NOT FOR NOW

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        ///TRANSITION - NOT FOR NOW

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        ///VIDEO - NOT FOR NOW

        currentFilters.addListener((ListChangeListener<ObservableImageFilter>) c -> {
            DrawingBotV3.INSTANCE.onImageFiltersChanged();
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
            this.enable.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onImageFiltersChanged());
            //changes to filter settings are called from the FXController
        }

        public void onSettingChanged(){
            ///actually updating the image filter rendering is done elsewhere, this shouldn't always change as values do
            this.settingsString.set(filterSettings.toString());
        }
    }

}
