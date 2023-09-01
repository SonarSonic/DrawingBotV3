package drawingbot.drawing;

import drawingbot.api.IDrawingSet;
import drawingbot.files.json.JsonData;
import drawingbot.javafx.GenericPreset;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@JsonData
public class DrawingSet implements IDrawingSet, IColorManagedDrawingSet {

    public String type;
    public String name;
    public List<DrawingPen> pens;

    //// COLOUR SPLITTER DATA \\\\
    public ColorSeparationHandler colorHandler = null;
    public ColorSeparationSettings colorSettings = null;

    //// PRESET DATA \\\\
    @Nullable
    public transient GenericPreset<DrawingSet> preset;

    public DrawingSet(){}

    public DrawingSet(String type, String name, List<DrawingPen> pens) {
        this.type = type;
        this.name = name;
        this.pens = pens;
    }

    public DrawingSet(String type, String name, List<DrawingPen> pens, ColorSeparationHandler colorHandler, ColorSeparationSettings colorSettings) {
        this(type, name, pens);
        this.colorHandler = colorHandler;
        this.colorSettings = colorSettings;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<DrawingPen> getPens() {
        return pens;
    }

    @Override
    public boolean isUserCreated() {
        return preset != null && preset.userCreated;
    }

    @Override
    public String toString(){
        return getName();
    }

    public DrawingSet copy(){
        return new DrawingSet(type, name, new ArrayList<>(pens));
    }

    @Override
    public ColorSeparationHandler getColorSeparationHandler() {
        return colorHandler;
    }

    @Override
    public ColorSeparationSettings getColorSeparationSettings() {
        return colorSettings;
    }

    @Override
    public void setColorSeparation(ColorSeparationHandler handler, ColorSeparationSettings settings) {
        this.colorHandler = handler;
        this.colorSettings = settings;
    }

}