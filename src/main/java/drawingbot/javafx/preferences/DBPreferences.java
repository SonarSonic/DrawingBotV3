package drawingbot.javafx.preferences;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.api.ISettings;
import drawingbot.files.DrawingExportHandler;
import drawingbot.files.ExportTask;
import drawingbot.files.exporters.GCodeSettings;
import drawingbot.files.json.PresetData;
import drawingbot.files.json.PresetType;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.editors.Editors;
import drawingbot.javafx.settings.*;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.canvas.CanvasUtils;
import drawingbot.plotting.canvas.ObservableCanvas;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static drawingbot.javafx.GenericSetting.*;

public class DBPreferences implements ISettings {

    public final static DBPreferences INSTANCE = new DBPreferences();

    ///////////////////////////////////////////////

    public static final String CATEGORY_GENERAL = "General";
    public static final String CATEGORY_ADVANCED = "Advanced";
    public static final String CATEGORY_OPTIMISATION = "Path Optimisation";
    public static final String CATEGORY_NOTIFICATIONS = "Notifications";
    public static final String CATEGORY_SVG = "SVG";
    public static final String CATEGORY_IMAGE = "Image";
    public static final String CATEGORY_ANIMATION = "Animation";
    public static final String CATEGORY_USER_INTERFACE = "User Interface";

    public final List<GenericSetting<?, ?>> settings = new ArrayList<>();
    public final ObservableList<GenericSetting<?, ?>> observableList;

    private DBPreferences(){
        observableList = PropertyUtil.createPropertiesList(settings);
    }

    public <T extends GenericSetting<?, ?>> T register(T add){
        settings.add(add);
        add.createDefaultGetterAndSetter();
        return add;
    }

    ///////////////////////////////////////////////

    public final IntegerSetting<?> maxTextureSize = register(createRangedIntSetting(DBPreferences.class, CATEGORY_ADVANCED, "maxTextureSize", -1, -1, 8096));
    public final BooleanSetting<?> disableOpenGLRenderer = register(createBooleanSetting(DBPreferences.class, CATEGORY_ADVANCED, "disableOpenGLRenderer", false));

    ///////////////////////////////////////////////

    //// DEFAULTS \\\\
    public final StringSetting<?> defaultPFM = register(createStringSetting(DBPreferences.class, CATEGORY_GENERAL, "defaultPFM", "Sketch Lines PFM"));
    public final ColourSetting<?> defaultCanvasColour = register(createColourSetting(DBPreferences.class, CATEGORY_GENERAL, "defaultCanvasColour", Color.WHITE));
    public final ColourSetting<?> defaultBackgroundColour = register(createColourSetting(DBPreferences.class, CATEGORY_GENERAL, "defaultBackgroundColour", ObservableCanvas.backgroundColourDefault));
    public final OptionSetting<?, EnumClippingMode> defaultClippingMode = register(createOptionSetting(DBPreferences.class, EnumClippingMode.class, CATEGORY_GENERAL, "defaultClippingMode", FXCollections.observableArrayList(EnumClippingMode.values()), EnumClippingMode.DRAWING));
    public final FloatSetting<?> defaultPenWidth = (FloatSetting<?>) register(createFloatSetting(DBPreferences.class, CATEGORY_GENERAL, "defaultPenWidth", 0.3F).setValidator(Math::abs));
    public final OptionSetting<?, EnumRescaleMode> defaultRescalingMode = register(createOptionSetting(DBPreferences.class, EnumRescaleMode.class, CATEGORY_GENERAL, "defaultRescaleNode", FXCollections.observableArrayList(EnumRescaleMode.values()), EnumRescaleMode.NORMAL_QUALITY));
    public final BooleanSetting<?> defaultRangeExport = register(createBooleanSetting(DBPreferences.class, CATEGORY_GENERAL, "defaultRangeExport", true));
    public final OptionSetting<?, EnumBlendMode> defaultBlendMode = register(createOptionSetting(DBPreferences.class, EnumBlendMode.class, CATEGORY_GENERAL, "defaultBlendMode", FXCollections.observableArrayList(EnumBlendMode.values()), EnumBlendMode.NORMAL));
    public final OptionSetting<?, ExportTask.Mode> quickExportMode = register(createOptionSetting(DBPreferences.class, ExportTask.Mode.class, CATEGORY_GENERAL, "defaultExportMode", FXCollections.observableArrayList(ExportTask.Mode.values()), ExportTask.Mode.PER_DRAWING));
    public final StringSetting<?> quickExportHandler = register(createStringSetting(DBPreferences.class, CATEGORY_GENERAL, "defaultExportHandler", "svg_default"));
    public final BooleanSetting<?> autoRunPFM = register(createBooleanSetting(DBPreferences.class, CATEGORY_GENERAL, "autoRunPFM", true));

