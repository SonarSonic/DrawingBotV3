package drawingbot.render.modes;

import drawingbot.DrawingBotV3;
import drawingbot.geom.shapes.GRectangle;
import drawingbot.image.ImageFilteringTask;
import drawingbot.image.format.FilteredImageData;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.canvas.SimpleCanvas;
import drawingbot.render.jfx.JavaFXRenderer;
import drawingbot.render.overlays.ShapeOverlays;
import drawingbot.render.shapes.JFXShape;
import drawingbot.render.shapes.JFXShapeList;
import drawingbot.render.shapes.JFXShapeManager;
import drawingbot.utils.flags.Flags;
import javafx.embed.swing.SwingFXUtils;
import org.fxmisc.easybind.EasyBind;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public abstract class ImageJFXDisplayMode extends AbstractJFXDisplayMode {

    @Override
    public void preRender(JavaFXRenderer jfr) {
        super.preRender(jfr);
        //NOP
    }

    @Override
    public void postRender(JavaFXRenderer jfr) {
        super.postRender(jfr);
        //NOP
    }

    public static class Image extends ImageJFXDisplayMode{

        @Override
        public void preRender(JavaFXRenderer jfr) {
            super.preRender(jfr);

            //update the filtered image
            FilteredImageData openImage = DrawingBotV3.project().openImage.get();
            if(openImage != null){
                if(renderFlags.anyMatch(Flags.IMAGE_FILTERS_FULL_UPDATE, Flags.IMAGE_FILTERS_PARTIAL_UPDATE, Flags.CANVAS_CHANGED)){
                    if(jfr.filteringTask == null || !jfr.filteringTask.updating.get()){
                        openImage.updateCropping = renderFlags.anyMatch(Flags.CANVAS_CHANGED);//jfr.croppingDirty;
                        openImage.updateAllFilters = renderFlags.anyMatch(Flags.IMAGE_FILTERS_FULL_UPDATE);//jfr.imageFiltersChanged;
                        DrawingBotV3.INSTANCE.startTask(DrawingBotV3.INSTANCE.imageFilteringService, jfr.filteringTask = new ImageFilteringTask(DrawingBotV3.context(), openImage));
                        renderFlags.markForClear(Flags.IMAGE_FILTERS_FULL_UPDATE, Flags.IMAGE_FILTERS_PARTIAL_UPDATE, Flags.CANVAS_CHANGED);
                    }
                }
            }

            //setup the canvas
            if (openImage != null) {
                jfr.setupCanvasSize(openImage.getDestCanvas());
            }else{
                jfr.setupCanvasSize(DrawingBotV3.project().drawingArea.get());
            }
        }

        @Override
        public void doRender(JavaFXRenderer jfr) {
            super.doRender(jfr);

            //render the image
            if (renderFlags.anyMatchAndMarkClear(Flags.FORCE_REDRAW, Flags.OPEN_IMAGE_UPDATED)) {
                jfr.clearCanvas();
                FilteredImageData openImage = DrawingBotV3.project().openImage.get();
                if (openImage != null) {
                    jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                    jfr.graphicsFX.translate(openImage.getDestCanvas().getScaledDrawingOffsetX(), openImage.getDestCanvas().getScaledDrawingOffsetY());
                    jfr.graphicsFX.drawImage(SwingFXUtils.toFXImage(openImage.getFilteredImage(), null), 0, 0);
                }
            }
        }

        @Override
        public String getName() {
            return "Image";
        }
    }

    public abstract static class GenericImage extends ImageJFXDisplayMode{

        @Override
        public void preRender(JavaFXRenderer jfr) {
            super.preRender(jfr);

            //setup the canvas
            BufferedImage image = getImage();
            if(image != null){
                jfr.setupCanvasSize(new SimpleCanvas(image.getWidth(), image.getHeight()));
            }else{
                jfr.setupCanvasSize(DrawingBotV3.project().getDrawingArea());
            }
        }

        @Override
        public void doRender(JavaFXRenderer jfr) {
            super.doRender(jfr);

            //render the image
            if (renderFlags.anyMatchAndMarkClear(Flags.FORCE_REDRAW, Flags.CURRENT_DRAWING_CHANGED)) {
                BufferedImage image = getImage();
                jfr.clearCanvas();
                if(image != null){
                    jfr.setupCanvasSize(image.getWidth(), image.getHeight());
                    jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                    jfr.graphicsFX.drawImage(SwingFXUtils.toFXImage(image, null), 0, 0);
                }
            }
        }

        public abstract BufferedImage getImage();
    }

    public static class Cropping extends GenericImage{

        public static JFXShapeList croppingList = new JFXShapeList();
        public static CropShape cropShape = new CropShape();
        public static boolean init = false;
        public static boolean transforming = false;

        public static ChangeListener<Number> valueChangeListener = (observable, oldValue, newValue) -> {
            if(!transforming){
                updateCropFromImageData();
            }
        };

        private static class CropShape extends JFXShape{

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

        public static void addListeners(FilteredImageData imageData){
            imageData.cropStartX.addListener(valueChangeListener);
            imageData.cropStartY.addListener(valueChangeListener);
            imageData.cropEndX.addListener(valueChangeListener);
            imageData.cropEndY.addListener(valueChangeListener);
        }

        public static void removeListeners(FilteredImageData imageData){
            imageData.cropStartX.removeListener(valueChangeListener);
            imageData.cropStartY.removeListener(valueChangeListener);
            imageData.cropEndX.removeListener(valueChangeListener);
            imageData.cropEndY.removeListener(valueChangeListener);
        }

        public static void updateImageDataFromCrop(){
            FilteredImageData imageData = DrawingBotV3.project().openImage.get();

            float scale = imageData.getSourceCanvas().getPlottingScale();
            Rectangle2D rectangle2D = cropShape.transformed.getAWTShape().getBounds2D();
            transforming = true;
            imageData.cropStartX.set((float) rectangle2D.getX() / scale);
            imageData.cropStartY.set((float) rectangle2D.getY() / scale);
            imageData.cropEndX.set((float) rectangle2D.getMaxX() / scale);
            imageData.cropEndY.set((float) rectangle2D.getMaxY() / scale);
            transforming = false;
            DrawingBotV3.INSTANCE.onCanvasChanged();
        }

        public static void updateCropFromImageData(){
            FilteredImageData imageData = DrawingBotV3.project().openImage.get();
            if(imageData != null){
                Rectangle2D rectangle2D = DrawingBotV3.project().openImage.get().getCrop();
                cropShape.geometry = new GRectangle((float) rectangle2D.getX(), (float) rectangle2D.getY(), (float) rectangle2D.getWidth(), (float) rectangle2D.getHeight());
                cropShape.setDisplayed(true);
            }else{
                cropShape.geometry = new GRectangle(0, 0, 0, 0);
                cropShape.setDisplayed(false);
            }
            cropShape.transformed = cropShape.geometry;
            cropShape.awtTransform = new AffineTransform();
            cropShape.updateFromTransform(cropShape.awtTransform);
            DrawingBotV3.INSTANCE.onCanvasChanged();
        }

        public void init(){
            if(!init){
                croppingList.getShapeList().add(cropShape);
                cropShape.setSelected(true);
                DrawingBotV3.project().openImage.addListener((observable, oldValue, newValue) -> {
                    if(oldValue != null){
                        removeListeners(oldValue);
                    }
                    if(newValue != null){
                        addListeners(newValue);
                        updateCropFromImageData();
                    }
                });
                init = true;
            }
        }

        @Override
        public String getName() {
            return "Image Cropping";
        }

        @Override
        public void applySettings() {
            super.applySettings();
            init();
            JFXShapeManager.INSTANCE.activeShapeList.unbind();
            JFXShapeManager.INSTANCE.activeShapeList.set(croppingList);
            ShapeOverlays.INSTANCE.enableRotation.set(false);
        }

        @Override
        public void resetSettings() {
            super.resetSettings();
            DrawingBotV3.INSTANCE.onCanvasChanged();
            JFXShapeManager.INSTANCE.activeShapeList.bind(EasyBind.select(DrawingBotV3.INSTANCE.activeProject).selectObject(p -> p.maskingSettings.get().shapeList));
            ShapeOverlays.INSTANCE.enableRotation.set(true);
        }

        @Override
        public BufferedImage getImage() {
            if(DrawingBotV3.project().openImage.get() != null){
                return DrawingBotV3.project().openImage.get().sourceImage;
            }
            return null;
        }
    }

    public static class Original extends GenericImage{

        @Override
        public String getName() {
            return "Original";
        }

        @Override
        public BufferedImage getImage() {
            if(DrawingBotV3.project().getCurrentDrawing() != null){
                return DrawingBotV3.project().getCurrentDrawing().getOriginalImage();
            }
            return null;
        }
    }

    public static class ToneMap extends GenericImage{

        @Override
        public String getName() {
            return "Tone Map";
        }

        @Override
        public BufferedImage getImage() {
            if(DrawingBotV3.project().getCurrentDrawing() != null){
                return DrawingBotV3.project().getCurrentDrawing().getToneMap();
            }
            return null;
        }
    }

    public static class Reference extends ImageJFXDisplayMode{

        @Override
        public void preRender(JavaFXRenderer jfr) {
            super.preRender(jfr);

            //setup the canvas
            PlottedDrawing drawing = DrawingBotV3.project().getCurrentDrawing();
            if (drawing != null) {
                jfr.setupCanvasSize(drawing.getCanvas());
            }else{
                jfr.setupCanvasSize(DrawingBotV3.project().getDrawingArea());
            }
        }

        @Override
        public void doRender(JavaFXRenderer jfr) {
            super.doRender(jfr);

            //render the image
            if (renderFlags.anyMatch(Flags.FORCE_REDRAW, Flags.CURRENT_DRAWING_CHANGED)) {
                jfr.clearCanvas();
                PlottedDrawing drawing = DrawingBotV3.project().getCurrentDrawing();
                if(drawing != null && drawing.getReferenceImage() != null){
                    jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                    jfr.graphicsFX.translate(drawing.getCanvas().getScaledDrawingOffsetX(), drawing.getCanvas().getScaledDrawingOffsetY());
                    jfr.graphicsFX.drawImage(SwingFXUtils.toFXImage(drawing.getReferenceImage(), null), 0, 0);
                }
                renderFlags.markForClear(Flags.FORCE_REDRAW, Flags.CURRENT_DRAWING_CHANGED);
            }
        }

        @Override
        public String getName() {
            return "Reference";
        }
    }

    public static class Lightened extends ImageJFXDisplayMode{

        @Override
        public void preRender(JavaFXRenderer jfr) {
            super.preRender(jfr);

            //setup the canvas
            PlottedDrawing drawing = DrawingBotV3.project().getCurrentDrawing();
            if (drawing != null) {
                jfr.setupCanvasSize(drawing.getCanvas());
            }else{
                jfr.setupCanvasSize(DrawingBotV3.project().getDrawingArea());
            }
        }

        @Override
        public void doRender(JavaFXRenderer jfr) {
            super.doRender(jfr);

            //render the image
            if (renderFlags.anyMatch(Flags.FORCE_REDRAW, Flags.CURRENT_DRAWING_CHANGED)) {
                jfr.clearCanvas();
                PlottedDrawing drawing = DrawingBotV3.project().getCurrentDrawing();
                if(drawing != null && drawing.getPlottingImage() != null){
                    jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                    jfr.graphicsFX.translate(drawing.getCanvas().getScaledDrawingOffsetX(), drawing.getCanvas().getScaledDrawingOffsetY());
                    jfr.graphicsFX.drawImage(SwingFXUtils.toFXImage(drawing.getPlottingImage(), null), 0, 0);
                }
                renderFlags.markForClear(Flags.FORCE_REDRAW, Flags.CURRENT_DRAWING_CHANGED);
            }
        }

        @Override
        public String getName() {
            return "Lightened";
        }
    }



}
