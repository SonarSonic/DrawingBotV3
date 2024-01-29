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
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
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
public class FilteredImageData implements IProperties {

    public final File sourceFile;
    public final ICanvas targetCanvas;

    public ICanvas sourceCanvas;
    public BufferedImage sourceImage;

    public ICanvas destCanvas;
    public BufferedImage filteredImage;

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

    /**
     * @return the file this {@link FilteredImageData} was imported from, could be null if it was created within the software
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

    /**
     * The rotation to be applied to the Rotation/Flipping stage
     */
    public final SimpleObjectProperty<EnumRotation> imageRotation = new SimpleObjectProperty<>(EnumRotation.R0);

    public EnumRotation getImageRotation() {
        return imageRotation.get();
    }

    public SimpleObjectProperty<EnumRotation> imageRotationProperty() {
        return imageRotation;
    }

    public void setImageRotation(EnumRotation imageRotation) {
        this.imageRotation.set(imageRotation);
    }

    ///////////////////////////////////////

    public final SimpleBooleanProperty imageFlipHorizontal = new SimpleBooleanProperty(false);

    public boolean shouldFlipHorizontal() {
        return imageFlipHorizontal.get();
    }

    public SimpleBooleanProperty flipHorizontalProperty() {
        return imageFlipHorizontal;
    }

    public void setFlipHorizontal(boolean flipHorizontal) {
        this.imageFlipHorizontal.set(flipHorizontal);
    }

    ///////////////////////////////////////

    public final SimpleBooleanProperty imageFlipVertical = new SimpleBooleanProperty(false);

    public boolean shouldFlipVertical() {
        return imageFlipVertical.get();
    }

    public SimpleBooleanProperty flipVerticalProperty() {
        return imageFlipVertical;
    }

    public void setFlipVertical(boolean imageFlipVertical) {
        this.imageFlipVertical.set(imageFlipVertical);
    }

    ///////////////////////////////////////

    public final SimpleFloatProperty cropStartX = new SimpleFloatProperty(0);
    public final SimpleFloatProperty cropStartY = new SimpleFloatProperty(0);
    public final SimpleFloatProperty cropWidth = new SimpleFloatProperty(0);
    public final SimpleFloatProperty cropHeight = new SimpleFloatProperty(0);

    public Rectangle2D getCrop(){
        double scale = getSourceCanvas().getPlottingScale();
        double startX = cropStartX.get() * scale;
        double startY = cropStartY.get() * scale;
        double width = cropWidth.get() * scale;
        double height = cropHeight.get() * scale;
        if(width == 0 || height == 0){
            return new Rectangle2D.Double(0, 0, sourceCanvas.getWidth(), sourceCanvas.getHeight());
        }
        return new Rectangle2D.Double(startX, startY, width, height);
    }

    public void resetCrop() {
        this.cropStartX.set(0);
        this.cropStartY.set(0);
        this.cropWidth.set(sourceCanvas.getWidth());
        this.cropHeight.set(sourceCanvas.getHeight());
    }

    ///////////////////////////////////////

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

    ///////////////////////////////////////


    public ICanvas createPreCropCanvas(){
        Rectangle2D crop = getCrop();
        return new SimpleCanvas((int)crop.getWidth(), (int)crop.getHeight());
    }

    public BufferedImage createPreCroppedImage() {
        return applyPreCropping(sourceImage, getCrop());
    }

    public BufferedImage createCroppedImage(ICanvas canvas, ImageFilterSettings settings){
        BufferedImage image = createPreCroppedImage();
        return applyCropping(image, canvas, imageRotation.get(), imageFlipHorizontal.get(), imageFlipVertical.get());
    }

    ///////////////////////////////////////

    private transient BufferedImage preCrop;
    private transient BufferedImage cropped;

    public void updateAll(ImageFilterSettings settings){
        UpdateType updateType = nextUpdate;
        nextUpdate = UpdateType.NONE;

        ImageCanvas preCropCanvas = null;
        if(cropped == null || preCrop == null || updateType.updateCropping()){
            preCrop = applyPreCropping(sourceImage, getCrop());
            preCropCanvas = new ImageCanvas(new SimpleCanvas(targetCanvas), createPreCropCanvas(), imageRotation.get().flipAxis);
            cropped = applyCropping(preCrop, preCropCanvas, imageRotation.get(), imageFlipHorizontal.get(), imageFlipVertical.get());
        }else{
            preCropCanvas = new ImageCanvas(new SimpleCanvas(targetCanvas), createPreCropCanvas(), imageRotation.get().flipAxis);
        }

        filteredImage = applyFilters(cropped, updateType.updateAllFilters(), settings);

        destCanvas = preCropCanvas;
    }

    /**
     * Creates a transform for converting from original image space -> canvas space
     * The transform handles crops + rotations + scaling
     * @param settings the image settings to generate the transform from
     * @return the canvas transform as an AWT transform
     */
    public AffineTransform getCanvasTransform(ImageFilterSettings settings, ICanvas destCanvas){
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
        AffineTransform scaleTransform = ImageTools.getCanvasScaleTransform(canvas, destCanvas);
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
            propertyList = PropertyUtil.createPropertiesList(imageRotation, imageFlipHorizontal, imageFlipVertical, cropStartX, cropStartY, cropWidth, cropHeight);
        }
        return propertyList;
    }

    @Override
    public String toString() {
        return "Filtered Image Data: Source Image: %s x %s (%s)".formatted(sourceImage.getWidth(), sourceImage.getHeight(), sourceFile == null ? "Internal" : sourceFile);
    }

    public String getPropertiesString(){
        return "Properties: Rotation: %s, Flip Horizontal: %s, Flip Vertical: %s, Crops: %s %s %s %s".formatted(imageRotation.get().name(), imageFlipHorizontal.get(), imageFlipVertical.get(), cropStartX.get(), cropStartY.get(), cropWidth.get(), cropHeight.get());
    }

}