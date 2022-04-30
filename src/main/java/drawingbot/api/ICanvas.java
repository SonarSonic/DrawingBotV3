package drawingbot.api;

import drawingbot.utils.EnumClippingMode;
import drawingbot.utils.EnumScalingMode;
import drawingbot.utils.UnitsLength;

import java.util.Objects;

public interface ICanvas {

    UnitsLength getUnits();

    EnumScalingMode getScalingMode();

    EnumClippingMode getClippingMode();

    boolean useOriginalSizing();

    boolean optimiseForPrint();

    float getPlottingScale();

    float getWidth();

    float getHeight();

    float getDrawingWidth();

    float getDrawingHeight();

    float getDrawingOffsetX();

    float getDrawingOffsetY();

    default float getCanvasScale(){
        return 1F;
    }

    default float getWidth(UnitsLength format){
        return UnitsLength.convert(getWidth(), getUnits(), format);
    }

    default float getHeight(UnitsLength format){
        return UnitsLength.convert(getHeight(), getUnits(), format);
    }

    default float getDrawingWidth(UnitsLength format){
        return UnitsLength.convert(getDrawingWidth(), getUnits(), format);
    }

    default float getDrawingHeight(UnitsLength format){
        return UnitsLength.convert(getDrawingHeight(), getUnits(), format);
    }

    default float getDrawingOffsetX(UnitsLength format){
        return UnitsLength.convert(getDrawingOffsetX(), getUnits(), format);
    }

    default float getDrawingOffsetY(UnitsLength format){
        return UnitsLength.convert(getDrawingOffsetY(), getUnits(), format);
    }

    default float getScaledWidth(){
        return getWidth(UnitsLength.PIXELS) * getPlottingScale();
    }

    default float getScaledHeight(){
        return getHeight(UnitsLength.PIXELS) * getPlottingScale();
    }

    default float getScaledDrawingWidth(){
        return getDrawingWidth(UnitsLength.PIXELS) * getPlottingScale();
    }

    default float getScaledDrawingHeight(){
        return getDrawingHeight(UnitsLength.PIXELS) * getPlottingScale();
    }

    default float getScaledDrawingOffsetX(){
        return getDrawingOffsetX(UnitsLength.PIXELS) * getPlottingScale();
    }

    default float getScaledDrawingOffsetY(){
        return getDrawingOffsetY(UnitsLength.PIXELS) * getPlottingScale();
    }

    static boolean matchingCanvas(ICanvas canvasA, ICanvas canvasB){
        return Objects.equals(canvasA.getUnits(), canvasB.getUnits())
                && Objects.equals(canvasA.getScalingMode(), canvasB.getScalingMode())
                && Objects.equals(canvasA.useOriginalSizing(), canvasB.useOriginalSizing())
                && Objects.equals(canvasA.optimiseForPrint(), canvasB.optimiseForPrint())
                && Objects.equals(canvasA.getPlottingScale(), canvasB.getPlottingScale())
                && Objects.equals(canvasA.getWidth(), canvasB.getWidth())
                && Objects.equals(canvasA.getHeight(), canvasB.getHeight())
                && Objects.equals(canvasA.getDrawingWidth(), canvasB.getDrawingWidth())
                && Objects.equals(canvasA.getDrawingHeight(), canvasB.getDrawingHeight())
                && Objects.equals(canvasA.getDrawingOffsetX(), canvasB.getDrawingOffsetX())
                && Objects.equals(canvasA.getDrawingOffsetY(), canvasB.getDrawingOffsetY())
                && Objects.equals(canvasA.getCanvasScale(), canvasB.getCanvasScale());
    }

}