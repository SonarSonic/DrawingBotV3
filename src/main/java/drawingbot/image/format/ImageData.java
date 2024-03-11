package drawingbot.image.format;

import drawingbot.api.ICanvas;
import drawingbot.api.IProgressCallback;
import drawingbot.api.IProperties;
import drawingbot.image.ImageFilterSettings;
import drawingbot.image.ImageTools;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.canvas.ImageCanvas;
import drawingbot.plotting.canvas.SimpleCanvas;
import drawingbot.utils.EnumRotation;
import drawingbot.utils.UnitsLength;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Represents an image data which can provide it's own "filteredImage" with cropping, rotation and image filters applied according to the given canvas and filter settings
 * <p>Order of Operations</p>
 * <ul>
 *     <li>Pre-Cropping: applies any cropping operations, typically defined by the user in the "Image Cropping" display mode</li>
 *     <li>Rotation / Flipping: applies any rotation or flipping to the pre-cropped image</li>
 *     <li>Filtering: applies any active image filters to the image data</li>
 * </ul>
 */
public class ImageData implements IProperties {

    public final File sourceFile;
    public final BufferedImage sourceImage;
    public final ICanvas sourceCanvas;
    public final ImageCropping imageCropping;

    public ImageData(File sourceFile, BufferedImage sourceImage){
        this(sourceFile, new SimpleCanvas(sourceImage.getWidth(), sourceImage.getHeight()), sourceImage);
    }

    public ImageData(File sourceFile, ICanvas sourceCanvas, BufferedImage sourceImage) {
        this.sourceFile = sourceFile;
        this.sourceCanvas = sourceCanvas;
        this.sourceImage = sourceImage;
        this.imageCropping = new ImageCropping(sourceCanvas);
    }

    /**
     * @return the file this {@link ImageData} was imported from, could be null if it was created within the software
     */
    @Nullable
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
     * @return The settings of the pre crop applied to the image before processing
     */
    public ImageCropping getImageCropping() {
        return imageCropping;
    }

    /**
     * @return the image which should be rendered in the viewport when in Image Mode.
     */
    public BufferedImage getSourceImage() {
        return sourceImage;
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

    public void resetCrop() {
        getImageCropping().reset(getSourceCanvas());
    }

    public Rectangle2D getCrop() {
        return getImageCropping().getCrop(getSourceCanvas());
    }

    ///////////////////////////////////////

    public ICanvas createPreCropCanvas(){
        Rectangle2D crop = getCrop();
        return new SimpleCanvas(crop.getWidth(), crop.getHeight());
    }

    public ICanvas createImageTargetCanvas(ICanvas targetCanvas){
        return new ImageCanvas(new SimpleCanvas(targetCanvas), createPreCropCanvas(), imageCropping.getImageRotation().flipAxis);
    }

    public BufferedImage createPreCroppedImage() {
        return applyPreCropping(sourceImage, getCrop());
    }

    public BufferedImage createCroppedImage(ICanvas targetCanvas){
        return applyCropping(createPreCroppedImage(), createImageTargetCanvas(targetCanvas), imageCropping);
    }

    ///////////////////////////////////////

    /**
     * Creates a transform for converting from original image space -> canvas space
     * The transform handles crops + rotations + scaling
     * @return the canvas transform as an AWT transform
     */
    public AffineTransform getCanvasTransform(ICanvas targetCanvas){
        AffineTransform transform = new AffineTransform();

        ICanvas canvas = getSourceCanvas();

        //cropping transform
        Rectangle2D bounds = getCrop();
        if(bounds != null){
            transform.preConcatenate(AffineTransform.getTranslateInstance(-bounds.getX(),-bounds.getY()));
            canvas = new SimpleCanvas((float)bounds.getWidth(), (float)bounds.getHeight(), canvas.getUnits());
        }

        //rotation transform
        AffineTransform rotationTransform = ImageTools.getCanvasRotationTransform(canvas, imageCropping);
        transform.preConcatenate(rotationTransform);

        //scale transform
        AffineTransform scaleTransform = ImageTools.getCanvasScaleTransform(canvas, targetCanvas);
        transform.preConcatenate(scaleTransform);
        return transform;
    }

    ///////////////////////////////////////

    /**
     * Crops the image to the pre-crop see {@link ImageTools#applyPreCrop(BufferedImage, Rectangle2D)}
     * @param src the image to crop
     * @param crop the crop rectangle
     * @return the cropped image
     */
    public static BufferedImage applyPreCropping(BufferedImage src, Rectangle2D crop){
        if(crop == null){
            return src;
        }
        return ImageTools.applyPreCrop(src, crop);
    }

    public static BufferedImage applyCropping(BufferedImage src, ICanvas canvas, ImageCropping cropping){
        src = ImageTools.rotateImage(src, cropping.getImageRotation(), cropping.shouldFlipHorizontal(), cropping.shouldFlipVertical());
        return ImageTools.cropToCanvas(src, canvas);
    }

    @Deprecated
    public static BufferedImage applyCropping(BufferedImage src, ICanvas canvas, EnumRotation imageRotation, boolean flipHorizontal, boolean flipVertical){
        src = ImageTools.rotateImage(src, imageRotation, flipHorizontal, flipVertical);
        return ImageTools.cropToCanvas(src, canvas);
    }

    public static BufferedImage applyFilters(BufferedImage src, boolean forceUpdate, ImageFilterSettings imgFilterSettings){
        return ImageTools.applyCurrentImageFilters(src, imgFilterSettings, forceUpdate, IProgressCallback.NULL);
    }

    ///////////////////////////////////////

    private ObservableList<Observable> propertyList = null;

    @Override
    public ObservableList<Observable> getPropertyList() {
        if(propertyList == null){
            propertyList = PropertyUtil.createPropertiesList(imageCropping);
        }
        return propertyList;
    }

    @Override
    public String toString() {
        return "Filtered Image Data: Source Image: %s x %s (%s)".formatted(sourceImage.getWidth(), sourceImage.getHeight(), sourceFile == null ? "Internal" : sourceFile);
    }

}