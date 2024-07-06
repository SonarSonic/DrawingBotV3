package drawingbot.plotting.canvas;

import drawingbot.api.ICanvas;
import drawingbot.api.IProperties;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.utils.*;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.concurrent.atomic.AtomicBoolean;

public class ObservableCanvas extends SpecialListenable<ObservableCanvas.Listener> implements ICanvas, IProperties {

    public static final double defaultWidthMM = 210, defaultHeightMM = 297; //DEFAULT - A4 Paper

    public final SimpleBooleanProperty useOriginalSizing = new SimpleBooleanProperty(true);
    public final SimpleObjectProperty<EnumCroppingMode> croppingMode = new SimpleObjectProperty<>(EnumCroppingMode.CROP_TO_FIT);
    public final SimpleObjectProperty<EnumClippingMode> clippingMode = new SimpleObjectProperty<>(DBPreferences.INSTANCE.defaultClippingMode.get());
    public final SimpleObjectProperty<UnitsLength> inputUnits = new SimpleObjectProperty<>(UnitsLength.MILLIMETRES);

    public final DoubleProperty width = new SimpleDoubleProperty(0);
    public final DoubleProperty height = new SimpleDoubleProperty(0);
    public final DoubleProperty drawingAreaPaddingLeft = new SimpleDoubleProperty(0);
    public final DoubleProperty drawingAreaPaddingRight = new SimpleDoubleProperty(0);
    public final DoubleProperty drawingAreaPaddingTop = new SimpleDoubleProperty(0);
    public final DoubleProperty drawingAreaPaddingBottom = new SimpleDoubleProperty(0);
    public final DoubleProperty drawingAreaPaddingGangedValue = new SimpleDoubleProperty(0);
    public final SimpleBooleanProperty drawingAreaGangPadding = new SimpleBooleanProperty(true);
    public final SimpleObjectProperty<EnumOrientation> orientation = new SimpleObjectProperty<>(EnumOrientation.PORTRAIT);

    public final DoubleProperty targetPenWidth = new SimpleDoubleProperty(DBPreferences.INSTANCE.defaultPenWidth.get());

    public final SimpleObjectProperty<EnumRescaleMode> rescaleMode = new SimpleObjectProperty<>(DBPreferences.INSTANCE.defaultRescalingMode.get());

    //the default JFX viewport background colours
    public static final Color backgroundColourDefault = new Color(244 / 255F, 244 / 255F, 244 / 255F, 1F);
    public static final Color backgroundColourDark = new Color(65 / 255F, 65 / 255F, 65 / 255F, 1F);

    //not saved
    public final SimpleObjectProperty<Color> canvasColor = new SimpleObjectProperty<>(DBPreferences.INSTANCE.defaultCanvasColour.get());
    public final SimpleObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(DBPreferences.INSTANCE.defaultBackgroundColour.get());

    public final ObservableList<Observable> observables = PropertyUtil.createPropertiesList(useOriginalSizing, croppingMode, clippingMode, inputUnits, width, height, drawingAreaPaddingLeft, drawingAreaPaddingRight, drawingAreaPaddingTop, drawingAreaPaddingBottom, drawingAreaGangPadding, rescaleMode, targetPenWidth, canvasColor);

