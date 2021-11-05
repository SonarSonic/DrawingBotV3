package drawingbot.render.jfx;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.ConfigFileHandler;
import drawingbot.geom.basic.IGeometry;
import drawingbot.image.ImageFilteringTask;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.plotting.PlottingTask;
import drawingbot.plotting.SplitPlottingTask;
import drawingbot.render.AbstractRenderer;
import drawingbot.utils.EnumDisplayMode;
import drawingbot.utils.EnumTaskStage;
import drawingbot.utils.GridOverlay;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import org.jfree.fx.FXGraphics2D;

public class JavaFXRenderer extends AbstractRenderer {

    ///

    public static int vertexRenderLimitNormal = 20000;
    public static int vertexRenderLimitBlendMode = 5000;
    public static int defaultMinTextureSize = 1024;
    public static int defaultMaxTextureSize = 4096;

    public double canvasScaling = 1F;

    ///

    public Canvas canvas;
    public GraphicsContext graphicsFX;
    public FXGraphics2D graphicsAWT;

    ///

    public int renderedLines = 0;
    private long drawingTime = 0;
    private PlottingTask lastDrawn = null;
    private EnumTaskStage lastState = null;
    private EnumDisplayMode lastMode = null;
    public boolean canvasNeedsUpdate = false;

    ///

    private boolean changedTask, changedMode, changedState, shouldRedraw;

    private ImageFilteringTask filteringTask;


    @Override
    public void forceCanvasUpdate(){
        canvasNeedsUpdate = true;
    }