    ///////////////////////////////////////////////

    //// ADVANCED \\\\

    public final DoubleSetting<?> importDPI = (DoubleSetting<?>) register(createRangedDoubleSetting(DBPreferences.class, CATEGORY_IMAGE, "importDPI", 150D, 1D, Short.MAX_VALUE)).setDisplaySlider(false);


    ///////////////////////////////////////////////

    //// FILES \\\\
    public final StringSetting<?> defaultImportDirectory = (StringSetting<?>) register(createStringSetting(DBPreferences.class, CATEGORY_GENERAL, "defaultImportDirectory", "").setEditorFactory((context, prop) -> Editors.createDirectoryPicker(context, prop, () -> DrawingBotV3.project().getImportDirectory())));
    public final StringSetting<?> defaultExportDirectory = (StringSetting<?>) register(createStringSetting(DBPreferences.class, CATEGORY_GENERAL, "defaultExportDirectory", "").setEditorFactory((context, prop) -> Editors.createDirectoryPicker(context, prop, () -> DrawingBotV3.project().getExportDirectory())));

    ///////////////////////////////////////////////

    //// PATH OPTIMISATION \\\\

    public final BooleanSetting<?> pathOptimisationEnabled = register(createBooleanSetting(DBPreferences.class, CATEGORY_OPTIMISATION, "pathOptimisationEnabled", true));

    public final BooleanSetting<?> lineSimplifyEnabled = register(createBooleanSetting(DBPreferences.class, CATEGORY_OPTIMISATION, "lineSimplifyEnabled", true));
    public final DoubleSetting<?> lineSimplifyTolerance = (DoubleSetting<?>) register(createRangedDoubleSetting(DBPreferences.class, CATEGORY_OPTIMISATION, "lineSimplifyTolerance", 0.1D, 0.1D, 100D).setDisplaySlider(false));
    public final OptionSetting<?, UnitsLength> lineSimplifyUnits = register(createOptionSetting(DBPreferences.class, UnitsLength.class, CATEGORY_OPTIMISATION, "lineSimplifyUnits", FXCollections.observableArrayList(UnitsLength.values()), UnitsLength.MILLIMETRES));

    public final BooleanSetting<?> lineMergingEnabled = register(createBooleanSetting(DBPreferences.class, CATEGORY_OPTIMISATION, "lineMergingEnabled", true));
    public final DoubleSetting<?> lineMergingTolerance = (DoubleSetting<?>) register(createRangedDoubleSetting(DBPreferences.class, CATEGORY_OPTIMISATION, "lineMergingTolerance", 0.5D, 0.1D, 100D).setDisplaySlider(false));
    public final OptionSetting<?, UnitsLength> lineMergingUnits = register(createOptionSetting(DBPreferences.class, UnitsLength.class, CATEGORY_OPTIMISATION, "lineMergingUnits", FXCollections.observableArrayList(UnitsLength.values()), UnitsLength.MILLIMETRES));