    public ObservableCanvas(){
        InvalidationListener genericListener = observable -> sendListenerEvent(listener -> listener.onCanvasPropertyChanged(this, observable));
        getPropertyList().forEach(prop -> prop.addListener(genericListener));

        AtomicBoolean internalChange = new AtomicBoolean(false);

        width.addListener((observable, oldValue, newValue) -> {
            if(internalChange.get()){
                return;
            }
            if(width.get() == height.get()){
                return;
            }
            internalChange.set(true);
            orientation.set(EnumOrientation.getType(width.get(), height.get()));
            internalChange.set(false);
        });

        height.addListener((observable, oldValue, newValue) -> {
            if(internalChange.get()){
                return;
            }
            if(width.get() == height.get()){
                return;
            }
            internalChange.set(true);
            orientation.set(EnumOrientation.getType(width.get(), height.get()));
            internalChange.set(false);
        });

        orientation.addListener(((observable, oldValue, newValue) -> {
            if(internalChange.get()){
                return;
            }
            if(newValue != null){
                internalChange.set(true);
                double newWidth = getHeight();
                double newHeight = getWidth();
                width.set(newWidth);
                height.set(newHeight);
                internalChange.set(false);
            }

        }));

        inputUnits.addListener((observable, oldValue, newValue) -> {
            internalChange.set(true);
            width.set(UnitsLength.convert(width.get(), oldValue, newValue));
            height.set(UnitsLength.convert(height.get(), oldValue, newValue));
            if(drawingAreaGangPadding.get()){
                drawingAreaPaddingGangedValue.set(UnitsLength.convert(drawingAreaPaddingGangedValue.get(), oldValue, newValue));
            }else{
                drawingAreaPaddingLeft.set(UnitsLength.convert(drawingAreaPaddingLeft.get(), oldValue, newValue));
                drawingAreaPaddingRight.set(UnitsLength.convert(drawingAreaPaddingRight.get(), oldValue, newValue));
                drawingAreaPaddingTop.set(UnitsLength.convert(drawingAreaPaddingTop.get(), oldValue, newValue));
                drawingAreaPaddingBottom.set(UnitsLength.convert(drawingAreaPaddingBottom.get(), oldValue, newValue));
            }
            internalChange.set(false);
        });

        updateGangedPadding();
        drawingAreaGangPadding.addListener((observable, oldValue, newValue) -> {
            updateGangedPadding();
        });

        //keep the ganged value updated so it always matches the last entered value
        drawingAreaPaddingLeft.addListener((observable, oldValue, newValue) -> {
            if(!internalChange.get() && !drawingAreaGangPadding.get()){
                drawingAreaPaddingGangedValue.set(newValue.doubleValue());
            }
        });
        drawingAreaPaddingRight.addListener((observable, oldValue, newValue) -> {
            if(!internalChange.get() && !drawingAreaGangPadding.get()){
                drawingAreaPaddingGangedValue.set(newValue.doubleValue());
            }
        });
        drawingAreaPaddingTop.addListener((observable, oldValue, newValue) -> {
            if(!internalChange.get() && !drawingAreaGangPadding.get()){
                drawingAreaPaddingGangedValue.set(newValue.doubleValue());
            }
        });
        drawingAreaPaddingBottom.addListener((observable, oldValue, newValue) -> {
            if(!internalChange.get() && !drawingAreaGangPadding.get()){
                drawingAreaPaddingGangedValue.set(newValue.doubleValue());
            }
        });
    }

    public void updateGangedPadding(){
        if(drawingAreaGangPadding.get()){
            drawingAreaPaddingLeft.bindBidirectional(drawingAreaPaddingGangedValue);
            drawingAreaPaddingRight.bindBidirectional(drawingAreaPaddingGangedValue);
            drawingAreaPaddingTop.bindBidirectional(drawingAreaPaddingGangedValue);
            drawingAreaPaddingBottom.bindBidirectional(drawingAreaPaddingGangedValue);
        }else{
            drawingAreaPaddingLeft.unbindBidirectional(drawingAreaPaddingGangedValue);
            drawingAreaPaddingRight.unbindBidirectional(drawingAreaPaddingGangedValue);
            drawingAreaPaddingTop.unbindBidirectional(drawingAreaPaddingGangedValue);
            drawingAreaPaddingBottom.unbindBidirectional(drawingAreaPaddingGangedValue);
        }
    }


    @Override
    public UnitsLength getUnits() {
        return inputUnits.get();
    }

    @Override
    public EnumCroppingMode getCroppingMode() {
        return croppingMode.get();
    }

    @Override
    public EnumClippingMode getClippingMode() {
        return clippingMode.get();
    }

    @Override
    public EnumRescaleMode getRescaleMode() {
        return rescaleMode.get();
    }

    @Override
    public double getPlottingScale(){
        return getRescaleMode().shouldRescale() ? 1F / getTargetPenWidth() : 1F;
    }

    @Override
    public double getTargetPenWidth() {
        return targetPenWidth.get() == 0 ? 1F : targetPenWidth.get();
    }

