package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.image.ImageTools;
import drawingbot.pfm.helpers.ColourSampleTest;
import drawingbot.pfm.helpers.LuminanceTest;
import org.joml.Vector2d;
import org.joml.Vector2i;

public class PFMCubicBezier extends PFMSketchLines {

    private final ColourSampleTest curveColourSamples = new ColourSampleTest();

    protected Vector2i darkControlPoint1;
    protected Vector2i darkControlPoint2;

    public int curveTests = 10;
    public int curveVariation = 50;

    @Override
    public void preProcess() {
        super.preProcess();
    }

    @Override
    public void doProcess() {
        super.doProcess();
        if(shouldLiftPen && curveColourSamples.getSampleCount() != 0){
            task.getPathBuilder().endPath();
            task.getLastGeometry().setCustomRGBA(curveColourSamples.getAndResetColourSamples());
        }
    }

    @Override
    public void postProcess() {
        if(curveColourSamples.getSampleCount() != 0){
            task.getPathBuilder().endPath();
            task.getLastGeometry().setCustomRGBA(curveColourSamples.getAndResetColourSamples());
        }
    }

    @Override
    public boolean findDarkestNeighbour(IPixelData pixels, int[] point, int[] darkestDst){
        float delta_angle;
        float start_angle = randomSeedF(startAngleMin, startAngleMax) + 0.5F;

        if (!enableShading || shadingThreshold > lumProgress) {
            delta_angle = drawingDeltaAngle / (float) lineTests;
        } else {
            delta_angle = shadingDeltaAngle;
        }

        float darkestCurve = -1F;
        LuminanceTest curveTest = new LuminanceTest();
        for (int d = 0; d < lineTests; d ++) {
            int nextLineLength = randomSeed(minLineLength, maxLineLength);
            float degree = (delta_angle * d) + start_angle;
            int endPointX = (int)(Math.cos(Math.toRadians(degree))*nextLineLength) + point[0];
            int endPointY = (int)(Math.sin(Math.toRadians(degree))*nextLineLength) + point[1];

            int[] edge = bresenham.findEdge(point[0], point[1], endPointX, endPointY, pixels.getWidth(), pixels.getHeight());
            endPointX = edge[0];
            endPointY = edge[1];

            if(endPointX == point[0] && endPointY == point[1]){
                continue;
            }

            Vector2i startPoint = new Vector2i(point[0], point[1]);
            Vector2i endPoint = new Vector2i(endPointX, endPointY);
            Vector2i midPoint = new Vector2i((startPoint.x+endPoint.x)/2, (startPoint.y+endPoint.y)/2);

            Vector2i controlPoint1 = new Vector2i((startPoint.x + midPoint.x)/2, (startPoint.y + midPoint.y)/2);
            Vector2i controlPoint2 = new Vector2i((endPoint.x + midPoint.x)/2, (endPoint.y + midPoint.y)/2);

            Vector2i q = new Vector2i(endPoint.x - startPoint.x, endPoint.y - startPoint.y);
            double magnitude = Math.sqrt(q.x * q.x + q.y * q.y);
            Vector2d n = new Vector2d(q.y / magnitude, -q.x /magnitude);

            int darkestControlPoint1X = controlPoint1.x;
            int darkestControlPoint1Y = controlPoint1.y;

            curveTest.resetTest();
            for(int t = 0; t < curveTests; t++){
                int offsetTest = (curveVariation / curveTests) - curveVariation / 2;
                int controlPoint1X = (int) (controlPoint1.x + n.x*offsetTest);
                int controlPoint1Y = (int) (controlPoint1.y + n.y*offsetTest);

                curveTest.resetSamples();
                bresenham.plotCubicBezier(point[0], point[1], controlPoint1X, controlPoint1Y, controlPoint2.x, controlPoint2.y, endPointX, endPointY, (x, y) -> curveTest.addSample(pixels, x, y));

                if(curveTest.getAndSetTestResult()){
                    darkestControlPoint1X = controlPoint1X;
                    darkestControlPoint1Y = controlPoint1Y;
                }
            }

            controlPoint1 = new Vector2i(darkestControlPoint1X, darkestControlPoint1Y);

            int darkestControlPoint2X = controlPoint2.x;
            int darkestControlPoint2Y = controlPoint2.y;

            curveTest.resetTest();
            for(int t = 0; t < curveTests; t++){
                int offsetTest = (curveVariation / curveTests) - curveVariation / 2;
                int controlPoint2X = (int) (controlPoint2.x + n.x*offsetTest);
                int controlPoint2Y = (int) (controlPoint2.y + n.y*offsetTest);

                curveTest.resetSamples();
                bresenham.plotCubicBezier(point[0], point[1], controlPoint1.x, controlPoint1.y, controlPoint2X, controlPoint2Y, endPointX, endPointY, (x, y) -> curveTest.addSample(pixels, x, y));

                if(curveTest.getAndSetTestResult()){
                    darkestControlPoint2X = controlPoint2X;
                    darkestControlPoint2Y = controlPoint2Y;
                }
            }

            controlPoint1 = new Vector2i(darkestControlPoint2X, darkestControlPoint2Y);

            if(darkestCurve == -1F || curveTest.getDarkestSample() < darkestCurve){
                darkestCurve = curveTest.getDarkestSample();
                darkestDst[0] = endPointX;
                darkestDst[1] = endPointY;
                this.darkControlPoint1 = controlPoint1;
                this.darkControlPoint2 = controlPoint2;
            }
        }
        return darkestCurve < pixels.getAverageLuminance();
    }

    @Override
    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust) {
        defaultColourTest.resetColourSamples(adjust);
        bresenham.plotCubicBezier(x1, y1, darkControlPoint1.x, darkControlPoint1.y, darkControlPoint2.x, darkControlPoint2.y, x2, y2, (x, y) -> defaultColourTest.addSample(task.getPixelData(), x, y));
        curveColourSamples.addSample(defaultColourTest.getCurrentAverage());

        if(!task.getPathBuilder().hasMoveTo){
            task.getPathBuilder().moveTo(x1, y1);
        }
        task.getPathBuilder().curveTo(darkControlPoint1.x, darkControlPoint1.y, darkControlPoint2.x, darkControlPoint2.y, x2, y2);
    }

    @Override
    public int minLineLength() {
        return minLineLength;
    }
}