    public final BooleanSetting<?> lineFilteringEnabled = register(createBooleanSetting(DBPreferences.class, CATEGORY_OPTIMISATION, "lineFilteringEnabled", false));
    public final DoubleSetting<?> lineFilteringTolerance = (DoubleSetting<?>) register(createRangedDoubleSetting(DBPreferences.class, CATEGORY_OPTIMISATION, "lineFilteringTolerance", 0.5D, 0.1D, 100D).setDisplaySlider(false));
    public final OptionSetting<?, UnitsLength> lineFilteringUnits = register(createOptionSetting(DBPreferences.class, UnitsLength.class, CATEGORY_OPTIMISATION, "lineFilteringUnits", FXCollections.observableArrayList(UnitsLength.values()), UnitsLength.MILLIMETRES));

    public final BooleanSetting<?> lineSortingEnabled = register(createBooleanSetting(DBPreferences.class, CATEGORY_OPTIMISATION, "lineSortingEnabled", true));
    public final DoubleSetting<?> lineSortingTolerance = (DoubleSetting<?>) register(createRangedDoubleSetting(DBPreferences.class, CATEGORY_OPTIMISATION, "lineSortingTolerance", 1D, 0.1D, 100D).setDisplaySlider(false));
    public final OptionSetting<?, UnitsLength> lineSortingUnits = register(createOptionSetting(DBPreferences.class, UnitsLength.class, CATEGORY_OPTIMISATION, "lineSortingUnits", FXCollections.observableArrayList(UnitsLength.values()), UnitsLength.MILLIMETRES));

    public final BooleanSetting<?> multipassEnabled = register(createBooleanSetting(DBPreferences.class, CATEGORY_OPTIMISATION, "multipassEnabled", false));
    public final IntegerSetting<?> multipassCount = (IntegerSetting<?>) register(createRangedIntSetting(DBPreferences.class, CATEGORY_OPTIMISATION, "multipassCount", 1, 1, 100)).setDisplaySlider(false);

    public final BooleanSetting<?> allowMultiplePathMoves = register(createBooleanSetting(DBPreferences.class, CATEGORY_OPTIMISATION, "allowMultiplePathMoves", false));


    ///////////////////////////////////////////////

    //// USER INTERFACE \\\\
    public final BooleanSetting<?> showExportedDrawing = register(createBooleanSetting(DBPreferences.class, CATEGORY_NOTIFICATIONS, "showExportedDrawing", true));
    public final BooleanSetting<?> darkTheme = register(createBooleanSetting(DBPreferences.class, CATEGORY_NOTIFICATIONS, "darkTheme", false));
    public final ObjectProperty<Color> defaultThemeColor = new SimpleObjectProperty<>();


    public final OptionSetting<?, EnumWindowSize> uiWindowSize = register(createOptionSetting(DBPreferences.class, EnumWindowSize.class, CATEGORY_USER_INTERFACE, "uiWindowSize", FXCollections.observableArrayList(EnumWindowSize.values()), EnumWindowSize.DEFAULT));
    public final BooleanSetting<?> restoreLayout = register(createBooleanSetting(DBPreferences.class, CATEGORY_NOTIFICATIONS, "restoreLayout", true));
    public final BooleanSetting<?> restoreProjectLayout = register(createBooleanSetting(DBPreferences.class, CATEGORY_NOTIFICATIONS, "restoreProjectLayout", true));

    //// OVERLAYS \\\\
    public final BooleanSetting<?> rulersEnabled = register(createBooleanSetting(DBPreferences.class, CATEGORY_NOTIFICATIONS, "rulersEnabled", true));
    public final BooleanSetting<?> drawingBordersEnabled = register(createBooleanSetting(DBPreferences.class, CATEGORY_NOTIFICATIONS, "drawingBordersEnabled", false));
    public final ColourSetting<?> drawingBordersColor = register(createColourSetting(DBPreferences.class, CATEGORY_NOTIFICATIONS, "drawingBordersColor", Color.BLACK));

    ///////////////////////////////////////////////

    //// NOTIFICATIONS \\\\

