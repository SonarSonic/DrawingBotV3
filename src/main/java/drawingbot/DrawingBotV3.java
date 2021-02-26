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
import drawingbot.drawing.DrawingRegistry;
import drawingbot.drawing.ObservableDrawingSet;
import drawingbot.files.BatchProcessingTask;
import drawingbot.files.ExportFormats;
import drawingbot.files.ExportTask;
import drawingbot.image.BufferedImageLoader;
import drawingbot.image.FilteredBufferedImage;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.javafx.FXController;
import drawingbot.api.IPathFindingModule;
import drawingbot.javafx.GenericFactory;
import drawingbot.utils.*;
import drawingbot.pfm.PFMMasterRegistry;
import drawingbot.plotting.PlottedPoint;
import drawingbot.plotting.PlottingTask;
import javafx.application.Platform;
import javafx.beans.property.*;
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

    // CONSTANTS \\\
    public static final String appName = "DrawingBotV3";
    public static final String majorVersion = "1";
    public static final String minorVersion = "0";
    public static final String patchVersion = "5";
    public static final String appVersion = majorVersion + "." + minorVersion + "." + patchVersion;

    //DRAWING AREA
    public static SimpleBooleanProperty useOriginalSizing = new SimpleBooleanProperty(true);
    public static SimpleObjectProperty<EnumScalingMode> scaling_mode = new SimpleObjectProperty<>(EnumScalingMode.CROP_TO_FIT);
    public static SimpleObjectProperty<Units> inputUnits = new SimpleObjectProperty<>(Units.MILLIMETRES);

    public static SimpleFloatProperty drawingAreaWidth = new SimpleFloatProperty(0);
    public static SimpleFloatProperty drawingAreaHeight = new SimpleFloatProperty(0);
    public static SimpleFloatProperty drawingAreaPaddingLeft = new SimpleFloatProperty(0);
    public static SimpleFloatProperty drawingAreaPaddingRight = new SimpleFloatProperty(0);
    public static SimpleFloatProperty drawingAreaPaddingTop = new SimpleFloatProperty(0);
    public static SimpleFloatProperty drawingAreaPaddingBottom = new SimpleFloatProperty(0);
    public static SimpleStringProperty drawingAreaPaddingGang = new SimpleStringProperty("0");

    //GCODE SETTINGS
    public static SimpleBooleanProperty enableAutoHome = new SimpleBooleanProperty(true);
    public static SimpleFloatProperty gcodeOffsetX = new SimpleFloatProperty(0);
    public static SimpleFloatProperty gcodeOffsetY = new SimpleFloatProperty(0);
    public static SimpleFloatProperty penDownZ = new SimpleFloatProperty(0);
    public static SimpleFloatProperty penUpZ = new SimpleFloatProperty(5);

    //PATH FINDING \\
    public static SimpleBooleanProperty isPlotting = new SimpleBooleanProperty(false);
    public static SimpleObjectProperty<GenericFactory<IPathFindingModule>> pfmFactory = new SimpleObjectProperty<>(PFMMasterRegistry.getDefaultPFMFactory());

    // PEN SETS \\
    public static final SimpleBooleanProperty observableDrawingSetFlag = new SimpleBooleanProperty(false); //just a marker flag can be binded
    public static ObservableDrawingSet observableDrawingSet = new ObservableDrawingSet(DrawingRegistry.INSTANCE.getDefaultSet(DrawingRegistry.INSTANCE.getDefaultSetType()));

    // DISPLAY \\
    public static SimpleObjectProperty<EnumDisplayMode> display_mode = new SimpleObjectProperty<>(EnumDisplayMode.DRAWING);

    //VIEWPORT SETTINGS \\
    public static double minScale = 0.1;
    public static SimpleBooleanProperty displayGrid = new SimpleBooleanProperty(false);
    public static SimpleDoubleProperty scaleMultiplier = new SimpleDoubleProperty(1.0F);
    public static boolean imageFiltersDirty = false;


    //// VARIABLES \\\\

    // THREADS \\
    public static ExecutorService taskService = initTaskService();
    public static ExecutorService backgroundService = initBackgroundService();
    public static ExecutorService imageLoadingService = initImageLoadingService();
    public static ExecutorService imageFilteringService = initImageFilteringService();

    // TASKS \\
    public static PlottingTask activeTask = null;
    public static ExportTask exportTask = null;
    public static BatchProcessingTask batchProcessingTask = null;

    public static BufferedImageLoader.Filtered loadingImage = null;
    public static FilteredBufferedImage openImage = null;
    public static File openFile = null;

    // GUI \\
    public static FXController controller;
    public static Canvas canvas;
    public static GraphicsContext graphicsFX;
    public static FXGraphics2D graphicsAWT;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public DrawingBotV3() {}

    public static float getDrawingAreaWidthMM(){
        return drawingAreaWidth.getValue() * inputUnits.get().convertToMM;
    }

    public static float getDrawingAreaHeightMM(){
        return drawingAreaHeight.getValue() * inputUnits.get().convertToMM;
    }

    public static float getDrawingWidthMM(){
        return (drawingAreaWidth.getValue() - drawingAreaPaddingLeft.get() - drawingAreaPaddingRight.get()) * inputUnits.get().convertToMM;
    }

    public static float getDrawingHeightMM(){
        return (drawingAreaHeight.getValue() - drawingAreaPaddingTop.get() - drawingAreaPaddingBottom.get()) * inputUnits.get().convertToMM;
    }

    public static float getDrawingOffsetXMM(){
        return drawingAreaPaddingLeft.get() * inputUnits.get().convertToMM;
    }

    public static float getDrawingOffsetYMM(){
        return drawingAreaPaddingTop.get() * inputUnits.get().convertToMM;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Double localProgress = null;
    private static String localMessage = null;

    public static void updateLocalMessage(String message){
        localMessage = message;
    }

    public static void updateLocalProgress(double progress){
        localProgress = progress;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// PROCESSING RENDERING

    public static void reRender(){
        markRenderDirty = true;
    }

    public static void draw() {
        long startTime = System.currentTimeMillis();

        preRender();
        if(activeTask != null){
            renderTask();
        }else{
            renderOpenImage();
        }
        postRender();

        updateUI();

        long endTime = System.currentTimeMillis();
        long lastDrawTick = (endTime - startTime);
        if(lastDrawTick > 1000/60){
            DrawingBotV3.logger.finest("DRAWING PHASE TOOK TOO LONG: " + lastDrawTick + " milliseconds" + " expected " + 1000/60);
        }
    }


    public static int renderedLines = 0;
    private static long drawingTime = 0;
    private static PlottingTask lastDrawn = null;
    private static EnumTaskStage lastState = null;
    public static boolean canvasNeedsUpdate = false;
    
    private static boolean markRenderDirty = true, changedTask, changedState, shouldRedraw;

    private static void preRender(){
        PlottingTask renderedTask = getActiveTask();

        changedTask = lastDrawn != renderedTask;
        changedState = renderedTask != null && lastState != renderedTask.stage;
        shouldRedraw = markRenderDirty || changedTask || changedState;

        updateCanvasScaling();
        graphicsFX.setGlobalBlendMode(BlendMode.SRC_OVER);
    }

    private static void postRender(){
        PlottingTask renderedTask = getActiveTask();

        markRenderDirty = false;
        lastDrawn = renderedTask;
        lastState = renderedTask == null ? null : renderedTask.stage;
    }

    private static void renderOpenImage(){
        if(loadingImage != null && loadingImage.isDone()){
            openImage = loadingImage.getValue();
            shouldRedraw = true;
            loadingImage = null;
            updateCanvasSize(openImage.getWidth(), openImage.getHeight());
            updateCanvasScaling();
        }
        if(openImage != null){
            if(imageFiltersDirty){
                openImage.applyCurrentFilters();
                imageFiltersDirty = false;
                shouldRedraw = true;
            }

            if(shouldRedraw){
                clearCanvas();
                graphicsFX.drawImage(SwingFXUtils.toFXImage(openImage.getFiltered(), null), 0, 0);
            }
        }else{
            clearCanvas();
        }
    }

    private static void renderTask() {
        PlottingTask renderedTask = getActiveTask();

        if(changedTask){
            canvasNeedsUpdate = true;
        }

        if(canvasNeedsUpdate && renderedTask.getPrintScale() > 0){
            double scaledPageWidth = renderedTask.getPrintPageWidth() / renderedTask.getPrintScale();
            double scaledPageHeight = renderedTask.getPrintPageHeight() / renderedTask.getPrintScale();
            updateCanvasSize(scaledPageWidth, scaledPageHeight);
            canvasNeedsUpdate = false;
        }

        switch (renderedTask.stage){
            case QUEUED:
            case PRE_PROCESSING:
                break;
            case DO_PROCESS:
                if(changedTask || changedState){ //avoids redrawing in some instances
                    clearCanvas();
                    renderedLines = 0;
                }
                if(renderedTask.plottedDrawing.getPlottedLineCount() != 0){

                    double scaledOffsetX = renderedTask.renderOffsetX  + (renderedTask.getPrintOffsetX() / renderedTask.getPrintScale());
                    double scaledOffsetY = renderedTask.renderOffsetY + (renderedTask.getPrintOffsetY() / renderedTask.getPrintScale());

                    graphicsFX.translate(scaledOffsetX, scaledOffsetY);

                    renderedTask.plottedDrawing.renderPointsFX(graphicsFX, renderedLines, renderedTask.plottedDrawing.getPlottedLineCount(), PlottedPoint.DEFAULT_FILTER, false);
                    renderedLines = renderedTask.plottedDrawing.getPlottedLineCount();
                    if(renderedTask.plottingFinished){
                        renderedTask.finishedRenderingPaths = true;
                    }
                    graphicsFX.translate(-scaledOffsetX, -scaledOffsetY);
                }
                break;
            case POST_PROCESSING:
            case FINISHING:
                // NOP - continue displaying the path finding result
                break;
            case FINISHED:
                switch (display_mode.get()){
                    case SELECTED_PEN:
                    case DRAWING:
                        EnumBlendMode blendMode = renderedTask.plottedDrawing.drawingPenSet.blendMode.get();
                        if(shouldRedraw){
                            clearCanvas(blendMode.additive ? Color.BLACK : Color.WHITE);
                            renderedLines = 0;
                            updateLocalMessage("Drawing");
                            updateLocalProgress(0);
                            drawingTime = System.currentTimeMillis();
                        }
                        if(renderedLines != -1){
                            double scaledOffsetX = renderedTask.renderOffsetX  + (renderedTask.getPrintOffsetX() / renderedTask.getPrintScale());
                            double scaledOffsetY = renderedTask.renderOffsetY + (renderedTask.getPrintOffsetY() / renderedTask.getPrintScale());

                            graphicsFX.translate(scaledOffsetX, scaledOffsetY);
                            graphicsFX.setGlobalBlendMode(blendMode.javaFXVersion);
                            int pen = display_mode.get() == EnumDisplayMode.DRAWING || controller.getSelectedPen() == null ? -1 : controller.getSelectedPen().penNumber.get();
                            int toRender = (blendMode == EnumBlendMode.NORMAL ? 20000 : 2000);

                            int displayedLines = renderedTask.plottedDrawing.getDisplayedLineCount()-1;
                            int start = displayedLines - (renderedLines);
                            int end = Math.max(0, displayedLines - (renderedLines + toRender));
                            renderedTask.plottedDrawing.renderPointsFX(graphicsFX, start, end, (point, p) -> p.isEnabled() && (pen == -1 || point.pen_number == pen),true);
                            renderedLines += toRender;
                            graphicsFX.translate(-scaledOffsetX, -scaledOffsetY);

                            updateLocalProgress((float)renderedLines / displayedLines);
                            if(renderedLines >= displayedLines){
                                long time = System.currentTimeMillis();
                                logger.finest("Drawing Took: " + (time-drawingTime) + " ms");
                                renderedLines = -1;
                            }
                        }
                        break;
                    case ORIGINAL:
                        if(shouldRedraw){
                            clearCanvas();
                            float screen_scale_x = (float)renderedTask.img_plotting.getWidth() / (float)renderedTask.img_original.getWidth();
                            float screen_scale_y = (float)renderedTask.img_plotting.getHeight() / (float)renderedTask.img_original.getHeight();
                            float screen_scale = Math.min(screen_scale_x, screen_scale_y);

                            graphicsFX.translate(renderedTask.renderOffsetX, renderedTask.renderOffsetY);
                            graphicsFX.scale(screen_scale, screen_scale);
                            graphicsFX.drawImage(SwingFXUtils.toFXImage(renderedTask.getOriginalImage(), null), 0, 0);
                            graphicsFX.scale(1/screen_scale, 1/screen_scale);
                            graphicsFX.translate(-renderedTask.renderOffsetX, -renderedTask.renderOffsetY);
                        }
                        break;
                    case REFERENCE:
                        if(shouldRedraw){
                            clearCanvas();
                            graphicsFX.translate(renderedTask.renderOffsetX, renderedTask.renderOffsetY);
                            graphicsFX.drawImage(SwingFXUtils.toFXImage(renderedTask.getReferenceImage(), null), 0, 0);
                            graphicsFX.translate(-renderedTask.renderOffsetX, -renderedTask.renderOffsetY);
                        }
                        break;
                    case LIGHTENED:
                        if(shouldRedraw){
                            clearCanvas();
                            graphicsFX.translate(renderedTask.renderOffsetX, renderedTask.renderOffsetY);
                            graphicsFX.drawImage(SwingFXUtils.toFXImage(renderedTask.getPlottingImage(), null), 0, 0);
                            graphicsFX.translate(-renderedTask.renderOffsetX, -renderedTask.renderOffsetY);
                        }
                        break;
                }
                break;
        }

        if(shouldRedraw){
            GridOverlay.grid();
        }
    }

    public static void clearCanvas(){
        clearCanvas(Color.WHITE);
    }

    public static void clearCanvas(Color color){
        canvas.getGraphicsContext2D().clearRect(0, 0,canvas.getWidth(), canvas.getHeight()); //ensures the canva's buffer is always cleared, some blend modes will prevent fillRect from triggering this
        canvas.getGraphicsContext2D().setFill(color);
        canvas.getGraphicsContext2D().fillRect(0, 0,canvas.getWidth(), canvas.getHeight());
    }

    public static void updateCanvasSize(double width, double height){
        if(canvas.getWidth() == width && canvas.getHeight() == height){
            return;
        }
        canvas.widthProperty().setValue(width);
        canvas.heightProperty().setValue(height);

        Platform.runLater(() -> {
            controller.viewportScrollPane.setHvalue(0.5);
            controller.viewportScrollPane.setVvalue(0.5);
            markRenderDirty = true;
        });

        clearCanvas();//wipe the canvas
    }

    public static void updateCanvasScaling(){
        double screen_scale_x = controller.viewportScrollPane.getWidth() / ((float) canvas.getWidth());
        double screen_scale_y = controller.viewportScrollPane.getHeight() / ((float) canvas.getHeight());
        double screen_scale = Math.min(screen_scale_x, screen_scale_y) * scaleMultiplier.doubleValue();
        if(canvas.getScaleX() != screen_scale){
            canvas.setScaleX(screen_scale);
            canvas.setScaleY(screen_scale);
        }
    }

    public static void updateUI(){
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

    public static void onTaskStageFinished(PlottingTask task, EnumTaskStage stage){
       controller.onTaskStageFinished(task, stage);
       if(stage == EnumTaskStage.FINISHING && batchProcessingTask == null){
           isPlotting.setValue(false);
       }
       logger.info("Plotting Task: Finished Stage " + stage.name());
    }

    public static void onTaskCancelled(){
        isPlotting.setValue(false);
    }

    public static void onDrawingPenChanged(){
        updateWeightedDistribution();
    }


    public static void onDrawingSetChanged(){
        updateWeightedDistribution();
        observableDrawingSetFlag.set(!observableDrawingSetFlag.get());
    }

    public static void onImageFiltersChanged(){
        if(openImage == null || imageFiltersDirty){
            return;
        }
        imageFiltersDirty = true;
    }

    public static void updateWeightedDistribution(){
        if(activeTask != null && activeTask.isTaskFinished()){
            activeTask.plottedDrawing.updateWeightedDistribution();
            reRender();
        }
    }

    //// PLOTTING TASKS

    public static void startPlotting(){
        if(activeTask != null){
            activeTask.cancel();
        }
        if(openImage != null){
            taskService.submit(new PlottingTask(DrawingBotV3.pfmFactory.get(), DrawingBotV3.observableDrawingSet, openImage, openFile));
            isPlotting.setValue(true);
        }
    }

    public static void stopPlotting(){
        if(activeTask != null){
            activeTask.stopElegantly();
            isPlotting.setValue(false);
        }
    }

    public static void resetPlotting(){
        taskService.shutdownNow();
        isPlotting.setValue(false);
        setActivePlottingTask(null);
        taskService = initTaskService();
        localProgress = 0D;
        localMessage = "";
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void openImage(File file, boolean internal){
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

    public static void setActivePlottingTask(PlottingTask task){
        if(activeTask != null){
            activeTask.reset(); //help GC by removing references to PlottedLines
        }
        activeTask = task;
    }

    public static PlottingTask getActiveTask(){
        return activeTask;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// EXPORT TASKS

    public static void createExportTask(ExportFormats format, PlottingTask plottingTask, IPointFilter pointFilter, String extension, File saveLocation, boolean seperatePens){
        taskService.submit(new ExportTask(format, plottingTask, pointFilter, extension, saveLocation, seperatePens, true));
    }

    public static void setActiveExportTask(ExportTask task){
        exportTask = task;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// INTERACTION EVENTS

    private static boolean ctrl_down = false;

    public static void keyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.CONTROL) { ctrl_down = false; }
    }

    public static void keyPressed(KeyEvent event) {
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

    static double pressX = 0;
    static double pressY = 0;
    static double locX = 0;
    static double locY = 0;

    public static void mousePressedJavaFX(MouseEvent event) {    // record a delta distance for the drag and drop operation.
        pressX = event.getX();
        pressY = event.getY();
        locX = controller.viewportScrollPane.getHvalue();
        locY = controller.viewportScrollPane.getVvalue();
    }

    public static void mouseDraggedJavaFX(MouseEvent event) {
        double relativeX = (pressX - event.getX()) / controller.viewportStackPane.getWidth();
        double relativeY = (pressY - event.getY()) / controller.viewportStackPane.getHeight();
        controller.viewportScrollPane.setHvalue(locX + relativeX);
        controller.viewportScrollPane.setVvalue(locY + relativeY);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// SERVICES

    private static final Thread.UncaughtExceptionHandler exceptionHandler = (thread, throwable) -> {
        DrawingBotV3.logger.log(Level.SEVERE, "Thread Exception: " + thread.getName(), throwable);
    };

    public static ExecutorService initTaskService(){
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Task Thread");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(exceptionHandler);
            return t;
        });
    }

    public static ExecutorService initBackgroundService(){
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Background Thread");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(exceptionHandler);
            return t ;
        });
    }

    public static ExecutorService initImageLoadingService(){
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Image Loading Thread");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(exceptionHandler);
            return t;
        });
    }

    public static ExecutorService initImageFilteringService(){
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Image Filtering Thread");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(exceptionHandler);
            return t;
        });
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
}