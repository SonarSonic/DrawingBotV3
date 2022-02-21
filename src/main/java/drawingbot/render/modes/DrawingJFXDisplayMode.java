package drawingbot.render.modes;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.geom.basic.IGeometry;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.plotting.AsynchronousGeometryIterator;
import drawingbot.plotting.DrawingGeometryIterator;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottingTask;
import drawingbot.render.RenderUtils;
import drawingbot.render.jfx.JavaFXRenderer;

public abstract class DrawingJFXDisplayMode extends AbstractJFXDisplayMode{

    private AsynchronousGeometryIterator asyncIterator;
    private DrawingGeometryIterator drawingIterator;
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
                        if(asyncIterator == null || asyncIterator.currentDrawing != renderedTask.plottedDrawing){
                            asyncIterator = new AsynchronousGeometryIterator(renderedTask.plottedDrawing);
                        }

                        if(jfr.clearedProcessDrawing || jfr.changedTask || jfr.changedState || jfr.changedMode){ //avoids redrawing in some instances
                            jfr.clearCanvas();
                            asyncIterator.reset();
                        }
                        if(asyncIterator.hasNext()){
                            jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                            jfr.graphicsFX.translate(renderedTask.resolution.getScaledOffsetX(), renderedTask.resolution.getScaledOffsetY());

                            RenderUtils.renderDrawingFX(jfr.graphicsFX, asyncIterator, getGeometryFilter(), jfr.getVertexRenderLimit());
                        }
                    }
                    break;
                case POST_PROCESSING:
                case FINISHING:
                    // NOP - continue displaying the path finding result
                    break;
                case FINISHED:
                    if(drawingIterator == null || drawingIterator.currentDrawing != renderedTask.plottedDrawing){
                        drawingIterator = new DrawingGeometryIterator(renderedTask.plottedDrawing);
                    }

                    EnumBlendMode blendMode = DrawingBotV3.INSTANCE.blendMode.get();
                    if(jfr.shouldRedraw){
                        jfr.clearCanvas();
                        drawingIterator.reset(renderedTask.plottedDrawing);
                        DrawingBotV3.INSTANCE.updateLocalMessage("Drawing");
                        DrawingBotV3.INSTANCE.updateLocalProgress(0);
                        drawingTime = System.currentTimeMillis();
                    }


                    if(drawingIterator.hasNext()){
                        jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                        jfr.graphicsFX.translate(renderedTask.resolution.getScaledOffsetX(), renderedTask.resolution.getScaledOffsetY());
                        jfr.graphicsFX.setGlobalBlendMode(blendMode.javaFXVersion);

                        RenderUtils.renderDrawingFX(jfr.graphicsFX, drawingIterator, getGeometryFilter(), jfr.getVertexRenderLimit());

                        DrawingBotV3.INSTANCE.updateLocalProgress(drawingIterator.getCurrentGeometryProgress());
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
            return IGeometryFilter.DEFAULT_VIEW_FILTER;
        }

        @Override
        public String getName() {
            return "Drawing";
        }
    }

    public static class SelectedPen extends DrawingJFXDisplayMode{

        @Override
        public IGeometryFilter getGeometryFilter() {
            return IGeometryFilter.SELECTED_PEN_FILTER;
        }

        @Override
        public String getName() {
            return "Selected pen";
        }
    }
}
