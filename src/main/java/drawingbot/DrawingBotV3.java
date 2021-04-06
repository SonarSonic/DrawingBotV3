/*
  DrawingBotV3 by Ollie Lansdell <ollielansdell@hotmail.co.uk
  Original by Scott Cooper, Dullbits.com, <scottslongemailaddress@gmail.com>
 */
package drawingbot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import drawingbot.api.IGeometryFilter;
import drawingbot.drawing.ObservableDrawingSet;
import drawingbot.files.*;
import drawingbot.geom.basic.IGeometry;
import drawingbot.image.BufferedImageLoader;
import drawingbot.image.FilteredBufferedImage;
import drawingbot.image.ImageFilteringTask;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.image.filters.ObservableImageFilter;
import drawingbot.javafx.FXController;
import drawingbot.javafx.TaskMonitor;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.SplitPlottingTask;
import drawingbot.utils.*;
import drawingbot.plotting.PlottingTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import org.jfree.fx.FXGraphics2D;

public class DrawingBotV3 {

    public static final Logger logger = Logger.getLogger("DrawingBotV3");
    public static DrawingBotV3 INSTANCE;

    //DRAWING AREA
    public final SimpleBooleanProperty useOriginalSizing = new SimpleBooleanProperty(true);
    public final SimpleObjectProperty<EnumScalingMode> scalingMode = new SimpleObjectProperty<>(EnumScalingMode.CROP_TO_FIT);
    public final SimpleObjectProperty<Units> inputUnits = new SimpleObjectProperty<>(Units.MILLIMETRES);

    public final SimpleFloatProperty drawingAreaWidth = new SimpleFloatProperty(0);
    public final SimpleFloatProperty drawingAreaHeight = new SimpleFloatProperty(0);
    public final SimpleFloatProperty drawingAreaPaddingLeft = new SimpleFloatProperty(0);
    public final SimpleFloatProperty drawingAreaPaddingRight = new SimpleFloatProperty(0);
    public final SimpleFloatProperty drawingAreaPaddingTop = new SimpleFloatProperty(0);
    public final SimpleFloatProperty drawingAreaPaddingBottom = new SimpleFloatProperty(0);
    public final SimpleStringProperty drawingAreaPaddingGang = new SimpleStringProperty("0");

    public final SimpleBooleanProperty optimiseForPrint = new SimpleBooleanProperty(true);
    public final SimpleFloatProperty targetPenWidth = new SimpleFloatProperty(0.5F);

    //VPYPE SETTINGS
    public final SimpleStringProperty vPypeExecutable = new SimpleStringProperty();
    public final SimpleStringProperty vPypeCommand = new SimpleStringProperty();
    public final SimpleBooleanProperty vPypeBypassOptimisation = new SimpleBooleanProperty();

    //GCODE SETTINGS
    public final SimpleFloatProperty gcodeOffsetX = new SimpleFloatProperty(0);
    public final SimpleFloatProperty gcodeOffsetY = new SimpleFloatProperty(0);
    public final SimpleObjectProperty<EnumDirection> gcodeXDirection = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<EnumDirection> gcodeYDirection = new SimpleObjectProperty<>();
    public final SimpleStringProperty gcodeStartCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodeEndCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodePenDownCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodePenUpCode = new SimpleStringProperty();

    //PRE-PROCESSING\\
    public final ObservableList<ObservableImageFilter> currentFilters = FXCollections.observableArrayList();

    //PATH FINDING \\
    public final SimpleObjectProperty<PFMFactory<?>> pfmFactory = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<EnumColourSplitter> colourSplitter = new SimpleObjectProperty<>();

    // PEN SETS \\
    public final SimpleBooleanProperty observableDrawingSetFlag = new SimpleBooleanProperty(false); //just a marker flag can be binded
    public ObservableDrawingSet observableDrawingSet = null;

    // DISPLAY \\
    public final SimpleObjectProperty<EnumDisplayMode> display_mode = new SimpleObjectProperty<>(EnumDisplayMode.IMAGE);

    //VIEWPORT SETTINGS \\
    public static int SVG_DPI = 96;
    public static int vertexRenderLimitNormal = 20000;
    public static int vertexRenderLimitBlendMode = 5000;
    public static int defaultMinTextureSize = 1024;
    public static int defaultMaxTextureSize = 4096;
    public static double minScale = 0.1;

