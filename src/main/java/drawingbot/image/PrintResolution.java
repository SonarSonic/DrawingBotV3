package drawingbot.image;

import drawingbot.DrawingBotV3;

import java.awt.image.BufferedImage;

public class PrintResolution {

    //the original images dimensions, in PX
    public final int sourceWidth;
    public final int sourceHeight;

    //the cropped area to be plotted
    public int cropX = 0;
    public int cropY = 0;
    public int cropWidth = 0;
    public int cropHeight = 0;

    //the output page dimensions, in MM
    public float printPageWidth;
    public float printPageHeight;

    //the outputs drawing dimensions, in MM
    public float printDrawingWidth;
    public float printDrawingHeight;

    //the outputs print offset in MM
    public float printOffsetX;
    public float printOffsetY;

    //the output render dimensions, includes any borders, in PX
    public int renderWidth = 0;
    public int renderHeight = 0;

    //the output render offsets, in PX
    public int renderOffsetX = 0;
    public int renderOffsetY = 0;

    public double printScale;
    public double scaledOffsetX;
    public double scaledOffsetY;
    public double scaledHeight;
    public double scaledWidth;

    //extras
    public float plottingResolution = 1;
    public int pixelPadding = 0;

    public PrintResolution(BufferedImage src){
        this.sourceWidth = src.getWidth();
        this.sourceHeight = src.getHeight();
        updateAll();
    }

    public void updateAll(){
        updatePrintResolution();
        updateCropping();
        updatePrintScale();
    }

    public void updatePrintResolution(){

        boolean useOriginal = DrawingBotV3.useOriginalSizing.get() || DrawingBotV3.getDrawingAreaWidthMM() == 0 || DrawingBotV3.getDrawingAreaHeightMM() == 0; //invalid

        this.printPageWidth = useOriginal ? sourceWidth : DrawingBotV3.getDrawingAreaWidthMM();
        this.printPageHeight = useOriginal ? sourceHeight : DrawingBotV3.getDrawingAreaHeightMM();

        this.printDrawingWidth = useOriginal ? sourceWidth : DrawingBotV3.getDrawingWidthMM();
        this.printDrawingHeight = useOriginal ? sourceHeight : DrawingBotV3.getDrawingHeightMM();

        this.printOffsetX = useOriginal ? 0 : DrawingBotV3.getDrawingOffsetXMM();
        this.printOffsetY = useOriginal ? 0 : DrawingBotV3.getDrawingOffsetYMM();
    }

    public void updateCropping(){
        float currentRatio = (float) sourceWidth / sourceHeight;
        float targetRatio = getPrintDrawingWidth() / getPrintDrawingHeight();

        int newWidth = -1, newHeight = -1;
        int taskOffsetX = 0, taskOffsetY = 0;

        this.cropX = 0;
        this.cropY = 0;
        this.cropWidth = sourceWidth;
        this.cropHeight = sourceHeight;

        if(targetRatio != currentRatio){
            int targetWidth = (int)(sourceHeight * targetRatio);
            int targetHeight = (int)(sourceWidth / targetRatio);

            if (currentRatio < targetRatio) {
                this.cropY = (sourceHeight - targetHeight) / 2;
                this.cropHeight = targetHeight;
            }else{
                this.cropX = (sourceWidth - targetWidth) / 2;
                this.cropWidth = targetWidth;
            }
            switch (DrawingBotV3.scaling_mode.get()){
                case CROP_TO_FIT:
                    //image = Scalr.crop(image, x, y, width, height);
                    break;
                case SCALE_TO_FIT:
                    int max = Math.max(sourceWidth, sourceHeight);
                    int maxWidth = (int)(max * targetRatio);
                    int maxHeight = (int)(max / targetRatio);

                    taskOffsetX = (maxWidth - sourceWidth) / 2;
                    taskOffsetY = (maxHeight - sourceHeight) / 2;
                    newWidth = maxWidth;
                    newHeight = maxHeight;
                    break;
                case STRETCH_TO_FIT:
                    //image = Scalr.resize(image, Scalr.Mode.FIT_EXACT, width, height);
                    break;
            }
        }

        renderWidth = (newWidth == -1 ? cropWidth : newWidth) + pixelPadding*2;
        renderHeight = (newHeight == -1 ? cropHeight : newHeight) + pixelPadding*2;

        renderOffsetX = taskOffsetX + pixelPadding;
        renderOffsetY = taskOffsetY + pixelPadding;
    }

    public void updatePrintScale(){
        double print_scale_x, print_scale_y;
        print_scale_x = printDrawingWidth / (renderWidth * plottingResolution);
        print_scale_y = printDrawingHeight / (renderHeight * plottingResolution);
        printScale = Math.min(print_scale_x, print_scale_y);


        scaledOffsetX = renderOffsetX  + (getPrintOffsetX() / getPrintScale());
        scaledOffsetY = renderOffsetY + (getPrintOffsetY() / getPrintScale());
        scaledWidth = getPrintPageWidth() / getPrintScale();
        scaledHeight = getPrintPageHeight() / getPrintScale();
    }

    public boolean hasCropping(){
        return cropX != 0 || cropY != 0 || cropWidth != sourceWidth || cropHeight != sourceHeight;
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

    public int getRenderWidth(){
        return renderWidth;
    }

    public int getRenderHeight(){
        return renderHeight;
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
