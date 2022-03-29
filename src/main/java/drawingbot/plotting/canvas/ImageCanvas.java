package drawingbot.plotting.canvas;

import drawingbot.api.ICanvas;
import drawingbot.image.ImageTools;
import drawingbot.utils.EnumScalingMode;
import drawingbot.utils.UnitsLength;

import java.awt.image.BufferedImage;

/**
 * A canvas implementation which wraps another canvas and uses a reference image size to calculate the canvas' layout
 */
public class ImageCanvas implements ICanvas {

    public ICanvas refCanvas;
    public int imageWidth;
    public int imageHeight;

    public ImageCanvas(ICanvas refCanvas, BufferedImage refImage, boolean flipAxis){
        this(refCanvas, refImage.getWidth(), refImage.getHeight(), flipAxis);
    }

    public ImageCanvas(ICanvas refCanvas, int imageWidth, int imageHeight, boolean flipAxis){
        this.refCanvas = refCanvas;
        this.imageWidth = flipAxis ? imageHeight : imageWidth;
        this.imageHeight = flipAxis ? imageWidth : imageHeight;
    }

    @Override
    public UnitsLength getUnits() {
        if(useOriginalSizing()){
            return UnitsLength.PIXELS;
        }
        return refCanvas.getUnits();
    }

    @Override
    public EnumScalingMode getScalingMode() {
        return refCanvas.getScalingMode();
    }

    @Override
    public boolean optimiseForPrint() {
        return refCanvas.optimiseForPrint();
    }

    @Override
    public boolean useOriginalSizing(){
        return refCanvas.useOriginalSizing();
    }

    @Override
    public float getPlottingScale() {
        if(useOriginalSizing()){
            return 1;
        }
        if(!optimiseForPrint() && getUnits() != UnitsLength.PIXELS){
            int[] imageSize = ImageTools.getEffectiveImageSize(refCanvas, imageWidth, imageHeight);

            float currentRatio = (float) imageSize[0] / (float)imageSize[1];
            float targetRatio = getDrawingWidth() / getDrawingHeight();

            return currentRatio < targetRatio ? imageSize[1] / getDrawingHeight(UnitsLength.PIXELS) : imageSize[0] / getDrawingWidth(UnitsLength.PIXELS) ;
        }

        return refCanvas.getPlottingScale();
    }

    @Override
    public float getWidth() {
        if(useOriginalSizing()){
            return imageWidth;
        }
        return refCanvas.getWidth();
    }

    @Override
    public float getHeight() {
        if(useOriginalSizing()){
            return imageHeight;
        }
        return refCanvas.getHeight();
    }

    @Override
    public float getDrawingWidth() {
        if(useOriginalSizing()){
            return getWidth();
        }
        return refCanvas.getDrawingWidth();
    }

    @Override
    public float getDrawingHeight() {
        if(useOriginalSizing()){
            return getHeight();
        }
        return refCanvas.getDrawingHeight();
    }

    @Override
    public float getDrawingOffsetX() {
        if(useOriginalSizing()){
            return 0;
        }
        float imageOffsetX = 0;
        if(getScalingMode() == EnumScalingMode.SCALE_TO_FIT){
            float currentRatio = (float) imageWidth / (float)imageHeight;
            float targetRatio = getDrawingWidth() / getDrawingHeight();
            float targetWidth = Math.round(getDrawingHeight() * currentRatio);

            imageOffsetX = currentRatio < targetRatio ? getDrawingWidth()/2 - targetWidth/2F: 0;
        }
        return refCanvas.getDrawingOffsetX() + imageOffsetX;
    }

    @Override
    public float getDrawingOffsetY() {
        if(useOriginalSizing()){
            return 0;
        }
        float imageOffsetY = 0;
        if(getScalingMode() == EnumScalingMode.SCALE_TO_FIT){
            float currentRatio = (float) imageWidth / (float)imageHeight;
            float targetRatio = getDrawingWidth() / getDrawingHeight();
            float targetHeight = Math.round(getDrawingWidth() / currentRatio);

            imageOffsetY = currentRatio < targetRatio ? 0: getDrawingHeight()/2 - targetHeight/2F;
        }
        return refCanvas.getDrawingOffsetY() + imageOffsetY;
    }
}
