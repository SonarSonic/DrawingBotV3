package drawingbot.render.modes;

import drawingbot.DrawingBotV3;
import drawingbot.image.ImageFilteringTask;
import drawingbot.plotting.PFMTaskImage;
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
            if(DrawingBotV3.INSTANCE.openImage.get() != null){
                if(renderFlags.anyMatch(Flags.IMAGE_FILTERS_FULL_UPDATE, Flags.IMAGE_FILTERS_PARTIAL_UPDATE, Flags.CANVAS_CHANGED)){
                    if(jfr.filteringTask == null || !jfr.filteringTask.updating.get()){
                        DrawingBotV3.INSTANCE.openImage.get().updateCropping = renderFlags.anyMatch(Flags.CANVAS_CHANGED);//jfr.croppingDirty;
                        DrawingBotV3.INSTANCE.openImage.get().updateAllFilters = renderFlags.anyMatch(Flags.IMAGE_FILTERS_FULL_UPDATE);//jfr.imageFiltersChanged;
                        DrawingBotV3.INSTANCE.startTask(DrawingBotV3.INSTANCE.imageFilteringService, jfr.filteringTask = new ImageFilteringTask(DrawingBotV3.INSTANCE.openImage.get()));
                        renderFlags.markForClear(Flags.IMAGE_FILTERS_FULL_UPDATE, Flags.IMAGE_FILTERS_PARTIAL_UPDATE, Flags.CANVAS_CHANGED);
                    }
                }
            }

            //setup the canvas
            if (DrawingBotV3.INSTANCE.openImage.get() != null) {
                jfr.setupCanvasSize(DrawingBotV3.INSTANCE.openImage.get().getCanvas());
            }else{
                jfr.setupCanvasSize(DrawingBotV3.INSTANCE.drawingArea);
            }
        }

        @Override
        public void doRender(JavaFXRenderer jfr) {

            //render the image
            if (renderFlags.anyMatch(Flags.FORCE_REDRAW, Flags.TASK_CHANGED, Flags.TASK_CHANGED_STATE)) {
                jfr.clearCanvas();
                if (DrawingBotV3.INSTANCE.openImage.get() != null) {
                    jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                    jfr.graphicsFX.translate(DrawingBotV3.INSTANCE.openImage.get().getCanvas().getScaledDrawingOffsetX(), DrawingBotV3.INSTANCE.openImage.get().getCanvas().getScaledDrawingOffsetY());
                    jfr.graphicsFX.drawImage(SwingFXUtils.toFXImage(DrawingBotV3.INSTANCE.openImage.get().getFiltered(), null), 0, 0);
                }
                renderFlags.markForClear(Flags.FORCE_REDRAW, Flags.TASK_CHANGED, Flags.TASK_CHANGED_STATE);
            }
        }

        @Override
        public String getName() {
            return "Image";
        }
    }

    public static class Original extends ImageJFXDisplayMode{

        @Override
        public void preRender(JavaFXRenderer jfr) {
            super.preRender(jfr);

            //setup the canvas
            if(DrawingBotV3.INSTANCE.renderedDrawing.get() != null && DrawingBotV3.INSTANCE.renderedDrawing.get().getOriginalImage() != null){
                BufferedImage originalImage = DrawingBotV3.INSTANCE.renderedDrawing.get().getOriginalImage();
                jfr.setupCanvasSize(originalImage.getWidth(), originalImage.getHeight());
            }else{
                jfr.setupCanvasSize(DrawingBotV3.INSTANCE.drawingArea);
            }
        }

        @Override
        public void doRender(JavaFXRenderer jfr) {

            //render the image
            if (renderFlags.anyMatch(Flags.FORCE_REDRAW, Flags.TASK_CHANGED, Flags.TASK_CHANGED_STATE)) {
                jfr.clearCanvas();
                if(DrawingBotV3.INSTANCE.renderedDrawing.get() != null && DrawingBotV3.INSTANCE.renderedDrawing.get().getOriginalImage() != null){
                    BufferedImage originalImage = DrawingBotV3.INSTANCE.renderedDrawing.get().getOriginalImage();
                    jfr.setupCanvasSize(originalImage.getWidth(), originalImage.getHeight());
                    jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                    jfr.graphicsFX.drawImage(SwingFXUtils.toFXImage(originalImage, null), 0, 0);
                }
                renderFlags.markForClear(Flags.FORCE_REDRAW, Flags.TASK_CHANGED, Flags.TASK_CHANGED_STATE);
            }
        }

        @Override
        public String getName() {
            return "Original";
        }
    }

    public static class Reference extends ImageJFXDisplayMode{

        @Override
        public void preRender(JavaFXRenderer jfr) {
            super.preRender(jfr);

            //setup the canvas
            if (DrawingBotV3.INSTANCE.renderedDrawing.get() != null) {
                jfr.setupCanvasSize(DrawingBotV3.INSTANCE.renderedDrawing.get().getCanvas());
            }else{
                jfr.setupCanvasSize(DrawingBotV3.INSTANCE.drawingArea);
            }
        }

        @Override
        public void doRender(JavaFXRenderer jfr) {

            //render the image
            if (renderFlags.anyMatch(Flags.FORCE_REDRAW, Flags.TASK_CHANGED, Flags.TASK_CHANGED_STATE)) {
                jfr.clearCanvas();
                if(DrawingBotV3.INSTANCE.renderedDrawing.get() != null && DrawingBotV3.INSTANCE.renderedDrawing.get().getReferenceImage() != null){
                    PlottedDrawing drawing = DrawingBotV3.INSTANCE.renderedDrawing.get();
                    jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                    jfr.graphicsFX.translate(drawing.getCanvas().getScaledDrawingOffsetX(), drawing.getCanvas().getScaledDrawingOffsetY());
                    jfr.graphicsFX.drawImage(SwingFXUtils.toFXImage(drawing.getReferenceImage(), null), 0, 0);
                }
                renderFlags.markForClear(Flags.FORCE_REDRAW, Flags.TASK_CHANGED, Flags.TASK_CHANGED_STATE);
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
            if (DrawingBotV3.INSTANCE.getRenderedTask() != null) {
                jfr.setupCanvasSize(DrawingBotV3.INSTANCE.renderedDrawing.get().getCanvas());
            }else{
                jfr.setupCanvasSize(DrawingBotV3.INSTANCE.drawingArea);
            }
        }

        @Override
        public void doRender(JavaFXRenderer jfr) {

            //render the image
            if (renderFlags.anyMatch(Flags.FORCE_REDRAW, Flags.TASK_CHANGED, Flags.TASK_CHANGED_STATE)) {
                jfr.clearCanvas();
                if(DrawingBotV3.INSTANCE.renderedDrawing.get() != null && DrawingBotV3.INSTANCE.renderedDrawing.get().getReferenceImage() != null){
                    PlottedDrawing drawing = DrawingBotV3.INSTANCE.renderedDrawing.get();
                    jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                    jfr.graphicsFX.translate(drawing.getCanvas().getScaledDrawingOffsetX(), drawing.getCanvas().getScaledDrawingOffsetY());
                    jfr.graphicsFX.drawImage(SwingFXUtils.toFXImage(drawing.getPlottingImage(), null), 0, 0);
                }
                renderFlags.markForClear(Flags.FORCE_REDRAW, Flags.TASK_CHANGED, Flags.TASK_CHANGED_STATE);
            }
        }

        @Override
        public String getName() {
            return "Lightened";
        }
    }



}
