package drawingbot.image.format;

import drawingbot.api.ICanvas;
import drawingbot.api.IProperties;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.utils.EnumRotation;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.awt.geom.Rectangle2D;

/**
 * Stores all of the data required for performing "pre-cropping" on a given image
 */
public class ImageCropping implements IProperties {

    public ImageCropping(){}

    public ImageCropping(ICanvas sourceCanvas){
        reset(sourceCanvas);
    }

    public void reset(ICanvas sourceCanvas){
        this.cropStartX.set(0);
        this.cropStartY.set(0);
        this.cropWidth.set(sourceCanvas.getWidth());
        this.cropHeight.set(sourceCanvas.getHeight());
    }

    public void update(ImageCropping cropping){
        this.setCropStartX(cropping.getCropStartX());
        this.setCropStartY(cropping.getCropStartY());
        this.setCropWidth(cropping.getCropWidth());
        this.setCropHeight(cropping.getCropHeight());
        this.setFlipVertical(cropping.shouldFlipVertical());
        this.setFlipHorizontal(cropping.shouldFlipHorizontal());
        this.setImageRotation(cropping.getImageRotation());
    }

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

    public final DoubleProperty cropStartX = new SimpleDoubleProperty(0);

    public double getCropStartX() {
        return cropStartX.get();
    }

    public DoubleProperty cropStartXProperty() {
        return cropStartX;
    }

    public void setCropStartX(double cropStartX) {
        this.cropStartX.set(cropStartX);
    }

    ///////////////////////////////////////

    public final DoubleProperty cropStartY = new SimpleDoubleProperty(0);

    public double getCropStartY() {
        return cropStartY.get();
    }

    public DoubleProperty cropStartYProperty() {
        return cropStartY;
    }

    public void setCropStartY(double cropStartY) {
        this.cropStartY.set(cropStartY);
    }

    ///////////////////////////////////////

    public final DoubleProperty cropWidth = new SimpleDoubleProperty(0);

    public double getCropWidth() {
        return cropWidth.get();
    }

    public DoubleProperty cropWidthProperty() {
        return cropWidth;
    }

    public void setCropWidth(double cropWidth) {
        this.cropWidth.set(cropWidth);
    }

    ///////////////////////////////////////

    public final DoubleProperty cropHeight = new SimpleDoubleProperty(0);

    public double getCropHeight() {
        return cropHeight.get();
    }

    public DoubleProperty cropHeightProperty() {
        return cropHeight;
    }

    public void setCropHeight(double cropHeight) {
        this.cropHeight.set(cropHeight);
    }

    ///////////////////////////////////////

    public Rectangle2D getCrop(ICanvas sourceCanvas){
        double scale = sourceCanvas.getPlottingScale();
        double startX = getCropStartX() * scale;
        double startY = getCropStartY() * scale;
        double width = getCropWidth() * scale;
        double height = getCropHeight() * scale;
        if(width == 0 || height == 0){
            return new Rectangle2D.Double(0, 0, sourceCanvas.getWidth(), sourceCanvas.getHeight());
        }
        return new Rectangle2D.Double(startX, startY, width, height);
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
    public String toString(){
        return "Image Cropping: Rotation: %s, Flip Horizontal: %s, Flip Vertical: %s, Crops: %s %s %s %s".formatted(imageRotation.get().name(), imageFlipHorizontal.get(), imageFlipVertical.get(), cropStartX.get(), cropStartY.get(), cropWidth.get(), cropHeight.get());
    }
}
