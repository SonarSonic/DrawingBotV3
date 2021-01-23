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
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Map;

import drawingbot.drawing.DrawingRegistry;
import drawingbot.drawing.DrawingSet;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.drawing.ObservableDrawingSet;
import drawingbot.helpers.*;
import drawingbot.javafx.FXController;
import drawingbot.pfm.PFMLoaders;
import drawingbot.plotting.PlottedLine;
import drawingbot.plotting.PlottingTask;
import drawingbot.plotting.PlottingThread;
import drawingbot.utils.EnumDisplayMode;
import drawingbot.utils.EnumTaskStage;
import drawingbot.utils.Units;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PSurface;

//TODO FIX BUG WITH PFMOriginal moving when it is redrawn.
public class DrawingBotV3 extends PApplet {

    public static DrawingBotV3 INSTANCE;

    ///constants
    public static final String appName = "DrawingBotV3";
    public static final String appVersion = "1.0.0";

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
    public PlottingThread plottingThread;

    // GUI \\
    public FXController controller;
    public Canvas canvas;

    //DRAWING AREA
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

    public static final float canvasScaling = 1.0F;
    public static final double minScale = 0.1;
    public static SimpleBooleanProperty displayGrid = new SimpleBooleanProperty(false);
    public static SimpleDoubleProperty scaleMultiplier = new SimpleDoubleProperty(1.0F);



    // GENERAL \\
    public String path_selected = "";
    public String file_selected = "";
    public String basefile_selected = ""; //TODO CHANGE ME

    // DRAWING \\\

    public DrawingBotV3() {
        INSTANCE = this;
    }

    public float getDrawingAreaWidthMM(){
        return drawingAreaWidth.getValue() * drawingAreaUnits.get().convertToMM;
    }

