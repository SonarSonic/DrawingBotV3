/*
  DrawingBotV3 by Ollie Lansdell <ollielansdell@hotmail.co.uk
  Original by Scott Cooper, Dullbits.com, <scottslongemailaddress@gmail.com>
 */
package drawingbot;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import drawingbot.api.IPointFilter;
import drawingbot.drawing.ObservableDrawingSet;
import drawingbot.files.BatchProcessingTask;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.ExportFormats;
import drawingbot.files.ExportTask;
import drawingbot.image.BufferedImageLoader;
import drawingbot.image.FilteredBufferedImage;
import drawingbot.image.ImageFilteringTask;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.image.filters.ObservableImageFilter;
import drawingbot.javafx.FXController;
import drawingbot.api.IPathFindingModule;
import drawingbot.javafx.GenericFactory;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.*;
import drawingbot.plotting.PlottedPoint;
import drawingbot.plotting.PlottingTask;
import javafx.application.Platform;
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
import org.jfree.fx.FXGraphics2D;

public class DrawingBotV3 {

    public static final Logger logger = Logger.getLogger("DrawingBotV3");
    public static DrawingBotV3 INSTANCE;

    //DRAWING AREA
    public SimpleBooleanProperty useOriginalSizing = new SimpleBooleanProperty(true);
    public SimpleObjectProperty<EnumScalingMode> scalingMode = new SimpleObjectProperty<>(EnumScalingMode.CROP_TO_FIT);
    public SimpleObjectProperty<Units> inputUnits = new SimpleObjectProperty<>(Units.MILLIMETRES);

    public SimpleFloatProperty drawingAreaWidth = new SimpleFloatProperty(0);
    public SimpleFloatProperty drawingAreaHeight = new SimpleFloatProperty(0);
    public SimpleFloatProperty drawingAreaPaddingLeft = new SimpleFloatProperty(0);
    public SimpleFloatProperty drawingAreaPaddingRight = new SimpleFloatProperty(0);
    public SimpleFloatProperty drawingAreaPaddingTop = new SimpleFloatProperty(0);
    public SimpleFloatProperty drawingAreaPaddingBottom = new SimpleFloatProperty(0);
    public SimpleStringProperty drawingAreaPaddingGang = new SimpleStringProperty("0");

    //GCODE SETTINGS
    public SimpleBooleanProperty enableAutoHome = new SimpleBooleanProperty(false);
    public SimpleFloatProperty gcodeOffsetX = new SimpleFloatProperty(0);
    public SimpleFloatProperty gcodeOffsetY = new SimpleFloatProperty(0);
    public SimpleFloatProperty penDownZ = new SimpleFloatProperty(0);
    public SimpleFloatProperty penUpZ = new SimpleFloatProperty(0);

    //PRE-PROCESSING\\
    public ObservableList<ObservableImageFilter> currentFilters = FXCollections.observableArrayList();

    //PATH FINDING \\
    public SimpleBooleanProperty isPlotting = new SimpleBooleanProperty(false);
    public SimpleObjectProperty<GenericFactory<IPathFindingModule>> pfmFactory = new SimpleObjectProperty<>();

    // PEN SETS \\
    public SimpleBooleanProperty observableDrawingSetFlag = new SimpleBooleanProperty(false); //just a marker flag can be binded
    public ObservableDrawingSet observableDrawingSet = null;

    // DISPLAY \\
    public SimpleObjectProperty<EnumDisplayMode> display_mode = new SimpleObjectProperty<>(EnumDisplayMode.IMAGE);

    //VIEWPORT SETTINGS \\
    public static int SVG_DPI = 96;
    public static int pointRenderLimitNormal = 20000;
    public static int pointRenderLimitBlendMode = 2000;
    public static int defaultMaxTextureSize = 4096;
    public static double minScale = 0.1;

    public SimpleBooleanProperty displayGrid = new SimpleBooleanProperty(false);
    public SimpleDoubleProperty scaleMultiplier = new SimpleDoubleProperty(1.0F);
    public double canvasScaling = 1F;
    public boolean imageFiltersDirty = false;
    public boolean drawingAreaDirty = false;

    //// VARIABLES \\\\

    // THREADS \\
    public ExecutorService taskService = initTaskService();
    public ExecutorService backgroundService = initBackgroundService();
    public ExecutorService imageLoadingService = initImageLoadingService();
    public ExecutorService imageFilteringService = initImageFilteringService();

    // TASKS \\
    public PlottingTask activeTask = null;
    public ExportTask exportTask = null;
    public BatchProcessingTask batchProcessingTask = null;

    public BufferedImageLoader.Filtered loadingImage = null;
    public FilteredBufferedImage openImage = null;
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

    public int getMaxTextureSize(){
        if(ConfigFileHandler.getApplicationSettings().maxTextureSize != -1){
            return ConfigFileHandler.getApplicationSettings().maxTextureSize;
        }
        return defaultMaxTextureSize;
    }

