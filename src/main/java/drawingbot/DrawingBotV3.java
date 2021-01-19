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
import java.io.IOException;
import java.util.Map;

import drawingbot.helpers.*;
import drawingbot.javafx.FXController;
import drawingbot.pfm.PFMLoaders;
import drawingbot.tasks.PlottingTask;
import drawingbot.tasks.PlottingThread;
import drawingbot.utils.EnumDisplayMode;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import processing.core.PApplet;
import processing.core.PSurface;

//TODO FIX BUG WITH PFMOriginal moving when it is redrawn.
public class DrawingBotV3 extends PApplet {

    public static DrawingBotV3 INSTANCE;

    // CONSTANTS \\
    public static final String appName = "DrawingBotV3";
    public static final String appVersion = "1.0.0";
    public static final float INCHES_TO_MILLIMETRES = 25.4F;
    public static final float paper_size_x = 32 * INCHES_TO_MILLIMETRES; //mm, papers width
    public static final float paper_size_y = 40 * INCHES_TO_MILLIMETRES; //mm, papers height
    public static final float image_size_x = 28 * INCHES_TO_MILLIMETRES; //mm, final image width
    public static final float image_size_y = 36 * INCHES_TO_MILLIMETRES; //mm, final image height
    public static final float paper_top_to_origin = 285; //mm, make smaller to move drawing down on paper
    public static final float pen_width = 0.5F; //mm, determines image_scale, reduce, if solid black areas are speckled with white holes.
    public static final int pen_count = 6;
    public static final char gcode_decimal_seperator = '.';
    public static final int gcode_decimals = 2;             // Number of digits right of the decimal point in the drawingbot.gcode files.
    public static final int svg_decimals = 2;               // Number of digits right of the decimal point in the SVG file.
    public static final float grid_scale = 25.4F;           // Use 10.0 for centimeters, 25.4 for inches, and between 444 and 529.2 for cubits.

    // THREADS \\
    public PlottingThread plottingThread;

    // GUI \\
    public FXController controller;


    //PATH FINDING \\
    public PFMLoaders pfmLoader = PFMLoaders.ORIGINAL;

    // PEN SETS \\
    public int pen_selected = 0;
    public int current_copic_set = 0;

    // DISPLAY \\
    public EnumDisplayMode display_mode = EnumDisplayMode.DRAWING;
    public float screen_scale;
    public float screen_scale_org;
    public int screen_rotate = 0;

    // MOUSE / INPUT VALUES \\
    public int mx = 0;
    public int my = 0;
    public int morgx = 0;
    public int morgy = 0;
    public boolean ctrl_down = false;
    public boolean is_grid_on = false;

    // GENERAL \\
    public String path_selected = "";
    public String file_selected = "";
    public String basefile_selected = ""; //TODO CHANGE ME

    // DRAWING \\\
    public CopicPenHelper copic;

