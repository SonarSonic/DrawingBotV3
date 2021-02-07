package drawingbot.drawing;

import drawingbot.api.IDrawingPen;

public class DrawingPen implements IDrawingPen {

    private final String name; //pens name
    private final int rgbColour; //rgb pen colour

    public DrawingPen(IDrawingPen source){
        this(source.getName(), source.getARGB());
    }

    public DrawingPen(String name, int colour){
        this.name = name;
        this.rgbColour = colour;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getARGB() {
        return rgbColour;
    }

    @Override
    public String toString(){
        return getName();
    }

}