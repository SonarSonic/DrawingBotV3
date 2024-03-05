package drawingbot.api;

import drawingbot.utils.EnumClippingMode;
import drawingbot.utils.EnumCroppingMode;
import drawingbot.utils.EnumRescaleMode;
import drawingbot.utils.UnitsLength;

import java.util.Objects;

public interface ICanvas {

    UnitsLength getUnits();

    EnumCroppingMode getCroppingMode();

    EnumClippingMode getClippingMode();

    EnumRescaleMode getRescaleMode();

    boolean useOriginalSizing();

    double getPlottingScale();

    double getWidth();

    double getHeight();

    double getDrawingWidth();

    double getDrawingHeight();

    double getDrawingOffsetX();

    double getDrawingOffsetY();

    /**
     * @return the user's desired pen width size in mm, (e.g. 0.3, 0.5, 0.7)
     * <br>
     * See: {@link #getRenderedPenWidth()}
     */
    default double getTargetPenWidth(){
        return 1D;
    }

    /**
     * Used internally by PFMs, when they should factor in the pen width into calculations
     * Typically the image is scaled so that 1 px = 1 pen width, so in most situations this value is 1.
     * However, when High Quality mode is enabled, the image size may be higher resolution
     * @return
     */
    default float getRenderedPenWidth(){
        if(!useOriginalSizing() && getRescaleMode().isHighQuality()){
            return (float) (getTargetPenWidth() * getPlottingScale());
        }
        return 1F;
    }

    default float getRenderedPenWidth(float strokeSize){
        if(!useOriginalSizing() && getRescaleMode().isHighQuality()){
            return strokeSize * getRenderedPenWidth();
        }
        return strokeSize;
    }

    default double getCanvasScale(){
        return 1D;
    }

    default double getWidth(UnitsLength format){
        return UnitsLength.convert(getWidth(), getUnits(), format);
    }

    default double getHeight(UnitsLength format){
        return UnitsLength.convert(getHeight(), getUnits(), format);
    }

    default double getDrawingWidth(UnitsLength format){
        return UnitsLength.convert(getDrawingWidth(), getUnits(), format);
    }

    default double getDrawingHeight(UnitsLength format){
        return UnitsLength.convert(getDrawingHeight(), getUnits(), format);
    }

    default double getDrawingOffsetX(UnitsLength format){
        return UnitsLength.convert(getDrawingOffsetX(), getUnits(), format);
    }

    default double getDrawingOffsetY(UnitsLength format){
        return UnitsLength.convert(getDrawingOffsetY(), getUnits(), format);
    }

    default double getScaledWidth(){
        return getWidth(UnitsLength.PIXELS) * getPlottingScale();
    }

    default double getScaledHeight(){
        return getHeight(UnitsLength.PIXELS) * getPlottingScale();
    }

    default double getScaledDrawingWidth(){
        return getDrawingWidth(UnitsLength.PIXELS) * getPlottingScale();
    }

    default double getScaledDrawingHeight(){
        return getDrawingHeight(UnitsLength.PIXELS) * getPlottingScale();
    }

    default double getScaledDrawingOffsetX(){
        return getDrawingOffsetX(UnitsLength.PIXELS) * getPlottingScale();
    }

    default double getScaledDrawingOffsetY(){
        return getDrawingOffsetY(UnitsLength.PIXELS) * getPlottingScale();
    }

    static boolean matchingCanvas(ICanvas canvasA, ICanvas canvasB){
        return Objects.equals(canvasA.getUnits(), canvasB.getUnits())
                && Objects.equals(canvasA.getCroppingMode(), canvasB.getCroppingMode())
                && Objects.equals(canvasA.getClippingMode(), canvasB.getClippingMode())
                && Objects.equals(canvasA.getRescaleMode(), canvasB.getRescaleMode())
                && Objects.equals(canvasA.useOriginalSizing(), canvasB.useOriginalSizing())
                && Objects.equals(canvasA.getPlottingScale(), canvasB.getPlottingScale())
                && Objects.equals(canvasA.getWidth(), canvasB.getWidth())
                && Objects.equals(canvasA.getHeight(), canvasB.getHeight())
                && Objects.equals(canvasA.getDrawingWidth(), canvasB.getDrawingWidth())
                && Objects.equals(canvasA.getDrawingHeight(), canvasB.getDrawingHeight())
                && Objects.equals(canvasA.getDrawingOffsetX(), canvasB.getDrawingOffsetX())
                && Objects.equals(canvasA.getDrawingOffsetY(), canvasB.getDrawingOffsetY())
                && Objects.equals(canvasA.getTargetPenWidth(), canvasB.getTargetPenWidth())
                && Objects.equals(canvasA.getCanvasScale(), canvasB.getCanvasScale());
    }

    default String asString(){
        return "Units: %s, Original Sizing: %s, Width: %s, Height: %s, Drawing Width: %s, Drawing Height: %s, Drawing Offset X: %s, Drawing Offset Y: %s, Plotting Scale: %s, Canvas Scale: %s, Pen Target Width: %s, Cropping Mode: %s, Clipping Mode: %s, Rescale Mode: %s".formatted(getUnits(), useOriginalSizing(), getWidth(), getHeight(), getDrawingWidth(), getDrawingHeight(), getDrawingOffsetX(), getDrawingOffsetY(), getPlottingScale(), getCanvasScale(), getTargetPenWidth(), getCroppingMode(), getClippingMode(), getRescaleMode());
    }

}