package drawingbot.drawing;

import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;

import java.util.ArrayList;
import java.util.List;

public class DrawingSet implements IDrawingSet {

    private final String name;
    private final List<IDrawingPen> pens;

    public DrawingSet(String name, List<IDrawingPen> pens) {
        this.name = name;
        this.pens = pens;
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
        return new DrawingSet(name, new ArrayList<>(pens));
    }
}
