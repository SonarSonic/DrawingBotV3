package drawingbot.drawing;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICustomPen;
import drawingbot.api.IDrawingPen;
import drawingbot.image.ImageTools;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

import java.awt.*;

public class ObservableDrawingPen implements IDrawingPen, ICustomPen {

    public SimpleIntegerProperty penNumber; //the pens index in the set
    public SimpleBooleanProperty enable; //if the pen should be enabled in renders / exports
    public SimpleStringProperty type; //pen's type
    public SimpleStringProperty name; //colour/pen name
    public SimpleObjectProperty<Color> javaFXColour; //rgb pen colour
    public SimpleIntegerProperty distributionWeight; //weight
    public SimpleFloatProperty strokeSize; //stroke size
    public SimpleStringProperty currentPercentage; //percentage
    public SimpleIntegerProperty currentLines; //lines
    public IDrawingPen source;

    public ObservableDrawingPen(int penNumber, IDrawingPen source){
        this.source = source;
        this.penNumber = new SimpleIntegerProperty(penNumber);
        this.enable = new SimpleBooleanProperty(true);
        this.type = new SimpleStringProperty(source.getType());
        this.name = new SimpleStringProperty(source.getName());
        this.javaFXColour = new SimpleObjectProperty<>(ImageTools.getColorFromARGB(source.getARGB()));
        this.distributionWeight = new SimpleIntegerProperty(source.getDistributionWeight());
        this.strokeSize = new SimpleFloatProperty(source.getStrokeSize());
        this.currentPercentage = new SimpleStringProperty("0.0");
        this.currentLines = new SimpleIntegerProperty(0);

        this.enable.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingPenChanged());
        this.name.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingPenChanged());
        this.type.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingPenChanged());
        this.javaFXColour.addListener((observable, oldValue, newValue) -> {DrawingBotV3.INSTANCE.onDrawingPenChanged(); awtColor = null;});
        this.distributionWeight.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingPenChanged());
        this.strokeSize.addListener((observable, oldValue, newValue) -> {DrawingBotV3.INSTANCE.onDrawingPenChanged(); awtStroke = null;});
    }

    public boolean isEnabled(){
        return enable.get();
    }

    @Override
    public String getType() {
        return type.get();
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public float getStrokeSize() {
        return strokeSize.get();
    }

    @Override
    public int getDistributionWeight() {
        return distributionWeight.get();
    }

    @Override
    public int getARGB() {
        return ImageTools.getARGBFromColor(javaFXColour.get());
    }

    @Override
    public int getCustomARGB(int pfmARGB) {
        return source instanceof ICustomPen ? ((ICustomPen) source).getCustomARGB(pfmARGB) : getARGB();
    }

    @Override
    public String toString(){
        return getName();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    private java.awt.Color awtColor = null;
    private BasicStroke awtStroke = null;

    public java.awt.Color getAWTColor(){
        if(awtColor == null){
            awtColor = new java.awt.Color(getARGB(), true);
        }
        return awtColor;
    }

    public java.awt.Color getAWTColor(Integer pfmARGB){
        if(pfmARGB != null && source instanceof ICustomPen){
            return new java.awt.Color(((ICustomPen) source).getCustomARGB(pfmARGB), true);
        }
        return getAWTColor();
    }

    public BasicStroke getAWTStroke(){
        if(awtStroke == null){
            awtStroke = new BasicStroke(strokeSize.get());
        }
        return awtStroke;
    }

    public Color getFXColor(Integer pfmARGB){
        if(pfmARGB != null && source instanceof ICustomPen){
            return ImageTools.getColorFromARGB(((ICustomPen) source).getCustomARGB(pfmARGB));
        }
        return javaFXColour.get();
    }
}
