package drawingbot.pfm;

///////////////////////////////////////////////////////////////////////////////////////////////////////
// This path finding module is the basis for nearly all my drawings.
// Find the darkest average line away from my current location and move there.
///////////////////////////////////////////////////////////////////////////////////////////////////////

import static processing.core.PApplet.*;

import drawingbot.DrawingBotV3;
import drawingbot.plotting.PlottingTask;
import drawingbot.helpers.ImageTools;
import drawingbot.helpers.AlgorithmHelper;
import drawingbot.files.GCodeExporter;
import processing.core.PImage;

import java.awt.*;
import java.util.ArrayList;

public class PFMOriginal extends PFM {

    public final int squiggle_length = 500;      // How often to lift the pen
    public final int adjustbrightness = 10;       // How fast it moves from dark to light, over-draw
    public final float desired_brightness = 250;   // How long to process.  You can always stop early with "s" key
    public final int squiggles_till_first_change = 190;

    public int tests = 13;                 // Reasonable values:  13 for development, 720 for final
    public int line_length = (int)(app.random(3, 40));  // Reasonable values:  3 through 100

    public int squiggle_count;
    public int darkest_x;
    public int darkest_y;
    public float darkest_value;
    public float darkest_neighbor = 256;

    private float initialProgress;
    private float progress;

    public PFMOriginal(PlottingTask task){
        super(task);
    }

