/*
  DrawingBotV3 by Ollie lansdell <ollielansdell@hotmail.co.uk
  Original by Scott Cooper, Dullbits.com, <scottslongemailaddress@gmail.com>
 */
package drawingbot;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

import drawingbot.drawing.DrawingRegistry;
import drawingbot.drawing.DrawingSet;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.drawing.ObservableDrawingSet;
import drawingbot.files.BatchProcessingTask;
import drawingbot.files.ExportFormats;
import drawingbot.files.ExportTask;
import drawingbot.helpers.*;
import drawingbot.javafx.FXController;
import drawingbot.pfm.PFMLoaders;
import drawingbot.plotting.PlottedLine;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.EnumDisplayMode;
import drawingbot.utils.EnumTaskStage;
import drawingbot.utils.Units;
import drawingbot.utils.Utils;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PSurface;

public class DrawingBotV3 extends PApplet {

    public static DrawingBotV3 INSTANCE;

    ///constants
    public static final String appName = "DrawingBotV3";
    public static final String majorVersion = "1";
    public static final String minorVersion = "0";
    public static final String patchVersion = "1";
    public static final String appVersion = majorVersion + "." + minorVersion + "." + patchVersion;
    public static final String PGraphicsFX9 = "drawingbot.javafx.PGraphicsFX9";

    ///plotting settings
    public static final float INCHES_TO_MILLIMETRES = 25.4F;
    public static final float paper_top_to_origin = 285; //mm, make smaller to move drawing down on paper
    public static final float image_scale = 1F; //determines image scale / plotting quality

    ///exports
    public static final char gcode_decimal_seperator = '.';
    public static final int gcode_decimals = 2; // Number of digits right of the decimal point in the drawingbot.gcode files.
    public static final int svg_decimals = 2; // Number of digits right of the decimal point in the SVG file.

    ///grid rendering
    public static final float paper_size_x = 32 * INCHES_TO_MILLIMETRES; //mm, papers width
    public static final float paper_size_y = 40 * INCHES_TO_MILLIMETRES; //mm, papers height
    public static final float grid_scale = 25.4F; // Use 10.0 for centimeters, 25.4 for inches, and between 444 and 529.2 for cubits.


