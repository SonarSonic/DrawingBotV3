package drawingbot.image.format;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.image.ImageFilterSettings;
import drawingbot.image.ImageTools;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.canvas.ImageCanvas;
import drawingbot.plotting.canvas.SimpleCanvas;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

public class FilteredImageData {

    private final File sourceFile;
    private final ICanvas targetCanvas;

    public ICanvas sourceCanvas;
    public BufferedImage sourceImage;

    public ICanvas destCanvas;
    public BufferedImage filteredImage;

    public FilteredImageData(File sourceFile, BufferedImage sourceImage){
        this(sourceFile, DrawingBotV3.INSTANCE.drawingArea, sourceImage);
    }

    public FilteredImageData(File sourceFile, ICanvas destCanvas, BufferedImage sourceImage){
        this(sourceFile, destCanvas, new SimpleCanvas(sourceImage.getWidth(), sourceImage.getHeight()), sourceImage);
    }

    public FilteredImageData(File sourceFile, ICanvas destCanvas, ICanvas sourceCanvas, BufferedImage sourceImage){
        this.sourceFile = sourceFile;
        this.targetCanvas = destCanvas;
        this.sourceCanvas = sourceCanvas;
        this.sourceImage = sourceImage;
    }

    public File getSourceFile(){
        return sourceFile;
    }

    /**
     * @return The canvas representing the sizing of the image, before any scaling or cropping has been applied
     */
    public ICanvas getSourceCanvas() {
        return sourceCanvas;
    }

    /**
     * @return The canvas which the filtered image should be sized to fit within
     */
    public ICanvas getTargetCanvas() {
        return targetCanvas;
    }

    /**
     * @return The canvas representing the created canvas.
     */
    public ICanvas getDestCanvas() {
        return destCanvas;
    }

    /**
     * @return the image which should be rendered in the viewport when in Image Mode.
     */
    public BufferedImage getSourceImage() {
        return sourceImage;
    }

    /**
     * @return creates a new filtered version of the image, this ensures the latest settings are used, even if the displayed image hasn't been updated.
     */
    public BufferedImage getFilteredImage() {
        return filteredImage == null ? sourceImage : filteredImage;
    }

    /**
     * @return used by imported SVG files
     */
    public PlottedDrawing getVectorFormatDrawing(){
        return null;
    }

    /**
     * @return true when the image data originates from an SVG / Vector, false otherwise, typically this also means that any image processing steps e.g. cropping use rasterise from the original vector rather than cropping a bitmap image
     */
    public boolean isVectorImage(){
        return false;
    }

    ///////////////////////////////////////

    public AffineTransform cropTransform;
    public Shape cropShape;
    public BufferedImage preCrop;
    public BufferedImage cropped;

    public boolean updateCropping = true;
    public boolean updateAllFilters = true;

    public BufferedImage createPreCroppedImage() {
        return applyPreCropping(sourceImage, cropTransform, cropShape);
    }

    public BufferedImage createCroppedImage(ICanvas canvas, ImageFilterSettings settings){
        BufferedImage image = createPreCroppedImage();
        return applyCropping(image, canvas, settings);
    }

    public void updateAll(ImageFilterSettings settings){
        ImageCanvas newCanvas = new ImageCanvas(new SimpleCanvas(targetCanvas), sourceCanvas, false);

        if(cropped == null || updateCropping){
            preCrop = applyPreCropping(sourceImage, cropTransform, cropShape);
            newCanvas = new ImageCanvas(new SimpleCanvas(targetCanvas), preCrop, settings.imageRotation.get().flipAxis);
            cropped = applyCropping(preCrop, newCanvas, settings);
        }
        filteredImage = applyFilters(cropped, updateCropping || updateAllFilters, settings);
        destCanvas = newCanvas;
    }

    public AffineTransform getCanvasTransform(ImageFilterSettings settings){
        AffineTransform transform = new AffineTransform();

        ICanvas canvas = getSourceCanvas();

        //cropping transform
        if(cropShape != null){
            Rectangle2D bounds = cropShape.getBounds2D();
            transform.preConcatenate(AffineTransform.getTranslateInstance(-bounds.getX(),-bounds.getY()));
            canvas = new SimpleCanvas((float)bounds.getWidth(), (float)bounds.getHeight(), canvas.getUnits());
        }

        //rotation transform
        if(settings != null){
            AffineTransform rotationTransform = ImageTools.getCanvasRotationTransform(canvas, settings.imageRotation.get(), settings.imageFlipHorizontal.get(), settings.imageFlipVertical.get());
            transform.preConcatenate(rotationTransform);
        }

        //scale transform
        AffineTransform scaleTransform = ImageTools.getCanvasScaleTransform(canvas, getTargetCanvas());
        transform.preConcatenate(scaleTransform);
        return transform;
    }

    ///////////////////////////////////////

    public static BufferedImage applyPreCropping(BufferedImage src, AffineTransform transform, Shape shape){
        if(transform == null || shape == null){
            return src;
        }
        return ImageTools.applyPreCrop(src, transform, shape);
    }

    public static BufferedImage applyCropping(BufferedImage src, ICanvas canvas, ImageFilterSettings imgFilterSettings){
        src = ImageTools.rotateImage(src, imgFilterSettings.imageRotation.get(), imgFilterSettings.imageFlipHorizontal.get(), imgFilterSettings.imageFlipVertical.get());
        return ImageTools.cropToCanvas(src, canvas);
    }

    public static BufferedImage applyFilters(BufferedImage src, boolean forceUpdate, ImageFilterSettings imgFilterSettings){
        return ImageTools.applyCurrentImageFilters(src, imgFilterSettings, forceUpdate, null);
    }

    ///////////////////////////////////////

}
