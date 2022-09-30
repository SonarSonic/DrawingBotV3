package drawingbot.javafx.preferences;

import drawingbot.api.IDrawingPen;
import drawingbot.api.IProperties;
import drawingbot.drawing.DrawingPen;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.settings.*;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.utils.UnitsLength;
import drawingbot.utils.UnitsTime;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class ProgramSettings implements IProperties {

    ///////////////////////////////////////////////

    public static final String CATEGORY_ADVANCED = "Advanced";
    public static final String CATEGORY_OPTIMISATION = "Path Optimisation";
    public static final String CATEGORY_NOTIFICATIONS = "Notifications";
    public static final String CATEGORY_SVG = "SVG";
    public static final String CATEGORY_IMAGE = "Image";
    public static final String CATEGORY_ANIMATION = "Animation";
    public static final String CATEGORY_USER_INTERFACE = "User Interface";


    ///////////////////////////////////////////////

    //public BooleanProperty isDeveloperMode = new SimpleBooleanProperty(false);
    public IntegerSetting<?> maxTextureSize = GenericSetting.createRangedIntSetting(ProgramSettings.class, CATEGORY_ADVANCED, "maxTextureSize", -1, -1, 8096);
    public BooleanSetting<?> disableOpenGLRenderer = GenericSetting.createBooleanSetting(ProgramSettings.class, CATEGORY_ADVANCED, "disableOpenGLRenderer", false);

    ///////////////////////////////////////////////

    //// PATH OPTIMISATION \\\\

    public BooleanSetting<?> pathOptimisationEnabled = GenericSetting.createBooleanSetting(ProgramSettings.class, CATEGORY_OPTIMISATION, "pathOptimisationEnabled", true);

    public BooleanSetting<?> lineSimplifyEnabled = GenericSetting.createBooleanSetting(ProgramSettings.class, CATEGORY_OPTIMISATION, "lineSimplifyEnabled", true);
    public DoubleSetting<?> lineSimplifyTolerance = GenericSetting.createRangedDoubleSetting(ProgramSettings.class, CATEGORY_OPTIMISATION, "lineSimplifyTolerance", 0.1D, 0.1D, 100D);
    public OptionSetting<?, UnitsLength> lineSimplifyUnits = GenericSetting.createOptionSetting(ProgramSettings.class, UnitsLength.class, CATEGORY_OPTIMISATION, "lineSimplifyUnits", UnitsLength.OBSERVABLE_LIST, UnitsLength.MILLIMETRES);

    public BooleanSetting<?> lineMergingEnabled = GenericSetting.createBooleanSetting(ProgramSettings.class, CATEGORY_OPTIMISATION, "lineMergingEnabled", true);
    public DoubleSetting<?> lineMergingTolerance = GenericSetting.createRangedDoubleSetting(ProgramSettings.class, CATEGORY_OPTIMISATION, "lineMergingTolerance", 0.5D, 0.1D, 100D);
    public OptionSetting<?, UnitsLength> lineMergingUnits = GenericSetting.createOptionSetting(ProgramSettings.class, UnitsLength.class, CATEGORY_OPTIMISATION, "lineMergingUnits", UnitsLength.OBSERVABLE_LIST, UnitsLength.MILLIMETRES);

    public BooleanSetting<?> lineFilteringEnabled = GenericSetting.createBooleanSetting(ProgramSettings.class, CATEGORY_OPTIMISATION, "lineFilteringEnabled", false);
    public DoubleSetting<?> lineFilteringTolerance = GenericSetting.createRangedDoubleSetting(ProgramSettings.class, CATEGORY_OPTIMISATION, "lineFilteringTolerance", 0.5D, 0.1D, 100D);
    public OptionSetting<?, UnitsLength> lineFilteringUnits = GenericSetting.createOptionSetting(ProgramSettings.class, UnitsLength.class, CATEGORY_OPTIMISATION, "lineFilteringUnits", UnitsLength.OBSERVABLE_LIST, UnitsLength.MILLIMETRES);

    public BooleanSetting<?> lineSortingEnabled = GenericSetting.createBooleanSetting(ProgramSettings.class, CATEGORY_OPTIMISATION, "lineSortingEnabled", false);
    public DoubleSetting<?> lineSortingTolerance = GenericSetting.createRangedDoubleSetting(ProgramSettings.class, CATEGORY_OPTIMISATION, "lineSortingTolerance", 1D, 0.1D, 100D);
    public OptionSetting<?, UnitsLength> lineSortingUnits = GenericSetting.createOptionSetting(ProgramSettings.class, UnitsLength.class, CATEGORY_OPTIMISATION, "lineSortingUnits", UnitsLength.OBSERVABLE_LIST, UnitsLength.MILLIMETRES);

    public BooleanSetting<?> multipassEnabled = GenericSetting.createBooleanSetting(ProgramSettings.class, CATEGORY_OPTIMISATION, "multipassEnabled", false);
    public IntegerSetting<?> multipassCount = GenericSetting.createRangedIntSetting(ProgramSettings.class, CATEGORY_OPTIMISATION, "multipassCount", 1, 1, 100);

    ///////////////////////////////////////////////

    //// NOTIFICATIONS \\\\

    public BooleanSetting<?> notificationsEnabled = GenericSetting.createBooleanSetting(ProgramSettings.class, CATEGORY_NOTIFICATIONS, "notificationsEnabled", true);
    public IntegerSetting<?> notificationsScreenTime = GenericSetting.createRangedIntSetting(ProgramSettings.class, CATEGORY_OPTIMISATION, "notificationsScreenTime", 7, 0, 1000);

    ///////////////////////////////////////////////

    //// SVG SETTINGS \\\\

    public StringSetting<?> svgLayerNaming = GenericSetting.createStringSetting(ProgramSettings.class, CATEGORY_SVG, "svgLayerNaming", "%NAME%");
    public BooleanSetting<?> exportSVGBackground = GenericSetting.createBooleanSetting(ProgramSettings.class, CATEGORY_SVG, "exportSVGBackground", false);

    ///////////////////////////////////////////////

    //// IMAGE SETTINGS \\\\

    public DoubleSetting<?> exportDPI = GenericSetting.createRangedDoubleSetting(ProgramSettings.class, CATEGORY_IMAGE, "exportDPI", 300D, 1D, Short.MAX_VALUE);

    ///////////////////////////////////////////////

    //// ANIMATION SETTINGS \\\\

    public IntegerSetting<?> framesPerSecond = GenericSetting.createIntSetting(ProgramSettings.class, CATEGORY_ANIMATION, "framesPerSecond", 25);
    public IntegerSetting<?> duration = GenericSetting.createIntSetting(ProgramSettings.class, CATEGORY_ANIMATION, "duration", 5);
    public IntegerSetting<?> frameHoldStart = GenericSetting.createIntSetting(ProgramSettings.class, CATEGORY_ANIMATION, "frameHoldStart", 1);
    public IntegerSetting<?> frameHoldEnd = GenericSetting.createIntSetting(ProgramSettings.class, CATEGORY_ANIMATION, "frameHoldEnd", 1);
    public OptionSetting<?, UnitsTime> durationUnits = GenericSetting.createOptionSetting(ProgramSettings.class, UnitsTime.class, CATEGORY_ANIMATION, "durationUnits", UnitsTime.OBSERVABLE_LIST, UnitsTime.SECONDS);

    ///////////////////////////////////////////////

    //// OPEN GL RENDERER \\\\

    public BooleanSetting<?> darkTheme = GenericSetting.createBooleanSetting(ProgramSettings.class, CATEGORY_USER_INTERFACE, "darkTheme", false);

    ///////////////////////////////////////////////

    //// PRESET SETTINGS \\\\

    public ObservableMap<String, String> defaultPresets = FXCollections.observableHashMap();

    ///////////////////////////////////////////////

    ////

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

    public final ObservableList<GenericSetting<?, ?>> settings = PropertyUtil.createPropertiesListFromSettings(/*developerMode*/maxTextureSize, pathOptimisationEnabled, lineSimplifyEnabled, lineSimplifyTolerance, lineSimplifyUnits, lineMergingEnabled, lineMergingTolerance, lineMergingUnits, lineFilteringEnabled, lineFilteringTolerance, lineFilteringUnits, lineSortingEnabled, lineSortingTolerance, lineSortingUnits, multipassEnabled, multipassCount, svgLayerNaming, exportSVGBackground, exportDPI, framesPerSecond, duration, frameHoldStart, frameHoldEnd, durationUnits, disableOpenGLRenderer, darkTheme);

    @Override
    public ObservableList<GenericSetting<?, ?>> getObservables() {
        return settings;
    }

}