    public final BooleanSetting<?> notificationsEnabled = register(createBooleanSetting(DBPreferences.class, CATEGORY_NOTIFICATIONS, "notificationsEnabled", true));
    public final IntegerSetting<?> notificationsScreenTime = (IntegerSetting<?>) register(createRangedIntSetting(DBPreferences.class, CATEGORY_OPTIMISATION, "notificationsScreenTime", 7, 0, 1000)).setDisplaySlider(false);

    ///////////////////////////////////////////////

    //// SVG SETTINGS \\\\

    public final StringSetting<?> svgLayerNaming = register(createStringSetting(DBPreferences.class, CATEGORY_SVG, "svgLayerNaming", "%NAME%"));
    public final BooleanSetting<?> exportSVGBackground = register(createBooleanSetting(DBPreferences.class, CATEGORY_SVG, "exportSVGBackground", false));
    public final BooleanSetting<?> svgDrawingStatsComment = register(createBooleanSetting(DBPreferences.class, CATEGORY_SVG, "svgDrawingStatsComment", true));
    public final BooleanSetting<?> svgPFMSettingsText = register(createBooleanSetting(DBPreferences.class, CATEGORY_SVG, "svgPFMSettingsText", false));

    ///////////////////////////////////////////////

    //// IMAGE SETTINGS \\\\

    public final DoubleSetting<?> exportDPI = (DoubleSetting<?>) register(createRangedDoubleSetting(DBPreferences.class, CATEGORY_IMAGE, "exportDPI", 300D, 1D, Short.MAX_VALUE)).setDisplaySlider(false).setDisplayName("Export DPI");
    public final BooleanSetting<?> transparentPNG = (BooleanSetting<?>) register(createBooleanSetting(DBPreferences.class, CATEGORY_IMAGE, "transparentPNG", false).setDisplayName("Export Transparent PNGs"));

    //// GCODE SETTINGS \\\\

    public final GCodeSettings gcodeSettings = new GCodeSettings();
    public final SimpleObjectProperty<GenericPreset<PresetData>> selectedGCodePreset = new SimpleObjectProperty<>();
    public final BooleanSetting<?> showGCodeExportSettings = register(createBooleanSetting(DBPreferences.class, CATEGORY_NOTIFICATIONS, "showGCodeExportSettings", false));

    ///////////////////////////////////////////////

    //// ANIMATION SETTINGS \\\\

    public final IntegerSetting<?> framesPerSecond = (IntegerSetting<?>) register(createIntSetting(DBPreferences.class, CATEGORY_ANIMATION, "framesPerSecond", 25).addRecommendedValues(24, 25, 30, 50, 60));
    public final IntegerSetting<?> duration = register(createIntSetting(DBPreferences.class, CATEGORY_ANIMATION, "duration", 5));
    public final IntegerSetting<?> frameHoldStart = register(createIntSetting(DBPreferences.class, CATEGORY_ANIMATION, "frameHoldStart", 1));
    public final IntegerSetting<?> frameHoldEnd = register(createIntSetting(DBPreferences.class, CATEGORY_ANIMATION, "frameHoldEnd", 1));
    public final OptionSetting<?, UnitsTime> durationUnits = register(createOptionSetting(DBPreferences.class, UnitsTime.class, CATEGORY_ANIMATION, "durationUnits", FXCollections.observableArrayList(UnitsTime.values()), UnitsTime.SECONDS));

    //// ANIMATION STATS \\\\\

    public final StringProperty imageExportSize = new SimpleStringProperty("");
    public final StringProperty animationFrameCount = new SimpleStringProperty("");
    public final StringProperty animationGeometriesPFrame = new SimpleStringProperty("");
    public final StringProperty animationVerticesPFrame = new SimpleStringProperty("");

    ///////////////////////////////////////////////

    //// OPEN GL RENDERER \\\\

    ///////////////////////////////////////////////

    //// PRESET SETTINGS \\\\

