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
import java.io.PrintWriter;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import drawingbot.helpers.*;
import drawingbot.pfm.IPFM;
import drawingbot.pfm.PFMLoaders;
import drawingbot.utils.Limit;
import drawingbot.utils.PlottedDrawing;
import processing.core.PApplet;
import processing.core.PImage;

import javax.swing.*;

public class DrawingBotV3 extends PApplet {

    public static DrawingBotV3 INSTANCE;

    // GUI \\
    DrawingBotGui controlPanel;

    int paper = color(0);
    int ballYPos=0;
    int ballWidth=20;
    int ballHeight=20;


    // CONSTANTS \\
    public final String appVersion = "1.0.0";
    public final float INCHES_TO_MILLIMETRES = 25.4F;
    public final float paper_size_x = 200; //mm, papers width
    public final float paper_size_y = 200; //mm, papers height
    public final float image_size_x = 180; //mm, final image width
    public final float image_size_y = 180; //mm, final image height
    public final float paper_top_to_origin = 0F; //mm, make smaller to move drawing down on paper
    public final float pen_width = 0.65F; //mm, determines image_scale, reduce, if solid black areas are speckled with white holes.
    public final int pen_count = 6;
    public final char gcode_decimal_seperator = '.';
    public final int gcode_decimals = 2;             // Number of digits right of the decimal point in the drawingbot.gcode files.
    public final int svg_decimals = 2;               // Number of digits right of the decimal point in the SVG file.
    public final float grid_scale = 10.0F;           // Use 10.0 for centimeters, 25.4 for inches, and between 444 and 529.2 for cubits.

    // PROGRAM \\
    public int startTime = 0;
    public int state = 1;

    //PATH FINDING \\
    public IPFM pfm;
    public int current_pfm = 0;

    // PEN SETS \\
    public int pen_selected = 0;
    public int current_copic_set = 14;

    // DISPLAY \\
    public int display_line_count;
    public String display_mode = "drawing";
    public float screen_scale;
    public float screen_scale_org;
    public int screen_rotate = 0;

    // IMAGE \\
    public PImage img_orginal;               // The original image
    public PImage img_reference;             // After pre_processing, croped, scaled, boarder, etc.  This is what we will try to draw.
    public PImage img;                       // Used during drawing for current brightness levels.  Gets damaged during drawing.

    // GCODE \\
    public float gcode_offset_x;
    public float gcode_offset_y;
    public float gcode_scale;

    // PLOTTING \\
    public float old_x = 0;
    public float old_y = 0;
    public boolean is_pen_down;

    // MOUSE / INPUT VALUES \\
    public int mx = 0;
    public int my = 0;
    public int morgx = 0;
    public int morgy = 0;
    public boolean ctrl_down = false;
    public boolean is_grid_on = false;
    public int pen_color = 0; //unused?

    // GENERAL \\
    public String path_selected = "";
    public String file_selected = "";
    public String basefile_selected = "";
    public String gcode_comments = "";

    // DRAWING \\\
    public Limit dx, dy;
    public CopicPenHelper copic;
    public PrintWriter output;
    public PlottedDrawing plottedDrawing;
    public float[] pen_distribution = new float[pen_count];



