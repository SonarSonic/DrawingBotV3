package drawingbot.pfm;

import drawingbot.image.ConvolutionMatrices;
import drawingbot.image.ImageTools;
import drawingbot.image.RawLuminanceData;
import drawingbot.plotting.PlottingTask;
import org.imgscalr.Scalr;
import processing.core.PImage;

import java.awt.image.BufferedImage;

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
    protected int mask = app.color(240, 240, 240);      // This color will not be drawn (WHITE)

    public RawLuminanceData rawBrightnessData;

    @Override
    public float progress() {
        float startRadius = distBetweenRings /2;
        return (radius-startRadius) / (endRadius-startRadius);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    public void preProcess() {
        BufferedImage dst = (BufferedImage) task.getPlottingImage().getNative();

        dst = ImageTools.cropToAspectRatio(dst, app.getDrawingAreaWidthMM() / app.getDrawingAreaHeightMM());

        dst = ImageTools.lazyConvolutionFilter(dst, ConvolutionMatrices.MATRIX_UNSHARP_MASK, 3, true);

        dst = ImageTools.lazyImageBorder(dst, "border/b6.png", 0, 0);
        dst = ImageTools.lazyRGBFilter(dst, ImageTools::grayscaleFilter);

        dst = Scalr.resize(dst, Scalr.Method.QUALITY, (int)(dst.getWidth() * plottingResolution), (int)(dst.getHeight()* plottingResolution));

        task.img_plotting = new PImage(dst);
        rawBrightnessData = RawLuminanceData.createBrightnessData(dst);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    public void doProcess() {
        radius = distBetweenRings /2;
        k = density/radius;
        alpha = k;
        radius += distBetweenRings /(360/k);

        // When have we reached the far corner of the image?
        // TODO: this will have to change if not centered
        endRadius = sqrt(pow((rawBrightnessData.width/2F), 2)+pow((rawBrightnessData.height/2F), 2));

        // Calculates the first point.  Currently just the center.
        // TODO: Allow for ajustable center
        task.penUp();
        x =  radius*cos(radians(alpha))+rawBrightnessData.width/2F;
        y = -radius*sin(radians(alpha))+rawBrightnessData.height/2F;
        task.moveAbs(0, x, y);
        xa = 0;
        xb = 0;
        ya = 0;
        yb = 0;

        // Have we reached the far corner of the image?
        while (radius < endRadius) {
            k = (density/2)/radius;
            alpha += k;
            radius += distBetweenRings /(360/k);
            x =  radius*cos(radians(alpha))+rawBrightnessData.width/2F;
            y = -radius*sin(radians(alpha))+rawBrightnessData.height/2F;

            // Are we within the the image?
            // If so check if the shape is open. If not, open it
            if ((x>=0) && (x<rawBrightnessData.width) && (y>0) && (y<rawBrightnessData.height)) {

                // Get the color and brightness of the sampled pixel
                b = rawBrightnessData.getBrightness((int)x, (int)y);
                b = map (b, 0, 255, distBetweenRings *ampScale, 0);

                // Move up according to sampled brightness
                aradius = radius+(b/ distBetweenRings);
                xa =  aradius*cos(radians(alpha))+rawBrightnessData.width/2F;
                ya = -aradius*sin(radians(alpha))+rawBrightnessData.height/2F;

                // Move down according to sampled brightness
                k = (density/2)/radius;
                alpha += k;
                radius += distBetweenRings /(360/k);
                bradius = radius-(b/ distBetweenRings);
                xb =  bradius*cos(radians(alpha))+rawBrightnessData.width/2F;
                yb = -bradius*sin(radians(alpha))+rawBrightnessData.height/2F;

                // If the sampled color is the mask color do not write to the shape
                if (app.brightness(mask) <= b) {
                    task.penUp();
                } else {
                    task.penDown();
                }
            } else {
                // We are outside of the image
                task.penUp();
            }

            int pen_number = (int)(map(b, 0, 255, 0, task.plottedDrawing.getPenCount()));
            task.moveAbs(pen_number, xa, ya);
            task.moveAbs(pen_number, xb, yb);
        }

        task.penUp();
        finish();
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    public void postProcess() {}

}