    @Override
    public void init(){
        canvas = new Canvas(500, 500);
        graphicsFX = canvas.getGraphicsContext2D();
        graphicsAWT = new FXGraphics2D(canvas.getGraphicsContext2D());


        DrawingBotV3.INSTANCE.controller.viewportStackPane.getChildren().add(canvas);

        //DrawingBotV3.INSTANCE.controller.viewportStackPane.minWidthProperty().bind(Bindings.createDoubleBinding(() -> canvas.getWidth() * Math.max(1, canvas.getScaleX()), canvas.widthProperty(), canvas.scaleXProperty()));
       // DrawingBotV3.INSTANCE.controller.viewportStackPane.minHeightProperty().bind(Bindings.createDoubleBinding(() -> canvas.getHeight() * Math.max(1, canvas.getScaleY()), canvas.heightProperty(), canvas.scaleYProperty()));

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// RENDERING

    public void draw() {
        long startTime = System.currentTimeMillis();

        preRender();
        render();
        postRender();

        long endTime = System.currentTimeMillis();
        long lastDrawTick = (endTime - startTime);
        if(lastDrawTick > 1000/60){
            DrawingBotV3.logger.finest("DRAWING PHASE TOOK TOO LONG: " + lastDrawTick + " milliseconds" + " expected " + 1000/60);
        }
    }


    ///

    private void preRender(){

        PlottingTask renderedTask = DrawingBotV3.INSTANCE.getRenderedTask();

        //update the flags from the last render
        changedTask = lastDrawn != renderedTask;
        changedMode = lastMode != DrawingBotV3.INSTANCE.display_mode.get();
        changedState = renderedTask != null && lastState != renderedTask.stage;
        shouldRedraw = markRenderDirty || changedTask || changedMode || changedState;
        canvasNeedsUpdate = canvasNeedsUpdate || lastMode == null || lastMode.type != DrawingBotV3.INSTANCE.display_mode.get().type;

        //reset canvas scaling
        graphicsFX.setTransform(1, 0, 0, 1, 0, 0);

        switch (DrawingBotV3.INSTANCE.display_mode.get().type){
            case IMAGE:
                ///we load the image, resize the canvas and redraw
                if(DrawingBotV3.INSTANCE.loadingImage != null && DrawingBotV3.INSTANCE.loadingImage.isDone()){
                    DrawingBotV3.INSTANCE.openImage.set(DrawingBotV3.INSTANCE.loadingImage.getValue());
                    shouldRedraw = true;
                    DrawingBotV3.INSTANCE.loadingImage = null;
                    canvasNeedsUpdate = true;
                }
                if(DrawingBotV3.INSTANCE.openImage.get() != null){
                    if(imageFiltersChanged || imageFilterDirty || croppingDirty){
                        if(filteringTask == null || !filteringTask.updating.get()){
                            DrawingBotV3.INSTANCE.openImage.get().updateCropping = croppingDirty;
                            DrawingBotV3.INSTANCE.openImage.get().updateAllFilters = imageFiltersChanged;
                            imageFiltersChanged = false;
                            imageFilterDirty = false;
                            croppingDirty = false;
                            DrawingBotV3.INSTANCE.imageFilteringService.submit(filteringTask = new ImageFilteringTask(DrawingBotV3.INSTANCE.openImage.get()));
                        }
                    }
                    //resize the canvas
                    if(canvasNeedsUpdate){
                        updateCanvasSize(DrawingBotV3.INSTANCE.openImage.get().resolution.getScaledWidth(), DrawingBotV3.INSTANCE.openImage.get().resolution.getScaledHeight());
                        updateCanvasScaling();
                        canvasNeedsUpdate = false;
                        shouldRedraw = true; //force redraw
                    }
                }
                break;
            case TASK:
                //if the task has changed the images size will also have changed
                if(changedTask){
                    canvasNeedsUpdate = true;
                }
                //we will only update the canvas when there is a correct print scale
                if(canvasNeedsUpdate && renderedTask != null && renderedTask.resolution.getPrintScale() > 0){
                    updateCanvasSize(renderedTask.resolution.getScaledWidth(), renderedTask.resolution.getScaledHeight());
                    updateCanvasScaling();
                    canvasNeedsUpdate = false;
                    shouldRedraw = true; //force redraw
                }
                break;
        }

        updateCanvasScaling();
        graphicsFX.setImageSmoothing(false);
        graphicsFX.setLineCap(StrokeLineCap.ROUND);
        graphicsFX.setLineJoin(StrokeLineJoin.ROUND);
        graphicsFX.setGlobalBlendMode(BlendMode.SRC_OVER);
        graphicsFX.save();
    }

    private void postRender(){
        graphicsFX.restore();

        PlottingTask renderedTask = DrawingBotV3.INSTANCE.getRenderedTask();
        markRenderDirty = false;
        lastDrawn = renderedTask;
        lastMode = DrawingBotV3.INSTANCE.display_mode.get();
        lastState = renderedTask == null ? null : renderedTask.stage;
    }

    private void render() {
        PlottingTask renderedTask = DrawingBotV3.INSTANCE.getRenderedTask();
        switch (DrawingBotV3.INSTANCE.display_mode.get()){
            case IMAGE:
                if(DrawingBotV3.INSTANCE.openImage.get() != null){
                    if(shouldRedraw){
                        clearCanvas();
                        graphicsFX.scale(canvasScaling, canvasScaling);
                        graphicsFX.translate(DrawingBotV3.INSTANCE.openImage.get().resolution.getScaledOffsetX(), DrawingBotV3.INSTANCE.openImage.get().resolution.getScaledOffsetY());
                        graphicsFX.drawImage(SwingFXUtils.toFXImage(DrawingBotV3.INSTANCE.openImage.get().getFiltered(), null), 0, 0);
                    }
                }else{
                    clearCanvas();
                }
                break;
            case SELECTED_PEN:
            case DRAWING:
                if(renderedTask != null){
                    renderPlottingTask(renderedTask);
                }else if(shouldRedraw){
                    clearCanvas();
                }
                break;
            case ORIGINAL:
                if(shouldRedraw){
                    clearCanvas();
                    if(renderedTask != null && renderedTask.getOriginalImage() != null){
                        float screen_scale_x = (float)renderedTask.imgPlotting.getWidth() / (float)renderedTask.imgOriginal.getWidth();
                        float screen_scale_y = (float)renderedTask.imgPlotting.getHeight() / (float)renderedTask.imgOriginal.getHeight();
                        float screen_scale = Math.min(screen_scale_x, screen_scale_y);

                        graphicsFX.scale(canvasScaling, canvasScaling);
                        graphicsFX.translate(renderedTask.resolution.imageOffsetX, renderedTask.resolution.imageOffsetY);
                        graphicsFX.scale(screen_scale, screen_scale);
                        graphicsFX.drawImage(SwingFXUtils.toFXImage(renderedTask.getOriginalImage(), null), 0, 0);
                    }
                }
                break;
            case REFERENCE:
                if(shouldRedraw){
                    clearCanvas();
                    if(renderedTask != null && renderedTask.getReferenceImage() != null){
                        graphicsFX.scale(canvasScaling, canvasScaling);
                        graphicsFX.translate(DrawingBotV3.INSTANCE.openImage.get().resolution.getScaledOffsetX(), DrawingBotV3.INSTANCE.openImage.get().resolution.getScaledOffsetY());
                        graphicsFX.drawImage(SwingFXUtils.toFXImage(renderedTask.getReferenceImage(), null), 0, 0);
                    }
                }
                break;
            case LIGHTENED:
                if(shouldRedraw){
                    clearCanvas();
                    if(renderedTask != null && renderedTask.getPlottingImage() != null){
                        graphicsFX.scale(canvasScaling, canvasScaling);
                        graphicsFX.translate(DrawingBotV3.INSTANCE.openImage.get().resolution.getScaledOffsetX(), DrawingBotV3.INSTANCE.openImage.get().resolution.getScaledOffsetY());
                        graphicsFX.drawImage(SwingFXUtils.toFXImage(renderedTask.getPlottingImage(), null), 0, 0);
                    }
                }
                break;
        }

        if(shouldRedraw){
            GridOverlay.grid();
        }
    }

    public void renderPlottingTask(PlottingTask renderedTask){
        switch (renderedTask.stage){
            case QUEUED:
            case PRE_PROCESSING:
                break;
            case DO_PROCESS:
                if(!(renderedTask instanceof SplitPlottingTask)){
                    if(changedTask || changedState || changedMode){ //avoids redrawing in some instances
                        clearCanvas();
                        renderedLines = 0;
                    }
                    if(renderedTask.plottedDrawing.getGeometryCount() != 0){
                        graphicsFX.scale(canvasScaling, canvasScaling);
                        graphicsFX.translate(renderedTask.resolution.getScaledOffsetX(), renderedTask.resolution.getScaledOffsetY());
                        renderedLines = renderedTask.plottedDrawing.renderGeometryFX(graphicsFX, renderedLines, renderedTask.plottedDrawing.getGeometryCount(), IGeometry.DEFAULT_FILTER, getVertexRenderLimit(), false);
                    }
                }else{
                    SplitPlottingTask splitPlottingTask = (SplitPlottingTask) renderedTask;
                    if(changedTask || changedState || changedMode){ //avoids redrawing in some instances
                        clearCanvas();
                        splitPlottingTask.renderedLines = new int[splitPlottingTask.splitter.getSplitCount()];
                    }
                    if(splitPlottingTask.subTasks != null){
                        graphicsFX.scale(canvasScaling, canvasScaling);
                        graphicsFX.translate(renderedTask.resolution.getScaledOffsetX(), renderedTask.resolution.getScaledOffsetY());
                        for(int i = 0; i < splitPlottingTask.splitter.getSplitCount(); i ++){
                            PlottingTask task = splitPlottingTask.subTasks.get(i);
                            splitPlottingTask.renderedLines[i] = task.plottedDrawing.renderGeometryFX(graphicsFX, splitPlottingTask.renderedLines[i], task.plottedDrawing.getGeometryCount(), IGeometry.DEFAULT_FILTER, getVertexRenderLimit() / splitPlottingTask.splitter.getSplitCount(), false);
                        }
                    }
                }
                break;
            case POST_PROCESSING:
            case FINISHING:
                // NOP - continue displaying the path finding result
                break;
            case FINISHED:
                EnumBlendMode blendMode = renderedTask.plottedDrawing.drawingPenSet.blendMode.get();
                if(shouldRedraw){
                    clearCanvas(blendMode.additive ? Color.BLACK : Color.WHITE);
                    renderedLines = renderedTask.plottedDrawing.getDisplayedGeometryCount()-1;
                    DrawingBotV3.INSTANCE.updateLocalMessage("Drawing");
                    DrawingBotV3.INSTANCE.updateLocalProgress(0);
                    drawingTime = System.currentTimeMillis();
                }
                if(renderedLines != -1){
                    graphicsFX.scale(canvasScaling, canvasScaling);
                    graphicsFX.translate(renderedTask.resolution.getScaledOffsetX(), renderedTask.resolution.getScaledOffsetY());
                    graphicsFX.setGlobalBlendMode(blendMode.javaFXVersion);

                    IGeometryFilter pointFilter = DrawingBotV3.INSTANCE.display_mode.get() == EnumDisplayMode.SELECTED_PEN ? IGeometry.SELECTED_PEN_FILTER : IGeometry.DEFAULT_FILTER;
                    renderedLines = renderedTask.plottedDrawing.renderGeometryFX(graphicsFX, 0, renderedLines, pointFilter, getVertexRenderLimit(),true);

                    int end = renderedTask.plottedDrawing.getDisplayedGeometryCount()-1;
                    DrawingBotV3.INSTANCE.updateLocalProgress((float)(end-renderedLines) / end);

                    if(renderedLines == 0){
                        long time = System.currentTimeMillis();
                        DrawingBotV3.logger.finest("Drawing Took: " + (time-drawingTime) + " ms");
                        renderedLines = -1;
                    }
                }
                break;
        }
    }

    public void clearProcessRendering(){
        Platform.runLater(() -> {
            if(DrawingBotV3.INSTANCE.getRenderedTask() instanceof SplitPlottingTask){
                SplitPlottingTask splitPlottingTask = (SplitPlottingTask) DrawingBotV3.INSTANCE.getRenderedTask();
                splitPlottingTask.renderedLines = new int[splitPlottingTask.splitter.getSplitCount()];
            }else{
                renderedLines = 0;
            }
            if(lastMode == EnumDisplayMode.DRAWING){
                clearCanvas();
            }
        });
    }

    public void clearCanvas(){
        clearCanvas(Color.WHITE);
    }

    public void clearCanvas(Color color){
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); //ensures the canva's buffer is always cleared, some blend modes will prevent fillRect from triggering this
        canvas.getGraphicsContext2D().setFill(color);
        canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void updateCanvasSize(double width, double height){
        if(width > getMaxTextureSize() || height > getMaxTextureSize()){
            double max = Math.max(width, height);
            canvasScaling = getMaxTextureSize() / max;
            width = Math.floor(width*canvasScaling);
            height = Math.floor(height*canvasScaling);
        }else if(width < getMinTextureSize() || height < getMinTextureSize()){
            double max = Math.max(width, height);
            double newScaling = getMinTextureSize() / max;
            double newWidth = Math.floor(width*newScaling);
            double newHeight = Math.floor(height*newScaling);
            if(newWidth > width && newHeight > height){ //sanity check, prevents scaling down images where one side is under and one is over the limit
                canvasScaling = newScaling;
                width = newWidth;
                height = newHeight;
            }else{
                canvasScaling = 1;
            }
        }else{
            canvasScaling = 1;
        }

        if(canvas.getWidth() == width && canvas.getHeight() == height){
            return;
        }
        canvas.widthProperty().setValue(width);
        canvas.heightProperty().setValue(height);

        Platform.runLater(() -> {
            DrawingBotV3.INSTANCE.controller.viewportScrollPane.setHvalue(0.5);
            DrawingBotV3.INSTANCE.controller.viewportScrollPane.setVvalue(0.5);
        });
        clearCanvas();//wipe the canvas
    }

    public void updateCanvasScaling(){
        double screen_scale_x = DrawingBotV3.INSTANCE.controller.viewportScrollPane.getWidth() / ((float) canvas.getWidth());
        double screen_scale_y = DrawingBotV3.INSTANCE.controller.viewportScrollPane.getHeight() / ((float) canvas.getHeight());
        double screen_scale = Math.min(screen_scale_x, screen_scale_y) * DrawingBotV3.INSTANCE.scaleMultiplier.doubleValue();
        if(canvas.getScaleX() != screen_scale){
            canvas.setScaleX(screen_scale);
            canvas.setScaleY(screen_scale);
        }
    }



    public int getMinTextureSize(){
        return defaultMinTextureSize;
    }

    public int getMaxTextureSize(){
        if(ConfigFileHandler.getApplicationSettings().maxTextureSize != -1){
            return ConfigFileHandler.getApplicationSettings().maxTextureSize;
        }
        return defaultMaxTextureSize;
    }

    public int getVertexRenderLimit(){
        return graphicsFX.getGlobalBlendMode() == BlendMode.SRC_OVER ? vertexRenderLimitNormal : vertexRenderLimitBlendMode;
    }

}
