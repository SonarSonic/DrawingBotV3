package drawingbot.drawing;

import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.utils.GenericPreset;

import java.util.ArrayList;
import java.util.List;

public class DrawingSet implements IDrawingSet {

    public String type;
    public String name;
    public List<IDrawingPen> pens;

    public DrawingSet(String type, String name, List<IDrawingPen> pens) {
        this.type = type;
        this.name = name;
        this.pens = pens;
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
    public List<IDrawingPen> getPens() {
        return pens;
    }

    @Override
    public String toString(){
        return getName();
    }

    public DrawingSet copy(){
        return new DrawingSet(type, name, new ArrayList<>(pens));
    }
}
