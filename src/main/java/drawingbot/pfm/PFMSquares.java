package drawingbot.pfm;

import drawingbot.plotting.PlottingTask;
import drawingbot.helpers.ImageTools;
import drawingbot.files.GCodeExporter;

import static processing.core.PApplet.*;

///////////////////////////////////////////////////////////////////////////////////////////////////////
// This path finding module makes some wavy squares
///////////////////////////////////////////////////////////////////////////////////////////////////////
public class PFMSquares extends AbstractSketchPFM {

    public PFMSquares(PlottingTask task){
        super(task);
        squiggle_length = 1000;
        adjustbrightness = 9;
        desired_brightness = 250;
        tests = 4;
        line_length = 30;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void pre_processing() {
        ImageTools.imageCrop(task);
        ImageTools.imageScale(task, 1000);
        ImageTools.imageUnsharpen(task, task.getPlottingImage(), 3);
        ImageTools.addImageBorder(task, "b6.png", 0, 0);
        ImageTools.imageDesaturate(task);

        initialProgress = ImageTools.avgImageBrightness(task);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void find_darkest_neighbor(int start_x, int start_y) {
        darkest_neighbor = 257;
        float start_angle;
        float delta_angle;

        start_angle = 36 + degrees((sin(radians(start_x/9F+46F)) + cos(radians(start_y/26F+26F))));
        delta_angle = 360.0F / (float)tests;

        for (int d = 0; d < tests; d ++) {
            bresenham_avg_brightness(start_x, start_y, line_length, (delta_angle * d) + start_angle);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void post_processing() {}

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void output_parameters() {
        task.comment("adjustbrightness: " + adjustbrightness);
        task.comment("squiggle_length: " + squiggle_length);
    }

}