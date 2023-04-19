package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.geom.easing.EasingUtils;
import drawingbot.geom.shapes.GLine;
import drawingbot.image.PixelDataARGBY;
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

    public EnumSpiralType spiralType = EnumSpiralType.ARCHIMEDEAN;
    public double spiralSize = 1.0;
    public double centreXScale = 0.5;
    public double centreYScale = 0.5;
    public double ringSpacing = 7;
    public double amplitude = 1F;
    public boolean variableVelocity = true;
    public double minVelocity = 50;
    public double maxVelocity = 180;
    public boolean connectedLines = true;
    public boolean ignoreWhite = false;
    private int pass = 0;

    protected int mask = 240;

    @Override
    public IPixelData createPixelData(int width, int height) {
        return new PixelDataARGBY(width, height);
    }

    @Override
    public void setup() {
        super.setup();
        if (maxVelocity < minVelocity) {
            double value = minVelocity;
            minVelocity = maxVelocity;
            maxVelocity = value;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    public void run() {
        //adjust for HQ mode
        if(tools.getCanvas().getRescaleMode().isHighQuality()){
            ringSpacing = ringSpacing * (tools.getCanvas().getTargetPenWidth());
            minVelocity = minVelocity * (tools.getCanvas().getTargetPenWidth());
            maxVelocity = maxVelocity * (tools.getCanvas().getTargetPenWidth());
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
        ringSpacing = parabolic ? ringSpacing/10 : ringSpacing;
        amplitude = parabolic ? amplitude/1.5D : amplitude;
        minVelocity = parabolic ? minVelocity/10 : minVelocity;
        maxVelocity = parabolic ? maxVelocity/10 : maxVelocity;

        if(!variableVelocity){
            maxVelocity = minVelocity;
        }

        double lastLuminance = 255;

        for(pass = 1; pass <= totalPasses; pass++){

            double k = (minVelocity/2D) / (ringSpacing / 2);
            double alpha = 0;
            double radius = (ringSpacing / (360 / k));

            double x, y, xa = 0, xb = 0, ya = 0, yb = 0;
            double lastX = -1; double lastY = -1;
            double luminance;
            double lumOffset;

            double aRadius, bRadius;

            // Have we reached the far corner of the image?
            boolean draw;

            while (parabolic ? radius * Math.sqrt(Math.toRadians(alpha)) < endRadius : radius < endRadius) {
                x = getSpiralX(spiralType, pass, radius, alpha, centreX);
                y = getSpiralY(spiralType, pass, radius, alpha, centreY);

                k = (minVelocity/2D) / radius;
                alpha += k;
                radius += ringSpacing / (360 / k);

                //Find a luminance reference which is within the image
                int lumRefX = Utils.clamp((int)x, 0, tools.getPixelData().getWidth()-1);
                int lumRefY = Utils.clamp((int)y, 0, tools.getPixelData().getHeight()-1);
                double nextLuminance = tools.getPixelData().getLuminance(lumRefX, lumRefY);
                int sampledRGB = tools.getPixelData().getARGB(lumRefX, lumRefY);
                luminance = (nextLuminance + lastLuminance)/2D;
                lastLuminance = nextLuminance;

                // Get the color and brightness of the sampled pixel
                lumOffset = Utils.mapDouble(luminance, 0D, 255D, ringSpacing/2 * amplitude, 0D);
                double velocity = minVelocity + EasingUtils.easeInSine(luminance/255D) * (maxVelocity-minVelocity);

                // Move along the spiral
                k = (velocity/2D) / radius;
                alpha += k;
                radius += ringSpacing / (360 / k);

                // Move up according to sampled brightness
                aRadius = radius + lumOffset;
                xa = getSpiralX(spiralType, pass, aRadius, alpha, centreX);
                ya = getSpiralY(spiralType, pass, aRadius, alpha, centreY);

                // Move along the spiral
                k = (velocity/2D) / radius;
                alpha += k;
                radius += ringSpacing / (360D / k);

                // Move down according to sampled brightness
                bRadius = radius - lumOffset;
                xb = getSpiralX(spiralType, pass, bRadius, alpha, centreX);
                yb = getSpiralY(spiralType, pass, bRadius, alpha, centreY);

                boolean overlapsImage = tools.withinPlottableAreaPrecise(xa, ya) || tools.withinPlottableAreaPrecise(xb, yb);
                if (!overlapsImage  || (ignoreWhite && mask <= luminance)) {
                    draw = false;
                    lastX = -1;
                    lastY = -1;
                } else {
                    draw = true;
                }

                if(draw){
                    int penIndex = getPenIndex(tools.getPixelData().clampX((int)x), tools.getPixelData().clampY((int)y));
                    if(connectedLines && lastX != -1 && lastY != -1){
                        tools.addGeometry(new GLine((float)lastX, (float)lastY, (float)xa, (float)ya), penIndex, sampledRGB);
                    }
                    tools.addGeometry(new GLine((float)xa, (float)ya, (float)xb, (float)yb), penIndex, sampledRGB);
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

    public static double getSpiralX(EnumSpiralType spiralType, int pass, double radius, double alpha, double centreX){
        if (spiralType == EnumSpiralType.PARABOLIC) {
            double offset = radius * Math.sqrt(Math.toRadians(alpha));
            if(pass == 2){
                //for the 2nd branch we offset the other way
                offset = -offset;
            }
            return offset * Math.cos(Math.toRadians(alpha)) + centreX;
        }
        return radius * Math.cos(Math.toRadians(alpha)) + centreX;

    }
    public static double getSpiralY(EnumSpiralType spiralType, int pass, double radius, double alpha, double centreY){
        if (spiralType == EnumSpiralType.PARABOLIC) {
            double offset = radius * Math.sqrt(Math.toRadians(alpha));
            if(pass == 2) {
                //for the 2nd branch we offset the other way
                offset = -offset;
            }
            return offset * Math.sin(Math.toRadians(alpha)) + centreY;
        }
        return radius * Math.sin(Math.toRadians(alpha)) + centreY;
    }

    public int getPenIndex(float x, float y){
        return pass-1;
    }

}