    public float getDrawingAreaHeightMM(){
        return drawingAreaHeight.getValue() * drawingAreaUnits.get().convertToMM;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void settings() {
        size(400, 400, FX2D);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected PSurface initSurface() {
        surface = super.initSurface();
        canvas = (Canvas) surface.getNative();
        Scene oldScene = canvas.getScene();
        Stage stage = (Stage) oldScene.getWindow();
        try {
            controller = new FXController();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/userinterface.fxml")); // abs path to fxml file
            loader.setController(controller);
            final Parent sceneFromFXML = loader.load();

            final Scene newScene = new Scene(sceneFromFXML, stage.getWidth(), stage.getHeight(), false, SceneAntialiasing.BALANCED);
            //TODO ADD CANVAS, TO TAKE THE RENDERING FROM THE OTHER CANVAS...
            controller.viewportStackPane.setOnMousePressed(this::mousePressedJavaFX);
            controller.viewportStackPane.setOnMouseDragged(this::mouseDraggedJavaFX);
            controller.viewportStackPane.getChildren().add(canvas);
            canvas.widthProperty().unbind();
            canvas.heightProperty().unbind();
            Platform.runLater(() -> stage.setScene(newScene));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return surface;
    }

    @Override
    public void setup() {
        frame.setLocation(200, 200);
        surface.setResizable(true);
        surface.setTitle(appName + ", Version: " + appVersion);

        plottingThread = new PlottingThread();
        plottingThread.setDaemon(true);
        plottingThread.start();

        DrawingSet defaultSet = DrawingRegistry.INSTANCE.getDefaultSet().copy();
        observableDrawingSet = new ObservableDrawingSet(defaultSet);
        controller.penTableView.setItems(DrawingBotV3.INSTANCE.observableDrawingSet.pens);

        colorMode(RGB);
        frameRate(1000);
        //randomSeed(millis());
        randomSeed(3);

    }

    public void updateUI(){
        controller.progressBarGeneral.setProgress(PlottingThread.progress);
        controller.progressBarLabel.setText(PlottingThread.status);
        if(getSelectedTask() != null){
            controller.labelPlottedLines.setText(NumberFormat.getNumberInstance().format(getSelectedTask().plottedDrawing.plottedLines.size()) + " lines");
            controller.labelElapsedTime.setText(getSelectedTask().getElapsedTime()/1000 + " s");
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public int renderedLines = 1;

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
        PlottingTask renderedTask = getSelectedTask();

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
            //controller.viewportStackPane.setMaxWidth(renderedTask.img_reference.width*canvasScaling);
            //controller.viewportStackPane.setMaxHeight(renderedTask.img_reference.height*canvasScaling);

            Platform.runLater(() -> {
                controller.viewportScrollPane.setHvalue(0.5);
                controller.viewportScrollPane.setVvalue(0.5);
            });
            canvasNeedsUpdate = false;

            lastDrawn = renderedTask;
            lastState = renderedTask.stage;
            return;
        }
        updateCanvasScaling();
        markRenderDirty = false;

        switch (renderedTask.stage){
            case QUEUED:
            case LOADING_IMAGE:
                break;
            case PRE_PROCESSING:
                //show the original image while the user is waiting
                if(shouldRedraw){
                    background(255, 255, 255);
                    if(renderedTask.img_reference != null){
                        image(renderedTask.img_reference, 0, 0);
                    }
                }
                break;
            case PATH_FINDING:
                if(changedTask || changedState){//MUST NOT ALLOW MOUSE MOVEMENT TO AFFECT TODO ?
                    background(255, 255, 255);
                    renderedLines = 1;
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
                //TODO ?
                break;
            case LOGGING:
                // NULL
                break;
            case FINISHED:
                switch (display_mode){
                    case DRAWING:
                        if(shouldRedraw){
                            background(255, 255, 255);
                            renderedLines = 1;
                            PlottingThread.setThreadStatus("Drawing");
                            PlottingThread.setThreadProgress(0);
                            drawingTime = System.currentTimeMillis();
                        }
                        if(renderedTask.plottedDrawing.getDisplayedLineCount() != 0 && renderedLines < renderedTask.plottedDrawing.getDisplayedLineCount()){
                            int next = Math.min(renderedLines + 1000, renderedTask.plottedDrawing.getPlottedLineCount());
                            renderedTask.plottedDrawing.renderLines(renderedLines, next);
                            renderedLines = next;
                            PlottingThread.setThreadProgress((double)renderedLines / renderedTask.plottedDrawing.getDisplayedLineCount());
                        }
                        if(renderedTask.plottedDrawing.getPlottedLineCount()-1 == renderedLines){
                            long time = System.currentTimeMillis();
                            println("Drawing Took: " + (time-drawingTime) + " ms");
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
                            renderedLines = 1;

                            PlottingThread.setThreadStatus("Drawing");
                        }
                        if(renderedTask.plottedDrawing.getDisplayedLineCount() != 0 && renderedLines < renderedTask.plottedDrawing.getDisplayedLineCount()){
                            int pen = controller.penTableView.getSelectionModel().getSelectedIndex();
                            int next = Math.min(renderedLines + 10000, renderedTask.plottedDrawing.getDisplayedLineCount());
                            renderedTask.plottedDrawing.renderLinesForPen(renderedLines, next, Math.max(pen, 0));
                            renderedLines = next;
                            PlottingThread.setThreadProgress((double)renderedLines / renderedTask.plottedDrawing.getDisplayedLineCount());
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
        if(getSelectedTask() != null && getSelectedTask().img_reference != null){
            double screen_scale_x = controller.viewportScrollPane.getWidth() / ((float)getSelectedTask().img_reference.width*canvasScaling);
            double screen_scale_y = controller.viewportScrollPane.getHeight() / ((float)getSelectedTask().img_reference.height*canvasScaling);
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
        if(true){ //MOVE MAJORITY TO BUTTONS
            return;
        }
        PlottingTask renderedTask = getSelectedTask();

        if (keyCode == CONTROL) { ctrl_down = true; }

        /*
        if (keyCode == 49 && ctrl_down && pen_count > 0) { display_mode = "pen";  pen_selected = 0; }  // ctrl 1
        if (keyCode == 50 && ctrl_down && pen_count > 1) { display_mode = "pen";  pen_selected = 1; }  // ctrl 2
        if (keyCode == 51 && ctrl_down && pen_count > 2) { display_mode = "pen";  pen_selected = 2; }  // ctrl 3
        if (keyCode == 52 && ctrl_down && pen_count > 3) { display_mode = "pen";  pen_selected = 3; }  // ctrl 4
        if (keyCode == 53 && ctrl_down && pen_count > 4) { display_mode = "pen";  pen_selected = 4; }  // ctrl 5
        if (keyCode == 54 && ctrl_down && pen_count > 5) { display_mode = "pen";  pen_selected = 5; }  // ctrl 6
        if (keyCode == 55 && ctrl_down && pen_count > 6) { display_mode = "pen";  pen_selected = 6; }  // ctrl 7
        if (keyCode == 56 && ctrl_down && pen_count > 7) { display_mode = "pen";  pen_selected = 7; }  // ctrl 8
        if (keyCode == 57 && ctrl_down && pen_count > 8) { display_mode = "pen";  pen_selected = 8; }  // ctrl 9
        if (keyCode == 48 && ctrl_down && pen_count > 9) { display_mode = "pen";  pen_selected = 9; }  // ctrl 0
        */
        if (key == ']') { renderedTask.screen_scale *= 1.05; }
        if (key == '[') { renderedTask.screen_scale *= 1 / 1.05; }

        //increase percentage of lines drawn with given line
        if (key == '1') { getCompletedTask().plottedDrawing.adjustDistribution(0, 1.1); }
        if (key == '2') { getCompletedTask().plottedDrawing.adjustDistribution(1, 1.1); }
        if (key == '3') { getCompletedTask().plottedDrawing.adjustDistribution(2, 1.1); }
        if (key == '4') { getCompletedTask().plottedDrawing.adjustDistribution(3, 1.1); }
        if (key == '5') { getCompletedTask().plottedDrawing.adjustDistribution(4, 1.1); }
        if (key == '6') { getCompletedTask().plottedDrawing.adjustDistribution(5, 1.1); }
        if (key == '7') { getCompletedTask().plottedDrawing.adjustDistribution(6, 1.1); }
        if (key == '8') { getCompletedTask().plottedDrawing.adjustDistribution(7, 1.1); }
        if (key == '9') { getCompletedTask().plottedDrawing.adjustDistribution(8, 1.1); }
        if (key == '0') { getCompletedTask().plottedDrawing.adjustDistribution(9, 1.1); }

        //decrease the percentage of lines drawn by a specific line
        if (key == '!') { getCompletedTask().plottedDrawing.adjustDistribution(0, 0.9); }
        if (key == '@') { getCompletedTask().plottedDrawing.adjustDistribution(1, 0.9); }
        if (key == '#') { getCompletedTask().plottedDrawing.adjustDistribution(2, 0.9); }
        if (key == '$') { getCompletedTask().plottedDrawing.adjustDistribution(3, 0.9); }
        if (key == '%') { getCompletedTask().plottedDrawing.adjustDistribution(4, 0.9); }
        if (key == '^') { getCompletedTask().plottedDrawing.adjustDistribution(5, 0.9); }
        if (key == '&') { getCompletedTask().plottedDrawing.adjustDistribution(6, 0.9); }
        if (key == '*') { getCompletedTask().plottedDrawing.adjustDistribution(7, 0.9); }
        if (key == '(') { getCompletedTask().plottedDrawing.adjustDistribution(8, 0.9); }
        if (key == ')') { getCompletedTask().plottedDrawing.adjustDistribution(9, 0.9); }

        if (key == 't') { getCompletedTask().plottedDrawing.setEvenDistribution(); }

        if (key == 'y') { getCompletedTask().plottedDrawing.setBlackDistribution(); }
        if (key == 'x') { GridOverlay.mouse_point(); }
       // if (key == '}' && current_copic_set < CopicPenPlugin.copic_sets.length -1) { current_copic_set++; }//FIXME
       // if (key == '{' && current_copic_set >= 1)                   { current_copic_set--; }//FIXME

        //if (key == 's') { if (state == 3) { state++; } }//FIXME - STOP!

        if (keyCode == 65 && ctrl_down)  {
            println("Holly freak, Ctrl-A was pressed!");
        }
        if (key == '9') {
            getCompletedTask().plottedDrawing.adjustDistribution(0, 1.00);
            getCompletedTask().plottedDrawing.adjustDistribution(1, 1.05);
            getCompletedTask().plottedDrawing.adjustDistribution(2, 1.10);
            getCompletedTask().plottedDrawing.adjustDistribution(3, 1.15);
            getCompletedTask().plottedDrawing.adjustDistribution(4, 1.20);
            getCompletedTask().plottedDrawing.adjustDistribution(5, 1.25);
            getCompletedTask().plottedDrawing.adjustDistribution(6, 1.30);
            getCompletedTask().plottedDrawing.adjustDistribution(7, 1.35);
            getCompletedTask().plottedDrawing.adjustDistribution(8, 1.40);
            getCompletedTask().plottedDrawing.adjustDistribution(9, 1.45);
        }
        if (key == '0') {
            getCompletedTask().plottedDrawing.adjustDistribution(0, 1.00);
            getCompletedTask().plottedDrawing.adjustDistribution(1, 0.95);
            getCompletedTask().plottedDrawing.adjustDistribution(2, 0.90);
            getCompletedTask().plottedDrawing.adjustDistribution(3, 0.85);
            getCompletedTask().plottedDrawing.adjustDistribution(4, 0.80);
            getCompletedTask().plottedDrawing.adjustDistribution(5, 0.75);
            getCompletedTask().plottedDrawing.adjustDistribution(6, 0.70);
            getCompletedTask().plottedDrawing.adjustDistribution(7, 0.65);
            getCompletedTask().plottedDrawing.adjustDistribution(8, 0.60);
            getCompletedTask().plottedDrawing.adjustDistribution(9, 0.55);
        }

        /*
        if (key == '<') {
            int delta = -10000;
            getCompletedTask().display_line_count = getCompletedTask().display_line_count + delta;
            getCompletedTask().display_line_count = constrain(getCompletedTask().display_line_count, 0, getCompletedTask().plottedDrawing.getPlottedLineCount());
            println("display_line_count: " + getCompletedTask().display_line_count);
        }
        if (key == '>') {
            int delta = 10000;
            getCompletedTask().display_line_count = (int)(getCompletedTask().display_line_count + delta);
            getCompletedTask().display_line_count = constrain(getCompletedTask().display_line_count, 0, getCompletedTask().plottedDrawing.getPlottedLineCount());
            println("display_line_count: " + getCompletedTask().display_line_count);
        }

         */
        if (key == CODED) {
            double delta = 0.01;
            double currentH = controller.viewportScrollPane.getHvalue();
            double currentV = controller.viewportScrollPane.getVvalue();
            if (keyCode == UP)    {
                controller.viewportScrollPane.setVvalue(currentV + delta);
            }
            if (keyCode == DOWN)  {
                controller.viewportScrollPane.setVvalue(currentV - delta);
            }
            if (keyCode == RIGHT) {
                controller.viewportScrollPane.setHvalue(currentH + delta);
            }
            if (keyCode == LEFT)  {
                controller.viewportScrollPane.setHvalue(currentH - delta);
            }
        }

        //TODO MAKE POST PRECESSING TASKS FOR PLOTTING THREAD
        getCompletedTask().plottedDrawing.normalizeDistribution();
        getCompletedTask().plottedDrawing.distributePenChangesAccordingToPercentages();
        //surface.setSize(img.width, img.height);
        reRender();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // TASKS

    public PlottingTask getActiveTask(){
        return plottingThread.activeTask;
    }

    public PlottingTask getCompletedTask(){
        return plottingThread.completedTask;
    }

    public PlottingTask getSelectedTask(){ //TODO MAKE USER SELECT
        if(plottingThread == null){
            return null;
        }
        return plottingThread.activeTask == null ? plottingThread.completedTask : plottingThread.activeTask;
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
        double relativeX = (pressX - event.getX()) / canvas.getWidth();
        double relativeY = (pressY - event.getY()) / canvas.getHeight();

        controller.viewportScrollPane.setHvalue(locX + relativeX);
        controller.viewportScrollPane.setVvalue(locY + relativeY);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] passedArgs) {
        PApplet.main(DrawingBotV3.class, passedArgs);
    }
}
