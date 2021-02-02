package drawingbot.pfm.legacy;

import drawingbot.plotting.PlottingTask;

import static processing.core.PApplet.*;

///////////////////////////////////////////////////////////////////////////////////////////////////////
// This path finding module makes some wavy squares
///////////////////////////////////////////////////////////////////////////////////////////////////////
class PFMSquaresLegacy extends AbstractSketchPFMLegacy {

    public PFMSquaresLegacy(PlottingTask task){
        super(task);
        squiggle_length = 1000;
        adjustbrightness = 9;
        desired_brightness = 250;
        tests = 4;
        line_length = 30;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void preProcess() {
        ImageToolsLegacy.imageCrop(task);
        ImageToolsLegacy.imageScale(task, 1000);
        ImageToolsLegacy.imageUnsharpen(task, task.getPlottingImage(), 3);
        ImageToolsLegacy.addImageBorder(task, "b6.png", 0, 0);
        ImageToolsLegacy.imageDesaturate(task);

        initialProgress = ImageToolsLegacy.avgImageBrightness(task);
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
    public void postProcess() {}

    /////////////////////////////////////////////////////////////////////////////////////////////////////

}