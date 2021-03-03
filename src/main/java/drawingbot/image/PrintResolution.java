package drawingbot.image;

import drawingbot.DrawingBotV3;
import drawingbot.utils.EnumScalingMode;

import java.awt.image.BufferedImage;

public class PrintResolution {


    //// SOURCE RESOLUTION \\\\

    //the original images dimensions, in PX
    public final int sourceWidth;
    public final int sourceHeight;


    //// IMAGE RESOLUTION \\\\

    //the cropped area to be plotted
    public int imageCropX = 0;
    public int imageCropY = 0;

    //the output render offsets, in PX
    public int imageOffsetX = 0;
    public int imageOffsetY = 0;

    //the output render dimensions, includes any borders, in PX
    public int imageWidth = 0;
    public int imageHeight = 0;

    public double imageScale = 1;

    //the resolution the PFM is plotting the image at
    public float plottingResolution = 1;

    //// PRINT RESOLUTION \\\\

    //the output page dimensions, in MM
    public float printPageWidth;
    public float printPageHeight;

    //the outputs drawing dimensions, in MM
    public float printDrawingWidth;
    public float printDrawingHeight;

    //the outputs print offset in MM
    public float printOffsetX;
    public float printOffsetY;


    //// IMAGE to PRINT SCALE \\\\

    public double printScale;
    public double scaledOffsetX;
    public double scaledOffsetY;
    public double scaledHeight;
    public double scaledWidth;

    public PrintResolution(BufferedImage src){
        this.sourceWidth = src.getWidth();
        this.sourceHeight = src.getHeight();
    }

    public void updateAll(){
        updatePrintResolution();
        updateCropping();
        updatePrintScale();
    }

    public void updatePrintResolution(){

        boolean useOriginal = DrawingBotV3.INSTANCE.useOriginalSizing.get() || DrawingBotV3.INSTANCE.getDrawingAreaWidthMM() == 0 || DrawingBotV3.INSTANCE.getDrawingAreaHeightMM() == 0; //invalid

        this.printPageWidth = useOriginal ? sourceWidth : DrawingBotV3.INSTANCE.getDrawingAreaWidthMM();
        this.printPageHeight = useOriginal ? sourceHeight : DrawingBotV3.INSTANCE.getDrawingAreaHeightMM();

        this.printDrawingWidth = useOriginal ? sourceWidth : DrawingBotV3.INSTANCE.getDrawingWidthMM();
        this.printDrawingHeight = useOriginal ? sourceHeight : DrawingBotV3.INSTANCE.getDrawingHeightMM();

        this.printOffsetX = useOriginal ? 0 : DrawingBotV3.INSTANCE.getDrawingOffsetXMM();
        this.printOffsetY = useOriginal ? 0 : DrawingBotV3.INSTANCE.getDrawingOffsetYMM();
    }

    public void updateCropping(){
        float currentRatio = (float) sourceWidth / sourceHeight;
        float targetRatio = getPrintDrawingWidth() / getPrintDrawingHeight();

        this.imageOffsetX = 0;
        this.imageOffsetY = 0;
        this.imageScale = 1;

        this.imageWidth = sourceWidth;
        this.imageHeight = sourceHeight;

        this.imageCropX = 0;
        this.imageCropY = 0;

        if(targetRatio != currentRatio){
            int targetWidth = (int)(sourceHeight * targetRatio);
            int targetHeight = (int)(sourceWidth / targetRatio);

            if (currentRatio < targetRatio) {
                this.imageCropY = (sourceHeight - targetHeight) / 2;
                this.imageHeight = targetHeight;
            }else{
                this.imageCropX = (sourceWidth - targetWidth) / 2;
                this.imageWidth = targetWidth;
            }
            if(DrawingBotV3.INSTANCE.scalingMode.get() == EnumScalingMode.SCALE_TO_FIT){
                if(currentRatio < targetRatio){
                    this.imageScale = sourceHeight /(double)targetHeight;
                    this.imageOffsetX = (targetWidth - sourceWidth) / 2;
                }else{
                    this.imageScale = sourceWidth / (double)targetWidth;
                    this.imageOffsetY = (targetHeight - sourceHeight) / 2;
                }
            }
        }
    }

    public void updatePrintScale(){
        double print_scale_x, print_scale_y;
        print_scale_x = printDrawingWidth / (imageWidth * imageScale * plottingResolution);
        print_scale_y = printDrawingHeight / (imageHeight * imageScale * plottingResolution);
        printScale = Math.min(print_scale_x, print_scale_y);

        scaledOffsetX = (imageOffsetX * plottingResolution)  + (getPrintOffsetX() / getPrintScale());
        scaledOffsetY = (imageOffsetY * plottingResolution) + (getPrintOffsetY() / getPrintScale());
        scaledWidth = getPrintPageWidth() / getPrintScale();
        scaledHeight = getPrintPageHeight() / getPrintScale();
    }

    public boolean hasCropping(){
        return imageCropX != 0 || imageCropY != 0 || imageWidth != sourceWidth || imageHeight != sourceHeight;
    }

    public float getPrintPageWidth() {
        return printPageWidth;
    }

    public float getPrintPageHeight() {
        return printPageHeight;
    }

    public float getPrintDrawingWidth() {
        return printDrawingWidth;
    }

    public float getPrintDrawingHeight() {
        return printDrawingHeight;
    }

    public float getPrintOffsetX() {
        return printOffsetX;
    }

    public float getPrintOffsetY() {
        return printOffsetY;
    }

    public int getImageWidth(){
        return imageWidth;
    }

    public int getImageHeight(){
        return imageHeight;
    }

    public double getScaledWidth(){
        return scaledWidth;
    }

    public double getScaledHeight(){
        return scaledHeight;
    }

    public double getScaledOffsetX(){
        return scaledOffsetX;
    }

    public double getScaledOffsetY(){
        return scaledOffsetY;
    }

    public double getPrintScale() {
        return printScale;
    }
}
