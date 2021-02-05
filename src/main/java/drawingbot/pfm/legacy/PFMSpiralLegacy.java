package drawingbot.pfm.legacy;

import drawingbot.pfm.AbstractPFM;
import drawingbot.plotting.PlottingTask;

import static processing.core.PApplet.*;

/**Original PFMSpiral Class*/
class PFMSpiralLegacy extends AbstractPFM {

    public int c = 0;                                  // Sampled color
    public float b;                                    // Sampled brightness
    public float dist = 7;                             // Distance between rings
    public float radius = dist/2;                      // Current radius
    public float aradius = 1;                          // Radius with brighness applied up
    public float bradius = 1;                          // Radius with brighness applied down
    public float alpha;                                // Initial rotation
    public float density = 75;                         // Density
    public float ampScale = 4.5F;                      // Controls the amplitude
    public float x, y, xa, ya, xb, yb;                 // Current X and Y + jittered X and Y
    public float k;                                    // Current radius
    public float endRadius;                            // Largest value the spiral needs to cover the image
    public int mask = app.color(240, 240, 240);        // This color will not be drawn (WHITE)

    @Override
    public float progress() {
        float startRadius = dist/2;
        return (radius-startRadius) / (endRadius-startRadius);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public void preProcess() {
        ImageToolsLegacy.imageCrop(task);
        ImageToolsLegacy.imageScale(task, 1000);
        ImageToolsLegacy.imageUnsharpen(task, task.getPlottingImage(), 3);
        ImageToolsLegacy.addImageBorder(task, "b6.png", 0, 0);
        ImageToolsLegacy.imageDesaturate(task);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public void doProcess() {

        k = density/radius;
        alpha = k;
        radius += dist/(360/k);

        // When have we reached the far corner of the image?
        // TODO: this will have to change if not centered
        endRadius = sqrt(pow((task.getPlottingImage().width/2F), 2)+pow((task.getPlottingImage().height/2F), 2));

        // Calculates the first point.  Currently just the center.
        // TODO: Allow for ajustable center
        task.penUp();
        x =  radius*cos(radians(alpha))+task.getPlottingImage().width/2F;
        y = -radius*sin(radians(alpha))+task.getPlottingImage().height/2F;
        task.moveAbs(0, x, y);
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
                    task.penUp();
                } else {
                    task.penDown();
                }
            } else {
                // We are outside of the image
                task.penUp();
            }

            int pen_number = (int)(map(app.brightness(c), 0, 255, 0, task.plottedDrawing.getPenCount()));
            task.moveAbs(pen_number, xa, ya);
            task.moveAbs(pen_number, xb, yb);
        }

        task.penUp();
        finish();
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public void postProcess() {}

}