package drawingbot.pfm;

import drawingbot.api.IPlottingTask;

import static processing.core.PApplet.*;

/**Original PFMSpiral Class*/
public class PFMSpiral extends AbstractPFM {

    public float distBetweenRings = 7;                  // Distance between rings
    public float density = 75;                          // Density
    public float ampScale = 4.5F;                       // Controls the amplitude

    protected float alpha;                              // Initial rotation
    protected float radius;                             // Current radius
    protected float aradius = 1;                        // Radius with brightness applied up
    protected float bradius = 1;                        // Radius with brightness applied down
    protected float b;                                  // Sampled brightness
    protected float x, y, xa, ya, xb, yb;               // Current X and Y + jittered X and Y
    protected float k;                                  // Current radius
    protected float endRadius;                          // Largest value the spiral needs to cover the image
    protected int mask = 240;                           // This color will not be drawn (WHITE)

    @Override
    public int getColourMode() {
        return 2;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    public void doProcess(IPlottingTask task) {
        radius = distBetweenRings /2;
        k = density/radius;
        alpha = k;
        radius += distBetweenRings /(360/k);
        
        // When have we reached the far corner of the image?
        // TODO: this will have to change if not centered
        endRadius = sqrt(pow((task.getPixelData().getWidth()/2F), 2)+pow((task.getPixelData().getHeight()/2F), 2));

        // Calculates the first point.  Currently just the center.
        // TODO: Allow for ajustable center
        task.movePenUp();
        x =  radius*cos(radians(alpha))+task.getPixelData().getWidth()/2F;
        y = -radius*sin(radians(alpha))+task.getPixelData().getHeight()/2F;
        task.moveAbsolute(x, y);
        xa = 0;
        xb = 0;
        ya = 0;
        yb = 0;

        // Have we reached the far corner of the image?
        while (radius < endRadius) {
            k = (density/2)/radius;
            alpha += k;
            radius += distBetweenRings /(360/k);
            x =  radius*cos(radians(alpha))+task.getPixelData().getWidth()/2F;
            y = -radius*sin(radians(alpha))+task.getPixelData().getHeight()/2F;

            // Are we within the the image?
            // If so check if the shape is open. If not, open it
            if ((x>=0) && (x<task.getPixelData().getWidth()) && (y>0) && (y<task.getPixelData().getHeight())) {

                // Get the color and brightness of the sampled pixel
                b = task.getPixelData().getBrightness((int)x, (int)y);
                b = map (b, 0, 255, distBetweenRings *ampScale, 0);

                // Move up according to sampled brightness
                aradius = radius+(b/ distBetweenRings);
                xa =  aradius*cos(radians(alpha))+task.getPixelData().getWidth()/2F;
                ya = -aradius*sin(radians(alpha))+task.getPixelData().getHeight()/2F;

                // Move down according to sampled brightness
                k = (density/2)/radius;
                alpha += k;
                radius += distBetweenRings /(360/k);
                bradius = radius-(b/ distBetweenRings);
                xb =  bradius*cos(radians(alpha))+task.getPixelData().getWidth()/2F;
                yb = -bradius*sin(radians(alpha))+task.getPixelData().getHeight()/2F;

                // If the sampled color is the mask color do not write to the shape
                if (mask <= b) {
                    task.movePenUp();
                } else {
                    task.movePenDown();
                }
            } else {
                // We are outside of the image
                task.movePenUp();
            }

            int pen_number = (int)(map(b, 0, 255, 0, task.getTotalPens()));
            task.setActivePen(pen_number);
            task.moveAbsolute(xa, ya);
            task.moveAbsolute(xb, yb);


            float startRadius = distBetweenRings /2;
            task.updateProgess(radius-startRadius, endRadius-startRadius);
        }

        task.movePenUp();
        task.finishProcess();
    }

}