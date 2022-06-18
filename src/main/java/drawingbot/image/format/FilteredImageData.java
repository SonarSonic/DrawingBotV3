package drawingbot.image.format;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.image.ImageFilterSettings;
import drawingbot.image.ImageTools;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.canvas.ImageCanvas;
import drawingbot.plotting.canvas.SimpleCanvas;
import drawingbot.render.modes.ImageJFXDisplayMode;
import drawingbot.render.shapes.JFXShape;
import drawingbot.utils.EnumRotation;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;

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
        this.destCanvas = destCanvas;
        this.sourceCanvas = sourceCanvas;
        this.sourceImage = sourceImage;
        this.resetCrop();
        this.cropShape = new ImageJFXDisplayMode.Cropping.CropShape(this);
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

    public final SimpleObjectProperty<EnumRotation> imageRotation = new SimpleObjectProperty<>(EnumRotation.R0);
    public final SimpleBooleanProperty imageFlipHorizontal = new SimpleBooleanProperty(false);
    public final SimpleBooleanProperty imageFlipVertical = new SimpleBooleanProperty(false);

    {
        imageRotation.addListener((observable, oldValue, newValue) -> {
            if(DrawingBotV3.INSTANCE.openImage.get() == this){
                DrawingBotV3.INSTANCE.onCanvasChanged();
            }
        });
        imageFlipHorizontal.addListener((observable, oldValue, newValue) -> {
            if(DrawingBotV3.INSTANCE.openImage.get() == this){
                DrawingBotV3.INSTANCE.onCanvasChanged();
            }
        });
        imageFlipVertical.addListener((observable, oldValue, newValue) -> {
            if(DrawingBotV3.INSTANCE.openImage.get() == this){
                DrawingBotV3.INSTANCE.onCanvasChanged();
            }
        });
    }

    public final SimpleFloatProperty cropStartX = new SimpleFloatProperty(0);
    public final SimpleFloatProperty cropStartY = new SimpleFloatProperty(0);
    public final SimpleFloatProperty cropEndX = new SimpleFloatProperty(0);
    public final SimpleFloatProperty cropEndY = new SimpleFloatProperty(0);
    public JFXShape cropShape;

    public BufferedImage preCrop;
    public BufferedImage cropped;

    public boolean updateCropping = true;
    public boolean updateAllFilters = true;


    public void resetCrop() {
        this.cropStartX.set(0);
        this.cropStartY.set(0);
        this.cropEndX.set(sourceCanvas.getWidth());
        this.cropEndY.set(sourceCanvas.getHeight());
    }

    public Rectangle2D getCrop(){
        float scale = getSourceCanvas().getPlottingScale();
        float startX = Math.min(cropStartX.get(), cropEndX.get()) * scale;
        float startY = Math.min(cropStartY.get(), cropEndY.get()) * scale;
        float width = Math.abs(cropEndX.get() - cropStartX.get()) * scale;
        float height = Math.abs(cropEndY.get() - cropStartY.get()) * scale;
        if(width == 0 || height == 0){
            return new Rectangle2D.Double(0, 0, sourceCanvas.getWidth(), sourceCanvas.getHeight());
        }
        return new Rectangle2D.Double(startX, startY, width, height);
    }

    public BufferedImage createPreCroppedImage() {
        return applyPreCropping(sourceImage, getCrop());
    }

    public BufferedImage createCroppedImage(ICanvas canvas, ImageFilterSettings settings){
        BufferedImage image = createPreCroppedImage();
        return applyCropping(image, canvas, imageRotation.get(), imageFlipHorizontal.get(), imageFlipVertical.get());
    }

    public void updateAll(ImageFilterSettings settings){
        ImageCanvas newCanvas = new ImageCanvas(new SimpleCanvas(targetCanvas), sourceCanvas, false);

        if(cropped == null || updateCropping){
            preCrop = applyPreCropping(sourceImage, getCrop());
            newCanvas = new ImageCanvas(new SimpleCanvas(targetCanvas), preCrop, imageRotation.get().flipAxis);
            cropped = applyCropping(preCrop, newCanvas, imageRotation.get(), imageFlipHorizontal.get(), imageFlipVertical.get());
        }
        filteredImage = applyFilters(cropped, updateCropping || updateAllFilters, settings);
        destCanvas = newCanvas;
    }

    public AffineTransform getCanvasTransform(ImageFilterSettings settings){
        AffineTransform transform = new AffineTransform();

        ICanvas canvas = getSourceCanvas();

        //cropping transform
        if(getCrop() != null){
            Rectangle2D bounds = getCrop();
            transform.preConcatenate(AffineTransform.getTranslateInstance(-bounds.getX(),-bounds.getY()));
            canvas = new SimpleCanvas((float)bounds.getWidth(), (float)bounds.getHeight(), canvas.getUnits());
        }

        //rotation transform
        if(settings != null){
            AffineTransform rotationTransform = ImageTools.getCanvasRotationTransform(canvas, imageRotation.get(), imageFlipHorizontal.get(), imageFlipVertical.get());
            transform.preConcatenate(rotationTransform);
        }

        //scale transform
        AffineTransform scaleTransform = ImageTools.getCanvasScaleTransform(canvas, getTargetCanvas());
        transform.preConcatenate(scaleTransform);
        return transform;
    }

    ///////////////////////////////////////

    public static BufferedImage applyPreCropping(BufferedImage src, Rectangle2D crop){
        if(crop == null){
            return src;
        }
        return ImageTools.applyPreCrop(src, crop);
    }

    public static BufferedImage applyCropping(BufferedImage src, ICanvas canvas, EnumRotation imageRotation, boolean flipHorizontal, boolean flipVertical){
        src = ImageTools.rotateImage(src, imageRotation, flipHorizontal, flipVertical);
        return ImageTools.cropToCanvas(src, canvas);
    }

    public static BufferedImage applyFilters(BufferedImage src, boolean forceUpdate, ImageFilterSettings imgFilterSettings){
        return ImageTools.applyCurrentImageFilters(src, imgFilterSettings, forceUpdate, null);
    }


    ///////////////////////////////////////

}
