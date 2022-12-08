package drawingbot.plotting.canvas;

import drawingbot.api.ICanvas;
import drawingbot.image.ImageTools;
import drawingbot.utils.EnumClippingMode;
import drawingbot.utils.EnumCroppingMode;
import drawingbot.utils.EnumRescaleMode;
import drawingbot.utils.UnitsLength;

import java.awt.image.BufferedImage;

/**
 * A canvas implementation which wraps another canvas and uses a reference image size to calculate the canvas' layout
 */
public class ImageCanvas implements ICanvas {

    public ICanvas targetCanvas;
    public ICanvas imageCanvas;
    private final boolean flipAxis;

    public ImageCanvas(ICanvas targetCanvas, BufferedImage refImage, boolean flipAxis){
        this(targetCanvas, new SimpleCanvas(refImage.getWidth(), refImage.getHeight()), flipAxis);
    }

    public ImageCanvas(ICanvas targetCanvas, ICanvas imageCanvas, boolean flipAxis){
        this.targetCanvas = targetCanvas;
        this.imageCanvas = imageCanvas;
        this.flipAxis = flipAxis;
    }

    public float getImageWidth(){
        return flipAxis ? imageCanvas.getHeight() : imageCanvas.getWidth();
    }

    public float getImageHeight(){
        return flipAxis ? imageCanvas.getWidth() : imageCanvas.getHeight();
    }

    public boolean flipAxis(){
        return flipAxis;
    }

    public float getImageOffsetX(){
        float imageOffsetX = 0;
        if(getCroppingMode() == EnumCroppingMode.SCALE_TO_FIT){
            float currentRatio = getImageWidth() / getImageHeight();
            float targetRatio = targetCanvas.getDrawingWidth() / targetCanvas.getDrawingHeight();
            float targetWidth = Math.round(targetCanvas.getDrawingHeight() * currentRatio);

            imageOffsetX = currentRatio < targetRatio ? targetCanvas.getDrawingWidth()/2 - targetWidth/2F: 0;
        }
        return imageOffsetX;
    }

    public float getImageOffsetY(){
        float imageOffsetY = 0;
        if(getCroppingMode() == EnumCroppingMode.SCALE_TO_FIT){
            float currentRatio = getImageWidth() / getImageHeight();
            float targetRatio = targetCanvas.getDrawingWidth() / targetCanvas.getDrawingHeight();
            float targetHeight = Math.round(getDrawingWidth() / currentRatio);

            imageOffsetY = currentRatio < targetRatio ? 0: targetCanvas.getDrawingHeight()/2 - targetHeight/2F;
        }
        return imageOffsetY;
    }

    @Override
    public UnitsLength getUnits() {
        if(useOriginalSizing()){
            return imageCanvas.getUnits();
        }
        return targetCanvas.getUnits();
    }

    @Override
    public EnumCroppingMode getCroppingMode() {
        if(useOriginalSizing()){
            return imageCanvas.getCroppingMode();
        }
        return targetCanvas.getCroppingMode();
    }

    @Override
    public EnumClippingMode getClippingMode() {
        if(useOriginalSizing()){
            return imageCanvas.getClippingMode();
        }
        return targetCanvas.getClippingMode();
    }

    @Override
    public EnumRescaleMode getRescaleMode() {
        if(useOriginalSizing()){
            return EnumRescaleMode.OFF;
        }
        return targetCanvas.getRescaleMode();
    }

    @Override
    public boolean useOriginalSizing(){
        return targetCanvas.useOriginalSizing() || targetCanvas.getDrawingWidth() == 0 || targetCanvas.getDrawingHeight() == 0;
    }

    @Override
    public float getPlottingScale() {
        if(useOriginalSizing()){
            return imageCanvas.getPlottingScale();
        }
        if(getRescaleMode().isHighQuality() || !getRescaleMode().shouldRescale() && getUnits() != UnitsLength.PIXELS){
            int[] imageSize = ImageTools.getEffectiveImageSize(targetCanvas, (int)getImageWidth(), (int)getImageHeight());

            float currentRatio = (float) imageSize[0] / (float)imageSize[1];
            float targetRatio = getDrawingWidth() / getDrawingHeight();

            float scale = currentRatio < targetRatio ? imageSize[1] / getDrawingHeight(UnitsLength.PIXELS) : imageSize[0] / getDrawingWidth(UnitsLength.PIXELS);
            float target = targetCanvas.getPlottingScale();
            return Math.max(scale, target);
        }
        return targetCanvas.getPlottingScale();
    }

    @Override
    public float getTargetPenWidth() {
        if(!useOriginalSizing() && getRescaleMode().isHighQuality()){
            return getPlottingScale() / targetCanvas.getPlottingScale();
        }
        return 1F;
    }

    @Override
    public float getCanvasScale(){
        return 1F;
    }

    @Override
    public float getWidth() {
        if(useOriginalSizing()){
            return getImageWidth();
        }
        return targetCanvas.getWidth();
    }

    @Override
    public float getHeight() {
        if(useOriginalSizing()){
            return getImageHeight();
        }
        return targetCanvas.getHeight();
    }

    @Override
    public float getDrawingWidth() {
        if(useOriginalSizing()){
            return getImageWidth();
        }
        return targetCanvas.getDrawingWidth() - getImageOffsetX()*2;
    }

    @Override
    public float getDrawingHeight() {
        if(useOriginalSizing()){
            return getImageHeight();
        }
        return targetCanvas.getDrawingHeight() - getImageOffsetY()*2;
    }

    @Override
    public float getDrawingOffsetX() {
        if(useOriginalSizing()){
            return flipAxis() ? imageCanvas.getDrawingOffsetX() : imageCanvas.getDrawingOffsetY();
        }
        return targetCanvas.getDrawingOffsetX() + getImageOffsetX();
    }

    @Override
    public float getDrawingOffsetY() {
        if(useOriginalSizing()){
            return flipAxis() ? imageCanvas.getDrawingOffsetY() : imageCanvas.getDrawingOffsetX();
        }
        return targetCanvas.getDrawingOffsetY() + getImageOffsetY();
    }
}