    @Override
    public float getRenderedPenWidth() {
        return 1F;
    }

    @Override
    public double getWidth(){
        if(width.getValue() > 0){
            return width.getValue();
        }
        return UnitsLength.convert(defaultWidthMM, UnitsLength.MILLIMETRES, getUnits());
    }

    @Override
    public double getHeight(){
        if(height.getValue() > 0){
            return height.getValue();
        }
        return UnitsLength.convert(defaultHeightMM, UnitsLength.MILLIMETRES, getUnits());
    }

    @Override
    public double getDrawingWidth(){
        return getWidth() - drawingAreaPaddingLeft.get() - drawingAreaPaddingRight.get();
    }

    @Override
    public double getDrawingHeight(){
        return getHeight() - drawingAreaPaddingTop.get() - drawingAreaPaddingBottom.get();
    }

    @Override
    public double getDrawingOffsetX(){
        return drawingAreaPaddingLeft.get();
    }

    @Override
    public double getDrawingOffsetY(){
        return drawingAreaPaddingTop.get();
    }

    @Override
    public boolean useOriginalSizing(){
        return useOriginalSizing.get();
    }

    public ObservableCanvas copy(){
        ObservableCanvas copy = new ObservableCanvas();
        copy.useOriginalSizing.set(useOriginalSizing.get());
        copy.croppingMode.set(croppingMode.get());
        copy.clippingMode.set(clippingMode.get());
        copy.inputUnits.set(inputUnits.get());

        copy.width.set(width.get());
        copy.height.set(height.get());

        copy.drawingAreaGangPadding.set(drawingAreaGangPadding.get());
        copy.drawingAreaPaddingLeft.set(drawingAreaPaddingLeft.get());
        copy.drawingAreaPaddingRight.set(drawingAreaPaddingRight.get());
        copy.drawingAreaPaddingTop.set(drawingAreaPaddingTop.get());
        copy.drawingAreaPaddingBottom.set(drawingAreaPaddingBottom.get());

        copy.rescaleMode.set(rescaleMode.get());
        copy.targetPenWidth.set(targetPenWidth.get());
        copy.canvasColor.set(canvasColor.get());
        return copy;
    }

    public void loadSimpleCanvas(ICanvas simpleCanvas) {
        useOriginalSizing.set(simpleCanvas.useOriginalSizing());
        croppingMode.set(simpleCanvas.getCroppingMode());
        clippingMode.set(simpleCanvas.getClippingMode());
        inputUnits.set(simpleCanvas.getUnits());

        width.set(simpleCanvas.getWidth());
        height.set(simpleCanvas.getHeight());
        orientation.set(EnumOrientation.getType(simpleCanvas.getWidth(), simpleCanvas.getHeight()));

        drawingAreaGangPadding.set(false);
        drawingAreaPaddingLeft.set(0);
        drawingAreaPaddingRight.set(0);
        drawingAreaPaddingTop.set(0);
        drawingAreaPaddingBottom.set(0);

        rescaleMode.set(simpleCanvas.getRescaleMode());
        targetPenWidth.set(simpleCanvas.getTargetPenWidth());
        canvasColor.set(DBPreferences.INSTANCE.defaultCanvasColour.get());
        backgroundColor.set(DBPreferences.INSTANCE.defaultBackgroundColour.get());
    }


    ///////////////////////////

    private ObservableList<Observable> propertyList = null;

    @Override
    public ObservableList<Observable> getPropertyList() {
        if(propertyList == null){
            propertyList = PropertyUtil.createPropertiesList(useOriginalSizing, croppingMode, clippingMode, inputUnits, width, height, drawingAreaPaddingLeft, drawingAreaPaddingRight, drawingAreaPaddingTop, drawingAreaPaddingBottom, drawingAreaGangPadding, orientation, rescaleMode, targetPenWidth, canvasColor, backgroundColor);
        }
        return propertyList;
    }
    ///////////////////////////

    public interface Listener {

        default void onCanvasPropertyChanged(ObservableCanvas canvas, Observable property) {}

    }


    @Override
    public String toString() {
        return asString();
    }

}