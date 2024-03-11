package drawingbot.render.modes;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.util.JFXUtils;
import drawingbot.plotting.DrawingGeometryIterator;
import drawingbot.plotting.PFMTask;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.WrappedGeometryIterator;
import drawingbot.render.RenderUtils;
import drawingbot.render.renderer.JFXRenderer;
import drawingbot.render.renderer.RendererFactory;
import drawingbot.utils.EnumTaskStage;
import drawingbot.utils.flags.Flags;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;

public abstract class JFXDrawingDisplayMode extends DisplayModeDrawing implements IJFXDisplayMode {

    @Override
    public RendererFactory getRendererFactory() {
        return JFXRenderer.JFX_RENDERER_FACTORY;
    }

    ////////////////////////////////////////////////////////

    private DrawingGeometryIterator drawingIterator;

    @Override
    public void doRender(JFXRenderer jfr) {
        PlottedDrawing drawing = getDisplayedDrawing();

        // Render the current active task using the Async - Iterator
        if(getDisplayedTask() != null){
            PFMTask renderedTask = getDisplayedTask();
            if (renderedTask.stage == EnumTaskStage.DO_PROCESS) {
                WrappedGeometryIterator iterator = renderedTask.getTaskGeometryIterator();
                if (getViewport().getRenderFlags().anyMatchAndClearOnMatch(Flags.FORCE_REDRAW, Flags.CLEAR_DRAWING_JFX, Flags.CURRENT_DRAWING_CHANGED, Flags.ACTIVE_TASK_CHANGED, Flags.ACTIVE_TASK_CHANGED_STATE)) {
                    jfr.clearCanvas();
                    iterator.reset();
                }
                if (iterator.hasNext()) {
                    jfr.graphicsFX.scale(jfr.getRenderScale(), jfr.getRenderScale());
                    jfr.graphicsFX.translate(renderedTask.drawing.getCanvas().getScaledDrawingOffsetX(), renderedTask.drawing.getCanvas().getScaledDrawingOffsetY());

                    RenderUtils.renderDrawingFX(jfr.graphicsFX, iterator, getGeometryFilter(), jfr.getVertexRenderLimit(), jfr.getVertexRenderTimeOut());
                }
            }else if (getViewport().getRenderFlags().anyMatchAndClearOnMatch(Flags.FORCE_REDRAW, Flags.CLEAR_DRAWING_JFX, Flags.CURRENT_DRAWING_CHANGED)){
                jfr.clearCanvas();
            }
            return;
        }
        if(drawing != null){
            if(drawingIterator == null || drawingIterator.currentDrawing != drawing){
                drawingIterator = new DrawingGeometryIterator(drawing);
            }
            if (getViewport().getRenderFlags().anyMatchAndClearOnMatch(Flags.FORCE_REDRAW, Flags.CLEAR_DRAWING_JFX, Flags.CURRENT_DRAWING_CHANGED)) {
                jfr.clearCanvas();
                drawingIterator.reset(drawing);
                setRenderStatus("Drawing");
                setRenderProgress(0);
            }
            if(drawingIterator.hasNext()){
                jfr.graphicsFX.scale(jfr.getRenderScale(), jfr.getRenderScale());
                jfr.graphicsFX.translate(drawing.getCanvas().getScaledDrawingOffsetX(), drawing.getCanvas().getScaledDrawingOffsetY());
                jfr.graphicsFX.setGlobalBlendMode(getViewport().getRendererBlendMode().javaFXVersion);

                RenderUtils.renderDrawingFX(jfr.graphicsFX, drawingIterator, getGeometryFilter(), jfr.getVertexRenderLimit(), jfr.getVertexRenderTimeOut());

                setRenderProgress(drawingIterator.getCurrentGeometryProgress());
            }
        }else if (getViewport().getRenderFlags().anyMatchAndClearOnMatch(Flags.FORCE_REDRAW, Flags.CLEAR_DRAWING_JFX, Flags.ACTIVE_TASK_CHANGED, Flags.ACTIVE_TASK_CHANGED_STATE, Flags.CURRENT_DRAWING_CHANGED)) {
            jfr.clearCanvas();
        }
    }

