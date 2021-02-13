package drawingbot.drawing;

import drawingbot.api.IDrawingPen;

public class DrawingPen implements IDrawingPen {

    private final String name; //the pen's name
    private final int argb; //the pen's argb colour

    public DrawingPen(IDrawingPen source){
        this(source.getName(), source.getARGB());
    }

    public DrawingPen(String name, int argb){
        this.name = name;
        this.argb = argb;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getARGB() {
        return argb;
    }

    @Override
    public String toString(){
        return getName();
    }

}