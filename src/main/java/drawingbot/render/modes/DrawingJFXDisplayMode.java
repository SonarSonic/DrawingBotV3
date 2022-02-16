package drawingbot.render.modes;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.geom.basic.IGeometry;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.plotting.PlottingTask;
import drawingbot.render.jfx.JavaFXRenderer;

public abstract class DrawingJFXDisplayMode extends AbstractJFXDisplayMode{

    private int renderedLines = 0;
    private long drawingTime = 0;

    @Override
    public void preRender(JavaFXRenderer jfr) {
        PlottingTask renderedTask = DrawingBotV3.INSTANCE.getRenderedTask();

        //if the task has changed the images size will also have changed
        if(jfr.changedTask){
            jfr.canvasNeedsUpdate = true;
        }
        //we will only update the canvas when there is a correct print scale
        if(jfr.canvasNeedsUpdate && renderedTask != null && renderedTask.resolution.getPrintScale() > 0){
            jfr.updateCanvasSize(renderedTask.resolution.getScaledWidth(), renderedTask.resolution.getScaledHeight());
            jfr.updateCanvasScaling();
            jfr.canvasNeedsUpdate = false;
            jfr.shouldRedraw = true; //force redraw
        }
    }

    @Override
    public void doRender(JavaFXRenderer jfr) {
        PlottingTask renderedTask = DrawingBotV3.INSTANCE.getRenderedTask();
        if(renderedTask != null){
            switch (renderedTask.stage){
                case QUEUED:
                case PRE_PROCESSING:
                    break;
                case DO_PROCESS:
                    if(renderedTask.handlesProcessRendering()){
                        renderedTask.renderProcessing(jfr, renderedTask);
                    }else{
                        if(jfr.clearedProcessDrawing || jfr.changedTask || jfr.changedState || jfr.changedMode){ //avoids redrawing in some instances
                            jfr.clearCanvas();
                            renderedLines = 0;
                        }
                        if(renderedTask.plottedDrawing.getGeometryCount() != 0){
                            jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                            jfr.graphicsFX.translate(renderedTask.resolution.getScaledOffsetX(), renderedTask.resolution.getScaledOffsetY());
                            renderedLines = renderedTask.plottedDrawing.renderGeometryFX(jfr.graphicsFX, renderedLines, renderedTask.plottedDrawing.getGeometryCount(), IGeometry.DEFAULT_EXPORT_FILTER, jfr.getVertexRenderLimit(), false);
                        }
                    }
                    break;
                case POST_PROCESSING:
                case FINISHING:
                    // NOP - continue displaying the path finding result
                    break;
                case FINISHED:
                    EnumBlendMode blendMode = renderedTask.plottedDrawing.drawingPenSet.blendMode.get();
                    if(jfr.shouldRedraw){
                        jfr.clearCanvas();
                        renderedLines = renderedTask.plottedDrawing.getDisplayedShapeMax()-1;
                        DrawingBotV3.INSTANCE.updateLocalMessage("Drawing");
                        DrawingBotV3.INSTANCE.updateLocalProgress(0);
                        drawingTime = System.currentTimeMillis();
                    }
                    if(renderedLines != -1){
                        jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                        jfr.graphicsFX.translate(renderedTask.resolution.getScaledOffsetX(), renderedTask.resolution.getScaledOffsetY());
                        jfr.graphicsFX.setGlobalBlendMode(blendMode.javaFXVersion);

                        renderedLines = renderedTask.plottedDrawing.renderGeometryFX(jfr.graphicsFX, renderedTask.plottedDrawing.getDisplayedShapeMin(), renderedLines, getGeometryFilter(), jfr.getVertexRenderLimit(),true);

                        int end = renderedTask.plottedDrawing.getDisplayedShapeMax()-1;
                        DrawingBotV3.INSTANCE.updateLocalProgress((float)(end-renderedLines) / end);

                        if(renderedLines == renderedTask.plottedDrawing.getDisplayedShapeMin()){
                            long time = System.currentTimeMillis();
                            DrawingBotV3.logger.finest("Drawing Took: " + (time-drawingTime) + " ms");
                            renderedLines = -1;
                        }
                    }
                    break;
            }

        }else if(jfr.shouldRedraw){
            jfr.clearCanvas();
        }
    }

    public abstract IGeometryFilter getGeometryFilter();

    public static class Drawing extends DrawingJFXDisplayMode{

        @Override
        public IGeometryFilter getGeometryFilter() {
            return IGeometry.DEFAULT_EXPORT_FILTER;
        }

        @Override
        public String getName() {
            return "Drawing";
        }
    }

    public static class SelectedPen extends DrawingJFXDisplayMode{

        @Override
        public IGeometryFilter getGeometryFilter() {
            return IGeometry.SELECTED_PEN_FILTER;
        }

        @Override
        public String getName() {
            return "Selected pen";
        }
    }
}
