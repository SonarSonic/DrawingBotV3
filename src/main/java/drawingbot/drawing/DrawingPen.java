package drawingbot.drawing;

public class DrawingPen implements IDrawingPen {

    private final String name; //pens name
    private final int rgbColour; //rgb pen colour

    public DrawingPen(IDrawingPen source){
        this(source.getName(), source.getRGBColour());
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
    public int getRGBColour() {
        return rgbColour;
    }

    @Override
    public String toString(){
        return getName();
    }

}