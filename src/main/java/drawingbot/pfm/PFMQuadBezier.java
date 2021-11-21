package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.image.ImageTools;
import drawingbot.pfm.helpers.ColourSampleTest;
import drawingbot.pfm.helpers.LuminanceTest;
import org.joml.Vector2d;
import org.joml.Vector2i;

public class PFMQuadBezier extends PFMSketchLines {

    private final ColourSampleTest curveColourSamples = new ColourSampleTest();

    protected int darkest_control_x;
    protected int darkest_control_y;

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
    protected boolean findDarkestNeighbour(IPixelData pixels, int[] point, int[] darkestDst) {
        float delta_angle;
        float start_angle = randomSeedF(startAngleMin, startAngleMax) + 0.5F;

        if (!enableShading || shadingThreshold > lumProgress) {
            delta_angle = drawingDeltaAngle / (float) lineTests;
        } else {
            delta_angle = shadingDeltaAngle;
        }

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

            Vector2i q = new Vector2i(endPoint.x - startPoint.x, endPoint.y - startPoint.y);
            double magnitude = Math.sqrt(q.x * q.x + q.y * q.y);
            Vector2d n = new Vector2d(q.y / magnitude, -q.x /magnitude);

            for(int t = 0; t < curveTests; t++){
                int offsetTest = (curveVariation / curveTests) - curveVariation / 2;
                int controlPointX = (int) (midPoint.x + n.x*offsetTest);
                int controlPointY = (int) (midPoint.y + n.y*offsetTest);

                curveTest.resetSamples();
                bresenham.plotQuadBezier(point[0], point[1], controlPointX, controlPointY, endPointX, endPointY, (x, y) -> curveTest.addSample(pixels, x, y));

                if(curveTest.getAndSetTestResult()){
                    darkestDst[0] = endPointX;
                    darkestDst[1] = endPointY;
                    darkest_control_x = controlPointX;
                    darkest_control_y= controlPointY;
                }
            }
        }
        return curveTest.getDarkestSample() < pixels.getAverageLuminance();
    }

    @Override
    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust) {
        task.getPathBuilder().quadTo(darkest_control_x, darkest_control_y, x2, y2);

        defaultColourTest.resetColourSamples(adjust);
        bresenham.plotQuadBezier(x1, y1, darkest_control_x, darkest_control_y, x2, y2, (x, y) -> defaultColourTest.addSample(task.getPixelData(), x, y));
        curveColourSamples.addSample(defaultColourTest.getCurrentAverage());
    }

    @Override
    public int minLineLength() {
        return minLineLength;
    }
}