    public final SimpleBooleanProperty displayGrid = new SimpleBooleanProperty(false);
    public final SimpleDoubleProperty scaleMultiplier = new SimpleDoubleProperty(1.0F);
    public double canvasScaling = 1F;
    public boolean imageFiltersDirty = false;
    public boolean drawingAreaDirty = false;

    //// VARIABLES \\\\

    // THREADS \\
    public ExecutorService taskService = initTaskService();
    public ExecutorService backgroundService = initBackgroundService();
    public ExecutorService imageFilteringService = initImageFilteringService();

    public TaskMonitor taskMonitor = new TaskMonitor(taskService);

    // TASKS \\
    public final SimpleObjectProperty<FilteredBufferedImage> openImage = new SimpleObjectProperty<>(null);
    public final SimpleObjectProperty<PlottingTask> activeTask = new SimpleObjectProperty<>(null);

    public PlottingTask renderedTask = null; //for tasks which generate sub tasks e.g. colour splitter, batch processing
    public BatchProcessingTask batchProcessingTask = null; //TODO REMOVE ME?

    public BufferedImageLoader.Filtered loadingImage = null;
    public File openFile = null;
    public boolean isUpdatingFilters = false;

    // GUI \\
    public FXController controller;
    public Canvas canvas;
    public GraphicsContext graphicsFX;
    public FXGraphics2D graphicsAWT;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public DrawingBotV3() {}

    public float getDrawingAreaWidthMM(){
        return drawingAreaWidth.getValue() * inputUnits.get().convertToMM;
    }

    public float getDrawingAreaHeightMM(){
        return drawingAreaHeight.getValue() * inputUnits.get().convertToMM;
    }

    public float getDrawingWidthMM(){
        return (drawingAreaWidth.getValue() - drawingAreaPaddingLeft.get() - drawingAreaPaddingRight.get()) * inputUnits.get().convertToMM;
    }

    public float getDrawingHeightMM(){
        return (drawingAreaHeight.getValue() - drawingAreaPaddingTop.get() - drawingAreaPaddingBottom.get()) * inputUnits.get().convertToMM;
    }

    public float getDrawingOffsetXMM(){
        return drawingAreaPaddingLeft.get() * inputUnits.get().convertToMM;
    }

