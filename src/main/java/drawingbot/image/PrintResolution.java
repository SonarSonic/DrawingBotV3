package drawingbot.image;

import drawingbot.DrawingBotV3;
import drawingbot.utils.EnumRotation;
import drawingbot.utils.EnumScalingMode;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class PrintResolution {

    //// SOURCE RESOLUTION \\\\

    //the original images dimensions, in PX
    public final EnumRotation imageRotation;
    public final boolean imageFlipHorizontal;
    public final boolean imageFlipVertical;

    public final int originalWidth;
    public final int originalHeight;

    public final int sourceWidth;
    public final int sourceHeight;


    //// PLOTTING RESOLUTION \\\\

    //the resolution the PFM is plotting the image at
    public float plottingResolution = 1;

    //for transforming from the plotting resolution to the image resolution
    public AffineTransform plottingTransform;


    //// IMAGE RESOLUTION \\\\

    //the cropped area to be plotted
    public int imageCropX = 0;
    public int imageCropY = 0;
    public int imageCropWidth = 0;
    public int imageCropHeight = 0;

    //the output render offsets, in PX
    public int imageOffsetX = 0;
    public int imageOffsetY = 0;

    //the output render dimensions, includes any borders, in PX
    public int imageWidth = 0; //TODO MAKE WARNING WHEN THIS EXCEEDS THE TEXTURE LIMIT.
    public int imageHeight = 0;

    public double imageRenderScale = 1;


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

    public double finalPrintScaleX = 1;
    public double finalPrintScaleY = 1;

    public PrintResolution(BufferedImage src){
        this(DrawingBotV3.INSTANCE.imageRotation.get(), DrawingBotV3.INSTANCE.imageFlipHorizontal.get(), DrawingBotV3.INSTANCE.imageFlipVertical.get(), src);
    }

    public PrintResolution(EnumRotation imageRotation, boolean flipHorizontal, boolean flipVertical, BufferedImage src){
        this(imageRotation, flipHorizontal, flipVertical, src.getWidth(), src.getHeight());
    }

    public PrintResolution(EnumRotation imageRotation, boolean flipHorizontal, boolean flipVertical, int originalWidth, int originalHeight){
        this.imageRotation = imageRotation;
        this.imageFlipHorizontal = flipHorizontal;
        this.imageFlipVertical = flipVertical;

        this.originalHeight = originalHeight;
        this.originalWidth = originalWidth;

        this.sourceWidth = imageRotation.flipAxis ? originalHeight : originalWidth;
        this.sourceHeight = imageRotation.flipAxis ? originalWidth : originalHeight;
    }

    public static PrintResolution copy(PrintResolution resolution){
        PrintResolution copy = new PrintResolution(resolution.imageRotation, resolution.imageFlipHorizontal, resolution.imageFlipVertical, resolution.originalWidth, resolution.originalHeight);
        copy.plottingResolution = resolution.plottingResolution;
        copy.plottingTransform = resolution.plottingTransform;

        //// IMAGE RESOLUTION \\\\

        copy.imageCropX = resolution.imageCropX;
        copy.imageCropY = resolution.imageCropY;
        copy.imageCropWidth = resolution.imageCropWidth;
        copy.imageCropHeight = resolution.imageCropHeight;
        copy.imageOffsetX = resolution.imageOffsetX;
        copy.imageOffsetY = resolution.imageOffsetY;
        copy.imageWidth = resolution.imageWidth;
        copy.imageHeight = resolution.imageHeight;

        copy.imageRenderScale = resolution.imageRenderScale;


        //// PRINT RESOLUTION \\\\

        copy.printPageWidth = resolution.printPageWidth;
        copy.printPageHeight = resolution.printPageHeight;
        copy.printDrawingWidth = resolution.printDrawingWidth;
        copy.printDrawingHeight = resolution.printDrawingHeight;
        copy.printOffsetX = resolution.printOffsetX;
        copy.printOffsetY = resolution.printOffsetY;


        //// IMAGE to PRINT SCALE \\\\

        copy.printScale = resolution.printScale;
        copy.scaledOffsetX = resolution.scaledOffsetX;
        copy.scaledOffsetY = resolution.scaledOffsetY;
        copy.scaledHeight = resolution.scaledHeight;
        copy.scaledWidth = resolution.scaledWidth;

        return copy;
    }

    public void changePrintResolution(int targetWidth, int targetHeight){
        double changeX = targetWidth / scaledWidth;
        double changeY = targetHeight / scaledHeight;

        scaledOffsetX *= changeX;
        scaledOffsetY *= changeY;
        scaledHeight = targetHeight;
        scaledWidth = targetWidth;
        finalPrintScaleX = changeX;
        finalPrintScaleY = changeY;
    }

    public void updateAll(){
        updatePlottingTransform();
        updatePrintResolution();
        updateCropping();
        updatePrintScale();
    }

    public void updatePlottingTransform(){
        plottingTransform = AffineTransform.getScaleInstance(1D / plottingResolution, 1D / plottingResolution);
    }

    public void updatePrintResolution(){
        boolean useOriginal = useOriginalSizing();

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
        this.imageRenderScale = 1;

        this.imageCropWidth = sourceWidth;
        this.imageCropHeight = sourceHeight;

        this.imageCropX = 0;
        this.imageCropY = 0;

        if(targetRatio != currentRatio){
            int targetWidth = (int)(sourceHeight * targetRatio);
            int targetHeight = (int)(sourceWidth / targetRatio);

            if (currentRatio < targetRatio) {
                this.imageCropY = (sourceHeight - targetHeight) / 2;
                this.imageCropHeight = targetHeight;
            }else{
                this.imageCropX = (sourceWidth - targetWidth) / 2;
                this.imageCropWidth = targetWidth;
            }
            if(DrawingBotV3.INSTANCE.scalingMode.get() == EnumScalingMode.SCALE_TO_FIT){
                if(currentRatio < targetRatio){
                    this.imageRenderScale = sourceHeight /(double)targetHeight;
                    this.imageOffsetX = (targetWidth - sourceWidth) / 2;
                }else{
                    this.imageRenderScale = sourceWidth / (double)targetWidth;
                    this.imageOffsetY = (targetHeight - sourceHeight) / 2;
                }
            }

        }

        if(!useOriginalSizing() && DrawingBotV3.INSTANCE.optimiseForPrint.get() && DrawingBotV3.INSTANCE.targetPenWidth.get() > 0){

            //divide the image so that 1 pixel = 1 pen mark
            imageWidth = (int)(getPrintDrawingWidth() / DrawingBotV3.INSTANCE.targetPenWidth.get());
            imageHeight = (int)(getPrintDrawingHeight() / DrawingBotV3.INSTANCE.targetPenWidth.get());

            if(DrawingBotV3.INSTANCE.scalingMode.get() == EnumScalingMode.SCALE_TO_FIT){
                //factor out the image's render scale & reset it
                if(currentRatio < targetRatio){
                    int renderWidth = (int)(imageWidth / imageRenderScale);
                    this.imageOffsetX = (imageWidth - renderWidth) / 2;
                    imageWidth = renderWidth;
                }else{
                    int renderHeight = (int)(imageHeight / imageRenderScale);
                    this.imageOffsetY = (imageHeight - renderHeight) / 2;
                    imageHeight = renderHeight;
                }
                this.imageRenderScale = 1;
            }

        }else{
            imageWidth = imageCropWidth;
            imageHeight = imageCropHeight;
        }


    }

    public void updatePrintScale(){
        double print_scale_x, print_scale_y;
        print_scale_x = printDrawingWidth / (imageWidth * imageRenderScale);
        print_scale_y = printDrawingHeight / (imageHeight * imageRenderScale);
        printScale = Math.min(print_scale_x, print_scale_y);

        //0.5 offset moves origin of geometry to the centre of pixel
        scaledOffsetX = (imageOffsetX + 0.5)  + (getPrintOffsetX() / getPrintScale());
        scaledOffsetY = (imageOffsetY + 0.5) + (getPrintOffsetY() / getPrintScale());

        scaledWidth = getPrintPageWidth() / getPrintScale();
        scaledHeight = getPrintPageHeight() / getPrintScale();

        finalPrintScaleX = 1;
        finalPrintScaleY = 1;
    }

    public boolean useOriginalSizing(){
        return DrawingBotV3.INSTANCE.useOriginalSizing.get() || DrawingBotV3.INSTANCE.getDrawingAreaWidthMM() == 0 || DrawingBotV3.INSTANCE.getDrawingAreaHeightMM() == 0;
    }

    //// IMAGE RESOLUTION \\\\

    public int getImageWidth(){
        return imageWidth;
    }

    public int getImageHeight(){
        return imageHeight;
    }

    public int getImageCropWidth(){
        return imageCropWidth;
    }

    public int getImageCropHeight(){
        return imageCropHeight;
    }


    //// PRINT RESOLUTION \\\\


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


    //// IMAGE to PRINT SCALE \\\\


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
