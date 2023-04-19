package drawingbot.plotting.canvas;

import drawingbot.api.ICanvas;
import drawingbot.files.json.JsonData;
import drawingbot.utils.EnumClippingMode;
import drawingbot.utils.EnumCroppingMode;
import drawingbot.utils.EnumRescaleMode;
import drawingbot.utils.UnitsLength;

@JsonData
public class SimpleCanvas implements ICanvas {

    public UnitsLength units = UnitsLength.MILLIMETRES;
    public EnumCroppingMode scalingMode = EnumCroppingMode.SCALE_TO_FIT;
    public EnumClippingMode clippingMode = EnumClippingMode.DRAWING;
    public EnumRescaleMode rescaleMode = EnumRescaleMode.HIGH_QUALITY;
    public boolean useOriginalSizing = false;
    public float penWidth = 1F;
    public float scale = 1F;
    public float pageWidth, pageHeight;
    public float drawingWidth, drawingHeight;
    public float drawingOffsetX, drawingOffsetY;
    public float canvasScale = 1F;

    public SimpleCanvas(){}

    public SimpleCanvas(ICanvas canvas){
        this(canvas.getUnits(), canvas.getCroppingMode(), canvas.getClippingMode(), canvas.getRescaleMode(), canvas.useOriginalSizing(), canvas.getTargetPenWidth(), canvas.getPlottingScale(), canvas.getWidth(), canvas.getHeight(), canvas.getDrawingWidth(), canvas.getDrawingHeight(), canvas.getDrawingOffsetX(), canvas.getDrawingOffsetY(), canvas.getCanvasScale());
    }

    public SimpleCanvas(float width, float height){
        this(width, height, UnitsLength.PIXELS);
    }

    public SimpleCanvas(float width, float height, UnitsLength units){
        this(units, EnumCroppingMode.CROP_TO_FIT, EnumClippingMode.DRAWING, EnumRescaleMode.HIGH_QUALITY, false, 1F, 1F, width, height, width, height, 0, 0, 1);
    }

    public SimpleCanvas(UnitsLength units, EnumCroppingMode scalingMode, EnumClippingMode clippingMode, EnumRescaleMode rescaleMode, boolean useOriginalSizing, float penWidth, float scale, float pageWidth, float pageHeight, float drawingWidth, float drawingHeight, float drawingOffsetX, float drawingOffsetY, float canvasScale){
        this.units = units;
        this.scalingMode = scalingMode;
        this.clippingMode = clippingMode;
        this.rescaleMode = rescaleMode;
        this.useOriginalSizing = useOriginalSizing;
        this.penWidth = penWidth;
        this.scale = scale;
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.drawingWidth = drawingWidth;
        this.drawingHeight = drawingHeight;
        this.drawingOffsetX = drawingOffsetX;
        this.drawingOffsetY = drawingOffsetY;
        this.canvasScale = canvasScale;
    }
    
    @Override
    public UnitsLength getUnits() {
        return units;
    }

    @Override
    public EnumCroppingMode getCroppingMode() {
        return scalingMode;
    }

    @Override
    public EnumClippingMode getClippingMode() {
        return clippingMode;
    }

    @Override
    public EnumRescaleMode getRescaleMode() {
        return rescaleMode;
    }

    @Override
    public boolean useOriginalSizing() {
        return useOriginalSizing;
    }

    @Override
    public float getTargetPenWidth() {
        return penWidth;
    }

    @Override
    public float getPlottingScale() {
        return scale;
    }

    @Override
    public float getWidth() {
        return pageWidth;
    }

    @Override
    public float getHeight() {
        return pageHeight;
    }

    @Override
    public float getDrawingWidth() {
        return drawingWidth;
    }

    @Override
    public float getDrawingHeight() {
        return drawingHeight;
    }

    @Override
    public float getDrawingOffsetX() {
        return drawingOffsetX;
    }

    @Override
    public float getDrawingOffsetY() {
        return drawingOffsetY;
    }

    @Override
    public float getCanvasScale() {
        return canvasScale;
    }
}
