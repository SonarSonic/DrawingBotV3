package drawingbot;///////////////////////////////////////////////////////////////////////////////////////////////////////
// My Drawbot, "Death to Sharpie"
// Jpeg to drawingbot.gcode simplified (kinda sorta works version, v3.75 (beta))
//
// Scott Cooper, Dullbits.com, <scottslongemailaddress@gmail.com>
//
// Open creative GPL source commons with some BSD public GNU foundation stuff sprinkled in...
// If anything here is remotely useable, please give me a shout.
//
// Useful math:    http://members.chello.at/~easyfilter/bresenham.html
// drawingbot.helpers.GClip:          https://forum.processing.org/two/discussion/6179/why-does-not-it-run-clipboard
// Dynamic class:  https://processing.org/discourse/beta/num_1262759715.html
///////////////////////////////////////////////////////////////////////////////////////////////////////
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
import processing.core.PSurface;
import processing.javafx.PGraphicsFX2D;

public class DrawingBotV3 extends PApplet {

    public static DrawingBotV3 INSTANCE;

    ///constants
    public static final String appName = "DrawingBotV3";
    public static final String appVersion = "1.0.0";
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
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t ;
    });
    private PlottingTask activeTask = null;
    private ExportTask exportTask = null;
    public BatchProcessingTask batchProcessingTask = null;

    // GUI \\
    public FXController controller;
    public Canvas canvas;

    //DRAWING AREA
    public static SimpleBooleanProperty useOriginalSizing = new SimpleBooleanProperty(true);
    public static SimpleFloatProperty drawingAreaWidth = new SimpleFloatProperty(0);
    public static SimpleFloatProperty drawingAreaHeight = new SimpleFloatProperty(0);
    public static SimpleObjectProperty<Units> drawingAreaUnits = new SimpleObjectProperty<Units>(Units.MILLIMETRES);

    //PATH FINDING \\
    public PFMLoaders pfmLoader = PFMLoaders.ORIGINAL;

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

    // DRAWING \\\

    public DrawingBotV3() {
        INSTANCE = this;
    }

    public float getDrawingAreaWidthMM(){
        if(useOriginalSizing.get()){
            return activeTask.img_original == null ? 0: activeTask.img_original.width;
        }
        return drawingAreaWidth.getValue() * drawingAreaUnits.get().convertToMM;
    }

    public float getDrawingAreaHeightMM(){
        if(useOriginalSizing.get()){
            return activeTask.img_original == null ? 0: activeTask.img_original.height;
        }
        return drawingAreaHeight.getValue() * drawingAreaUnits.get().convertToMM;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void settings() {
        size(1200, 1200, PGraphicsFX9);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////


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

    private Double localProgress = null;
    private String localMessage = null;

    public void updateLocalMessage(String message){
        localMessage = message;
    }

    public void updateLocalProgress(double progress){
        localProgress = progress;
    }

    public void updateUI(){
        String prefix = batchProcessingTask == null ? "" : batchProcessingTask.getTitle() + " - ";
        if(getActiveTask() != null && getActiveTask().isRunning()){
            controller.progressBarGeneral.setProgress(getActiveTask().progressProperty().get());
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

    public int renderedLines = 0;

    private boolean markRenderDirty = false;
    private PlottingTask lastDrawn = null;
    private EnumTaskStage lastState = null;
    private boolean canvasNeedsUpdate = false;

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

        if(renderedTask == null){
            background(255, 255, 255);
            return;
        }

        /// GENERAL SCALING \\\\
        scale(canvasScaling);
        //translate(mx, my);
        //rotate(HALF_PI*renderedTask.screen_rotate);


        boolean changedTask = lastDrawn != renderedTask;
        boolean changedState = lastState != renderedTask.stage;
        boolean shouldRedraw = markRenderDirty || changedTask || changedState;

        if(changedTask){
            canvasNeedsUpdate = true;
        }

        if(canvasNeedsUpdate && renderedTask.img_reference != null){
            canvas.widthProperty().setValue(renderedTask.img_reference.width*canvasScaling);
            canvas.heightProperty().setValue(renderedTask.img_reference.height*canvasScaling);

            //controller.viewportStackPane.setMaxWidth((renderedTask.img_reference.width*canvasScaling)*2);
            //controller.viewportStackPane.setMaxHeight((renderedTask.img_reference.height*canvasScaling)*2); //TODO MAKE SURE THIS IS BIG ENOUGH TO SCALE UP THE VIEWPORT!

            Platform.runLater(() -> {
                controller.viewportScrollPane.setHvalue(0.5);
                controller.viewportScrollPane.setVvalue(0.5);
            });
            canvasNeedsUpdate = false;

            lastDrawn = renderedTask;
            lastState = renderedTask.stage;

            background(255, 255, 255); //it's a fresh image, lets wipe the old one
            markRenderDirty = true;
            return;
        }

        updateCanvasScaling();
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
            case LOGGING:
                // NOP - continue displaying the path finding result
                break;
            case FINISHED:
                switch (display_mode){
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

                        int max = Math.min(renderedLines + 10000, renderedTask.plottedDrawing.getDisplayedLineCount());
                        blendMode(renderedTask.plottedDrawing.drawingPenSet.blendMode.get().constant);
                        for(; renderedLines < max; renderedLines++){
                            int nextReversed = renderedTask.plottedDrawing.getDisplayedLineCount()-1-renderedLines;
                            PlottedLine line = renderedTask.plottedDrawing.plottedLines.get(nextReversed);
                            renderedTask.plottedDrawing.renderLine(line);
                        }

                        updateLocalProgress((float)renderedLines / renderedTask.plottedDrawing.getDisplayedLineCount());
                        if(renderedLines == renderedTask.plottedDrawing.getDisplayedLineCount()-1){
                            long time = System.currentTimeMillis();
                            println("Drawing Took: " + (time-drawingTime) + " ms");
                            renderedLines = 0;
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
                    case SELECTED_PEN:
                        if(shouldRedraw){
                            background(255, 255, 255);
                            renderedLines = 0;
                            updateLocalMessage("Drawing");
                        }
                        if(renderedTask.plottedDrawing.getDisplayedLineCount() != 0 && renderedLines < renderedTask.plottedDrawing.getDisplayedLineCount()){
                            int pen = controller.penTableView.getSelectionModel().getSelectedIndex();
                            int next = Math.min(renderedLines + 1000, renderedTask.plottedDrawing.getDisplayedLineCount());
                            renderedTask.plottedDrawing.renderLinesForPen(renderedLines, next, Math.max(pen, 0));
                            renderedLines = next;
                            updateLocalProgress((double)renderedLines / renderedTask.plottedDrawing.getDisplayedLineCount());
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

    public void updateCanvasScaling(){
        if(getActiveTask() != null && getActiveTask().img_reference != null){
            double screen_scale_x = controller.viewportScrollPane.getWidth() / ((float) getActiveTask().img_reference.width*canvasScaling);
            double screen_scale_y = controller.viewportScrollPane.getHeight() / ((float) getActiveTask().img_reference.height*canvasScaling);
            double screen_scale = Math.min(screen_scale_x, screen_scale_y) * scaleMultiplier.doubleValue();
            canvas.setScaleX(screen_scale);
            canvas.setScaleY(screen_scale);
        }
    }

    ////// EVENTS

    public void onTaskStageFinished(PlottingTask task, EnumTaskStage stage){
       controller.onTaskStageFinished(task, stage);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void keyReleased() {
        if (keyCode == CONTROL) { ctrl_down = false; }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void keyPressed() {
        PlottingTask renderedTask = getActiveTask();
        if (keyCode == CONTROL) { ctrl_down = true; }
        if (key == 'x') { GridOverlay.mouse_point(); }

        //if (key == 's') { if (state == 3) { state++; } }//FIXME - ADD STOP & START BUTTON

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

    //// PLOTTING TASKS

    public void createPlottingTask(String url){
        if(activeTask != null){
            activeTask.cancel();
        }
        executorService.submit(new PlottingTask(DrawingBotV3.INSTANCE.pfmLoader, DrawingBotV3.INSTANCE.observableDrawingSet, url));
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

    //// EXPORT TASKS

    public void createExportTask(ExportFormats format, PlottingTask plottingTask, BiFunction<PlottedLine, ObservableDrawingPen, Boolean> lineFilter, String extension, File saveLocation, boolean seperatePens){
        executorService.submit(new ExportTask(format, plottingTask, lineFilter, extension, saveLocation, seperatePens, true));
    }

    public void setActiveExportTask(ExportTask task){
        exportTask = task;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////


    public void action_rotate(){
        /* TODO IMAGE ROTATE?
        if(getCompletedTask() == null){
            return;
        }
        PlottingTask renderedTask = getSelectedTask();

        renderedTask.screen_rotate ++;
        if (renderedTask.screen_rotate == 4) { renderedTask.screen_rotate = 0; }

        switch(renderedTask.screen_rotate) {
            case 0:
                my -= getCompletedTask().height();
                break;
            case 1:
                mx += getCompletedTask().height();
                break;
            case 2:
                my += getCompletedTask().height();
                break;
            case 3:
                mx -= getCompletedTask().height();
                break;
        }
        reRender();

         */
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// INTERACTION EVENTS



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

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void mouseDraggedJavaFX(MouseEvent event) {
        double relativeX = (pressX - event.getX()) / controller.viewportStackPane.getWidth();
        double relativeY = (pressY - event.getY()) / controller.viewportStackPane.getHeight();

        controller.viewportScrollPane.setHvalue(locX + relativeX);
        controller.viewportScrollPane.setVvalue(locY + relativeY);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

}