    // THREADS \\
    public ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "DrawingBotV3 - Task Thread");
        t.setDaemon(true);
        return t ;
    });

    // TASKS \\
    private PlottingTask activeTask = null;
    private ExportTask exportTask = null;
    public BatchProcessingTask batchProcessingTask = null;

    public PImage loadingImage = null;
    public PImage openImage = null;

    // GUI \\
    public FXController controller;
    public Canvas canvas;

    //DRAWING AREA
    public static SimpleBooleanProperty useOriginalSizing = new SimpleBooleanProperty(true);
    public static SimpleObjectProperty<Units> inputUnits = new SimpleObjectProperty<Units>(Units.MILLIMETRES);

    public static SimpleFloatProperty drawingAreaWidth = new SimpleFloatProperty(0);
    public static SimpleFloatProperty drawingAreaHeight = new SimpleFloatProperty(0);

    //GCODE SETTINGS
    public static SimpleBooleanProperty enableAutoHome = new SimpleBooleanProperty(true);
    public static SimpleFloatProperty gcodeOffsetX = new SimpleFloatProperty(0);
    public static SimpleFloatProperty gcodeOffsetY = new SimpleFloatProperty(0);
    public static SimpleFloatProperty penDownZ = new SimpleFloatProperty(0);
    public static SimpleFloatProperty penUpZ = new SimpleFloatProperty(5);

    //PATH FINDING \\
    public SimpleBooleanProperty isPlotting = new SimpleBooleanProperty(false);
    public SimpleObjectProperty<PFMLoaders> pfmLoader = new SimpleObjectProperty<>(PFMLoaders.SKETCH);

    // PEN SETS \\
    public ObservableDrawingSet observableDrawingSet;

    // DISPLAY \\
    public EnumDisplayMode display_mode = EnumDisplayMode.DRAWING;

    // MOUSE / INPUT VALUES \\
    public boolean ctrl_down = false;

    //VIEWPORT SETTINGS \\
    public static final float canvasScaling = 1.0F;
    public static final double minScale = 0.1;
    public static SimpleBooleanProperty displayGrid = new SimpleBooleanProperty(false);
    public static SimpleDoubleProperty scaleMultiplier = new SimpleDoubleProperty(1.0F);

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public DrawingBotV3() {
        INSTANCE = this;
    }

    private Double localProgress = null;
    private String localMessage = null;

    public void updateLocalMessage(String message){
        localMessage = message;
    }

    public void updateLocalProgress(double progress){
        localProgress = progress;
    }

    public float getDrawingAreaWidthMM(){
        if(useOriginalSizing.get()){
            return activeTask.img_original == null ? 0: activeTask.img_original.width;
        }
        return drawingAreaWidth.getValue() * inputUnits.get().convertToMM;
    }

    public float getDrawingAreaHeightMM(){
        if(useOriginalSizing.get()){
            return activeTask.img_original == null ? 0: activeTask.img_original.height;
        }
        return drawingAreaHeight.getValue() * inputUnits.get().convertToMM;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// PROCESSING INIT

    public void settings() {
        size(1200, 1200, PGraphicsFX9);
    }

    @Override
    protected PSurface initSurface() {
        g = createPrimaryGraphics();
        surface = g.createSurface();
        FXApplication.setupSurface(this);
        return surface;
    }

    @Override
    public void setup() {
        surface.setResizable(true);
        surface.setTitle(appName + ", Version: " + appVersion);

        DrawingSet defaultSet = DrawingRegistry.INSTANCE.getDefaultSet().copy();
        observableDrawingSet = new ObservableDrawingSet(defaultSet);
        controller.penTableView.setItems(observableDrawingSet.pens);
        controller.renderOrderComboBox.valueProperty().bindBidirectional(observableDrawingSet.renderOrder);
        controller.blendModeComboBox.valueProperty().bindBidirectional(observableDrawingSet.blendMode);

        colorMode(RGB);
        frameRate(1000);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// PROCESSING RENDERING

    public int renderedLines = 0;

    private boolean markRenderDirty = false;
    private PlottingTask lastDrawn = null;
    private EnumTaskStage lastState = null;
    public boolean canvasNeedsUpdate = false;

    private long drawingTime = 0;

    public void reRender(){
        markRenderDirty = true;
    }

    @Override
    public final void draw() {
        long startTime = System.currentTimeMillis();

        renderTask();
        updateUI();
        long endTime = System.currentTimeMillis();
        long lastDrawTick = (endTime - startTime);
        if(lastDrawTick > 1000/60){
            println("DRAWING PHASE TOOK TOO LONG: " + lastDrawTick + " milliseconds" + " expected " + 1000/60);
        }
    }

    private void renderTask() {
        PlottingTask renderedTask = getActiveTask();

        boolean changedTask = lastDrawn != renderedTask;
        boolean changedState = renderedTask != null && lastState != renderedTask.stage;
        boolean shouldRedraw = markRenderDirty || changedTask || changedState;

        if(renderedTask == null){
            if(loadingImage != null){
                if(loadingImage.width > 0){
                    openImage = loadingImage;
                    shouldRedraw = true;
                    canvasNeedsUpdate = true;
                    loadingImage = null;
                }else if(loadingImage.width == -1){
                    println("INVALID IMAGE FILE");
                    loadingImage = null;
                }
            }
            if(openImage != null){
                if(canvasNeedsUpdate){
                    updateCanvasSize(openImage);
                    updateCanvasScaling(openImage);
                    canvasNeedsUpdate = false;
                    return;
                }
                if(shouldRedraw){
                    background(255, 255, 255);
                    image(openImage, 0, 0);
                }
            }else{
                background(255, 255, 255);
            }
            return;
        }

        if(changedTask){
            canvasNeedsUpdate = true;
        }

        if(renderedTask.img_reference != null){
            if(canvasNeedsUpdate){
                updateCanvasSize(renderedTask.img_reference);
                lastDrawn = renderedTask;
                lastState = renderedTask.stage;
                canvasNeedsUpdate = false;
                return;
            }
            updateCanvasScaling(renderedTask.img_reference);
        }

        markRenderDirty = false;

        switch (renderedTask.stage){
            case QUEUED:
            case LOADING_IMAGE:
            case PRE_PROCESSING:
                background(255, 255, 255);
                break;
            case PATH_FINDING:
                if(changedTask || changedState){ //avoids redrawing in some instances
                    background(255, 255, 255);
                    renderedLines = 0;
                }
                if(renderedTask.plottedDrawing.getPlottedLineCount() != 0){
                    renderedTask.plottedDrawing.renderLines(renderedLines, renderedTask.plottedDrawing.getPlottedLineCount());
                    renderedLines = renderedTask.plottedDrawing.getPlottedLineCount();
                    if(renderedTask.pfm.finished()){
                        renderedTask.finishedRenderingPaths = true;
                    }
                }
                break;
            case POST_PROCESSING:
                // NOP - continue displaying the path finding result
                break;
            case FINISHING:
                // NOP - continue displaying the path finding result
                break;
            case FINISHED:
                switch (display_mode){
                    case SELECTED_PEN:
                    case DRAWING:
                        if(shouldRedraw){
                            if(renderedTask.plottedDrawing.drawingPenSet.blendMode.get().additive){
                                background(0, 0, 0);
                            }else{
                                background(255, 255, 255);
                            }
                            renderedLines = 0;
                            updateLocalMessage("Drawing");
                            updateLocalProgress(0);
                            drawingTime = System.currentTimeMillis();
                        }
                        if(renderedLines != -1){
                            int pen = display_mode == EnumDisplayMode.DRAWING || controller.penTableView.getSelectionModel().isEmpty() ? -1 : controller.penTableView.getSelectionModel().getSelectedItem().penNumber.get();
                            int max = Math.min(renderedLines + 20000, renderedTask.plottedDrawing.getDisplayedLineCount());
                            blendMode(renderedTask.plottedDrawing.drawingPenSet.blendMode.get().constant);
                            for(; renderedLines < max; renderedLines++){
                                int nextReversed = renderedTask.plottedDrawing.getDisplayedLineCount()-1-renderedLines;
                                PlottedLine line = renderedTask.plottedDrawing.plottedLines.get(nextReversed);
                                if(pen == -1 || line.pen_number == pen){
                                    renderedTask.plottedDrawing.renderLine(line);
                                }
                            }

                            updateLocalProgress((float)renderedLines / renderedTask.plottedDrawing.getDisplayedLineCount());
                            if(renderedLines == renderedTask.plottedDrawing.getDisplayedLineCount()-1){
                                long time = System.currentTimeMillis();
                                println("Drawing Took: " + (time-drawingTime) + " ms");
                                renderedLines = -1;
                            }
                        }
                        break;
                    case ORIGINAL:
                        if(shouldRedraw){
                            background(255, 255, 255);
                            float screen_scale_x = (float)renderedTask.img_plotting.width / (float)renderedTask.img_original.width;
                            float screen_scale_y = (float)renderedTask.img_plotting.height / (float)renderedTask.img_original.height;
                            float screen_scale = Math.min(screen_scale_x, screen_scale_y);
                            scale(screen_scale);
                            image(renderedTask.getOriginalImage(), 0, 0);
                        }
                        break;
                    case REFERENCE:
                        if(shouldRedraw){
                            background(255, 255, 255);
                            image(renderedTask.getReferenceImage(), 0, 0);
                        }
                        break;
                    case LIGHTENED:
                        if(shouldRedraw){
                            background(255, 255, 255);
                            image(renderedTask.getPlottingImage(), 0, 0);
                        }
                        break;
                }
                break;
        }

        if(shouldRedraw){
            GridOverlay.grid();
        }

        lastDrawn = renderedTask;
        lastState = renderedTask.stage;


    }

    public void updateCanvasSize(PImage targetSize){
        double newWidth = targetSize.width*canvasScaling;
        double newHeight = targetSize.height*canvasScaling;
        if(canvas.getWidth() == newWidth && canvas.getHeight() == newHeight){
            return;
        }
        canvas.widthProperty().setValue(newWidth);
        canvas.heightProperty().setValue(newHeight);

        Platform.runLater(() -> {
            controller.viewportScrollPane.setHvalue(0.5);
            controller.viewportScrollPane.setVvalue(0.5);
        });
        markRenderDirty = true;
        background(255, 255, 255);//wipe the canvas
    }

    public void updateCanvasScaling(PImage targetSize){
        double screen_scale_x = controller.viewportScrollPane.getWidth() / ((float) targetSize.width*canvasScaling);
        double screen_scale_y = controller.viewportScrollPane.getHeight() / ((float) targetSize.height*canvasScaling);
        double screen_scale = Math.min(screen_scale_x, screen_scale_y) * scaleMultiplier.doubleValue();
        canvas.setScaleX(screen_scale);
        canvas.setScaleY(screen_scale);
    }

    public void updateUI(){
        String prefix = batchProcessingTask == null ? "" : batchProcessingTask.getTitle() + " - ";
        if(getActiveTask() != null && getActiveTask().isRunning()){
            controller.progressBarGeneral.setProgress(getActiveTask().pfm == null ? 0 :getActiveTask().pfm.progress());
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

    public void onTaskStageFinished(PlottingTask task, EnumTaskStage stage){
       controller.onTaskStageFinished(task, stage);
       if(stage == EnumTaskStage.FINISHING && batchProcessingTask == null){
           isPlotting.setValue(false);
       }
    }

    public void onTaskCancelled(){
        isPlotting.setValue(false);
    }

    public void onDrawingPenChanged(){
        updateWeightedDistribution();
    }


    public void onDrawingSetChanged(){
        updateWeightedDistribution();
    }

    public void updateWeightedDistribution(){
        if(activeTask != null && activeTask.isTaskFinished()){
            activeTask.plottedDrawing.updateWeightedDistribution();
            reRender();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void openImage(String url){
        if(activeTask != null){
            activeTask.cancel();
            activeTask = null;
            openImage = null;
            loadingImage = null;
        }
        loadingImage = requestImage(url);
    }

    //// PLOTTING TASKS

    public void startPlotting(){
        if(activeTask != null){
            activeTask.cancel();
        }
        if(openImage != null){
            executorService.submit(new PlottingTask(DrawingBotV3.INSTANCE.pfmLoader.get(), DrawingBotV3.INSTANCE.observableDrawingSet, openImage));
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

    }

    public void setActivePlottingTask(PlottingTask task){
        if(activeTask != null){
            activeTask.reset(); //help GC by removing references to PlottedLines
        }
        activeTask = task;
    }

    public PlottingTask getCompletedTask(){
        return activeTask.isTaskFinished() ? activeTask : null;
    }

    public PlottingTask getActiveTask(){
        return activeTask;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// EXPORT TASKS

    public void createExportTask(ExportFormats format, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter, String extension, File saveLocation, boolean seperatePens){
        executorService.submit(new ExportTask(format, plottingTask, lineFilter, extension, saveLocation, seperatePens, true));
    }

    public void setActiveExportTask(ExportTask task){
        exportTask = task;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// INTERACTION EVENTS

    public void keyReleased() {
        if (keyCode == CONTROL) { ctrl_down = false; }
    }

    public void keyPressed() {
        PlottingTask renderedTask = getActiveTask();
        if (keyCode == CONTROL) { ctrl_down = true; }
        if (key == 'x') { GridOverlay.mouse_point(); }

        if (keyCode == 65 && ctrl_down)  {
            println("Holly freak, Ctrl-A was pressed!");
        }
        if (key == CODED) {
            double delta = 0.01;
            double currentH = controller.viewportScrollPane.getHvalue();
            double currentV = controller.viewportScrollPane.getVvalue();
            if (keyCode == UP)    {
                controller.viewportScrollPane.setVvalue(currentV - delta);
            }
            if (keyCode == DOWN)  {
                controller.viewportScrollPane.setVvalue(currentV + delta);
            }
            if (keyCode == RIGHT) {
                controller.viewportScrollPane.setHvalue(currentH - delta);
            }
            if (keyCode == LEFT)  {
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

}