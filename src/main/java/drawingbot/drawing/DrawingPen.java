package drawingbot.drawing;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.api.IDrawingPen;
import drawingbot.files.json.JsonData;
import drawingbot.files.json.adapters.JsonAdapterDrawingPen;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.preferences.DBPreferences;
import org.jetbrains.annotations.Nullable;

@JsonData
@JsonAdapter(JsonAdapterDrawingPen.class)
public class DrawingPen implements IDrawingPen {

    public String type; //the pen's type
    public String name; //the pen's name
    public int argb; //the pen's argb colour
    public int distributionWeight;
    public float strokeSize;
    public boolean isEnabled = true;

    //// COLOUR SPLITTER DATA \\\\
    public boolean hasColourSplitterData = false;
    public float colorSplitMultiplier = DBPreferences.INSTANCE.defaultColorSplitterPenMultiplier.get();
    public float colorSplitOpacity = DBPreferences.INSTANCE.defaultColorSplitterPenOpacity.get();
    public float colorSplitOffsetX = 0F;
    public float colorSplitOffsetY = 0F;

    //// PRESET DATA \\\\
    @Nullable
    public transient GenericPreset<DrawingPen> preset;

    public DrawingPen(){} //for GSON

    public DrawingPen(IDrawingPen source){
        update(source);
    }

    public DrawingPen(String type, String name, int argb){
        this(type, name, argb, 100, 1F);
    }

    public DrawingPen(String type, String name, int argb, int distributionWeight, float strokeSize){
        this(type, name, argb, distributionWeight, strokeSize, true);
    }

    public DrawingPen(String type, String name, int argb, int distributionWeight, float strokeSize, boolean active){
        update(type, name, argb, distributionWeight, strokeSize, active);
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
        if(pen.hasColorSplitterData()){
            this.hasColourSplitterData = true;
            this.colorSplitMultiplier = pen.getColorSplitMultiplier();
            this.colorSplitOpacity = pen.getColorSplitOpacity();
            this.colorSplitOffsetX = pen.getColorSplitOffsetX();
            this.colorSplitOffsetY = pen.getColorSplitOffsetY();
        }else{
            this.hasColourSplitterData = false;
            this.colorSplitMultiplier = DBPreferences.INSTANCE.defaultColorSplitterPenMultiplier.get();
            this.colorSplitOpacity = DBPreferences.INSTANCE.defaultColorSplitterPenOpacity.get();
            this.colorSplitOffsetX = 0F;
            this.colorSplitOffsetY = 0F;
        }
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
    public boolean isUserCreated() {
        return preset != null && preset.userCreated;
    }

    //// COLOUR SPLITTER DATA \\\\

    public DrawingPen setColorSplitData(float multiplier, float opacity, float offsetX, float offsetY){
        this.hasColourSplitterData = true;
        this.colorSplitMultiplier = multiplier;
        this.colorSplitOpacity = opacity;
        this.colorSplitOffsetX = offsetX;
        this.colorSplitOffsetY = offsetY;
        return this;
    }

    @Override
    public boolean hasColorSplitterData() {
        return hasColourSplitterData;
    }

    @Override
    public float getColorSplitMultiplier() {
        return colorSplitMultiplier;
    }

    @Override
    public float getColorSplitOpacity() {
        return colorSplitOpacity;
    }

    @Override
    public float getColorSplitOffsetX() {
        return colorSplitOffsetX;
    }

    @Override
    public float getColorSplitOffsetY() {
        return colorSplitOffsetY;
    }

    @Override
    public String toString(){
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof IDrawingPen){
            IDrawingPen otherPen = (IDrawingPen) obj;
            return otherPen.getType().equals(getType()) && otherPen.getName().equals(getName()) && otherPen.isEnabled() == isEnabled()  && otherPen.getARGB() == getARGB() && otherPen.getStrokeSize() == getStrokeSize() && otherPen.getDistributionWeight() == getDistributionWeight();
        }
        return super.equals(obj);
    }
}