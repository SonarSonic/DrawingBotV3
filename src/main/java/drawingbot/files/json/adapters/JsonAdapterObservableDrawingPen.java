package drawingbot.files.json.adapters;

import drawingbot.api.IDrawingPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableDrawingPen;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class JsonAdapterObservableDrawingPen extends JsonAdapterAbstract<ObservableDrawingPen>{

    public static List<GenericSetting<?, ?>> settings;

    static{
        settings = new ArrayList<>();
        settings.add(GenericSetting.createObjectSetting(ObservableDrawingPen.class, IDrawingPen.class, "source", null, (I, V) -> I.source = V).setGetter(I -> new DrawingPen(I.source)));
        settings.add(GenericSetting.createIntSetting(ObservableDrawingPen.class, "penNumber", 0, i -> i.penNumber));
        settings.add(GenericSetting.createBooleanSetting(ObservableDrawingPen.class, "isEnabled", true, i -> i.enable));
        settings.add(GenericSetting.createStringSetting(ObservableDrawingPen.class, "type", "", i -> i.type));
        settings.add(GenericSetting.createStringSetting(ObservableDrawingPen.class, "name", "", i -> i.name));
        settings.add(GenericSetting.createColourSetting(ObservableDrawingPen.class, "argb", Color.BLACK, i -> i.javaFXColour));
        settings.add(GenericSetting.createIntSetting(ObservableDrawingPen.class, "distributionWeight", 100, i -> i.distributionWeight));
        settings.add(GenericSetting.createFloatSetting(ObservableDrawingPen.class, "strokeSize", 1F, i -> i.strokeSize));

        //// COLOUR SPLITTER DATA \\\\
        settings.add(GenericSetting.createFloatSetting(ObservableDrawingPen.class, "colorSplitMultiplier", 1F, i -> i.colorSplitMultiplier));
        settings.add(GenericSetting.createFloatSetting(ObservableDrawingPen.class, "colorSplitOpacity", 1F, i -> i.colorSplitOpacity));
        settings.add(GenericSetting.createFloatSetting(ObservableDrawingPen.class, "colorSplitOffsetX", 0F, i -> i.colorSplitOffsetX));
        settings.add(GenericSetting.createFloatSetting(ObservableDrawingPen.class, "colorSplitOffsetY", 0F, i -> i.colorSplitOffsetY));
     }


    @Override
    public List<GenericSetting<?, ?>> getSettings() {
        return settings;
    }

    @Override
    public ObservableDrawingPen getInstance() {
        return new ObservableDrawingPen();
    }
}
