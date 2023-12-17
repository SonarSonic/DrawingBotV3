package drawingbot.pfm.helpers;

import drawingbot.api.IPixelData;
import drawingbot.plotting.PlottingTools;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * A class which passes back options for the available lines given a point and parameters
 * N.B this class is not thread safe.
 */
public class SimpleLineSampler {

    public PlottingTools tools;

    private final LuminanceTestLine luminanceTest = new LuminanceTestLine();
    private final BresenhamHelper.IPixelSetter forEndPoints = this::forEndPoint;

    //internal only
    private IPixelData pixels;
    private int startX;
    private int startY;
    private boolean safeValues;
    public float lastAngle = 0F;

    public SimpleLineSampler(PlottingTools tools){
        this.tools = tools;
    }

    public int startX(){
        return startX;
    }

    public int startY(){
        return startY;
    }

    public void setupSampler(IPixelData pixels, int startX, int startY, boolean safe){
        setupSampler(pixels, startX, startY, lastAngle, safe);
    }

    public void setupSampler(IPixelData pixels, int startX, int startY, float lastAngle, boolean safe){
        this.pixels = pixels;
        this.startX = startX;
        this.startY = startY;
        this.lastAngle = lastAngle;
        this.safeValues = safe;
    }

    public float findDarkestLine(IPixelData pixels, Shape softClip, int startX, int startY, int minLength, int maxLength, int maxTests, float startAngle, float drawingDeltaAngle, boolean shading, int[] darkestDst) {
        luminanceTest.resetTest();
        luminanceTest.setup(darkestDst, minLength, maxLength, true, softClip);
        forAvailableEndPoints(pixels, startX, startY, maxLength, maxTests, startAngle, drawingDeltaAngle, shading, false);
        return luminanceTest.getDarkestSample();
    }

    public void forAvailableEndPoints(IPixelData pixels, int startX, int startY, int maxLength, int maxTests, float startAngle, float drawingDeltaAngle, boolean shading, boolean safe){
        forAvailableEndPoints(pixels, startX, startY, maxLength, maxTests, startAngle, drawingDeltaAngle, shading, safe, forEndPoints);
    }

    public void forAvailableEndPoints(IPixelData pixels, int startX, int startY, int maxLength, int maxTests, float startAngle, float drawingDeltaAngle, boolean shading, boolean safe, BresenhamHelper.IPixelSetter consumer){
        setupSampler(pixels, startX, startY, safe);
        if(drawingDeltaAngle == 360 && !shading && (maxTests == -1 || tools.bresenham.getBresenhamCircleSize(maxLength) <= maxTests)){
            tools.bresenham.plotCircle(startX, startY, maxLength, consumer);
        }else{
            float deltaAngle = shading ? drawingDeltaAngle : drawingDeltaAngle / (float) maxTests;
            AffineTransform transform = new AffineTransform();
            Point2D originalPoint = new Point2D.Double(startX, startY);
            Point2D transformedPoint = new Point2D.Double();
            for (int d = 0; d < (shading ? 2 : maxTests); d++) {
                double angleDegrees = startAngle + (deltaAngle * d);
                transform.setToRotation(Math.toRadians(angleDegrees), startX, startY);
                transform.translate(maxLength, 0);
                transform.transform(originalPoint, transformedPoint);
                consumer.setPixel((int) transformedPoint.getX(), (int) transformedPoint.getY());
            }
        }
    }

    public void forEndPoint(int endX, int endY){
        boolean isSafe = pixels.withinXY(endX, endY);
        if(safeValues && !isSafe){
            int[] edgePixel = tools.bresenham.findEdge(startX, startY, endX, endY, pixels.getWidth(), pixels.getHeight());
            endX = edgePixel[0];
            endY = edgePixel[1];
        }
        luminanceTest.resetSamples();
        tools.bresenham.plotLine(startX, startY, endX, endY, (xT, yT) -> luminanceTest.addSample(pixels, xT, yT));
    }
}
