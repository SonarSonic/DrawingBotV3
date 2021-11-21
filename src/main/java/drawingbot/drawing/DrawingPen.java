package drawingbot.drawing;

import drawingbot.api.IDrawingPen;

public class DrawingPen implements IDrawingPen {

    public String type; //the pen's type
    public String name; //the pen's name
    public int argb; //the pen's argb colour
    public int distributionWeight;
    public float strokeSize;
    public boolean isEnabled = true;

    public DrawingPen(){} //for GSON

    public DrawingPen(IDrawingPen source){
        update(source);
    }

    public DrawingPen(String type, String name, int argb){
        this(type, name, argb, 100, 1F);
    }

    public DrawingPen(String type, String name, int argb, int distributionWeight, float strokeSize){
        update(type, name, argb, distributionWeight, strokeSize, true);
    }

    public void update(String type, String name, int argb, int distributionWeight, float strokeSize, boolean active){
        this.type = type;
        this.name = name;
        this.argb = argb;
        this.distributionWeight = distributionWeight;
        this.strokeSize = strokeSize;
        this.isEnabled = active;
    }

    public void update(IDrawingPen pen){
        update(pen.getType(), pen.getName(), pen.getARGB(), pen.getDistributionWeight(), pen.getStrokeSize(), pen.isEnabled());
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
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
    public int getARGB() {
        return argb;
    }

    @Override
    public float getStrokeSize() {
        return strokeSize;
    }

    @Override
    public int getDistributionWeight() {
        return distributionWeight;
    }

    @Override
    public String toString(){
        return getName();
    }

}