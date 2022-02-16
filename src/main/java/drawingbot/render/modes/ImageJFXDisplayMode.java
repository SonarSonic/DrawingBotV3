package drawingbot.render.modes;

import drawingbot.DrawingBotV3;
import drawingbot.image.ImageFilteringTask;
import drawingbot.plotting.PlottingTask;
import drawingbot.render.jfx.JavaFXRenderer;
import javafx.embed.swing.SwingFXUtils;

public abstract class ImageJFXDisplayMode extends AbstractJFXDisplayMode {

    @Override
    public void preRender(JavaFXRenderer jfr) {
        if(DrawingBotV3.INSTANCE.openImage.get() != null){
            if(jfr.imageFiltersChanged || jfr.imageFilterDirty || jfr.croppingDirty){
                if(jfr.filteringTask == null || !jfr.filteringTask.updating.get()){
                    DrawingBotV3.INSTANCE.openImage.get().updateCropping = jfr.croppingDirty;
                    DrawingBotV3.INSTANCE.openImage.get().updateAllFilters = jfr.imageFiltersChanged;
                    jfr.imageFiltersChanged = false;
                    jfr.imageFilterDirty = false;
                    jfr.croppingDirty = false;
                    DrawingBotV3.INSTANCE.startTask(DrawingBotV3.INSTANCE.imageFilteringService, jfr.filteringTask = new ImageFilteringTask(DrawingBotV3.INSTANCE.openImage.get()));
                }
            }
            //resize the canvas
            if(jfr.canvasNeedsUpdate){
                jfr.updateCanvasSize(DrawingBotV3.INSTANCE.openImage.get().resolution.getScaledWidth(), DrawingBotV3.INSTANCE.openImage.get().resolution.getScaledHeight());
                jfr.updateCanvasScaling();
                jfr.canvasNeedsUpdate = false;
                jfr.shouldRedraw = true; //force redraw
            }
        }
    }

    @Override
    public void postRender(JavaFXRenderer jfr) {
        //NOP
    }

    public static class Image extends ImageJFXDisplayMode{

        @Override
        public void doRender(JavaFXRenderer jfr) {
            if(DrawingBotV3.INSTANCE.openImage.get() != null){
                if(jfr.shouldRedraw){
                    jfr.clearCanvas();
                    jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                    jfr.graphicsFX.translate(DrawingBotV3.INSTANCE.openImage.get().resolution.getScaledOffsetX() - 0.5F, DrawingBotV3.INSTANCE.openImage.get().resolution.getScaledOffsetY() - 0.5F);
                    jfr.graphicsFX.drawImage(SwingFXUtils.toFXImage(DrawingBotV3.INSTANCE.openImage.get().getFiltered(), null), 0, 0);
                }
            }else{
                jfr.clearCanvas();
            }
        }

        @Override
        public String getName() {
            return "Image";
        }
    }

    public static class Original extends ImageJFXDisplayMode{

        @Override
        public void doRender(JavaFXRenderer jfr) {
            PlottingTask renderedTask = DrawingBotV3.INSTANCE.getRenderedTask();
            if(jfr.shouldRedraw){
                jfr.clearCanvas();
                if(renderedTask != null && renderedTask.getOriginalImage() != null){
                    float screen_scale_x = (float)renderedTask.imgPlotting.getWidth() / (float)renderedTask.imgOriginal.getWidth();
                    float screen_scale_y = (float)renderedTask.imgPlotting.getHeight() / (float)renderedTask.imgOriginal.getHeight();
                    float screen_scale = Math.min(screen_scale_x, screen_scale_y);

                    jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                    jfr.graphicsFX.translate(renderedTask.resolution.imageOffsetX, renderedTask.resolution.imageOffsetY);
                    jfr.graphicsFX.scale(screen_scale, screen_scale);
                    jfr.graphicsFX.drawImage(SwingFXUtils.toFXImage(renderedTask.getOriginalImage(), null), 0, 0);
                }
            }
        }

        @Override
        public String getName() {
            return "Original";
        }
    }

    public static class Reference extends ImageJFXDisplayMode{

        @Override
        public void doRender(JavaFXRenderer jfr) {
            PlottingTask renderedTask = DrawingBotV3.INSTANCE.getRenderedTask();
            if(jfr.shouldRedraw){
                jfr.clearCanvas();
                if(renderedTask != null && renderedTask.getReferenceImage() != null){
                    jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                    jfr.graphicsFX.translate(DrawingBotV3.INSTANCE.openImage.get().resolution.getScaledOffsetX() - 0.5F, DrawingBotV3.INSTANCE.openImage.get().resolution.getScaledOffsetY() - 0.5F);
                    jfr.graphicsFX.drawImage(SwingFXUtils.toFXImage(renderedTask.getReferenceImage(), null), 0, 0);
                }
            }
        }

        @Override
        public String getName() {
            return "Reference";
        }
    }

    public static class Lightened extends ImageJFXDisplayMode{

        @Override
        public void doRender(JavaFXRenderer jfr) {
            PlottingTask renderedTask = DrawingBotV3.INSTANCE.getRenderedTask();
            if(jfr.shouldRedraw){
                jfr.clearCanvas();
                if(renderedTask != null && renderedTask.getPlottingImage() != null){
                    jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                    jfr.graphicsFX.translate(DrawingBotV3.INSTANCE.openImage.get().resolution.getScaledOffsetX() - 0.5F, DrawingBotV3.INSTANCE.openImage.get().resolution.getScaledOffsetY() - 0.5F);
                    jfr.graphicsFX.drawImage(SwingFXUtils.toFXImage(renderedTask.getPlottingImage(), null), 0, 0);
                }
            }
        }

        @Override
        public String getName() {
            return "Lightened";
        }
    }



}
