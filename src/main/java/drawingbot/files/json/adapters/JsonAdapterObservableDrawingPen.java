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
        settings.add(GenericSetting.createIntSetting(ObservableDrawingPen.class, "penNumber", 0, (I, V) -> I.penNumber.set(V)).setGetter(I -> I.penNumber.get()));

        settings.add(GenericSetting.createBooleanSetting(ObservableDrawingPen.class, "isEnabled", true, (I, V) -> I.enable.set(V)).setGetter(I -> I.enable.get()));
        settings.add(GenericSetting.createStringSetting(ObservableDrawingPen.class, "type", "", (I, V) -> I.type.set(V)).setGetter(I -> I.type.get()));
        settings.add(GenericSetting.createStringSetting(ObservableDrawingPen.class, "name", "", (I, V) -> I.name.set(V)).setGetter(I -> I.name.get()));
        settings.add(GenericSetting.createColourSetting(ObservableDrawingPen.class, "argb", Color.BLACK, (I, V) -> I.javaFXColour.set(V)).setGetter(I -> I.javaFXColour.get()));
        settings.add(GenericSetting.createIntSetting(ObservableDrawingPen.class, "distributionWeight", 100, (I, V) -> I.distributionWeight.set(V)).setGetter(I -> I.distributionWeight.get()));
        settings.add(GenericSetting.createFloatSetting(ObservableDrawingPen.class, "strokeSize", 1F, (I, V) -> I.strokeSize.set(V)).setGetter(I -> I.strokeSize.get()));
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
