package drawingbot.render.modes;

import drawingbot.DrawingBotV3;
import drawingbot.image.FilteredBufferedImage;
import drawingbot.image.ImageFilteringTask;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.render.jfx.JavaFXRenderer;
import drawingbot.utils.flags.Flags;
import javafx.embed.swing.SwingFXUtils;

import java.awt.image.BufferedImage;

public abstract class ImageJFXDisplayMode extends AbstractJFXDisplayMode {

    @Override
    public void preRender(JavaFXRenderer jfr) {
        //NOP
    }

    @Override
    public void postRender(JavaFXRenderer jfr) {
        //NOP
    }

    public static class Image extends ImageJFXDisplayMode{

        @Override
        public void preRender(JavaFXRenderer jfr) {
            super.preRender(jfr);

            //update the filtered image
            FilteredBufferedImage openImage = DrawingBotV3.INSTANCE.openImage.get();
            if(openImage != null){
                if(renderFlags.anyMatch(Flags.IMAGE_FILTERS_FULL_UPDATE, Flags.IMAGE_FILTERS_PARTIAL_UPDATE, Flags.CANVAS_CHANGED)){
                    if(jfr.filteringTask == null || !jfr.filteringTask.updating.get()){
                        openImage.updateCropping = renderFlags.anyMatch(Flags.CANVAS_CHANGED);//jfr.croppingDirty;
                        openImage.updateAllFilters = renderFlags.anyMatch(Flags.IMAGE_FILTERS_FULL_UPDATE);//jfr.imageFiltersChanged;
                        DrawingBotV3.INSTANCE.startTask(DrawingBotV3.INSTANCE.imageFilteringService, jfr.filteringTask = new ImageFilteringTask(openImage));
                        renderFlags.markForClear(Flags.IMAGE_FILTERS_FULL_UPDATE, Flags.IMAGE_FILTERS_PARTIAL_UPDATE, Flags.CANVAS_CHANGED);
                    }
                }
            }

            //setup the canvas
            if (openImage != null) {
                jfr.setupCanvasSize(openImage.getCurrentCanvas());
            }else{
                jfr.setupCanvasSize(DrawingBotV3.INSTANCE.drawingArea);
            }
        }

        @Override
        public void doRender(JavaFXRenderer jfr) {

            //render the image
            if (renderFlags.anyMatch(Flags.FORCE_REDRAW, Flags.OPEN_IMAGE_UPDATED)) {
                jfr.clearCanvas();
                FilteredBufferedImage openImage = DrawingBotV3.INSTANCE.openImage.get();
                if (openImage != null) {
                    jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                    jfr.graphicsFX.translate(openImage.getCurrentCanvas().getScaledDrawingOffsetX(), openImage.getCurrentCanvas().getScaledDrawingOffsetY());
                    jfr.graphicsFX.drawImage(SwingFXUtils.toFXImage(openImage.getFiltered(), null), 0, 0);
                }
                renderFlags.markForClear(Flags.FORCE_REDRAW, Flags.OPEN_IMAGE_UPDATED);
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
                jfr.setupCanvasSize(image.getWidth(), image.getHeight());
            }else{
                jfr.setupCanvasSize(DrawingBotV3.INSTANCE.drawingArea);
            }
        }

        @Override
        public void doRender(JavaFXRenderer jfr) {

            //render the image
            if (renderFlags.anyMatch(Flags.FORCE_REDRAW, Flags.CURRENT_DRAWING_CHANGED)) {
                BufferedImage image = getImage();
                jfr.clearCanvas();
                if(image != null){
                    jfr.setupCanvasSize(image.getWidth(), image.getHeight());
                    jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                    jfr.graphicsFX.drawImage(SwingFXUtils.toFXImage(image, null), 0, 0);
                }
                renderFlags.markForClear(Flags.FORCE_REDRAW, Flags.CURRENT_DRAWING_CHANGED);
            }
        }

        public abstract BufferedImage getImage();
    }

    public static class Original extends GenericImage{

        @Override
        public String getName() {
            return "Original";
        }

        @Override
        public BufferedImage getImage() {
            if(DrawingBotV3.INSTANCE.getCurrentDrawing() != null){
                return DrawingBotV3.INSTANCE.getCurrentDrawing().getOriginalImage();
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
            if(DrawingBotV3.INSTANCE.getCurrentDrawing() != null){
                return DrawingBotV3.INSTANCE.getCurrentDrawing().getToneMap();
            }
            return null;
        }
    }

    public static class Reference extends ImageJFXDisplayMode{

        @Override
        public void preRender(JavaFXRenderer jfr) {
            super.preRender(jfr);

            //setup the canvas
            PlottedDrawing drawing = DrawingBotV3.INSTANCE.getCurrentDrawing();
            if (drawing != null) {
                jfr.setupCanvasSize(drawing.getCanvas());
            }else{
                jfr.setupCanvasSize(DrawingBotV3.INSTANCE.drawingArea);
            }
        }

        @Override
        public void doRender(JavaFXRenderer jfr) {

            //render the image
            if (renderFlags.anyMatch(Flags.FORCE_REDRAW, Flags.CURRENT_DRAWING_CHANGED)) {
                jfr.clearCanvas();
                PlottedDrawing drawing = DrawingBotV3.INSTANCE.getCurrentDrawing();
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
            PlottedDrawing drawing = DrawingBotV3.INSTANCE.getCurrentDrawing();
            if (drawing != null) {
                jfr.setupCanvasSize(drawing.getCanvas());
            }else{
                jfr.setupCanvasSize(DrawingBotV3.INSTANCE.drawingArea);
            }
        }

        @Override
        public void doRender(JavaFXRenderer jfr) {

            //render the image
            if (renderFlags.anyMatch(Flags.FORCE_REDRAW, Flags.CURRENT_DRAWING_CHANGED)) {
                jfr.clearCanvas();
                PlottedDrawing drawing = DrawingBotV3.INSTANCE.getCurrentDrawing();
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
