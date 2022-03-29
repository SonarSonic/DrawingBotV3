package drawingbot.plotting.canvas;

import drawingbot.api.ICanvas;
import drawingbot.utils.EnumScalingMode;
import drawingbot.utils.UnitsLength;

public class SimpleCanvas implements ICanvas {

    public UnitsLength units;
    public EnumScalingMode scalingMode;
    public boolean usePlottingScale;
    public boolean useOriginalSizing;
    public float scale;
    public float pageWidth, pageHeight;
    public float drawingWidth, drawingHeight;
    public float drawingOffsetX, drawingOffsetY;
    public float canvasScale;

    public SimpleCanvas(){}

    public SimpleCanvas(ICanvas canvas){
        this(canvas.getUnits(), canvas.getScalingMode(), canvas.optimiseForPrint(), canvas.useOriginalSizing(), canvas.getPlottingScale(), canvas.getWidth(), canvas.getHeight(), canvas.getDrawingWidth(), canvas.getDrawingHeight(), canvas.getDrawingOffsetX(), canvas.getDrawingOffsetY(), canvas.getCanvasScale());
    }

    public SimpleCanvas(int width, int height){
        this(UnitsLength.PIXELS, EnumScalingMode.CROP_TO_FIT, false, false, 1F, width, height, width, height, 0, 0, 1);
    }

    public SimpleCanvas(UnitsLength units, EnumScalingMode scalingMode, boolean usePlottingScale, boolean useOriginalSizing, float scale, float pageWidth, float pageHeight, float drawingWidth, float drawingHeight, float drawingOffsetX, float drawingOffsetY, float canvasScale){
        this.units = units;
        this.scalingMode = scalingMode;
        this.usePlottingScale = usePlottingScale;
        this.useOriginalSizing = useOriginalSizing;
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
    public EnumScalingMode getScalingMode() {
        return scalingMode;
    }

    @Override
    public boolean useOriginalSizing() {
        return useOriginalSizing;
    }

    @Override
    public boolean optimiseForPrint() {
        return usePlottingScale;
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