    public float getDrawingOffsetYMM(){
        return drawingAreaPaddingTop.get() * inputUnits.get().convertToMM;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    private Double localProgress = null;
    private String localMessage = null;

    public void updateLocalMessage(String message){
        localMessage = message;
    }

    public void updateLocalProgress(double progress){
        localProgress = progress;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// RENDERING

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

    public void reRender(){
        markRenderDirty = true;
    }

    public void draw() {
        long startTime = System.currentTimeMillis();

        preRender();
        render();
        postRender();
        updateUI();

        long endTime = System.currentTimeMillis();
        long lastDrawTick = (endTime - startTime);
        if(lastDrawTick > 1000/60){
            DrawingBotV3.logger.finest("DRAWING PHASE TOOK TOO LONG: " + lastDrawTick + " milliseconds" + " expected " + 1000/60);
        }
    }

    public EnumDistributionType updateDistributionType = null;

    public int renderedLines = 0;
    private long drawingTime = 0;
    private PlottingTask lastDrawn = null;
    private EnumTaskStage lastState = null;
    private EnumDisplayMode lastMode = null;
    public boolean canvasNeedsUpdate = false;
    
    private boolean markRenderDirty = true, changedTask, changedMode, changedState, shouldRedraw;

    private void preRender(){

        PlottingTask renderedTask = getRenderedTask();

        //update the flags from the last render
        changedTask = lastDrawn != renderedTask;
        changedMode = lastMode != display_mode.get();
        changedState = renderedTask != null && lastState != renderedTask.stage;
        shouldRedraw = markRenderDirty || changedTask || changedMode || changedState;
        canvasNeedsUpdate = canvasNeedsUpdate || lastMode == null || lastMode.type != display_mode.get().type;

        switch (display_mode.get().type){
            case IMAGE:
                ///we load the image, resize the canvas and redraw
                if(loadingImage != null && loadingImage.isDone()){
                    openImage.set(loadingImage.getValue());
                    shouldRedraw = true;
                    loadingImage = null;
                    canvasNeedsUpdate = true;
                }
                if(openImage.get() != null){
                    if(imageFiltersDirty || drawingAreaDirty){
                        if(!isUpdatingFilters){
                            isUpdatingFilters = true;
                            imageFiltersDirty = false;
                            drawingAreaDirty = false;
                            imageFilteringService.submit(new ImageFilteringTask(openImage.get()));
                        }
                    }
                    //resize the canvas
                    if(canvasNeedsUpdate){
                        updateCanvasSize(openImage.get().resolution.getScaledWidth(), openImage.get().resolution.getScaledHeight());
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
        graphicsFX.setGlobalBlendMode(BlendMode.SRC_OVER);
        graphicsFX.save();
    }

    private void postRender(){
        graphicsFX.restore();

        PlottingTask renderedTask = getRenderedTask();
        markRenderDirty = false;
        lastDrawn = renderedTask;
        lastMode = display_mode.get();
        lastState = renderedTask == null ? null : renderedTask.stage;
    }

    private void render() {
        PlottingTask renderedTask = getRenderedTask();
        switch (display_mode.get()){
            case IMAGE:
                if(openImage.get() != null){
                    if(shouldRedraw){
                        clearCanvas();
                        graphicsFX.scale(canvasScaling, canvasScaling);
                        graphicsFX.translate(openImage.get().resolution.getScaledOffsetX(), openImage.get().resolution.getScaledOffsetY());
                        graphicsFX.drawImage(SwingFXUtils.toFXImage(openImage.get().getFiltered(), null), 0, 0);
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
                        float screen_scale_x = (float)renderedTask.img_plotting.getWidth() / (float)renderedTask.img_original.getWidth();
                        float screen_scale_y = (float)renderedTask.img_plotting.getHeight() / (float)renderedTask.img_original.getHeight();
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
                        graphicsFX.translate(openImage.get().resolution.getScaledOffsetX(), openImage.get().resolution.getScaledOffsetY());
                        graphicsFX.drawImage(SwingFXUtils.toFXImage(renderedTask.getReferenceImage(), null), 0, 0);
                    }
                }
                break;
            case LIGHTENED:
                if(shouldRedraw){
                    clearCanvas();
                    if(renderedTask != null && renderedTask.getPlottingImage() != null){
                        graphicsFX.scale(canvasScaling, canvasScaling);
                        graphicsFX.translate(openImage.get().resolution.getScaledOffsetX(), openImage.get().resolution.getScaledOffsetY());
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
                    updateLocalMessage("Drawing");
                    updateLocalProgress(0);
                    drawingTime = System.currentTimeMillis();
                }
                if(renderedLines != -1){
                    graphicsFX.scale(canvasScaling, canvasScaling);
                    graphicsFX.translate(renderedTask.resolution.getScaledOffsetX(), renderedTask.resolution.getScaledOffsetY());
                    graphicsFX.setGlobalBlendMode(blendMode.javaFXVersion);

                    IGeometryFilter pointFilter = display_mode.get() == EnumDisplayMode.SELECTED_PEN ? IGeometry.SELECTED_PEN_FILTER : IGeometry.DEFAULT_FILTER;
                    renderedLines = renderedTask.plottedDrawing.renderGeometryFX(graphicsFX, 0, renderedLines, pointFilter, getVertexRenderLimit(),true);

                    int end = renderedTask.plottedDrawing.getDisplayedGeometryCount()-1;
                    updateLocalProgress((float)(end-renderedLines) / end);

                    if(renderedLines == 0){
                        long time = System.currentTimeMillis();
                        logger.finest("Drawing Took: " + (time-drawingTime) + " ms");
                        renderedLines = -1;
                    }
                }
                break;
        }
    }

    public void clearProcessRendering(){
        Platform.runLater(() -> {
            if(getRenderedTask() instanceof SplitPlottingTask){
                SplitPlottingTask splitPlottingTask = (SplitPlottingTask) getRenderedTask();
                splitPlottingTask.renderedLines = new int[splitPlottingTask.splitter.getSplitCount()];
            }else{
                DrawingBotV3.INSTANCE.renderedLines = 0;
            }
            DrawingBotV3.INSTANCE.clearCanvas();
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
            controller.viewportScrollPane.setHvalue(0.5);
            controller.viewportScrollPane.setVvalue(0.5);
        });
        clearCanvas();//wipe the canvas
    }

    public void updateCanvasScaling(){
        double screen_scale_x = controller.viewportScrollPane.getWidth() / ((float) canvas.getWidth());
        double screen_scale_y = controller.viewportScrollPane.getHeight() / ((float) canvas.getHeight());
        double screen_scale = Math.min(screen_scale_x, screen_scale_y) * scaleMultiplier.doubleValue();
        if(canvas.getScaleX() != screen_scale){
            canvas.setScaleX(screen_scale);
            canvas.setScaleY(screen_scale);
        }
    }


    public void updateUI(){

        //TODO MAKE THIS AUTOMATED PER TASK - KEEP TRACK OF ALL OF THEM MULTIPLE TASKS ON SAME PROGRESS BAR?????

        //String prefix = batchProcessingTask == null ? "" : batchProcessingTask.getTitle() + " - ";
        if(getActiveTask() != null && getActiveTask().isRunning()){
            int geometryCount = getActiveTask().plottedDrawing.getGeometryCount();
            long vertexCount = getActiveTask().plottedDrawing.getVertexCount();

            if(getActiveTask() instanceof SplitPlottingTask){
                geometryCount = ((SplitPlottingTask) getActiveTask()).getCurrentGeometryCount();
                vertexCount = ((SplitPlottingTask) getActiveTask()).getCurrentVertexCount();
            }
            controller.labelPlottedShapes.setText(Utils.defaultNF.format(geometryCount));
            controller.labelPlottedVertices.setText(Utils.defaultNF.format(vertexCount));
            controller.labelElapsedTime.setText(getActiveTask().getElapsedTime()/1000 + " s");
        }
        /*
        else if(exportTask != null){
            controller.progressBarGeneral.setProgress(exportTask.progressProperty().get());
            controller.progressBarLabel.setText(prefix + exportTask.titleProperty().get());
        }else if(vPypeTask != null){
            controller.progressBarGeneral.setProgress(vPypeTask.progressProperty().get());
            controller.progressBarLabel.setText(prefix + vPypeTask.titleProperty().get());
        }else{
            if(localProgress != null){
                controller.progressBarGeneral.setProgress(localProgress);
                localProgress = null;
            }
            if(localMessage != null){
                controller.progressBarLabel.setText(localMessage);
                localMessage = null;
            }
        }
         */
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    ////// EVENTS

    public void onPlottingTaskStageFinished(PlottingTask task, EnumTaskStage stage){
        switch (stage){
            case QUEUED:
                break;
            case PRE_PROCESSING:
                Platform.runLater(() -> display_mode.setValue(EnumDisplayMode.DRAWING));
                break;
            case DO_PROCESS:
                Platform.runLater(() -> {
                    controller.sliderDisplayedLines.setValue(1.0F);
                    controller.textFieldDisplayedLines.setText(String.valueOf(task.plottedDrawing.getGeometryCount()));
                    controller.labelPlottedShapes.setText(Utils.defaultNF.format(task.plottedDrawing.getGeometryCount()));
                    controller.labelPlottedVertices.setText(Utils.defaultNF.format(task.plottedDrawing.getVertexCount()));
                });
                break;
            case POST_PROCESSING:
                break;
            case FINISHING:
                break;
            case FINISHED:
                break;
        }
       logger.info("Plotting Task: Finished Stage " + stage.name());
    }

    public void onDrawingAreaChanged(){
        drawingAreaDirty = true;
    }

    public void onDrawingPenChanged(){
        updatePenDistribution();
    }

    public void onDrawingSetChanged(){
        updatePenDistribution();
        observableDrawingSetFlag.set(!observableDrawingSetFlag.get());
    }

    public void onImageFiltersChanged(){
        imageFiltersDirty = true;
    }

    public void updatePenDistribution(){
        if(activeTask.get() != null && activeTask.get().isTaskFinished()){
            activeTask.get().plottedDrawing.updatePenDistribution();
            reRender();
        }
    }

    //// PLOTTING TASKS

    public PlottingTask initPlottingTask(PFMFactory<?> pfmFactory, ObservableDrawingSet drawingPenSet, BufferedImage image, File originalFile, EnumColourSplitter splitter){
        //only update the distribution type the first time the PFM is changed, also only trigger the update when Start Plotting is hit again, so the current drawing doesn't get re-rendered
        Platform.runLater(() -> {
            if(updateDistributionType != null && colourSplitter.get() == EnumColourSplitter.DEFAULT){
                drawingPenSet.distributionType.set(updateDistributionType);
                updateDistributionType = null;
            }
        });
        return colourSplitter.get() == EnumColourSplitter.DEFAULT ? new PlottingTask(pfmFactory, drawingPenSet, image, originalFile) : new SplitPlottingTask(pfmFactory, drawingPenSet, image, originalFile, splitter);
    }

    public void startPlotting(){
        if(activeTask.get() != null){
            activeTask.get().cancel();
        }
        if(openImage.get() != null){
            taskMonitor.queueTask(initPlottingTask(pfmFactory.get(), observableDrawingSet, openImage.get().getSource(), openFile, colourSplitter.get()));
        }
    }

    public void stopPlotting(){
        if(activeTask.get() != null){
            activeTask.get().stopElegantly();
        }
    }

    public void resetPlotting(){
        taskService.shutdownNow();
        setActivePlottingTask(null);
        taskService = initTaskService();
        taskMonitor.resetMonitor(taskService);

        localProgress = 0D;
        localMessage = "";
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void openImage(File file, boolean internal){
        if(activeTask.get() != null){
            activeTask.get().cancel();
            setActivePlottingTask(null);
            openImage.set(null);
            loadingImage = null;
        }
        openFile = file;
        loadingImage = new BufferedImageLoader.Filtered(file.getAbsolutePath(), internal);
        taskMonitor.queueTask(loadingImage);

        FXApplication.primaryStage.setTitle(DBConstants.appName + ", Version: " + DBConstants.appVersion + ", '" + file.getName() + "'");
    }

    public void setActivePlottingTask(PlottingTask task){
        if(activeTask.get() != null){
            activeTask.get().reset(); //help GC by removing references to PlottedLines
        }
        activeTask.set(task);

        if(activeTask.get() == null){
            display_mode.setValue(EnumDisplayMode.IMAGE);
        }
    }

    public PlottingTask getActiveTask(){
        return activeTask.get();
    }

    public PlottingTask getRenderedTask(){
        return renderedTask == null ? activeTask.get() : renderedTask;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// EXPORT TASKS

    public ExportTask createExportTask(ExportFormats format, PlottingTask plottingTask, IGeometryFilter pointFilter, String extension, File saveLocation, boolean seperatePens, boolean forceBypassOptimisation){
        ExportTask task = new ExportTask(format, plottingTask, pointFilter, extension, saveLocation, seperatePens, true, forceBypassOptimisation);
        taskMonitor.queueTask(task);
        return task;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// INTERACTION EVENTS

    private boolean ctrl_down = false;

    public void keyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.CONTROL) { ctrl_down = false; }
    }

    public void keyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.CONTROL) { ctrl_down = true; }
        if (event.getCode() == KeyCode.X) { GridOverlay.mouse_point(); }

        if (event.getCode().isArrowKey()) {
            double delta = 0.01;
            double currentH = controller.viewportScrollPane.getHvalue();
            double currentV = controller.viewportScrollPane.getVvalue();
            if (event.getCode() == KeyCode.UP)    {
                controller.viewportScrollPane.setVvalue(currentV - delta);
            }
            if (event.getCode() == KeyCode.DOWN)  {
                controller.viewportScrollPane.setVvalue(currentV + delta);
            }
            if (event.getCode() == KeyCode.RIGHT) {
                controller.viewportScrollPane.setHvalue(currentH - delta);
            }
            if (event.getCode() == KeyCode.LEFT)  {
                controller.viewportScrollPane.setHvalue(currentH + delta);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    double pressX = 0;
    double pressY = 0;
    double locX = 0;
    double locY = 0;

    public void mousePressedJavaFX(MouseEvent event) {    // record a delta distance for the drag and drop operation.
        pressX = event.getX();
        pressY = event.getY();
        locX = controller.viewportScrollPane.getHvalue();
        locY = controller.viewportScrollPane.getVvalue();
    }

    public void mouseDraggedJavaFX(MouseEvent event) {
        double relativeX = (pressX - event.getX()) / controller.viewportStackPane.getWidth();
        double relativeY = (pressY - event.getY()) / controller.viewportStackPane.getHeight();
        controller.viewportScrollPane.setHvalue(locX + relativeX);
        controller.viewportScrollPane.setVvalue(locY + relativeY);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// SERVICES

    public final Thread.UncaughtExceptionHandler exceptionHandler = (thread, throwable) -> {
        DrawingBotV3.logger.log(Level.SEVERE, "Thread Exception: " + thread.getName(), throwable);
    };

    public ExecutorService initTaskService(){
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Task Thread");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(exceptionHandler);
            return t;
        });
    }

    public ExecutorService initBackgroundService(){
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Background Thread");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(exceptionHandler);
            return t ;
        });
    }

    public ExecutorService initImageLoadingService(){
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Image Loading Thread");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(exceptionHandler);
            return t;
        });
    }

    public ExecutorService initImageFilteringService(){
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Image Filtering Thread");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(exceptionHandler);
            return t;
        });
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
}