    @Override
    public float progress() {
        return progress;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public void pre_processing() {
        ImageTools.imageCrop(task);
        ImageTools.imageScale(task, (int)(app.getDrawingAreaWidthMM() * DrawingBotV3.image_scale));
        //image_sharpen(img);
        //image_blurr(img);
        //image_unsharpen(img, 5);
        ImageTools.imageUnsharpen(task, task.getPlottingImage(), 4);
        ImageTools.imageUnsharpen(task, task.getPlottingImage(), 3);
        //image_unsharpen(img, 2);
        //image_unsharpen(img, 1);
        //image_motion_blur(img);
        //image_outline(img);
        //image_edge_detect(img);
        //ImageTools.imageSobel(task.getPlottingImage(), 1.0F, 0);
        //ImageTools.imagePosterize(task, 6);
        //image_erode();
        //image_dilate();
        //image_invert();
        //image_blur(2);
        ImageTools.addImageBorder(task, "b1.png", 0, 0);
        ImageTools.addImageBorder(task, "b11.png", 0, 0);
        ImageTools.imageDesaturate(task);

        initialProgress = ImageTools.avgImageBrightness(task);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public void find_path() {
        int x, y;

        //find_darkest();
        find_darkest_area();
        x = darkest_x;
        y = darkest_y;
        squiggle_count++;

        find_darkest_neighbor(x, y);
        task.moveAbs(0, darkest_x, darkest_y);
        task.penDown();

        for (int s = 0; s < squiggle_length; s++) {
            find_darkest_neighbor(x, y);
            AlgorithmHelper.bresenham_lighten(task, x, y, darkest_x, darkest_y, adjustbrightness);
            task.moveAbs(0, darkest_x, darkest_y);
            x = darkest_x;
            y = darkest_y;
        }
        task.penUp();

        float avgBrightness = ImageTools.avgImageBrightness(task);
        progress = (avgBrightness-initialProgress) / (desired_brightness-initialProgress);
        if(avgBrightness > desired_brightness){
            finish();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    private void find_darkest() {
        darkest_value = 257;
        int darkest_loc = 0;

        for (int loc = 0; loc < task.getPlottingImage().width * task.getPlottingImage().height; loc++) {
            float r = app.brightness(task.getPlottingImage().pixels[loc]);
            if (r < darkest_value) {
                darkest_value = r + app.random(1);
                darkest_loc = loc;
            }
        }
        darkest_x = darkest_loc % task.getPlottingImage().width;
        darkest_y = (darkest_loc-darkest_x) / task.getPlottingImage().width;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    private void find_darkest_area() {
        // Warning, Experimental:
        // Finds the darkest square area by down sampling the img into a much smaller area then finding
        // the darkest pixel within that.  It returns a random pixel within that darkest area.

        int area_size = 10;
        darkest_value = 999;
        int darkest_loc = 1;

        PImage img2;
        img2 = app.createImage(task.getPlottingImage().width / area_size, task.getPlottingImage().height / area_size, RGB); //TODO REMOVE SCALING!!!
        img2.copy(task.getPlottingImage(), 0, 0, task.getPlottingImage().width, task.getPlottingImage().height, 0, 0, img2.width, img2.height);

        for (int loc=0; loc < img2.width * img2.height; loc++) {
            float r = app.brightness(img2.pixels[loc]);

            if (r < darkest_value) {
                darkest_value = r + app.random(1);
                darkest_loc = loc;
            }
        }

        darkest_x = darkest_loc % img2.width;
        darkest_y = (darkest_loc - darkest_x) / img2.width;
        darkest_x = darkest_x * area_size + (int)(app.random(area_size));
        darkest_y = darkest_y * area_size + (int)(app.random(area_size));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    private void find_darkest_neighbor(int start_x, int start_y) {
        darkest_neighbor = 257;
        float delta_angle;
        float start_angle;

        //start_angle = random(-35, -15) + cos(radians(start_x/4+(start_y/6)))*30;
        //start_angle = random(-95, -75) + cos(radians(start_y/15))*90;
        //start_angle = 36 + degrees( ( sin(radians(start_x/9+46)) + cos(radians(start_y/26+26)) ));
        //start_angle = 34 + degrees( ( sin(radians(start_x/9+46)) + cos(radians(start_y/-7+26)) ));
        //if (squiggle_count <220) { tests = 20; } else { tests = 2; }
        //start_angle = random(20, 1);       // Cuba 1
        start_angle = app.random(-72, -52);    // Spitfire
        //start_angle = random(-120, -140);  // skier
        //start_angle = random(-360, -1);    // gradiant magic
        //start_angle = squiggle_count % 360;
        //start_angle += squiggle_count/4;
        //start_angle = -45;
        //start_angle = (squiggle_count * 37) % 360;

        //delta_angle = 180 + 10 / (float)tests;
        //delta_angle = 360.0 / (float)tests;

        if (squiggle_count < squiggles_till_first_change) {
            //line_length = int(random(3, 60));
            delta_angle = 360.0F / (float)tests;
        } else {
            //start_angle = degrees(atan2(img.height/2.0 - start_y -470, img.width/2.0 - start_x+130) )-10+90;    // wierd spiral
            //start_angle = degrees(atan2(img.height/2.0 - start_y +145, img.width/2.0 - start_x+45) )-10+90;    //cuba car
            //start_angle = degrees(atan2(img.height/2.0 - start_y +210, img.width/2.0 - start_x-100) )-10;    // italy
            delta_angle = 180F + 7F / (float)tests;
        }

        for (int d = 0; d < tests; d ++) {
            float b = bresenham_avg_brightness(start_x, start_y, line_length, (delta_angle * d) + start_angle);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    float bresenham_avg_brightness(int x0, int y0, float distance, float degree) {
        int x1, y1;
        int sum_brightness = 0;
        int count_brightness = 0;
        ArrayList<Point> pnts;

        x1 = (int)(cos(radians(degree))*distance) + x0;
        y1 = (int)(sin(radians(degree))*distance) + y0;
        x0 = constrain(x0, 0, task.getPlottingImage().width-1);
        y0 = constrain(y0, 0, task.getPlottingImage().height-1);
        x1 = constrain(x1, 0, task.getPlottingImage().width-1);
        y1 = constrain(y1, 0, task.getPlottingImage().height-1);

        pnts = AlgorithmHelper.bresenham(x0, y0, x1, y1);
        for (Point p : pnts) {
            int loc = p.x + p.y*task.getPlottingImage().width;
            sum_brightness += app.brightness(task.getPlottingImage().pixels[loc]);
            count_brightness++;
            if (sum_brightness / count_brightness < darkest_neighbor) {
                darkest_x = p.x;
                darkest_y = p.y;
                darkest_neighbor = (float)sum_brightness / (float)count_brightness;
            }
            //println(x0+","+y0+"  "+p.x+","+p.y+"  brightness:"+sum_brightness / count_brightness+"  darkest:"+darkest_neighbor+"  "+darkest_x+","+darkest_y);
        }
        //println();
        return( sum_brightness / count_brightness );
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public void post_processing() {}

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public void output_parameters() {
        GCodeExporter.gcodeComment(task, "adjustbrightness: " + adjustbrightness);
        GCodeExporter.gcodeComment(task, "squiggle_length: " + squiggle_length);
    }

}