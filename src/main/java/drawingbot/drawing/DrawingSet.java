package drawingbot.drawing;

import drawingbot.api.IDrawingSet;

import java.util.ArrayList;
import java.util.List;

public class DrawingSet implements IDrawingSet {

    public String type;
    public String name;
    public List<DrawingPen> pens;

    public DrawingSet(){}

    public DrawingSet(String type, String name, List<DrawingPen> pens) {
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
    public List<DrawingPen> getPens() {
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
