package drawingbot.render.modes;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.plotting.DrawingGeometryIterator;
import drawingbot.plotting.PFMTask;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.WrappedGeometryIterator;
import drawingbot.render.RenderUtils;
import drawingbot.render.jfx.JavaFXRenderer;
import drawingbot.utils.EnumTaskStage;
import drawingbot.utils.flags.Flags;

public abstract class DrawingJFXDisplayMode extends AbstractJFXDisplayMode{

    private DrawingGeometryIterator drawingIterator;

    public abstract PlottedDrawing getCurrentDrawing();

    @Override
    public void preRender(JavaFXRenderer jfr) {
        super.preRender(jfr);
        //setup the canvas
        if(!(this instanceof ExportedDrawing) && DrawingBotV3.taskManager().getRenderedTask() != null && DrawingBotV3.taskManager().getRenderedTask().stage != EnumTaskStage.FINISHED){
            jfr.setupCanvasSize(DrawingBotV3.taskManager().getRenderedTask().drawing.getCanvas());
        }else if(getCurrentDrawing() != null){
            jfr.setupCanvasSize(getCurrentDrawing().getCanvas());
        }else if(DrawingBotV3.project().openImage.get() != null) {
            jfr.setupCanvasSize(DrawingBotV3.project().openImage.get().getTargetCanvas());
        }else{
            jfr.setupCanvasSize(DrawingBotV3.project().drawingArea.get());
        }
    }

    @Override
    public void doRender(JavaFXRenderer jfr) {
        super.doRender(jfr);

        // Render the current active task using the Async - Iterator
        if(!(this instanceof ExportedDrawing) && DrawingBotV3.taskManager().getRenderedTask() != null){
            PFMTask renderedTask = DrawingBotV3.taskManager().getRenderedTask();
            if (renderedTask.stage == EnumTaskStage.DO_PROCESS) {
                WrappedGeometryIterator iterator = renderedTask.getTaskGeometryIterator();
                if (renderFlags.anyMatch(Flags.FORCE_REDRAW, Flags.CLEAR_DRAWING_JFX, Flags.CURRENT_DRAWING_CHANGED, Flags.ACTIVE_TASK_CHANGED, Flags.ACTIVE_TASK_CHANGED_STATE)) {
                    jfr.clearCanvas();
                    iterator.reset();
                    renderFlags.markForClear(Flags.FORCE_REDRAW, Flags.CLEAR_DRAWING_JFX, Flags.CURRENT_DRAWING_CHANGED, Flags.ACTIVE_TASK_CHANGED, Flags.ACTIVE_TASK_CHANGED_STATE);
                }
                if (iterator.hasNext()) {
                    jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                    jfr.graphicsFX.translate(renderedTask.drawing.getCanvas().getScaledDrawingOffsetX(), renderedTask.drawing.getCanvas().getScaledDrawingOffsetY());

                    RenderUtils.renderDrawingFX(jfr.graphicsFX, iterator, getGeometryFilter(), jfr.getVertexRenderLimit(), jfr.getRenderTimeout());
                }
            }else if (renderFlags.anyMatch(Flags.FORCE_REDRAW, Flags.CLEAR_DRAWING_JFX, Flags.CURRENT_DRAWING_CHANGED)){
                jfr.clearCanvas();
                renderFlags.markForClear(Flags.FORCE_REDRAW, Flags.CLEAR_DRAWING_JFX, Flags.CURRENT_DRAWING_CHANGED);
            }
            return;
        }
        PlottedDrawing drawing = getCurrentDrawing();
        if(drawing != null){
            if(drawingIterator == null || drawingIterator.currentDrawing != drawing){
                drawingIterator = new DrawingGeometryIterator(drawing);
            }
            EnumBlendMode blendMode = DrawingBotV3.project().blendMode.get();
            if (renderFlags.anyMatch(Flags.FORCE_REDRAW, Flags.CLEAR_DRAWING_JFX, Flags.CURRENT_DRAWING_CHANGED)) {
                jfr.clearCanvas();
                drawingIterator.reset(drawing);
                DrawingBotV3.INSTANCE.updateLocalMessage("Drawing");
                DrawingBotV3.INSTANCE.updateLocalProgress(0);
                renderFlags.markForClear(Flags.FORCE_REDRAW, Flags.CLEAR_DRAWING_JFX, Flags.CURRENT_DRAWING_CHANGED);
            }
            if(drawingIterator.hasNext()){
                jfr.graphicsFX.scale(jfr.canvasScaling, jfr.canvasScaling);
                jfr.graphicsFX.translate(drawing.getCanvas().getScaledDrawingOffsetX(), drawing.getCanvas().getScaledDrawingOffsetY());
                jfr.graphicsFX.setGlobalBlendMode(blendMode.javaFXVersion);

                RenderUtils.renderDrawingFX(jfr.graphicsFX, drawingIterator, getGeometryFilter(), jfr.getVertexRenderLimit(), jfr.getRenderTimeout());

                DrawingBotV3.INSTANCE.updateLocalProgress(drawingIterator.getCurrentGeometryProgress());
            }
        }else if (renderFlags.anyMatch(Flags.FORCE_REDRAW, Flags.CLEAR_DRAWING_JFX, Flags.ACTIVE_TASK_CHANGED, Flags.ACTIVE_TASK_CHANGED_STATE, Flags.CURRENT_DRAWING_CHANGED)) {
            jfr.clearCanvas();
            renderFlags.markForClear(Flags.FORCE_REDRAW, Flags.CLEAR_DRAWING_JFX, Flags.ACTIVE_TASK_CHANGED, Flags.ACTIVE_TASK_CHANGED_STATE, Flags.CURRENT_DRAWING_CHANGED);
        }
    }

    public abstract IGeometryFilter getGeometryFilter();

    public static class Drawing extends DrawingJFXDisplayMode{

        @Override
        public PlottedDrawing getCurrentDrawing() {
            return DrawingBotV3.taskManager().getCurrentDrawing();
        }

        @Override
        public IGeometryFilter getGeometryFilter() {
            return IGeometryFilter.DEFAULT_VIEW_FILTER;
        }

        @Override
        public String getName() {
            return "Drawing";
        }
    }

    public static class ExportedDrawing extends DrawingJFXDisplayMode{

        @Override
        public PlottedDrawing getCurrentDrawing() {
            if(DrawingBotV3.project().getExportDrawing() != null){
                return DrawingBotV3.project().getExportDrawing();
            }
            return null;
        }

        @Override
        public IGeometryFilter getGeometryFilter() {
            return IGeometryFilter.DEFAULT_VIEW_FILTER;
        }

        @Override
        public String getName() {
            return "Exported Drawing";
        }
    }

    public static class SelectedPen extends DrawingJFXDisplayMode{

        @Override
        public PlottedDrawing getCurrentDrawing() {
            return DrawingBotV3.taskManager().getCurrentDrawing();
        }

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
