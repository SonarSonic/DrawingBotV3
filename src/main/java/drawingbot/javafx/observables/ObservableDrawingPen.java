package drawingbot.javafx.observables;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.api.ICustomPen;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IProperties;
import drawingbot.files.json.adapters.JsonAdapterObservableDrawingPen;
import drawingbot.image.ImageTools;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.utils.SpecialListenable;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.awt.*;
import java.text.NumberFormat;

@JsonAdapter(JsonAdapterObservableDrawingPen.class)
public class ObservableDrawingPen extends SpecialListenable<ObservableDrawingPen.Listener> implements IDrawingPen, ICustomPen, IProperties {

    public IDrawingPen source;
    public final SimpleIntegerProperty penNumber = new SimpleIntegerProperty(); //the pens index in the set
    public final SimpleBooleanProperty enable = new SimpleBooleanProperty(); //if the pen should be enabled in renders / exports
    public final SimpleStringProperty type = new SimpleStringProperty(); //pen's type
    public final SimpleStringProperty name = new SimpleStringProperty(); //colour/pen name
    public final SimpleObjectProperty<Color> javaFXColour = new SimpleObjectProperty<>(Color.BLACK); //rgb pen colour
    public final SimpleIntegerProperty distributionWeight = new SimpleIntegerProperty(); //weight
    public final SimpleFloatProperty strokeSize = new SimpleFloatProperty(); //stroke size
    public final transient SimpleStringProperty currentPercentage = new SimpleStringProperty("0%"); //percentage
    public final transient SimpleIntegerProperty currentGeometries = new SimpleIntegerProperty(0); //geometries
    public final SimpleBooleanProperty forceOverlap = new SimpleBooleanProperty(false);

    //// COLOUR SPLITTER DATA \\\\
    public final SimpleBooleanProperty useColorSplitOpacity = new SimpleBooleanProperty(false);
    public final SimpleFloatProperty colorSplitMultiplier = new SimpleFloatProperty(DBPreferences.INSTANCE.defaultColorSplitterPenMultiplier.get());
    public final SimpleFloatProperty colorSplitOpacity = new SimpleFloatProperty(DBPreferences.INSTANCE.defaultColorSplitterPenOpacity.get());
    public final SimpleFloatProperty colorSplitOffsetX = new SimpleFloatProperty(0F);
    public final SimpleFloatProperty colorSplitOffsetY = new SimpleFloatProperty(0F);

    //// INTERNAL \\\\
    private final ObjectProperty<Color> finalJFXColor = new SimpleObjectProperty<>();

    public ObservableDrawingPen(){
        init();
    }

    public ObservableDrawingPen(int penNumber, IDrawingPen source){
        this.source = source instanceof ObservableDrawingPen ? ((ObservableDrawingPen) source).source : source;
        this.penNumber.set(penNumber);
        this.enable.set(source.isEnabled());
        this.type.set(source.getType());
        this.name.set(source.getName());
        this.javaFXColour.set(ImageTools.getColorFromARGB(source.getARGB()));
        this.distributionWeight.set(source.getDistributionWeight());
        this.strokeSize.set(source.getStrokeSize());
        this.currentPercentage.set("0.0");
        this.currentGeometries.set(0);

        this.colorSplitMultiplier.set(source.getColorSplitMultiplier());
        this.colorSplitOpacity.set(source.getColorSplitOpacity());
        this.colorSplitOffsetX.set(source.getColorSplitOffsetX());
        this.colorSplitOffsetY.set(source.getColorSplitOffsetY());
        init();
    }

    public void init(){
        InvalidationListener genericListener = observable -> sendListenerEvent(listener -> listener.onDrawingPenPropertyChanged(this, observable));
        getPropertyList().forEach(prop -> prop.addListener(genericListener));

        this.strokeSize.addListener(observable -> refreshAWTStroke());
        this.javaFXColour.addListener(observable -> refreshAWTColor());

        this.colorSplitOpacity.addListener(observable -> refreshAWTColor());
        this.finalJFXColor.bind(Bindings.createObjectBinding(() -> new Color(javaFXColour.get().getRed(), javaFXColour.get().getGreen(), javaFXColour.get().getBlue(),  useColorSplitOpacity.get() ? javaFXColour.get().getOpacity()  * colorSplitOpacity.get() : javaFXColour.get().getOpacity()), javaFXColour, colorSplitOpacity, useColorSplitOpacity));

    }

