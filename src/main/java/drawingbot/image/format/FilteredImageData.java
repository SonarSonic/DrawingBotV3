package drawingbot.image.format;

import drawingbot.api.ICanvas;
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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

public class FilteredImageData implements IProperties {

    private final File sourceFile;
    private final ICanvas targetCanvas;

    public ICanvas sourceCanvas;
    public BufferedImage sourceImage;

    public ICanvas destCanvas;
    public BufferedImage filteredImage;

    public transient ImageFilterSettings lastFilterSettings;

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
    public final SimpleFloatProperty cropStartX = new SimpleFloatProperty(0);
    public final SimpleFloatProperty cropStartY = new SimpleFloatProperty(0);
    public final SimpleFloatProperty cropEndX = new SimpleFloatProperty(0);
    public final SimpleFloatProperty cropEndY = new SimpleFloatProperty(0);

    public BufferedImage preCrop;
    public BufferedImage cropped;

    public UpdateType nextUpdate = UpdateType.FULL_UPDATE;

    public enum UpdateType{
        FULL_UPDATE, // The cropping of the image has changed, update canvas size, cropping and all filters
        ALL_FILTERS, // All the filters should be updated
        PARTIAL_FILTERS, // Some of the filters may have changed
        NONE;

        public boolean updateCropping(){
            return this == FULL_UPDATE;
        }

        public boolean updateAllFilters(){
            return updateCropping() || this == ALL_FILTERS;
        }

        public boolean updatePartialFilters(){
            return updateAllFilters() || this == PARTIAL_FILTERS;
        }
    }

    public void markUpdate(UpdateType type){
        if(nextUpdate.ordinal() > type.ordinal()){
            nextUpdate = type;
        }
    }

    public void markCanvasForUpdate(){
        markUpdate(UpdateType.FULL_UPDATE);
    }

    public void markFiltersForUpdate(){
        markUpdate(UpdateType.ALL_FILTERS);
    }

    public void markPartialFiltersForUpdate(){
        markUpdate(UpdateType.PARTIAL_FILTERS);
    }

    public boolean isValidated(){
        return nextUpdate == UpdateType.NONE;
    }

    public void resetCrop() {
        this.cropStartX.set(0);
        this.cropStartY.set(0);
        this.cropEndX.set(sourceCanvas.getWidth());
        this.cropEndY.set(sourceCanvas.getHeight());
    }

    public Rectangle2D getCrop(){
        double scale = getSourceCanvas().getPlottingScale();
        double startX = Math.min(cropStartX.get(), cropEndX.get()) * scale;
        double startY = Math.min(cropStartY.get(), cropEndY.get()) * scale;
        double width = Math.abs(cropEndX.get() - cropStartX.get()) * scale;
        double height = Math.abs(cropEndY.get() - cropStartY.get()) * scale;
        if(width == 0 || height == 0){
            return new Rectangle2D.Double(0, 0, sourceCanvas.getWidth(), sourceCanvas.getHeight());
        }
        return new Rectangle2D.Double(startX, startY, width, height);
    }

    public BufferedImage createPreCroppedImage() {
        return applyPreCropping(sourceImage, getCrop());
    }

    public BufferedImage createCroppedImage(ICanvas canvas, ImageFilterSettings settings){
        lastFilterSettings = settings;
        BufferedImage image = createPreCroppedImage();
        return applyCropping(image, canvas, imageRotation.get(), imageFlipHorizontal.get(), imageFlipVertical.get());
    }

    public void updateAll(ImageFilterSettings settings){
        lastFilterSettings = settings;
        UpdateType updateType = nextUpdate;
        nextUpdate = UpdateType.NONE;

        ImageCanvas newCanvas = null;
        if(cropped == null || preCrop == null || updateType.updateCropping()){
            preCrop = applyPreCropping(sourceImage, getCrop());
            newCanvas = new ImageCanvas(new SimpleCanvas(targetCanvas), preCrop, imageRotation.get().flipAxis);
            cropped = applyCropping(preCrop, newCanvas, imageRotation.get(), imageFlipHorizontal.get(), imageFlipVertical.get());

            if(sourceCanvas.getUnits() != UnitsLength.PIXELS){
                Rectangle2D crop = getCrop();
                double width = crop.getWidth() / sourceCanvas.getPlottingScale();
                double height = crop.getHeight() / sourceCanvas.getPlottingScale();
                SimpleCanvas copy = new SimpleCanvas((float)width, (float)height, sourceCanvas.getUnits());
                copy.scale = sourceCanvas.getPlottingScale();
                newCanvas = new ImageCanvas(new SimpleCanvas(getTargetCanvas()), copy, imageRotation.get().flipAxis);
            }
        }else{
            newCanvas = new ImageCanvas(new SimpleCanvas(targetCanvas), preCrop, imageRotation.get().flipAxis);
        }

        filteredImage = applyFilters(cropped, updateType.updateAllFilters(), settings);
        destCanvas = newCanvas;
    }

    public AffineTransform getCanvasTransform(ImageFilterSettings settings){
        lastFilterSettings = settings;
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

    private ObservableList<Observable> propertyList = null;

    @Override
    public ObservableList<Observable> getPropertyList() {
        if(propertyList == null){
            propertyList = PropertyUtil.createPropertiesList(imageRotation, imageFlipHorizontal, imageFlipVertical, cropStartX, cropStartY, cropEndX, cropEndY);
        }
        return propertyList;
    }

}
