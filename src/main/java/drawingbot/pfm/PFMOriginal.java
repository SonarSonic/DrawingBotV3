package drawingbot.pfm;

import drawingbot.DrawingBotV3;
import drawingbot.plotting.PlottingTask;
import drawingbot.helpers.ImageTools;
import drawingbot.files.GCodeExporter;

///////////////////////////////////////////////////////////////////////////////////////////////////////
// This path finding module is the basis for nearly all my drawings.
// Find the darkest average line away from my current location and move there.
///////////////////////////////////////////////////////////////////////////////////////////////////////
public class PFMOriginal extends AbstractSketchPFM {

    public int squiggles_till_first_change = 190;

    public PFMOriginal(PlottingTask task){
        super(task);
        squiggle_length = 500;
        adjustbrightness = 10;
        desired_brightness = 250;
        tests = 13;
        line_length = 30;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void pre_processing() {
        ImageTools.imageCrop(task);
        ImageTools.imageScale(task, (int)(app.getDrawingAreaWidthMM() * DrawingBotV3.image_scale));
        ImageTools.imageUnsharpen(task, task.getPlottingImage(), 4);
        ImageTools.imageUnsharpen(task, task.getPlottingImage(), 3);
        ImageTools.addImageBorder(task, "b1.png", 0, 0);
        ImageTools.addImageBorder(task, "b11.png", 0, 0);
        ImageTools.imageDesaturate(task);

        initialProgress = ImageTools.avgImageBrightness(task);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void find_darkest_neighbor(int start_x, int start_y) {
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
            bresenham_avg_brightness(start_x, start_y, line_length, (delta_angle * d) + start_angle);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void post_processing() {}

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void output_parameters() {
        GCodeExporter.gcodeComment(task, "adjustbrightness: " + adjustbrightness);
        GCodeExporter.gcodeComment(task, "squiggle_length: " + squiggle_length);
    }

}