    public void resetGeometryStats(){
        this.currentPercentage.set("0%");
        this.currentGeometries.set(0);
    }

    public void setGeometryStats(int count, int max){
        this.currentPercentage.set(NumberFormat.getPercentInstance().format((float)count/max));
        this.currentGeometries.set(count);
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
    public int getARGB() {
        return ImageTools.getARGBFromColor(javaFXColour.get());
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
    public boolean isUserCreated() {
        return false;
    }

    @Override
    public int getCustomARGB(int pfmARGB) {
        return source instanceof ICustomPen ? ((ICustomPen) source).getCustomARGB(pfmARGB) : getARGB();
    }

    @Override
    public String toString(){
        return getName();
    }

    /**
     * If when rendering in a GlobalRender order the pen should overlap all others, used for Export Drawing Pens.
     */
    public boolean shouldForceOverlap(){
        return forceOverlap.get();
    }

    //// COLOR SPLITTER \\\\

    public void resetColourSplitterData(){
        this.colorSplitMultiplier.set(source.getColorSplitMultiplier());
        this.colorSplitOpacity.set(source.getColorSplitOpacity());
        this.colorSplitOffsetX.set(source.getColorSplitOffsetX());
        this.colorSplitOffsetY.set(source.getColorSplitOffsetY());
    }

    @Override
    public boolean hasColorSplitterData() {
        return useColorSplitOpacity.get();
    }

    @Override
    public float getColorSplitMultiplier() {
        return colorSplitMultiplier.get();
    }

    @Override
    public float getColorSplitOpacity() {
        return colorSplitOpacity.get();
    }

    @Override
    public float getColorSplitOffsetX() {
        return colorSplitOffsetX.get();
    }

    @Override
    public float getColorSplitOffsetY() {
        return colorSplitOffsetY.get();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    private java.awt.Color awtColor = null;
    private BasicStroke awtStroke = null;

    public void refreshAWTStroke(){
        awtColor = null;
    }

    public void refreshAWTColor(){
        awtColor = null;
    }

    public java.awt.Color getAWTColor(){
        if(awtColor == null){
            awtColor = new java.awt.Color((int)(finalJFXColor.get().getRed()*255F), (int)(finalJFXColor.get().getGreen()*255F), (int)(finalJFXColor.get().getBlue()*255F), (int)(finalJFXColor.get().getOpacity()*255F));
        }
        return awtColor;
    }

    public java.awt.Color getAWTColor(Integer pfmARGB){
        if(pfmARGB != null && source instanceof ICustomPen){
            return new java.awt.Color(((ICustomPen) source).getCustomARGB(pfmARGB), true);
        }
        return getAWTColor();
    }

    public BasicStroke getAWTStroke(float lineWidth){
        if(awtStroke == null || awtStroke.getLineWidth() != lineWidth){
            awtStroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }
        return awtStroke;
    }

    public Color getFXColor(){
        return finalJFXColor.get();
    }

    public Color getFXColor(Integer pfmARGB){
        if(pfmARGB != null && source instanceof ICustomPen){
            return ImageTools.getColorFromARGB(((ICustomPen) source).getCustomARGB(pfmARGB));
        }
        return finalJFXColor.get();
    }

    ///////////////////////////

    public int getPenNumber(){
        return penNumber.get();
    }

    public ObservableDrawingPen setPenNumber(int penNumber){
        this.penNumber.set(penNumber);
        return this;
    }

    ///////////////////////////

    private ObservableList<Observable> propertyList = null;

    @Override
    public ObservableList<Observable> getPropertyList() {
        if(propertyList == null){
            propertyList = PropertyUtil.createPropertiesList(penNumber, enable, type, name, javaFXColour, distributionWeight, strokeSize, forceOverlap, useColorSplitOpacity, colorSplitMultiplier, colorSplitOffsetX, colorSplitOffsetY, colorSplitOpacity);
        }
        return propertyList;
    }

    public ObservableDrawingPen duplicate(){
        return new ObservableDrawingPen(getPenNumber(), this);
    }

    ///////////////////////////

    public interface Listener {

        default void onDrawingPenPropertyChanged(ObservableDrawingPen pen, Observable property) {}

    }
}
