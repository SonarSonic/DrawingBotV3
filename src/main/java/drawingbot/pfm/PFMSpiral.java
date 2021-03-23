package drawingbot.pfm;

import drawingbot.geom.basic.GLine;
import drawingbot.utils.Utils;
import org.locationtech.jts.algorithm.construct.MaximumInscribedCircle;

import java.awt.geom.Point2D;

/**https://github.com/krummrey/SpiralFromImage
 * TODO FIX SPIRAL PFM*/
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

    public double centreXScale = 0.5;
    public double centreYScale = 0.5;
    public double fillPercentage = 1.0;

    @Override
    public int getColourMode() {
        return 2;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    public void doProcess() {
        radius = distBetweenRings / 2;
        k = density / radius;
        alpha = k;
        radius += distBetweenRings / (360 / k);

        int centreX = (int)(task.getPixelData().getWidth()*centreXScale);
        int centreY = (int)(task.getPixelData().getHeight()*centreYScale);

        // find the furthest corner of the image
        double topLeft = Point2D.distance(centreX, centreY, 0, 0);
        double topRight = Point2D.distance(centreX, centreY, 0, task.getPixelData().getHeight()-1);
        double bottomLeft = Point2D.distance(centreX, centreY, task.getPixelData().getWidth()-1, 0);
        double bottomRight = Point2D.distance(centreX, centreY, task.getPixelData().getWidth()-1, task.getPixelData().getHeight()-1);

        endRadius = (float)(Math.max(Math.max(topLeft, topRight), Math.max(bottomLeft, bottomRight))*fillPercentage);

        // Calculates the first point
        x =  (float)(radius * Math.cos(Math.toRadians(alpha)) + centreX);
        y = (float)(-radius * Math.sin(Math.toRadians(alpha)) + centreY);
        xa = 0;
        xb = 0;
        ya = 0;
        yb = 0;

        // Have we reached the far corner of the image?
        boolean draw;
        while (radius < endRadius) {
            k = (density / 2) / radius;
            alpha += k;
            radius += distBetweenRings / (360 / k);
            x =  (float)(radius * Math.cos(Math.toRadians(alpha)) + centreX);
            y = (float)(-radius * Math.sin(Math.toRadians(alpha)) + centreY);

            // Are we within the the image?
            // If so check if the shape is open. If not, open it
            if ((x >= 0) && (x < task.getPixelData().getWidth()) && (y > 0) && (y < task.getPixelData().getHeight())) {

                // Get the color and brightness of the sampled pixel
                b = task.getPixelData().getLuminance((int)x, (int)y);
                b = Utils.mapFloat(b, 0, 255, distBetweenRings * ampScale, 0);

                // Move up according to sampled brightness
                aradius = radius + (b / distBetweenRings);
                xa =  (float)(aradius * Math.cos(Math.toRadians(alpha)) + centreX);
                ya = (float)(-aradius * Math.sin(Math.toRadians(alpha)) + centreY);

                // Move down according to sampled brightness
                k = (density / 2) /radius;
                alpha += k;
                radius += distBetweenRings / (360 / k);
                bradius = radius-(b/ distBetweenRings);
                xb =  (float)(bradius * Math.cos(Math.toRadians(alpha)) + centreX);
                yb = (float)(-bradius * Math.sin(Math.toRadians(alpha)) + centreY);

                // If the sampled color is the mask color do not write to the shape
                if (mask <= b) {
                    draw = false;
                } else {
                    draw = true;
                }
            } else {
                // We are outside of the image
                draw = false;
            }

            if(draw){
                int pen_number = (int)(Utils.mapFloat(b, 0, 255, 0, task.getTotalPens()));
                task.addGeometry(new GLine(xa, ya, xb, yb), pen_number, null);
            }

            float startRadius = distBetweenRings / 2;
            task.updatePlottingProgress(radius-startRadius, endRadius-startRadius);
        }

        //task.closePath();
        task.finishProcess();
    }

}