    public DrawingBotV3() {
        INSTANCE = this;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void settings() {
        size(1415, 900, FX2D);
    }

    @Override
    public void setup() {
        frame.setLocation(200, 200);
        surface.setResizable(true);
        surface.setTitle(appName + ", Version: " + appVersion);

        plottingThread = new PlottingThread();
        plottingThread.start();


        colorMode(RGB);
        frameRate(999);
        //randomSeed(millis());
        randomSeed(3);
        copic = new CopicPenHelper();

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public int renderedLines = 1;
    public boolean renderDrawing = false;


    //TODO MAKE LOOP SAFE - E.G. It's always looping anyway and it just knows if it needs to do something
    @Override
    public void draw() {
        long startTime = System.currentTimeMillis();

        if(getActiveTask() != null && getActiveTask().state == 1){
            if(!renderDrawing){
                renderedLines = 1;
                renderDrawing = true;
            }
        }

        if(!renderDrawing){ //to show the drawing steps background rendering is disabled
            background(255, 255, 255);
        }

        //TODO RENDER ORIGINAL IMAGE

        scale(screen_scale);
        translate(mx, my);
        rotate(HALF_PI*screen_rotate);

        if(getActiveTask() != null && renderDrawing){
            if(getActiveTask().plottedDrawing.line_count != 0){
                getActiveTask().plottedDrawing.render_between(renderedLines, getActiveTask().plottedDrawing.line_count-1);
                renderedLines = getActiveTask().plottedDrawing.line_count-1;
                if(getActiveTask().isTaskFinished()){
                    renderedLines = 1;
                    renderDrawing = false;
                }
            }
        }

        if(getCompletedTask() != null && getCompletedTask().isTaskFinished()){

            //println("render_all: " + display_mode + ", " + getCompletedTask().display_line_count + " lines, with pen set " + current_copic_set);

            switch (display_mode){
                case DRAWING:
                    /* TODO RENDER CACHED IMAGE
                    getCompletedTask().plottedDrawing.render_between(renderedLines, Math.min(renderedLines + linesPerFrame, getCompletedTask().display_line_count));
                    renderedLines = renderedLines + linesPerFrame;
                    if(renderedLines > getCompletedTask().display_line_count){
                        renderDrawing = false;
                        noLoop();
                    }
                    */
                    break;
                case ORIGINAL:
                    image(getCompletedTask().getOriginalImage(), 0, 0);
                    break;
                case REFERENCE:
                    image(getCompletedTask().getReferenceImage(), 0, 0);
                    break;
                case LIGHTENED:
                    image(getCompletedTask().getPlottingImage(), 0, 0);
                    break;
                case PEN:
                    /* TODO GENERATE CACHED IMAGE
                    getCompletedTask().plottedDrawing.render_one_pen(getCompletedTask().display_line_count, pen_selected);
                     */
                    break;
            }
            ScalingHelper.grid();

        }
        long endTime = System.currentTimeMillis();
        long lastDrawTick = (endTime - startTime);
        //controller.progressBarLabel.setText("Draw: " + lastDrawTick + " milliseconds");


    }

    @Override
    protected PSurface initSurface() {
        surface = super.initSurface();
        final Canvas canvas = (Canvas) surface.getNative();
        final Scene oldScene = canvas.getScene();
        final Stage stage = (Stage) oldScene.getWindow();
        //canvas.snapshot() TODO SNAPSHOT
        try {
            controller = new FXController();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/userinterface.fxml")); // abs path to fxml file
            loader.setController(controller);
            final Parent sceneFromFXML = loader.load();
            final Map<String, Object> namespace = loader.getNamespace();

            final Scene newScene = new Scene(sceneFromFXML, stage.getWidth(), stage.getHeight(), false, SceneAntialiasing.BALANCED);
            final AnchorPane pane = (AnchorPane)namespace.get("pdeView"); // get element by fx:id

            pane.getChildren().add(canvas); // processing to stackPane
            canvas.widthProperty().bind(pane.widthProperty()); // bind canvas dimensions to pane
            canvas.heightProperty().bind(pane.heightProperty()); // bind canvas dimensions to pane
            Platform.runLater(() -> stage.setScene(newScene));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return surface;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void keyReleased() {
        if (keyCode == CONTROL) { ctrl_down = false; }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void keyPressed() {
        if(getCompletedTask() == null){
            return;
        }

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
        if (key == 'G') { is_grid_on = ! is_grid_on; }
        if (key == ']') { screen_scale *= 1.05; }
        if (key == '[') { screen_scale *= 1 / 1.05; }

        if (key == '1') { DrawingTools.adjustDistribution(getCompletedTask(), 0, 1.1); }
        if (key == '2') { DrawingTools.adjustDistribution(getCompletedTask(),1, 1.1); }
        if (key == '3') { DrawingTools.adjustDistribution(getCompletedTask(),2, 1.1); }
        if (key == '4') { DrawingTools.adjustDistribution(getCompletedTask(),3, 1.1); }
        if (key == '5') { DrawingTools.adjustDistribution(getCompletedTask(),4, 1.1); }
        if (key == '6') { DrawingTools.adjustDistribution(getCompletedTask(),5, 1.1); }
        if (key == '7') { DrawingTools.adjustDistribution(getCompletedTask(),6, 1.1); }
        if (key == '8') { DrawingTools.adjustDistribution(getCompletedTask(),7, 1.1); }
        if (key == '9') { DrawingTools.adjustDistribution(getCompletedTask(),8, 1.1); }
        if (key == '0') { DrawingTools.adjustDistribution(getCompletedTask(),9, 1.1); }

        if (key == '!') { DrawingTools.adjustDistribution(getCompletedTask(),0, 0.9); }
        if (key == '@') { DrawingTools.adjustDistribution(getCompletedTask(),1, 0.9); }
        if (key == '#') { DrawingTools.adjustDistribution(getCompletedTask(),2, 0.9); }
        if (key == '$') { DrawingTools.adjustDistribution(getCompletedTask(),3, 0.9); }
        if (key == '%') { DrawingTools.adjustDistribution(getCompletedTask(),4, 0.9); }
        if (key == '^') { DrawingTools.adjustDistribution(getCompletedTask(),5, 0.9); }
        if (key == '&') { DrawingTools.adjustDistribution(getCompletedTask(),6, 0.9); }
        if (key == '*') { DrawingTools.adjustDistribution(getCompletedTask(),7, 0.9); }
        if (key == '(') { DrawingTools.adjustDistribution(getCompletedTask(),8, 0.9); }
        if (key == ')') { DrawingTools.adjustDistribution(getCompletedTask(),9, 0.9); }

        if (key == 't') { DrawingTools.set_even_distribution(getCompletedTask()); }
        if (key == 'y') { DrawingTools.set_black_distribution(getCompletedTask()); }
        if (key == 'x') { ScalingHelper.mouse_point(); }
        if (key == '}' && current_copic_set < CopicPenHelper.copic_sets.length -1) { current_copic_set++; }
        if (key == '{' && current_copic_set >= 1)                   { current_copic_set--; }

        //if (key == 's') { if (state == 3) { state++; } }//FIXME - STOP!

        if (keyCode == 65 && ctrl_down)  {
            println("Holly freak, Ctrl-A was pressed!");
        }
        if (key == '9') {
            DrawingTools.adjustDistribution(getCompletedTask(),0, 1.00);
            DrawingTools.adjustDistribution(getCompletedTask(),1, 1.05);
            DrawingTools.adjustDistribution(getCompletedTask(),2, 1.10);
            DrawingTools.adjustDistribution(getCompletedTask(),3, 1.15);
            DrawingTools.adjustDistribution(getCompletedTask(),4, 1.20);
            DrawingTools.adjustDistribution(getCompletedTask(),5, 1.25);
            DrawingTools.adjustDistribution(getCompletedTask(),6, 1.30);
            DrawingTools.adjustDistribution(getCompletedTask(),7, 1.35);
            DrawingTools.adjustDistribution(getCompletedTask(),8, 1.40);
            DrawingTools.adjustDistribution(getCompletedTask(),9, 1.45);
        }
        if (key == '0') {
            DrawingTools.adjustDistribution(getCompletedTask(),0, 1.00);
            DrawingTools.adjustDistribution(getCompletedTask(),1, 0.95);
            DrawingTools.adjustDistribution(getCompletedTask(),2, 0.90);
            DrawingTools.adjustDistribution(getCompletedTask(),3, 0.85);
            DrawingTools.adjustDistribution(getCompletedTask(),4, 0.80);
            DrawingTools.adjustDistribution(getCompletedTask(),5, 0.75);
            DrawingTools.adjustDistribution(getCompletedTask(),6, 0.70);
            DrawingTools.adjustDistribution(getCompletedTask(),7, 0.65);
            DrawingTools.adjustDistribution(getCompletedTask(),8, 0.60);
            DrawingTools.adjustDistribution(getCompletedTask(),9, 0.55);
        }

        if (key == '\\') { screen_scale = screen_scale_org; screen_rotate=0; mx=0; my=0; }
        if (key == '<') {
            int delta = -10000;
            getCompletedTask().display_line_count = getCompletedTask().display_line_count + delta;
            getCompletedTask().display_line_count = constrain(getCompletedTask().display_line_count, 0, getCompletedTask().plottedDrawing.line_count);
            println("display_line_count: " + getCompletedTask().display_line_count);
        }
        if (key == '>') {
            int delta = 10000;
            getCompletedTask().display_line_count = (int)(getCompletedTask().display_line_count + delta);
            getCompletedTask().display_line_count = constrain(getCompletedTask().display_line_count, 0, getCompletedTask().plottedDrawing.line_count);
            println("display_line_count: " + getCompletedTask().display_line_count);
        }
        if (key == CODED) {
            int delta = 15;
            if (keyCode == UP)    { my+= delta; };
            if (keyCode == DOWN)  { my-= delta; };
            if (keyCode == RIGHT) { mx-= delta; };
            if (keyCode == LEFT)  { mx+= delta; };
        }


        DrawingTools.normalize_distribution(getCompletedTask());
        getCompletedTask().plottedDrawing.distribute_pen_changes_according_to_percentages(getCompletedTask().display_line_count, pen_count);
        //surface.setSize(img.width, img.height);
        redraw();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // TASKS

    public PlottingTask getActiveTask(){
        return plottingThread.activeTask;
    }

    public PlottingTask getCompletedTask(){
        return plottingThread.completedTask;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////



    public void action_save(){
        GCodeHelper.create_gcode_files(getCompletedTask(), getCompletedTask().display_line_count);
        GCodeHelper.create_gcode_test_file(getCompletedTask());
        GCodeHelper.create_svg_file(getCompletedTask(), getCompletedTask().display_line_count);
        getCompletedTask().plottedDrawing.render_to_pdf(getCompletedTask().display_line_count);
        getCompletedTask().plottedDrawing.render_each_pen_to_pdf(getCompletedTask().display_line_count);
    }

    public void action_rotate(){
        if(getCompletedTask() == null){
            return;
        }
        
        screen_rotate ++;
        if (screen_rotate == 4) { screen_rotate = 0; }

        switch(screen_rotate) {
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
        redraw();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void mousePressed() {
        morgx = mouseX - mx;
        morgy = mouseY - my;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void mouseDragged() {


        mx = mouseX-morgx;
        my = mouseY-morgy;

        redraw();
        System.out.println("mouse dragged!");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] passedArgs) {
        PApplet.main(DrawingBotV3.class, passedArgs);
    }
}
