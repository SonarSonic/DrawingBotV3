package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.geom.shapes.GLine;
import drawingbot.image.PixelDataLuminance;
import drawingbot.utils.Utils;

import java.awt.geom.Point2D;

/**Originally developed from : https://github.com/krummrey/SpiralFromImage*/
public class PFMSpiralBasic extends AbstractPFMImage {

    public enum EnumSpiralType {
        ARCHIMEDEAN,
        PARABOLIC;

        @Override
        public String toString() {
            return Utils.capitalize(name());
        }
    }

    public EnumSpiralType spiralType = EnumSpiralType.PARABOLIC;
    public double spiralSize = 1.0;
    public double centreXScale = 0.5;
    public double centreYScale = 0.5;
    public double ringSpacing = 7;
    public double amplitude = 4.5F;
    public double density = 75;
    public boolean connectedLines = true;
    public boolean ignoreWhite = false;
    private int pass = 0;

    protected int mask = 240;

    @Override
    public IPixelData createPixelData(int width, int height) {
        return new PixelDataLuminance(width, height);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    public void run() {
        //adjust for HQ mode
        if(tools.getCanvas().getRescaleMode().isHighQuality()){
            ringSpacing = ringSpacing * (tools.getCanvas().getTargetPenWidth());
            density = density * (tools.getCanvas().getTargetPenWidth());
            amplitude = amplitude * (tools.getCanvas().getTargetPenWidth());
            mask = (int) (mask * (tools.getCanvas().getTargetPenWidth()));
        }

        boolean parabolic = spiralType == EnumSpiralType.PARABOLIC;

        int centreX = (int)(tools.getPixelData().getWidth()*centreXScale);
        int centreY = (int)(tools.getPixelData().getHeight()*centreYScale);

        // find the furthest corner of the image
        double topLeft = Point2D.distance(centreX, centreY, 0, 0);
        double topRight = Point2D.distance(centreX, centreY, 0, tools.getPixelData().getHeight()-1);
        double bottomLeft = Point2D.distance(centreX, centreY, tools.getPixelData().getWidth()-1, 0);
        double bottomRight = Point2D.distance(centreX, centreY, tools.getPixelData().getWidth()-1, tools.getPixelData().getHeight()-1);

        double endRadius = Math.max(Math.max(topLeft, topRight), Math.max(bottomLeft, bottomRight))* spiralSize;

        int totalPasses = parabolic ? 2 : 1;
        ringSpacing = parabolic ? ringSpacing/8 : ringSpacing;
        amplitude = parabolic ? amplitude/10 : amplitude;
        density = parabolic ? density/10 : density;


        for(pass = 1; pass <= totalPasses; pass++){

            double k = (density) / (ringSpacing / 2);
            double alpha = k;
            double radius = (ringSpacing / (360 / k));

            double x, y, xa = 0, xb = 0, ya = 0, yb = 0;
            double lastX = -1; double lastY = -1;
            int luminance;
            double lumOffset;

            double aRadius, bRadius;

            // Have we reached the far corner of the image?
            boolean draw;
            while (radius < endRadius) {
                x = getSpiralX(radius, alpha, centreX);
                y = getSpiralY(radius, alpha, centreY);

                k = (density / 2) / radius;
                alpha += k;
                radius += ringSpacing / (360 / k);


                if (tools.withinPlottableArea(x, y)){

                    // Get the color and brightness of the sampled pixel
                    luminance = tools.getPixelData().getLuminance((int)x, (int)y);
                    lumOffset = Utils.mapDouble(luminance, 0D, 255D, ringSpacing * amplitude, 0D);

                    // Move up according to sampled brightness
                    aRadius = radius + (lumOffset / ringSpacing);
                    xa = getSpiralX(aRadius, alpha, centreX);
                    ya = getSpiralY(aRadius, alpha, centreY);

                    // Move along the spiral
                    k = ((density / 2D) / radius);
                    alpha += k;
                    radius += ringSpacing / (360D / k);

                    // Move down according to sampled brightness
                    bRadius = radius - (lumOffset / ringSpacing);
                    xb = getSpiralX(bRadius, alpha, centreX);
                    yb = getSpiralY(bRadius, alpha, centreY);

                    // If the sampled color is the mask color do not write to the shape
                    if (ignoreWhite && mask <= luminance) {
                        draw = false;
                        lastX = -1;
                        lastY = -1;
                    } else {
                        draw = true;
                    }
                } else {
                    // We are outside of the image
                    draw = false;
                    lastX = -1;
                    lastY = -1;
                }

                if(draw){
                    int penIndex = getPenIndex((float)x, (float)y);
                    if(connectedLines && lastX != -1 && lastY != -1){
                        tools.addGeometry(new GLine((float)lastX, (float)lastY, (float)xa, (float)ya), penIndex, -1);
                    }
                    tools.addGeometry(new GLine((float)xa, (float)ya, (float)xb, (float)yb), penIndex, -1);
                    lastX = xb;
                    lastY = yb;
                }

                double startRadius = ringSpacing / 2D;
                tools.updateProgress(radius-startRadius, endRadius-startRadius);


                if(tools.isFinished()){
                    break;
                }
            }
        }
    }

    public double getSpiralX(double radius, double alpha, double centreX){
        if (spiralType == EnumSpiralType.PARABOLIC) {
            double offset = radius * Math.sqrt(Math.toRadians(alpha));
            if(pass == 2){
                //for the 2nd branch we offset the other way
                offset = -offset;
            }
            return offset * Math.cos(Math.toRadians(alpha)) + centreX;
        }
        return -radius * Math.cos(Math.toRadians(alpha)) + centreX;

    }
    public double getSpiralY(double radius, double alpha, double centreY){
        if (spiralType == EnumSpiralType.PARABOLIC) {
            double offset = radius * Math.sqrt(Math.toRadians(alpha));
            if(pass == 2) {
                //for the 2nd branch we offset the other way
                offset = -offset;
            }
            return offset * Math.sin(Math.toRadians(alpha)) + centreY;
        }
        return -radius * Math.sin(Math.toRadians(alpha)) + centreY;
    }

    public int getPenIndex(float x, float y){
        return pass;
    }

}