package drawingbot.pfm;///////////////////////////////////////////////////////////////////////////////////////////////////////
// Path finding module:  https://github.com/krummrey/SpiralFromImage
//
// Issues:
//    Transparencys currently do not work as a mask colour
///////////////////////////////////////////////////////////////////////////////////////////////////////

import drawingbot.DrawingBotV3;
import drawingbot.PlottingTask;
import drawingbot.helpers.ImageTools;
import drawingbot.helpers.GCodeHelper;

import static processing.core.PApplet.*;

public class PFMSpiral extends PFM {

    public PFMSpiral(PlottingTask task){
        super(task);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public void pre_processing() {
        ImageTools.image_crop(task);
        ImageTools.image_scale(task, 1000);
        ImageTools.image_unsharpen(task, task.getPlottingImage(), 3);
        ImageTools.image_boarder(task, "b6.png", 0, 0);
        ImageTools.image_desaturate(task);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public void find_path() {
        int c = 0;                                  // Sampled color
        float b;                                    // Sampled brightness
        float dist = 7;                             // Distance between rings
        float radius = dist/2;                      // Current radius
        float aradius = 1;                          // Radius with brighness applied up
        float bradius = 1;                          // Radius with brighness applied down
        float alpha;                                // Initial rotation
        float density = 75;                         // Density
        float ampScale = 4.5F;                      // Controls the amplitude
        float x, y, xa, ya, xb, yb;                 // Current X and Y + jittered X and Y
        float k;                                    // Current radius
        float endRadius;                            // Largest value the spiral needs to cover the image
        int mask = app.color(240, 240, 240);        // This color will not be drawn (WHITE)

        k = density/radius;
        alpha = k;
        radius += dist/(360/k);

        // When have we reached the far corner of the image?
        // TODO: this will have to change if not centered
        endRadius = sqrt(pow((task.getPlottingImage().width/2F), 2)+pow((task.getPlottingImage().height/2F), 2));

        // Calculates the first point.  Currently just the center.
        // TODO: Allow for ajustable center
        GCodeHelper.pen_up(task);
        x =  radius*cos(radians(alpha))+task.getPlottingImage().width/2F;
        y = -radius*sin(radians(alpha))+task.getPlottingImage().height/2F;
        GCodeHelper.move_abs(task, 0, x, y);
        xa = 0;
        xb = 0;
        ya = 0;
        yb = 0;

        // Have we reached the far corner of the image?
        while (radius < endRadius) {
            k = (density/2)/radius;
            alpha += k;
            radius += dist/(360/k);
            x =  radius*cos(radians(alpha))+task.getPlottingImage().width/2F;
            y = -radius*sin(radians(alpha))+task.getPlottingImage().height/2F;

            // Are we within the the image?
            // If so check if the shape is open. If not, open it
            if ((x>=0) && (x<task.getPlottingImage().width) && (y>0) && (y<task.getPlottingImage().height)) {

                // Get the color and brightness of the sampled pixel
                c =task.getPlottingImage().get((int)(x), (int)(y));
                b = app.brightness(c);
                b = map (b, 0, 255, dist*ampScale, 0);

                // Move up according to sampled brightness
                aradius = radius+(b/dist);
                xa =  aradius*cos(radians(alpha))+task.getPlottingImage().width/2F;
                ya = -aradius*sin(radians(alpha))+task.getPlottingImage().height/2F;

                // Move down according to sampled brightness
                k = (density/2)/radius;
                alpha += k;
                radius += dist/(360/k);
                bradius = radius-(b/dist);
                xb =  bradius*cos(radians(alpha))+task.getPlottingImage().width/2F;
                yb = -bradius*sin(radians(alpha))+task.getPlottingImage().height/2F;

                // If the sampled color is the mask color do not write to the shape
                if (app.brightness(mask) <= app.brightness(c)) {
                    GCodeHelper.pen_up(task);
                } else {
                    GCodeHelper.pen_down(task);
                }
            } else {
                // We are outside of the image
                GCodeHelper.pen_up(task);
            }

            int pen_number = (int)(map(app.brightness(c), 0, 255, 0, app.pen_count-1)+0.5);
            GCodeHelper.move_abs(task, pen_number, xa, ya);
            GCodeHelper.move_abs(task, pen_number, xb, yb);
        }

        GCodeHelper.pen_up(task);
        finish();
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public void post_processing() {
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public void output_parameters() {
        //gcode_comment("dist: " + dist);
        //gcode_comment("ampScale: " + ampScale);
    }

}