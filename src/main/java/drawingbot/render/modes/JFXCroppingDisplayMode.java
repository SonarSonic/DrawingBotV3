package drawingbot.render.modes;

import drawingbot.DrawingBotV3;
import drawingbot.geom.shapes.GRectangle;
import drawingbot.image.format.ImageData;
import drawingbot.javafx.util.JFXUtils;
import drawingbot.plotting.canvas.SimpleCanvas;
import drawingbot.render.overlays.ShapeListEditingOverlays;
import drawingbot.render.shapes.JFXShape;
import drawingbot.render.shapes.JFXShapeList;
import drawingbot.render.shapes.editing.ViewportEditMode;
import drawingbot.render.viewport.Viewport;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class JFXCroppingDisplayMode extends JFXImageDisplayMode {

    private CropShape cropShape;
    private JFXShapeList croppingList;
    private ShapeListEditingOverlays croppingOverlays;
    private boolean transforming = false;

    public ChangeListener<Number> valueChangeListener = (observable, oldValue, newValue) -> {
        if(!transforming){
            updateCropFromImageData(getFilteredImageData());
        }
    };

    @Override
    public void init() {
        super.init();
        cropShape = new CropShape();
        cropShape.setSelected(true);

        croppingList = new JFXShapeList();
        croppingList.getShapeList().add(cropShape);

        croppingOverlays = new ShapeListEditingOverlays();
        croppingOverlays.enabledProperty().bind(activeProperty());
        croppingOverlays.enableRotation.set(false);
        croppingOverlays.setEditMode(ViewportEditMode.SELECT);
        croppingOverlays.setActiveList(croppingList);

        JFXUtils.subscribeListener(filteredImageDataProperty(), (observable, oldValue, newValue) -> {
            if(oldValue != null){
                removeListeners(oldValue);
            }
            if(newValue != null){
                addListeners(newValue);
                updateCropFromImageData(newValue);

                canvasProperty().unbind();
                setCanvas(new SimpleCanvas(newValue.getSourceImage().getWidth(), newValue.getSourceImage().getHeight()));
                setDisplayedImage(newValue.getSourceImage());

            }else{
                if(!canvasProperty().isBound()){
                    canvasProperty().bind(fallbackCanvasProperty());
                }
                setDisplayedImage(null);
            }
        });

        //TODO, this is the only line which binds us to DrawingBotV3 instance, if we want to use this display mode for other image data we need to remove this
        filteredImageDataProperty().bind(DrawingBotV3.INSTANCE.projectOpenImage);
    }

    public  void addListeners(ImageData imageData){
        if(imageData == null){
            return;
        }
        imageData.getImageCropping().cropStartX.addListener(valueChangeListener);
        imageData.getImageCropping().cropStartY.addListener(valueChangeListener);
        imageData.getImageCropping().cropWidth.addListener(valueChangeListener);
        imageData.getImageCropping().cropHeight.addListener(valueChangeListener);
    }

    public void removeListeners(ImageData imageData){
        if(imageData == null){
            return;
        }
        imageData.getImageCropping().cropStartX.removeListener(valueChangeListener);
        imageData.getImageCropping().cropStartY.removeListener(valueChangeListener);
        imageData.getImageCropping().cropWidth.removeListener(valueChangeListener);
        imageData.getImageCropping().cropHeight.removeListener(valueChangeListener);
    }

    public void updateImageDataFromCrop(){
        ImageData imageData = getFilteredImageData();

        double scale = imageData.getSourceCanvas().getPlottingScale();
        Rectangle2D rectangle2D = cropShape.transformed.getAWTShape().getBounds2D();
        transforming = true;
        imageData.getImageCropping().cropStartX.set(rectangle2D.getX() / scale);
        imageData.getImageCropping().cropStartY.set(rectangle2D.getY() / scale);
        imageData.getImageCropping().cropWidth.set(rectangle2D.getWidth() / scale);
        imageData.getImageCropping().cropHeight.set( rectangle2D.getHeight() / scale);
        transforming = false;
    }

    public void updateCropFromImageData(ImageData imageData){
        if(imageData != null){
            Rectangle2D rectangle2D = imageData.getCrop();
            cropShape.geometry = new GRectangle((float) rectangle2D.getX(), (float) rectangle2D.getY(), (float) rectangle2D.getWidth(), (float) rectangle2D.getHeight());
            cropShape.setDisplayed(true);
        }else{
            cropShape.geometry = new GRectangle(0, 0, 0, 0);
            cropShape.setDisplayed(false);
        }
        cropShape.transformed = cropShape.geometry;
        cropShape.awtTransform = new AffineTransform();
        cropShape.updateFromTransform(cropShape.awtTransform);
    }

    @Override
    public void activateDisplayMode(Viewport viewport) {
        super.activateDisplayMode(viewport);
        cropShape.setSelected(true);
        viewport.getViewportOverlays().add(croppingOverlays);
    }

    @Override
    public void deactivateDisplayMode(Viewport viewport) {
        super.deactivateDisplayMode(viewport);
        viewport.getViewportOverlays().remove(croppingOverlays);
    }

    ////////////////////////////////////////////////////////

    private class CropShape extends JFXShape {

        public CropShape() {
            super(new GRectangle(0, 0, 0, 0));
            this.setType(JFXShape.Type.RESHAPE);
            this.setEditable(false);
        }

        @Override
        public void setAwtTransform(AffineTransform newTransform) {
            super.setAwtTransform(newTransform);
            updateImageDataFromCrop();
        }
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<ImageData> filteredImageData = new SimpleObjectProperty<>();

    public ImageData getFilteredImageData() {
        return filteredImageData.get();
    }

    public ObjectProperty<ImageData> filteredImageDataProperty() {
        return filteredImageData;
    }

    public void setFilteredImageData(ImageData filteredImageData) {
        this.filteredImageData.set(filteredImageData);
    }

    ////////////////////////////////////////////////////////

    @Override
    public String getName() {
        return "Image Cropping";
    }
}
