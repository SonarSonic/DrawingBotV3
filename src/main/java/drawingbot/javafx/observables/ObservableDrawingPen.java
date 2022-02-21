package drawingbot.javafx.observables;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.DrawingBotV3;
import drawingbot.api.ICustomPen;
import drawingbot.api.IDrawingPen;
import drawingbot.files.presets.JsonAdapterObservableDrawingPen;
import drawingbot.files.presets.JsonAdapterObservableDrawingSet;
import drawingbot.image.ImageTools;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

import java.awt.*;

@JsonAdapter(JsonAdapterObservableDrawingPen.class)
public class ObservableDrawingPen implements IDrawingPen, ICustomPen {

    public IDrawingPen source;
    public final SimpleIntegerProperty penNumber = new SimpleIntegerProperty(); //the pens index in the set
    public final SimpleBooleanProperty enable = new SimpleBooleanProperty(); //if the pen should be enabled in renders / exports
    public final SimpleStringProperty type = new SimpleStringProperty(); //pen's type
    public final SimpleStringProperty name = new SimpleStringProperty(); //colour/pen name
    public final SimpleObjectProperty<Color> javaFXColour = new SimpleObjectProperty<>(); //rgb pen colour
    public final SimpleIntegerProperty distributionWeight = new SimpleIntegerProperty(); //weight
    public final SimpleFloatProperty strokeSize = new SimpleFloatProperty(); //stroke size
    public final transient SimpleStringProperty currentPercentage = new SimpleStringProperty(); //percentage
    public final transient SimpleIntegerProperty currentGeometries = new SimpleIntegerProperty(); //geometries

    public ObservableDrawingPen(){}

    public ObservableDrawingPen(int penNumber, IDrawingPen source){
        this.source = source;
        this.penNumber.set(penNumber);
        this.enable.set(source.isEnabled());
        this.type.set(source.getType());
        this.name.set(source.getName());
        this.javaFXColour.set(ImageTools.getColorFromARGB(source.getARGB()));
        this.distributionWeight.set(source.getDistributionWeight());
        this.strokeSize.set(source.getStrokeSize());
        this.currentPercentage.set("0.0");
        this.currentGeometries.set(0);
        initListeners();
    }

    public void initListeners(){
        this.enable.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingPenChanged());
        this.name.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingPenChanged());
        this.type.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingPenChanged());
        this.javaFXColour.addListener((observable, oldValue, newValue) -> {DrawingBotV3.INSTANCE.onDrawingPenChanged(); awtColor = null;});
        this.distributionWeight.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingPenChanged());
        this.strokeSize.addListener((observable, oldValue, newValue) -> {DrawingBotV3.INSTANCE.onDrawingPenChanged(); awtStroke = null;});
    }

    @Override
    public boolean isEnabled() {
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
            awtColor = new java.awt.Color((float)javaFXColour.get().getRed(), (float)javaFXColour.get().getGreen(), (float)javaFXColour.get().getBlue(), (float)javaFXColour.get().getOpacity());
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
            awtStroke = new BasicStroke(strokeSize.get(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }
        return awtStroke;
    }

    public Color getFXColor(){
        return javaFXColour.get();
    }

    public Color getFXColor(Integer pfmARGB){
        if(pfmARGB != null && source instanceof ICustomPen){
            return ImageTools.getColorFromARGB(((ICustomPen) source).getCustomARGB(pfmARGB));
        }
        return javaFXColour.get();
    }
}
