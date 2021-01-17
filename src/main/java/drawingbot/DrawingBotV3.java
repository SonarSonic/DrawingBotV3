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

import com.formdev.flatlaf.FlatDarkLaf;
import drawingbot.helpers.*;
import drawingbot.pfm.PFMLoaders;
import drawingbot.utils.EnumDisplayMode;
import processing.core.PApplet;
import processing.core.PImage;

//TODO FIX BUG WITH PFMOriginal moving when it is redrawn.
public class DrawingBotV3 extends PApplet {

    public static DrawingBotV3 INSTANCE;

    // GUI \\
    DrawingBotV3Frame controlFrame;

    // CONSTANTS \\
    public static final String appVersion = "1.0.0";
    public static final float INCHES_TO_MILLIMETRES = 25.4F;
    public static final float paper_size_x = 200; //mm, papers width
    public static final float paper_size_y = 200; //mm, papers height
    public static final float image_size_x = 180; //mm, final image width
    public static final float image_size_y = 180; //mm, final image height
    public static final float paper_top_to_origin = 0F; //mm, make smaller to move drawing down on paper
    public static final float pen_width = 0.65F; //mm, determines image_scale, reduce, if solid black areas are speckled with white holes.
    public static final int pen_count = 6;
    public static final char gcode_decimal_seperator = '.';
    public static final int gcode_decimals = 2;             // Number of digits right of the decimal point in the drawingbot.gcode files.
    public static final int svg_decimals = 2;               // Number of digits right of the decimal point in the SVG file.
    public static final float grid_scale = 10.0F;           // Use 10.0 for centimeters, 25.4 for inches, and between 444 and 529.2 for cubits.

    // PROGRAM \\
    //public int state = 1;

    // IMAGE \\

    //PATH FINDING \\
    public int current_pfm = 0;

