package drawingbot.drawing;

import drawingbot.api.IDrawingPen;

public class DrawingPen implements IDrawingPen {

    public String type; //the pen's type
    public String name; //the pen's name
    public int argb; //the pen's argb colour
    public int distributionWeight;
    public float strokeSize;

    public DrawingPen(){} //for GSON

    public DrawingPen(IDrawingPen source){
        update(source);
    }

    public DrawingPen(String type, String name, int argb){
        this(type, name, argb, 100, 1F);
    }

    public DrawingPen(String type, String name, int argb, int distributionWeight, float strokeSize){
        update(type, name, argb, distributionWeight, strokeSize);
    }

    public void update(String type, String name, int argb, int distributionWeight, float strokeSize){
        this.type = type;
        this.name = name;
        this.argb = argb;
        this.distributionWeight = distributionWeight;
        this.strokeSize = strokeSize;
    }

    public void update(IDrawingPen pen){
        update(pen.getType(), pen.getName(), pen.getCustomARGB(), pen.getDistributionWeight(), pen.getStrokeSize());
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
    public int getCustomARGB() {
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