/*
  DrawingBotV3 by Ollie Lansdell <ollielansdell@hotmail.co.uk
  Original by Scott Cooper, Dullbits.com, <scottslongemailaddress@gmail.com>
 */
package drawingbot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.logging.Logger;

import drawingbot.drawing.DrawingRegistry;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.drawing.ObservableDrawingSet;
import drawingbot.files.BatchProcessingTask;
import drawingbot.files.ExportFormats;
import drawingbot.files.ExportTask;
import drawingbot.image.BufferedImageLoader;
import drawingbot.javafx.FXController;
import drawingbot.api.IPathFindingModule;
import drawingbot.utils.*;
import drawingbot.pfm.PFMMasterRegistry;
import drawingbot.plotting.PlottedLine;
import drawingbot.plotting.PlottingTask;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
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
    public static final String patchVersion = "2";
    public static final String appVersion = majorVersion + "." + minorVersion + "." + patchVersion;

    //DRAWING AREA
    public static SimpleBooleanProperty useOriginalSizing = new SimpleBooleanProperty(true);
    public static SimpleObjectProperty<Units> inputUnits = new SimpleObjectProperty<>(Units.MILLIMETRES);

    public static SimpleFloatProperty drawingAreaWidth = new SimpleFloatProperty(0);
    public static SimpleFloatProperty drawingAreaHeight = new SimpleFloatProperty(0);

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
    public static ObservableDrawingSet observableDrawingSet = new ObservableDrawingSet(DrawingRegistry.INSTANCE.getDefaultSet().copy());

    // DISPLAY \\
    public static SimpleObjectProperty<EnumDisplayMode> display_mode = new SimpleObjectProperty<>(EnumDisplayMode.DRAWING);

    //VIEWPORT SETTINGS \\
    public static double minScale = 0.1;
    public static SimpleBooleanProperty displayGrid = new SimpleBooleanProperty(false);
    public static SimpleDoubleProperty scaleMultiplier = new SimpleDoubleProperty(1.0F);

    //// VARIABLES \\\\

    // THREADS \\
    public static ExecutorService taskService = initTaskService();
    public static ExecutorService backgroundService = initBackgroundService();
    public static ExecutorService imageLoadingService = initImageLoadingService();

    // TASKS \\
    public static PlottingTask activeTask = null;
    public static ExportTask exportTask = null;
    public static BatchProcessingTask batchProcessingTask = null;

    public static BufferedImageLoader loadingImage = null;
    public static BufferedImage openImage = null;

    // GUI \\
    public static FXController controller;
    public static Canvas canvas;
    public static FXGraphics2D graphics;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public DrawingBotV3() {}

    private static Double localProgress = null;
    private static String localMessage = null;

    public static void updateLocalMessage(String message){
        localMessage = message;
    }

    public static void updateLocalProgress(double progress){
        localProgress = progress;
    }

    public static float getDrawingAreaWidthMM(PlottingTask task){
        if(useOriginalSizing.get()){
            return task.img_original == null ? 0: task.img_original.getWidth();
        }
        return drawingAreaWidth.getValue() * inputUnits.get().convertToMM;
    }

    public static float getDrawingAreaHeightMM(PlottingTask task){
        if(useOriginalSizing.get()){
            return task.img_original == null ? 0: task.img_original.getHeight();
        }
        return drawingAreaHeight.getValue() * inputUnits.get().convertToMM;
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
            if(shouldRedraw){
                clearCanvas();
                graphics.drawImage(openImage, 0, 0, null);
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

        if(canvasNeedsUpdate && renderedTask.getPlottingWidth() != -1){
            updateCanvasSize(renderedTask.getPlottingWidth(), renderedTask.getPlottingHeight());
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
                    renderedTask.plottedDrawing.renderLines(graphics, renderedLines, renderedTask.plottedDrawing.getPlottedLineCount());
                    renderedLines = renderedTask.plottedDrawing.getPlottedLineCount();
                    if(renderedTask.plottingFinished){
                        renderedTask.finishedRenderingPaths = true;
                    }
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
                        if(shouldRedraw){
                            clearCanvas();
                            canvas.setBlendMode(observableDrawingSet.blendMode.get().javaFXVersion);
                            /* TODO FIX BLEND MODES
                            if(renderedTask.plottedDrawing.drawingPenSet.blendMode.get().additive){
                                background(0, 0, 0);
                            }else{
                                background(255, 255, 255);
                            }

                             */
                            renderedLines = 0;
                            updateLocalMessage("Drawing");
                            updateLocalProgress(0);
                            drawingTime = System.currentTimeMillis();
                        }
                        if(renderedLines != -1){
                            int pen = display_mode.get() == EnumDisplayMode.DRAWING || controller.penTableView.getSelectionModel().isEmpty() ? -1 : controller.penTableView.getSelectionModel().getSelectedItem().penNumber.get();
                            int max = Math.min(renderedLines + 20000, renderedTask.plottedDrawing.getDisplayedLineCount());
                            //blendMode(renderedTask.plottedDrawing.drawingPenSet.blendMode.get().constant); //TODO FIX BLEND MODE
                            for(; renderedLines < max; renderedLines++){
                                int nextReversed = renderedTask.plottedDrawing.getDisplayedLineCount()-1-renderedLines;
                                PlottedLine line = renderedTask.plottedDrawing.plottedLines.get(nextReversed);
                                if(pen == -1 || line.pen_number == pen){
                                    renderedTask.plottedDrawing.renderLine(graphics, line);
                                }
                            }

                            updateLocalProgress((float)renderedLines / renderedTask.plottedDrawing.getDisplayedLineCount());
                            if(renderedLines == renderedTask.plottedDrawing.getDisplayedLineCount()-1){
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
                            graphics.scale(screen_scale, screen_scale);
                            graphics.drawImage(renderedTask.getOriginalImage(), 0, 0, null);
                            graphics.scale(1/screen_scale, 1/screen_scale);
                        }
                        break;
                    case REFERENCE:
                        if(shouldRedraw){
                            clearCanvas();
                            graphics.drawImage(renderedTask.getReferenceImage(), 0, 0, null);
                        }
                        break;
                    case LIGHTENED:
                        if(shouldRedraw){
                            clearCanvas();
                            graphics.drawImage(renderedTask.getPlottingImage(), 0, 0, null);
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
        canvas.getGraphicsContext2D().setFill(Color.WHITE);
        canvas.getGraphicsContext2D().fillRect(0, 0,(int)canvas.getWidth(), (int)canvas.getHeight());
        //canvas.getGraphicsContext2D().clearRect(0, 0,(int)canvas.getWidth(), (int)canvas.getHeight());
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
        canvas.setScaleX(screen_scale);
        canvas.setScaleY(screen_scale);
    }

    public static void updateUI(){
        String prefix = batchProcessingTask == null ? "" : batchProcessingTask.getTitle() + " - ";
        if(getActiveTask() != null && getActiveTask().isRunning()){
            controller.progressBarGeneral.setProgress(getActiveTask().pfm == null ? 0 : getActiveTask().plottingProgress);
            controller.progressBarLabel.setText(prefix + getActiveTask().titleProperty().get() + " - " + getActiveTask().messageProperty().get());
            controller.labelPlottedLines.setText(Utils.defaultNF.format(getActiveTask().plottedDrawing.plottedLines.size()) + " lines");
            controller.labelElapsedTime.setText(getActiveTask().getElapsedTime()/1000 + " s");
        }else if(exportTask != null && exportTask.isRunning()){
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
            taskService.submit(new PlottingTask(DrawingBotV3.pfmFactory.get(), DrawingBotV3.observableDrawingSet, openImage));
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
        activeTask = null;
        taskService = initTaskService();
        localProgress = 0D;
        localMessage = "";
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void openImage(String url, boolean internal){
        if(activeTask != null){
            activeTask.cancel();
            activeTask = null;
            openImage = null;
            loadingImage = null;
        }
        loadingImage = new BufferedImageLoader(url, internal);
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

    public static void createExportTask(ExportFormats format, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter, String extension, File saveLocation, boolean seperatePens){
        taskService.submit(new ExportTask(format, plottingTask, lineFilter, extension, saveLocation, seperatePens, true));
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

    public static ExecutorService initTaskService(){
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Task Thread");
            t.setDaemon(true);
            return t;
        });
    }

    public static ExecutorService initBackgroundService(){
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Background Thread");
            t.setDaemon(true);
            return t ;
        });
    }

    public static ExecutorService initImageLoadingService(){
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Image Loading Thread");
            t.setDaemon(true);
            return t ;
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
}