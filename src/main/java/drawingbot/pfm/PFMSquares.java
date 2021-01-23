package drawingbot.pfm;///////////////////////////////////////////////////////////////////////////////////////////////////////
// This path finding module makes some wavy squares
///////////////////////////////////////////////////////////////////////////////////////////////////////

import drawingbot.plotting.PlottingTask;
import drawingbot.helpers.ImageTools;
import drawingbot.helpers.AlgorithmHelper;
import drawingbot.files.GCodeExporter;
import processing.core.PImage;

import java.awt.*;
import java.util.ArrayList;

import static processing.core.PApplet.*;

public class PFMSquares extends PFM {

    public final int squiggle_length = 1000;      // How often to lift the pen
    public final int adjustbrightness = 9;        // How fast it moves from dark to light, over-draw
    public final float desired_brightness = 250;  // How long to process.  You can always stop early with "s" key

    public int tests = 4;                  // Reasonable values:  13 for development, 720 for final
    public int line_length = 30;           // Reasonable values:  3 through 100

    public int squiggle_count;
    public int darkest_x;
    public int darkest_y;
    public float darkest_value;
    public float darkest_neighbor = 256;

    private float initialProgress;
    private float progress;

    public PFMSquares(PlottingTask task){
        super(task);
    }

    @Override
    public float progress() {
        return progress;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public void pre_processing() {
        ImageTools.imageCrop(task);
        ImageTools.imageScale(task, 1000);
        ImageTools.imageUnsharpen(task, task.getPlottingImage(), 3);
        ImageTools.addImageBorder(task, "b6.png", 0, 0);
        ImageTools.imageDesaturate(task);

        initialProgress = ImageTools.avgImageBrightness(task);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public void find_path() {
        find_squiggle();

        float avgBrightness = ImageTools.avgImageBrightness(task);
        progress = (avgBrightness-initialProgress) / (desired_brightness-initialProgress);
        if (avgBrightness > desired_brightness ) {
            finish();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    private void find_squiggle() {
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
        img2 = app.createImage(task.getPlottingImage().width / area_size, task.getPlottingImage().height / area_size, RGB);
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
        float start_angle;
        float delta_angle;

        start_angle = 36 + degrees( ( sin(radians(start_x/9F+46F)) + cos(radians(start_y/26F+26F)) ));
        delta_angle = 360.0F / (float)tests;

        for (int d=0; d<tests; d++) {
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
    public void post_processing() {
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public void output_parameters() {
        GCodeExporter.gcodeComment(task, "adjustbrightness: " + adjustbrightness);
        GCodeExporter.gcodeComment(task, "squiggle_length: " + squiggle_length);
    }

}