    private final MapSetting<?, String, String> defaultPresets = register(new MapSetting<>(DBPreferences.class, String.class, String.class, CATEGORY_ADVANCED, "defaultPresets", FXCollections.observableMap(new HashMap<>())));
    public BooleanProperty flagDefaultPresetChange = new SimpleBooleanProperty(false);

    ///////////////////////////////////////////////

    //// CMYK SETTINGS \\\\
    public final FloatSetting<?> defaultColorSplitterPenMultiplier = (FloatSetting<?>) register(createRangedFloatSetting(DBPreferences.class, "CMYK", "defaultColorSplitterPenMultiplier", 1F, 0F, 1F)).setDisplayName("Generic Pen Multiplier");
    public final FloatSetting<?> defaultColorSplitterPenOpacity = (FloatSetting<?>) register(createRangedFloatSetting(DBPreferences.class, "CMYK", "defaultColorSplitterPenOpacity", 0.25F, 0F, 1F)).setDisplayName("Generic Pen Opacity");


    public String getDefaultPreset(PresetType type) {
        return defaultPresets.get().get(type.getRegistryName());
    }

    public void setDefaultPreset(PresetType type, GenericPreset<?> preset){
        defaultPresets.get().put(type.getRegistryName(), preset.getPresetSubType() + ":" + preset.getPresetName());
        onDefaultPresetsChanged();
    }

    public void clearDefaultPreset(PresetType type){
        defaultPresets.get().remove(type.getRegistryName());
        onDefaultPresetsChanged();
    }

    public String getDefaultPreset(PresetType type, String subType) {
        return defaultPresets.get().get(type.getRegistryName() + ":" + subType);
    }

    public void setDefaultPresetSubType(PresetType type, GenericPreset<?> preset){
        defaultPresets.get().put(type.getRegistryName() + ":" + preset.getPresetSubType(), preset.getPresetName());
        onDefaultPresetsChanged();
    }

    public void clearDefaultPresetSubType(PresetType type, String subType){
        defaultPresets.get().remove(type.getRegistryName() + ":" + subType);
        onDefaultPresetsChanged();
    }

    private void onDefaultPresetsChanged(){
        Register.PRESET_LOADER_PREFERENCES.updateConfigs();
        flagDefaultPresetChange.set(!flagDefaultPresetChange.get());
    }

    ////

    public DrawingExportHandler getQuickExportHandler(){
        DrawingExportHandler handler = MasterRegistry.INSTANCE.drawingExportHandlers.get(quickExportHandler.get());
        if(handler == null){
            quickExportHandler.set(Register.EXPORT_SVG.getRegistryName());
        }
        return handler;
    }

    public int getFrameCount(){
        return (int)(framesPerSecond.get() * durationUnits.get().toSeconds(duration.get()));
    }

    public int getFrameHoldStartCount(){
        return (int)(framesPerSecond.get() * durationUnits.get().toSeconds(frameHoldStart.get()));
    }

    public int getFrameHoldEndCount(){
        return (int)(framesPerSecond.get() * durationUnits.get().toSeconds(frameHoldEnd.get()));
    }

    public int getGeometriesPerFrame(int count){
        return Math.max(1, count / getFrameCount());
    }

    public long getVerticesPerFrame(long count){
        return Math.max(1, count / getFrameCount());
    }