    @Override
    public boolean isRenderDirty(JFXRenderer jfr) {
        if(getViewport().getRenderFlags().anyMatch(Flags.FORCE_REDRAW, Flags.CLEAR_DRAWING_JFX, Flags.CURRENT_DRAWING_CHANGED, Flags.ACTIVE_TASK_CHANGED, Flags.ACTIVE_TASK_CHANGED_STATE)){
            return true;
        }
        PFMTask displayedTask = getDisplayedTask();
        if(displayedTask!= null) {
            return displayedTask.stage == EnumTaskStage.DO_PROCESS && displayedTask.getTaskGeometryIterator().hasNext();
        }
        return drawingIterator != null && drawingIterator.hasNext();
    }

    public static class Drawing extends JFXDrawingDisplayMode {

        @Override
        public void init() {
            super.init();
            setGeometryFilter(IGeometryFilter.DEFAULT_VIEW_FILTER);
            displayedTaskProperty().bind(DrawingBotV3.INSTANCE.projectRenderedTask);
            displayedDrawingProperty().bind(Bindings.createObjectBinding(() -> {
                PFMTask projectTask = DrawingBotV3.INSTANCE.projectRenderedTask.get();
                if(projectTask != null && projectTask.drawing != null && projectTask.stage != EnumTaskStage.FINISH){
                    return projectTask.drawing;
                }
                return DrawingBotV3.INSTANCE.projectCurrentDrawing.get();
            }, DrawingBotV3.INSTANCE.projectRenderedTask, DrawingBotV3.INSTANCE.projectCurrentDrawing));
            fallbackCanvasProperty().bind(DrawingBotV3.INSTANCE.projectDrawingArea);
        }

        @Override
        public String getName() {
            return "Drawing";
        }
    }

    public static class ExportedDrawing extends JFXDrawingDisplayMode {

        @Override
        public void init() {
            super.init();
            setGeometryFilter(IGeometryFilter.DEFAULT_VIEW_FILTER);
            displayedDrawingProperty().bind(DrawingBotV3.INSTANCE.projectExportedDrawing);
            fallbackCanvasProperty().bind(DrawingBotV3.INSTANCE.projectDrawingArea);
        }

        @Override
        public String getName() {
            return "Exported Drawing";
        }
    }

    public static class SelectedPen extends JFXDrawingDisplayMode {

        @Override
        public void init() {
            super.init();
            setGeometryFilter((drawing, geometry, pen) -> IGeometryFilter.DEFAULT_VIEW_FILTER.filter(drawing, geometry, pen) && (DrawingBotV3.project().selectedPens.get().isEmpty() || DrawingBotV3.project().selectedPens.get().contains(pen)));
            displayedDrawingProperty().bind(DrawingBotV3.INSTANCE.projectCurrentDrawing);
            fallbackCanvasProperty().bind(DrawingBotV3.INSTANCE.projectDrawingArea);

            //Bind listeners to the projects selected pens so we can re-render when the selected pen changes
            ListChangeListener<ObservableDrawingPen> drawingPenListChangeListener = c -> {
                if(getViewport() != null){
                    getViewport().getRenderFlags().setFlag(Flags.FORCE_REDRAW, true);
                }
            };

            JFXUtils.subscribeListener(DrawingBotV3.INSTANCE.projectSelectedPens, (observable, oldValue, newValue) -> {
                if(oldValue != null){
                    oldValue.removeListener(drawingPenListChangeListener);
                }
                if(newValue != null){
                    newValue.addListener(drawingPenListChangeListener);
                }
                if(getViewport() != null){
                    getViewport().getRenderFlags().setFlag(Flags.FORCE_REDRAW, true);
                }
            });

        }

        @Override
        public String getName() {
            return "Selected pen";
        }
    }
}
