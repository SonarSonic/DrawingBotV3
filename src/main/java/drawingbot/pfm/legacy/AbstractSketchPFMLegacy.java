package drawingbot.pfm.legacy;

import drawingbot.utils.AlgorithmHelper;
import drawingbot.pfm.AbstractPFM;
import drawingbot.plotting.PlottingTask;
import processing.core.PImage;

import static processing.core.PApplet.*;
import static processing.core.PApplet.constrain;

/**Original AbstractSketchPFM Class*/
abstract class AbstractSketchPFMLegacy extends AbstractPFM {

    public int squiggle_length;      // How often to lift the pen
    public int adjustbrightness;        // How fast it moves from dark to light, over-draw
    public float desired_brightness;  // How long to process.  You can always stop early with "s" key

    public int tests;                  // Reasonable values:  13 for development, 720 for final
    public int line_length;           // Reasonable values:  3 through 100 - Impacts the amount of lines drawn

    public int squiggle_count;
    public int darkest_x;
    public int darkest_y;
    public float darkest_value;
    public float darkest_neighbor;

    protected float initialProgress;
    protected float progress;

    ///bresenham calculations
    private int sum_brightness = 0;
    private int count_brightness = 0;

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public float progress() {
        return progress;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void doProcess() {
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
            bresenham_lighten(task, x, y, darkest_x, darkest_y, adjustbrightness);
            task.moveAbs(0, darkest_x, darkest_y);
            x = darkest_x;
            y = darkest_y;
        }
        task.penUp();

        float avgBrightness = ImageToolsLegacy.avgImageBrightness(task);
        progress = (avgBrightness-initialProgress) / (desired_brightness-initialProgress);
        if(avgBrightness > desired_brightness){
            finish();
        }
    }

    protected abstract void find_darkest_neighbor(int x, int y);

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void find_darkest_area() {
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

    protected void find_darkest() {
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

    protected void bresenham_avg_brightness(int x0, int y0, float distance, float degree) {
        sum_brightness = 0;
        count_brightness = 0;
        int x1, y1;

        x1 = (int)(cos(radians(degree))*distance) + x0;
        y1 = (int)(sin(radians(degree))*distance) + y0;
        x0 = constrain(x0, 0, task.getPlottingImage().width-1);
        y0 = constrain(y0, 0, task.getPlottingImage().height-1);
        x1 = constrain(x1, 0, task.getPlottingImage().width-1);
        y1 = constrain(y1, 0, task.getPlottingImage().height-1);

        AlgorithmHelper.bresenham(x0, y0, x1, y1, this::bresenhamTest);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void bresenhamTest(int x, int y){
        int loc = x + y*task.getPlottingImage().width;
        sum_brightness += app.brightness(task.getPlottingImage().pixels[loc]);
        count_brightness++;
        if (sum_brightness / count_brightness < darkest_neighbor) {
            darkest_x = x;
            darkest_y = y;
            darkest_neighbor = (float)sum_brightness / (float)count_brightness;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void bresenham_lighten(PlottingTask task, int x0, int y0, int x1, int y1, int adjustbrightness) {
        AlgorithmHelper.bresenham(x0, y0, x1, y1, (x, y) -> ImageToolsLegacy.lightenOnePixel(task, adjustbrightness * 5, x, y));
    }
}