    public void updateImageSequenceStats(){
        PlottedDrawing drawing = DrawingBotV3.taskManager().getCurrentDrawing();

        int frameCount = getFrameCount();
        int geometriesPerFrame = drawing == null ? 0 : getGeometriesPerFrame(drawing.getGeometryCount());
        long verticesPerFrame = drawing == null ? 0 : getVerticesPerFrame(drawing.getVertexCount());


        if(verticesPerFrame == 1 && frameCount > verticesPerFrame){
            frameCount = (int) drawing.getVertexCount();
        }

        if(DrawingBotV3.project().getOpenImage() != null){

            int exportWidth = CanvasUtils.getRasterExportWidth(DrawingBotV3.project().getDrawingArea(), exportDPI.get(), false);
            int exportHeight = CanvasUtils.getRasterExportHeight(DrawingBotV3.project().getDrawingArea(), exportDPI.get(), false);
            DBPreferences.INSTANCE.imageExportSize.set(exportWidth + " x " + exportHeight);

        }else{
            DBPreferences.INSTANCE.imageExportSize.set("0 x 0");
        }

        DBPreferences.INSTANCE.animationFrameCount.set("" + Utils.defaultNF.format(frameCount));
        DBPreferences.INSTANCE.animationGeometriesPFrame.set("" + Utils.defaultNF.format(geometriesPerFrame));
        DBPreferences.INSTANCE.animationVerticesPFrame.set("" + Utils.defaultNF.format(verticesPerFrame));
    }

    public void postInit(){
        defaultCanvasColour.valueProperty().addListener((observable, oldValue, newValue) -> {
            DrawingBotV3.project().getDrawingArea().canvasColor.set(defaultCanvasColour.getValue());
        });
        defaultBackgroundColour.valueProperty().addListener((observable, oldValue, newValue) -> {
            DrawingBotV3.project().getDrawingArea().backgroundColor.set(defaultBackgroundColour.getValue());
        });
        defaultPenWidth.valueProperty().addListener((observable, oldValue, newValue) -> {
            DrawingBotV3.project().getDrawingArea().targetPenWidth.set(defaultPenWidth.getValue());
        });
        defaultClippingMode.valueProperty().addListener((observable, oldValue, newValue) -> {
            DrawingBotV3.project().getDrawingArea().clippingMode.set(defaultClippingMode.getValue());
        });
        defaultRescalingMode.valueProperty().addListener((observable, oldValue, newValue) -> {
            DrawingBotV3.project().getDrawingArea().rescaleMode.set(defaultRescalingMode.getValue());
        });
        defaultBlendMode.valueProperty().addListener((observable, oldValue, newValue) -> {
            DrawingBotV3.project().blendMode.set(defaultBlendMode.getValue());
        });
        defaultRangeExport.valueProperty().addListener((observable, oldValue, newValue) -> {
            DrawingBotV3.project().exportRange.set(defaultRangeExport.getValue());
        });

        //TODO BINDINGS
        exportDPI.valueProperty().addListener((observable, oldValue, newValue) -> updateImageSequenceStats());
        framesPerSecond.valueProperty().addListener((observable, oldValue, newValue) -> updateImageSequenceStats());
        duration.valueProperty().addListener((observable, oldValue, newValue) -> updateImageSequenceStats());
        frameHoldStart.valueProperty().addListener((observable, oldValue, newValue) -> updateImageSequenceStats());
        frameHoldEnd.valueProperty().addListener((observable, oldValue, newValue) -> updateImageSequenceStats());
        durationUnits.valueProperty().addListener((observable, oldValue, newValue) -> updateImageSequenceStats());
        updateImageSequenceStats();

        selectedGCodePreset.setValue(Register.PRESET_LOADER_GCODE_SETTINGS.getDefaultPreset());
        selectedGCodePreset.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                Register.PRESET_MANAGER_GCODE_SETTINGS.applyPreset(DrawingBotV3.context(), gcodeSettings, newValue, false);
            }
        });

        uiWindowSize.valueProperty().addListener((observable, oldValue, newValue) -> {
            uiWindowSize.get().setupStage(FXApplication.primaryStage);
        });
        darkTheme.valueProperty().addListener((observable, oldValue, newValue) -> {
            FXApplication.applyCurrentTheme();
        });

        defaultThemeColor.bind(Bindings.createObjectBinding(() -> darkTheme.get() ? Color.WHITE : Color.BLACK, darkTheme.valueProperty()));
    }

    @Override
    public ObservableList<GenericSetting<?, ?>> getPropertyList(){
        return observableList;
    }
}