    public DrawingBotV3() {
        INSTANCE = this;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void settings() {
        size(1415, 900, P3D);
    }

    @Override
    public void setup() {
        setupGui();
        frame.setLocation(200, 200);
        surface.setResizable(true);
        surface.setTitle("DrawingBotV3, Version: " + appVersion);
        colorMode(RGB);
        frameRate(999);
        //randomSeed(millis());
        randomSeed(3);
        plottedDrawing = new PlottedDrawing();
        dx = new Limit();
        dy = new Limit();
        copic = new CopicPenHelper();
        loadPFM(PFMLoaders.values()[current_pfm]);

        //TODO REMOVE ME
        path_selected = "E:\\04_Personal\\Processing Projects\\src\\main\\resources\\pics\\weird_cat.jpg";
        state++;
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
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void draw() {
        drawGui();
        /*
        if (state != 3) {
            background(255, 255, 255);
        }

        scale(screen_scale);
        translate(mx, my);
        rotate(HALF_PI*screen_rotate);

        switch(state) {
            case 1:
                println("State=1, Waiting for filename selection");
                break;
            case 2:
                println("State=2, Setup squiggles");
                loop();
                setup_squiggles();
                startTime = millis();
                break;
            case 3:
                //println("State=3, Drawing image");
                if (display_line_count <= 1) {
                    background(255);
                }
                pfm.find_path();
                display_line_count = plottedDrawing.line_count;
                break;
            case 4:
                println("State=4, pfm.post_processing");
                pfm.post_processing();

                set_even_distribution();
                normalize_distribution();
                plottedDrawing.evenly_distribute_pen_changes(plottedDrawing.get_line_count(), pen_count);
                plottedDrawing.distribute_pen_changes_according_to_percentages(display_line_count, pen_count);

                println("elapsed time: " + (millis() - startTime) / 1000.0 + " seconds");
                display_line_count = plottedDrawing.line_count;

                GCodeHelper.gcode_comment("extreams of X: " + dx.min + " thru " + dx.max);
                GCodeHelper.gcode_comment("extreams of Y: " + dy.min + " thru " + dy.max);
                state++;
                break;
            case 5:
                render_all();
                noLoop();
                break;
            default:
                println("invalid state: " + state);
                break;
        }

         */
    }

    int timer=0;

    public void setupGui(){
        JFrame frame =new JFrame("Controls");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        controlPanel = new DrawingBotGui();
        controlPanel.setOpaque(true); //content panes must be opaque
        frame.setContentPane(controlPanel);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public void drawGui(){
        if (timer!=0)
        {
            if (millis()>=timer)
            {
                //controlPanel.jcomp1.setLabel("Again!");
                timer=0;
            }
        }

        background(paper);
        ellipse(millis()/10%width, height/2-ballYPos, ballWidth, ballHeight);
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
            state++;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setup_squiggles() {
        float   gcode_scale_x, gcode_scale_y;
        float   screen_scale_x, screen_scale_y;

        //println("setup_squiggles...");

        plottedDrawing.line_count = 0;
        //randomSeed(millis());
        img = loadImage(path_selected, "jpeg");  // Load the image into the program //TODO ALLOW OTHER EXTENSIONS
        GCodeHelper.gcode_comment("loaded image: " + path_selected);

        ImageTools.image_rotate();

        img_orginal = createImage(img.width, img.height, RGB);
        img_orginal.copy(img, 0, 0, img.width, img.height, 0, 0, img.width, img.height);

        pfm.pre_processing();
        img.loadPixels();
        img_reference = createImage(img.width, img.height, RGB);
        img_reference.copy(img, 0, 0, img.width, img.height, 0, 0, img.width, img.height);

        gcode_scale_x = image_size_x / img.width;
        gcode_scale_y = image_size_y / img.height;
        gcode_scale = min(gcode_scale_x, gcode_scale_y);
        gcode_offset_x = - (img.width * gcode_scale / 2.0F);
        gcode_offset_y = - (paper_top_to_origin - (paper_size_y - (img.height * gcode_scale)) / 2.0F);

        screen_scale_x = width / (float)img.width;
        screen_scale_y = height / (float)img.height;
        screen_scale = min(screen_scale_x, screen_scale_y);
        screen_scale_org = screen_scale;

        GCodeHelper.gcode_comment("final dimensions: " + img.width + " by " + img.height);
        GCodeHelper.gcode_comment("paper_size: " + nf(paper_size_x,0,2) + " by " + nf(paper_size_y,0,2) + "      " + nf(paper_size_x/25.4F,0,2) + " by " + nf(paper_size_y/25.4F,0,2));
        GCodeHelper.gcode_comment("drawing size max: " + nf(image_size_x,0,2) + " by " + nf(image_size_y,0,2) + "      " + nf(image_size_x/25.4F,0,2) + " by " + nf(image_size_y/25.4F,0,2));
        GCodeHelper.gcode_comment("drawing size calculated " + nf(img.width * gcode_scale,0,2) + " by " + nf(img.height * gcode_scale,0,2) + "      " + nf(img.width * gcode_scale/25.4F,0,2) + " by " + nf(img.height * gcode_scale/25.4F,0,2));
        GCodeHelper.gcode_comment("gcode_scale X:  " + nf(gcode_scale_x,0,2));
        GCodeHelper.gcode_comment("gcode_scale Y:  " + nf(gcode_scale_y,0,2));
        GCodeHelper.gcode_comment("gcode_scale:    " + nf(gcode_scale,0,2));
        pfm.output_parameters();

        state++;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    void render_all() {
        println("render_all: " + display_mode + ", " + display_line_count + " lines, with pen set " + current_copic_set);

        if (display_mode == "drawing") {
            plottedDrawing.render_some(display_line_count);
        }

        if (display_mode == "pen") {
            plottedDrawing.render_one_pen(display_line_count, pen_selected);
        }

        if (display_mode == "original") {
            image(img_orginal, 0, 0);
        }

        if (display_mode == "reference") {
            image(img_reference, 0, 0);
        }

        if (display_mode == "lightened") {
            image(img, 0, 0);
        }
        ScalingHelper.grid();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void keyReleased() {
        if (keyCode == CONTROL) { ctrl_down = false; }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void keyPressed() {
        if (keyCode == CONTROL) { ctrl_down = true; }

        if (key == 'p') {
            current_pfm ++;
            if (current_pfm >= PFMLoaders.values().length) { current_pfm = 0; }
            //display_line_count = 0;
            loadPFM(PFMLoaders.values()[current_pfm]);
            state = 2;
        }

        if (key == 'd') { display_mode = "drawing";   }
        if (key == 'O') { display_mode = "original";  }
        if (key == 'o') { display_mode = "reference";  }
        if (key == 'l') { display_mode = "lightened"; }
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
        if (key == 'G') { is_grid_on = ! is_grid_on; }
        if (key == ']') { screen_scale *= 1.05; }
        if (key == '[') { screen_scale *= 1 / 1.05; }

        if (key == '1') { adjustDistribution(0, 1.1); }
        if (key == '2') { adjustDistribution(1, 1.1); }
        if (key == '3') { adjustDistribution(2, 1.1); }
        if (key == '4') { adjustDistribution(3, 1.1); }
        if (key == '5') { adjustDistribution(4, 1.1); }
        if (key == '6') { adjustDistribution(5, 1.1); }
        if (key == '7') { adjustDistribution(6, 1.1); }
        if (key == '8') { adjustDistribution(7, 1.1); }
        if (key == '9') { adjustDistribution(8, 1.1); }
        if (key == '0') { adjustDistribution(9, 1.1); }

        if (key == '!') { adjustDistribution(0, 0.9); }
        if (key == '@') { adjustDistribution(1, 0.9); }
        if (key == '#') { adjustDistribution(2, 0.9); }
        if (key == '$') { adjustDistribution(3, 0.9); }
        if (key == '%') { adjustDistribution(4, 0.9); }
        if (key == '^') { adjustDistribution(5, 0.9); }
        if (key == '&') { adjustDistribution(6, 0.9); }
        if (key == '*') { adjustDistribution(7, 0.9); }
        if (key == '(') { adjustDistribution(8, 0.9); }
        if (key == ')') { adjustDistribution(9, 0.9); }

        if (key == 't') { set_even_distribution(); }
        if (key == 'y') { set_black_distribution(); }
        if (key == 'x') { ScalingHelper.mouse_point(); }
        if (key == '}' && current_copic_set < CopicPenHelper.copic_sets.length -1) { current_copic_set++; }
        if (key == '{' && current_copic_set >= 1)                   { current_copic_set--; }

        if (key == 's') { if (state == 3) { state++; } }
        if (keyCode == 65 && ctrl_down)  {
            println("Holly freak, Ctrl-A was pressed!");
        }
        if (key == '9') {
            adjustDistribution(0, 1.00);
            adjustDistribution(1, 1.05);
            adjustDistribution(2, 1.10);
            adjustDistribution(3, 1.15);
            adjustDistribution(4, 1.20);
            adjustDistribution(5, 1.25);
            adjustDistribution(6, 1.30);
            adjustDistribution(7, 1.35);
            adjustDistribution(8, 1.40);
            adjustDistribution(9, 1.45);
        }
        if (key == '0') {
            adjustDistribution(0, 1.00);
            adjustDistribution(1, 0.95);
            adjustDistribution(2, 0.90);
            adjustDistribution(3, 0.85);
            adjustDistribution(4, 0.80);
            adjustDistribution(5, 0.75);
            adjustDistribution(6, 0.70);
            adjustDistribution(7, 0.65);
            adjustDistribution(8, 0.60);
            adjustDistribution(9, 0.55);
        }
        if (key == 'g') {
            GCodeHelper.create_gcode_files(display_line_count);
            GCodeHelper.create_gcode_test_file ();
            GCodeHelper.create_svg_file(display_line_count);
            plottedDrawing.render_to_pdf(display_line_count);
            plottedDrawing.render_each_pen_to_pdf(display_line_count);
        }

        if (key == '\\') { screen_scale = screen_scale_org; screen_rotate=0; mx=0; my=0; }
        if (key == '<') {
            int delta = -10000;
            display_line_count = display_line_count + delta;
            display_line_count = constrain(display_line_count, 0, plottedDrawing.line_count);
            println("display_line_count: " + display_line_count);
        }
        if (key == '>') {
            int delta = 10000;
            display_line_count = (int)(display_line_count + delta);
            display_line_count = constrain(display_line_count, 0, plottedDrawing.line_count);
            println("display_line_count: " + display_line_count);
        }
        if (key == CODED) {
            int delta = 15;
            if (keyCode == UP)    { my+= delta; };
            if (keyCode == DOWN)  { my-= delta; };
            if (keyCode == RIGHT) { mx-= delta; };
            if (keyCode == LEFT)  { mx+= delta; };
        }
        if (key == 'r') {
            screen_rotate ++;
            if (screen_rotate == 4) { screen_rotate = 0; }

            switch(screen_rotate) {
                case 0:
                    my -= img.height;
                    break;
                case 1:
                    mx += img.height;
                    break;
                case 2:
                    my += img.height;
                    break;
                case 3:
                    mx -= img.height;
                    break;
            }
        }

        normalize_distribution();
        plottedDrawing.distribute_pen_changes_according_to_percentages(display_line_count, pen_count);
        //surface.setSize(img.width, img.height);
        redraw();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void adjustDistribution(int pen, double value){
        if(pen_count > pen){
            pen_distribution[pen] *= value;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    void set_even_distribution() {
        println("set_even_distribution");
        for (int p = 0; p<pen_count; p++) {
            pen_distribution[p] = display_line_count / pen_count;
            //println("pen_distribution[" + p + "] = " + pen_distribution[p]);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    void set_black_distribution() {
        println("set_black_distribution");
        for (int p=0; p<pen_count; p++) {
            pen_distribution[p] = 0;
            //println("pen_distribution[" + p + "] = " + pen_distribution[p]);
        }
        pen_distribution[0] = display_line_count;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    void normalize_distribution() {
        float total = 0;

        println();
        //println("normalize_distribution");

        for (int p=0; p<pen_count; p++) {
            total = total + pen_distribution[p];
        }

        for (int p = 0; p<pen_count; p++) {
            pen_distribution[p] = display_line_count * pen_distribution[p] / total;
            print("Pen " + p + ", ");
            System.out.printf("%-4s", CopicPenHelper.copic_sets[current_copic_set][p]);
            System.out.printf("%8.0f  ", pen_distribution[p]);

            // Display approximately one star for every percent of total
            for (int s = 0; s<(int)(pen_distribution[p]/total*100); s++) {
                print("*");
            }
            println();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void loadPFM(PFMLoaders pfm){
        this.pfm = pfm.createNewPFM();
        println("Loaded PFM: " + pfm.getName());
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
