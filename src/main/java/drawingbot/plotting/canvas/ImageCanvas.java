package drawingbot.plotting.canvas;

import drawingbot.api.ICanvas;
import drawingbot.image.ImageTools;
import drawingbot.javafx.preferences.DBPreferences;
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

    public double getImageWidth(){
        return flipAxis ? imageCanvas.getHeight() : imageCanvas.getWidth();
    }

    public double getImageHeight(){
        return flipAxis ? imageCanvas.getWidth() : imageCanvas.getHeight();
    }

    public boolean flipAxis(){
        return flipAxis;
    }

    public double getImageOffsetX(){
        double imageOffsetX = 0;
        if(getCroppingMode() == EnumCroppingMode.SCALE_TO_FIT){
            double currentRatio = getImageWidth() / getImageHeight();
            double targetRatio = targetCanvas.getDrawingWidth() / targetCanvas.getDrawingHeight();
            double targetWidth = targetCanvas.getDrawingHeight() * currentRatio;

            imageOffsetX = currentRatio < targetRatio ? targetCanvas.getDrawingWidth()/2 - targetWidth/2F: 0;
        }
        return imageOffsetX;
    }

    public double getImageOffsetY(){
        double imageOffsetY = 0;
        if(getCroppingMode() == EnumCroppingMode.SCALE_TO_FIT){
            double currentRatio = getImageWidth() / getImageHeight();
            double targetRatio = targetCanvas.getDrawingWidth() / targetCanvas.getDrawingHeight();
            double targetHeight = targetCanvas.getDrawingWidth() / currentRatio;

            imageOffsetY = currentRatio < targetRatio ? 0: targetCanvas.getDrawingHeight()/2D - targetHeight/2D;
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
    public double getPlottingScale() {
        if(useOriginalSizing()){
            return imageCanvas.getPlottingScale();
        }
        if (getRescaleMode().isHighQuality()) {
            int[] imageSize = ImageTools.getEffectiveImageSize(targetCanvas, (int) (getDrawingWidth(UnitsLength.INCHES) * DBPreferences.INSTANCE.importDPI.get()), (int) (getDrawingHeight(UnitsLength.INCHES) * DBPreferences.INSTANCE.importDPI.get()));
            double currentRatio = (double) imageSize[0] / (double) imageSize[1];
            double targetRatio = getDrawingWidth() / getDrawingHeight();

            double scale = currentRatio < targetRatio ? imageSize[1] / getDrawingHeight(UnitsLength.PIXELS) : imageSize[0] / getDrawingWidth(UnitsLength.PIXELS);
            double target = targetCanvas.getPlottingScale();
            return Math.max(scale, target);
        }
        return targetCanvas.getPlottingScale();
    }

    @Override
    public double getTargetPenWidth() {
        if(useOriginalSizing()){
            return 1F;
        }
        return targetCanvas.getTargetPenWidth();
    }

    @Override
    public double getCanvasScale(){
        return 1F;
    }

    @Override
    public double getWidth() {
        if(useOriginalSizing()){
            return getImageWidth();
        }
        return targetCanvas.getWidth();
    }

    @Override
    public double getHeight() {
        if(useOriginalSizing()){
            return getImageHeight();
        }
        return targetCanvas.getHeight();
    }

    @Override
    public double getDrawingWidth() {
        if(useOriginalSizing()){
            return getImageWidth();
        }
        return targetCanvas.getDrawingWidth() - getImageOffsetX()*2;
    }

    @Override
    public double getDrawingHeight() {
        if(useOriginalSizing()){
            return getImageHeight();
        }
        return targetCanvas.getDrawingHeight() - getImageOffsetY()*2;
    }

    @Override
    public double getDrawingOffsetX() {
        if(useOriginalSizing()){
            return flipAxis() ? imageCanvas.getDrawingOffsetX() : imageCanvas.getDrawingOffsetY();
        }
        return targetCanvas.getDrawingOffsetX() + getImageOffsetX();
    }

    @Override
    public double getDrawingOffsetY() {
        if(useOriginalSizing()){
            return flipAxis() ? imageCanvas.getDrawingOffsetY() : imageCanvas.getDrawingOffsetX();
        }
        return targetCanvas.getDrawingOffsetY() + getImageOffsetY();
    }

    @Override
    public String toString() {
        return asString();
    }

}