    // PEN SETS \\
    public int pen_selected = 0;
    public int current_copic_set = 14;

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
        size(1415, 900, P3D);
    }

    @Override
    public void setup() {
        controlFrame = new DrawingBotV3Frame();



        frame.setLocation(200, 200);
        surface.setResizable(true);
        surface.setTitle("DrawingBotV3, Version: " + appVersion);

        colorMode(RGB);
        frameRate(999);
        //randomSeed(millis());
        randomSeed(3);
        copic = new CopicPenHelper();

        //TODO REMOVE ME
        path_selected = "E:\\04_Personal\\Processing Projects - GitHub\\Drawbot_image_to_gcode_v2\\src\\main\\resources\\pics\\weirdmnesss.jpg";

        /*
        // If the clipboard contains a URL, try to download the picture instead of using local storage.
        String url = GClip.paste();
        if (match(url.toLowerCase(), "^https?:...*(jpg|png)") != null) {
            println("Image URL found on clipboard: "+ url);
            path_selected = url;
            state++;
        } else {
            println("image URL not found on clipboard");
            selectInput("Select an image to process:", "fileSelected");
        }

         */

        PImage img_plotting = loadImage(path_selected, "jpeg");  // Load the image into the program
        PlottingTask newTask = new PlottingTask(this, PFMLoaders.values()[current_pfm], img_plotting);
        TaskQueue.addTask(newTask);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void draw() {

        //!= essential to see the drawing in action
        background(255, 255, 255);



        scale(screen_scale);
        translate(mx, my);
        rotate(HALF_PI*screen_rotate);

        TaskQueue.plot();

        if(getActiveTask() != null && getActiveTask().isTaskFinished()){
           render_all();
           noLoop();
        }


    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void fileSelected(File selection) {
        if (selection == null) {
            println("no image file selected, exiting program.");
            exit();
        } else {
            path_selected = selection.getAbsolutePath();
            file_selected = selection.getName();
            String[] fileparts = split(file_selected, '.');
            basefile_selected = fileparts[0];
            println("user selected: " + path_selected);
            //println("user selected: " + file_selected);
            //println("user selected: " + basefile_selected);
            //state++; //TODO
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    void render_all() {
        println("render_all: " + display_mode + ", " + getActiveTask().display_line_count + " lines, with pen set " + current_copic_set);

        switch (display_mode){
            case DRAWING:
                getActiveTask().plottedDrawing.render_some(getActiveTask().display_line_count);
                break;
            case ORIGINAL:
                image(getActiveTask().getOriginalImage(), 0, 0);
                break;
            case REFERENCE:
                image(getActiveTask().getReferenceImage(), 0, 0);
                break;
            case LIGHTENED:
                image(getActiveTask().getPlottingImage(), 0, 0);
                break;
            case PEN:
                getActiveTask().plottedDrawing.render_one_pen(getActiveTask().display_line_count, pen_selected);
                break;
        }
        ScalingHelper.grid();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void keyReleased() {
        if (keyCode == CONTROL) { ctrl_down = false; }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void keyPressed() {
        if(getActiveTask() == null){
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

        if (key == '1') { DrawingTools.adjustDistribution(getActiveTask(), 0, 1.1); }
        if (key == '2') { DrawingTools.adjustDistribution(getActiveTask(),1, 1.1); }
        if (key == '3') { DrawingTools.adjustDistribution(getActiveTask(),2, 1.1); }
        if (key == '4') { DrawingTools.adjustDistribution(getActiveTask(),3, 1.1); }
        if (key == '5') { DrawingTools.adjustDistribution(getActiveTask(),4, 1.1); }
        if (key == '6') { DrawingTools.adjustDistribution(getActiveTask(),5, 1.1); }
        if (key == '7') { DrawingTools.adjustDistribution(getActiveTask(),6, 1.1); }
        if (key == '8') { DrawingTools.adjustDistribution(getActiveTask(),7, 1.1); }
        if (key == '9') { DrawingTools.adjustDistribution(getActiveTask(),8, 1.1); }
        if (key == '0') { DrawingTools.adjustDistribution(getActiveTask(),9, 1.1); }

        if (key == '!') { DrawingTools.adjustDistribution(getActiveTask(),0, 0.9); }
        if (key == '@') { DrawingTools.adjustDistribution(getActiveTask(),1, 0.9); }
        if (key == '#') { DrawingTools.adjustDistribution(getActiveTask(),2, 0.9); }
        if (key == '$') { DrawingTools.adjustDistribution(getActiveTask(),3, 0.9); }
        if (key == '%') { DrawingTools.adjustDistribution(getActiveTask(),4, 0.9); }
        if (key == '^') { DrawingTools.adjustDistribution(getActiveTask(),5, 0.9); }
        if (key == '&') { DrawingTools.adjustDistribution(getActiveTask(),6, 0.9); }
        if (key == '*') { DrawingTools.adjustDistribution(getActiveTask(),7, 0.9); }
        if (key == '(') { DrawingTools.adjustDistribution(getActiveTask(),8, 0.9); }
        if (key == ')') { DrawingTools.adjustDistribution(getActiveTask(),9, 0.9); }

        if (key == 't') { DrawingTools.set_even_distribution(getActiveTask()); }
        if (key == 'y') { DrawingTools.set_black_distribution(getActiveTask()); }
        if (key == 'x') { ScalingHelper.mouse_point(); }
        if (key == '}' && current_copic_set < CopicPenHelper.copic_sets.length -1) { current_copic_set++; }
        if (key == '{' && current_copic_set >= 1)                   { current_copic_set--; }

        //if (key == 's') { if (state == 3) { state++; } }//FIXME - STOP!

        if (keyCode == 65 && ctrl_down)  {
            println("Holly freak, Ctrl-A was pressed!");
        }
        if (key == '9') {
            DrawingTools.adjustDistribution(getActiveTask(),0, 1.00);
            DrawingTools.adjustDistribution(getActiveTask(),1, 1.05);
            DrawingTools.adjustDistribution(getActiveTask(),2, 1.10);
            DrawingTools.adjustDistribution(getActiveTask(),3, 1.15);
            DrawingTools.adjustDistribution(getActiveTask(),4, 1.20);
            DrawingTools.adjustDistribution(getActiveTask(),5, 1.25);
            DrawingTools.adjustDistribution(getActiveTask(),6, 1.30);
            DrawingTools.adjustDistribution(getActiveTask(),7, 1.35);
            DrawingTools.adjustDistribution(getActiveTask(),8, 1.40);
            DrawingTools.adjustDistribution(getActiveTask(),9, 1.45);
        }
        if (key == '0') {
            DrawingTools.adjustDistribution(getActiveTask(),0, 1.00);
            DrawingTools.adjustDistribution(getActiveTask(),1, 0.95);
            DrawingTools.adjustDistribution(getActiveTask(),2, 0.90);
            DrawingTools.adjustDistribution(getActiveTask(),3, 0.85);
            DrawingTools.adjustDistribution(getActiveTask(),4, 0.80);
            DrawingTools.adjustDistribution(getActiveTask(),5, 0.75);
            DrawingTools.adjustDistribution(getActiveTask(),6, 0.70);
            DrawingTools.adjustDistribution(getActiveTask(),7, 0.65);
            DrawingTools.adjustDistribution(getActiveTask(),8, 0.60);
            DrawingTools.adjustDistribution(getActiveTask(),9, 0.55);
        }

        if (key == '\\') { screen_scale = screen_scale_org; screen_rotate=0; mx=0; my=0; }
        if (key == '<') {
            int delta = -10000;
            getActiveTask().display_line_count = getActiveTask().display_line_count + delta;
            getActiveTask().display_line_count = constrain(getActiveTask().display_line_count, 0, getActiveTask().plottedDrawing.line_count);
            println("display_line_count: " + getActiveTask().display_line_count);
        }
        if (key == '>') {
            int delta = 10000;
            getActiveTask().display_line_count = (int)(getActiveTask().display_line_count + delta);
            getActiveTask().display_line_count = constrain(getActiveTask().display_line_count, 0, getActiveTask().plottedDrawing.line_count);
            println("display_line_count: " + getActiveTask().display_line_count);
        }
        if (key == CODED) {
            int delta = 15;
            if (keyCode == UP)    { my+= delta; };
            if (keyCode == DOWN)  { my-= delta; };
            if (keyCode == RIGHT) { mx-= delta; };
            if (keyCode == LEFT)  { mx+= delta; };
        }


        DrawingTools.normalize_distribution(getActiveTask());
        getActiveTask().plottedDrawing.distribute_pen_changes_according_to_percentages(getActiveTask().display_line_count, pen_count);
        //surface.setSize(img.width, img.height);
        redraw();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // TASKS

    public PlottingTask getActiveTask(){
        return TaskQueue.activeTask;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void action_save(){
        GCodeHelper.create_gcode_files(getActiveTask(), getActiveTask().display_line_count);
        GCodeHelper.create_gcode_test_file(getActiveTask());
        GCodeHelper.create_svg_file(getActiveTask(), getActiveTask().display_line_count);
        getActiveTask().plottedDrawing.render_to_pdf(getActiveTask().display_line_count);
        getActiveTask().plottedDrawing.render_each_pen_to_pdf(getActiveTask().display_line_count);
    }

    public void action_changePFM(PFMLoaders pfm){
        //TODO FIXME
        redraw();
    }

    public void changeDisplayMode(EnumDisplayMode mode){
        display_mode = mode;
        redraw();
    }

    public void rotate(){
        screen_rotate ++;
        if (screen_rotate == 4) { screen_rotate = 0; }

        switch(screen_rotate) {
            case 0:
                my -= getActiveTask().height();
                break;
            case 1:
                mx += getActiveTask().height();
                break;
            case 2:
                my += getActiveTask().height();
                break;
            case 3:
                mx -= getActiveTask().height();
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
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] passedArgs) {
        PApplet.main(DrawingBotV3.class, passedArgs);
        FlatDarkLaf.install(); //install dark swing theme
    }
}