    public int getPointRenderLimit(){
        return graphicsFX.getGlobalBlendMode() == BlendMode.SRC_OVER ? pointRenderLimitNormal : pointRenderLimitBlendMode;
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


    public int renderedLines = 0;
    private long drawingTime = 0;
    private PlottingTask lastDrawn = null;
    private EnumTaskStage lastState = null;
    private EnumDisplayMode lastMode = null;
    public boolean canvasNeedsUpdate = false;
    
    private boolean markRenderDirty = true, changedTask, changedMode, changedState, shouldRedraw;

    private void preRender(){
        PlottingTask renderedTask = getActiveTask();

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
                    openImage = loadingImage.getValue();
                    shouldRedraw = true;
                    loadingImage = null;
                    canvasNeedsUpdate = true;
                }
                if(openImage != null){
                    if(imageFiltersDirty || drawingAreaDirty){
                        if(!isUpdatingFilters){
                            isUpdatingFilters = true;
                            imageFiltersDirty = false;
                            drawingAreaDirty = false;
                            imageFilteringService.submit(new ImageFilteringTask(openImage));
                        }
                    }
                    //resize the canvas
                    if(canvasNeedsUpdate){
                        updateCanvasSize(openImage.resolution.getScaledWidth(), openImage.resolution.getScaledHeight());
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

        PlottingTask renderedTask = getActiveTask();
        markRenderDirty = false;
        lastDrawn = renderedTask;
        lastMode = display_mode.get();
        lastState = renderedTask == null ? null : renderedTask.stage;
    }

    private void render() {
        PlottingTask renderedTask = getActiveTask();
        switch (display_mode.get()){
            case IMAGE:
                if(openImage != null){
                    if(shouldRedraw){
                        clearCanvas();
                        graphicsFX.scale(canvasScaling, canvasScaling);
                        graphicsFX.translate(openImage.resolution.getScaledOffsetX(), openImage.resolution.getScaledOffsetY());
                        graphicsFX.drawImage(SwingFXUtils.toFXImage(openImage.getFiltered(), null), 0, 0);
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
                        graphicsFX.translate(openImage.resolution.getScaledOffsetX(), openImage.resolution.getScaledOffsetY());
                        graphicsFX.drawImage(SwingFXUtils.toFXImage(renderedTask.getReferenceImage(), null), 0, 0);
                    }
                }
                break;
            case LIGHTENED:
                if(shouldRedraw){
                    clearCanvas();
                    if(renderedTask != null && renderedTask.getPlottingImage() != null){
                        graphicsFX.scale(canvasScaling, canvasScaling);
                        graphicsFX.translate(openImage.resolution.getScaledOffsetX(), openImage.resolution.getScaledOffsetY());
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
                if(changedTask || changedState || changedMode){ //avoids redrawing in some instances
                    clearCanvas();
                    renderedLines = 0;
                }
                if(renderedTask.plottedDrawing.getPlottedLineCount() != 0){
                    graphicsFX.scale(canvasScaling, canvasScaling);
                    graphicsFX.translate(renderedTask.resolution.getScaledOffsetX(), renderedTask.resolution.getScaledOffsetY());
                    renderedLines = renderedTask.plottedDrawing.renderPointsFX(graphicsFX, renderedLines, renderedTask.plottedDrawing.getPlottedLineCount(), PlottedPoint.DEFAULT_FILTER, getPointRenderLimit(), false);
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
                    renderedLines = renderedTask.plottedDrawing.getDisplayedLineCount()-1;
                    updateLocalMessage("Drawing");
                    updateLocalProgress(0);
                    drawingTime = System.currentTimeMillis();
                }
                if(renderedLines != -1){
                    graphicsFX.scale(canvasScaling, canvasScaling);
                    graphicsFX.translate(renderedTask.resolution.getScaledOffsetX(), renderedTask.resolution.getScaledOffsetY());
                    graphicsFX.setGlobalBlendMode(blendMode.javaFXVersion);

                    IPointFilter pointFilter = display_mode.get() == EnumDisplayMode.SELECTED_PEN ? PlottedPoint.SELECTED_PEN_FILTER : PlottedPoint.DEFAULT_FILTER;
                    renderedLines = renderedTask.plottedDrawing.renderPointsFX(graphicsFX, 0, renderedLines, pointFilter, getPointRenderLimit(),true);

                    int end = renderedTask.plottedDrawing.getDisplayedLineCount()-1;
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

    public void clearCanvas(){
        clearCanvas(Color.WHITE);
    }

    public void clearCanvas(Color color){
        canvas.getGraphicsContext2D().clearRect(0, 0,canvas.getWidth(), canvas.getHeight()); //ensures the canva's buffer is always cleared, some blend modes will prevent fillRect from triggering this
        canvas.getGraphicsContext2D().setFill(color);
        canvas.getGraphicsContext2D().fillRect(0, 0,canvas.getWidth(), canvas.getHeight());
    }

    public void updateCanvasSize(double width, double height){
        if(width > getMaxTextureSize() || height > getMaxTextureSize()){
            double max = Math.max(width, height);
            canvasScaling = getMaxTextureSize() / max;
            width = Math.floor(width*canvasScaling);
            height = Math.floor(height*canvasScaling);
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
        String prefix = batchProcessingTask == null ? "" : batchProcessingTask.getTitle() + " - ";
        if(getActiveTask() != null && getActiveTask().isRunning()){
            controller.progressBarGeneral.setProgress(getActiveTask().pfm == null ? 0 : getActiveTask().plottingProgress);
            controller.progressBarLabel.setText(prefix + getActiveTask().titleProperty().get() + " - " + getActiveTask().messageProperty().get());
            controller.labelPlottedLines.setText(Utils.defaultNF.format(getActiveTask().plottedDrawing.plottedPoints.size()) + " lines");
            controller.labelElapsedTime.setText(getActiveTask().getElapsedTime()/1000 + " s");
        }else if(exportTask != null){
            controller.progressBarGeneral.setProgress(exportTask.progressProperty().get());
            controller.progressBarLabel.setText(prefix + exportTask.titleProperty().get() + exportTask.extension + " - " + exportTask.messageProperty().get());
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
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    ////// EVENTS

    public void onTaskStageFinished(PlottingTask task, EnumTaskStage stage){
        switch (stage){
            case QUEUED:
                break;
            case PRE_PROCESSING:
                Platform.runLater(() -> display_mode.setValue(EnumDisplayMode.DRAWING));
                break;
            case DO_PROCESS:
                Platform.runLater(() -> {
                    controller.sliderDisplayedLines.setValue(1.0F);
                    controller.textFieldDisplayedLines.setText(String.valueOf(task.plottedDrawing.getPlottedLineCount()));
                });
                break;
            case POST_PROCESSING:
                break;
            case FINISHING:
                if(batchProcessingTask == null){
                    isPlotting.setValue(false);
                }
                break;
            case FINISHED:
                break;
        }
       logger.info("Plotting Task: Finished Stage " + stage.name());
    }

    public void onTaskCancelled(){
        isPlotting.setValue(false);
    }

    public void onDrawingAreaChanged(){
        drawingAreaDirty = true;
    }

    public void onDrawingPenChanged(){
        updateWeightedDistribution();
    }

    public void onDrawingSetChanged(){
        updateWeightedDistribution();
        observableDrawingSetFlag.set(!observableDrawingSetFlag.get());
    }

    public void onImageFiltersChanged(){
        imageFiltersDirty = true;
    }

    public void updateWeightedDistribution(){
        if(activeTask != null && activeTask.isTaskFinished()){
            activeTask.plottedDrawing.updateWeightedDistribution();
            reRender();
        }
    }

    //// PLOTTING TASKS

    public void startPlotting(){
        if(activeTask != null){
            activeTask.cancel();
        }
        if(openImage != null){
            taskService.submit(new PlottingTask(pfmFactory.get(), observableDrawingSet, openImage.getSource(), openFile));
            isPlotting.setValue(true);
        }
    }

    public void stopPlotting(){
        if(activeTask != null){
            activeTask.stopElegantly();
            isPlotting.setValue(false);
        }
    }

    public void resetPlotting(){
        taskService.shutdownNow();
        isPlotting.setValue(false);
        setActivePlottingTask(null);
        taskService = initTaskService();
        localProgress = 0D;
        localMessage = "";
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void openImage(File file, boolean internal){
        if(activeTask != null){
            activeTask.cancel();
            setActivePlottingTask(null);
            openImage = null;
            loadingImage = null;
        }
        openFile = file;
        loadingImage = new BufferedImageLoader.Filtered(file.getAbsolutePath(), internal);
        imageLoadingService.submit(loadingImage);
    }

    public void setActivePlottingTask(PlottingTask task){
        if(activeTask != null){
            activeTask.reset(); //help GC by removing references to PlottedLines
        }
        activeTask = task;

        if(activeTask == null){
            display_mode.setValue(EnumDisplayMode.IMAGE);
        }
    }

    public PlottingTask getActiveTask(){
        return activeTask;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// EXPORT TASKS

    public void createExportTask(ExportFormats format, PlottingTask plottingTask, IPointFilter pointFilter, String extension, File saveLocation, boolean seperatePens){
        taskService.submit(new ExportTask(format, plottingTask, pointFilter, extension, saveLocation, seperatePens, true));
    }

    public void setActiveExportTask(ExportTask task){
        exportTask = task;
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

    private final Thread.UncaughtExceptionHandler exceptionHandler = (thread, throwable) -> {
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