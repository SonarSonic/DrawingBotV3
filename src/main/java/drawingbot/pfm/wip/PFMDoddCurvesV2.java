package drawingbot.pfm.wip;
/*
import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.image.ImageTools;
import drawingbot.image.PixelDataARGB;
import drawingbot.pfm.PFMSketchLines;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class PFMDoddCurvesV2 extends PFMSketchLines {

    public float tension = 0.4F;

    ///rgba samples
    private long red, green, blue, alpha;
    private int count;

    private IPixelData brightenedLineData;
    private int curveLength = 20;

    private List<Vector2i> lastCurve = null;

    public int curveTests = 10;
    public int curveVariation = 50;

    ///for when testing lines, we need to remember the last angle, to avoid the curve going back on itself.
    private float testAngle;
    private float darkestAngle;

    @Override
    public void preProcess() {
        super.preProcess();
        task.getPathBuilder().setCatmullCurveTension(tension);
        resetLineData();
    }

    @Override
    public void doProcess() {
        super.doProcess();
        if(shouldLiftPen){
            task.getPathBuilder().endCatmullCurve();
            task.getLastGeometry().setCustomRGBA(getCurveARGB());
        }
    }

    @Override
    public void postProcess() {
        task.getPathBuilder().endCatmullCurve();
        task.getLastGeometry().setCustomRGBA(getCurveARGB());
    }

    public void resetLineData(){
        brightenedLineData = ImageTools.copy(task.getPixelData(), new PixelDataARGB(task.getPixelData().getWidth(), task.getPixelData().getHeight()));
    }

    @Override
    public void findDarkestNeighbour(IPixelData pixels, int[] point, int[] darkestDst){
        float delta_angle;
        float start_angle = randomSeedF(startAngleMin, startAngleMax) + 0.5F;
        float avoidanceAngle = 320F;

        if (!enableShading || shadingThreshold > lumProgress) {
            delta_angle = drawingDeltaAngle / (float) lineTests;
        } else {
            delta_angle = shadingDeltaAngle;
        }

        resetLineData();
        List<Vector2i> points = new ArrayList<>();
        points.add(new Vector2i(point[0], point[1]));
        float lastAngle = -1F;
        for(int i = 0; i < curveLength; i++){
            Vector2i previousPoint = points.get(points.size()-1);
            resetLuminanceTest();
            int nextLineLength = randomSeed(minLineLength, maxLineLength);

            for (int d = 0; d < lineTests; d ++) {
                if(lastAngle != -1F){
                    //logic to avoid a line going back on itself.
                    float oppositeAngle = (lastAngle + 180);
                    start_angle = (oppositeAngle + (avoidanceAngle/2));
                    delta_angle = (360 - avoidanceAngle) / (float) lineTests;
                }
                testAngle = ((delta_angle * d) + start_angle);

                luminanceTestAngledLine(brightenedLineData, previousPoint.x, previousPoint.y, nextLineLength, testAngle, darkestDst);
            }

            if(i == 0)
                lastAngle = darkestAngle;

            bresenham.plotLine(previousPoint.x, previousPoint.y, darkestDst[0], darkestDst[1], (x, y) -> adjustLuminanceColour(brightenedLineData, x, y, adjustbrightness));
            points.add(new Vector2i(darkestDst[0], darkestDst[1]));
        }
        task.finishProcess();

        Vector2i p1 = null;
        Vector2i p2 = null;
        Vector2i p3 = null;

        /// optimise the curves

        for(Vector2i p : points){
            p1 = p2;
            p2 = p3;
            p3 = p;

            if(p1  != null && p2  != null && p3 != null){
                Vector2i q = new Vector2i(p3.x - p1.x, p3.y - p1.y);
                double magnitude = Math.sqrt(q.x * q.x + q.y * q.y);
                Vector2d n = new Vector2d(q.y / magnitude, -q.x /magnitude);

                Vector2i original = new Vector2i(p2.x, p2.y);
                Vector2i best = original;

                resetLuminanceTest();
                for(int t = 0; t < curveTests; t++){
                    int offsetTest = (curveVariation / curveTests) - curveVariation / 2;
                    p2.x = (int) (original.x + n.x*offsetTest);
                    p2.y = (int) (original.y + n.y*offsetTest);

                    resetLuminanceSamples();
                    bresenham.plotCatmullRom(points, task.getPathBuilder().getCatmullTension(), (x, y) -> luminanceTally(pixels, x, y));

                    if(testAndSetLuminanceTest()){
                        //best = new Vector2i(p2.x, p2.y);
                    }
                }
                p2.x = best.x;
                p2.y = best.y;
            }
        }


        lastCurve = points;
        darkestDst[0] = lastCurve.get(lastCurve.size()-1).x;
        darkestDst[1] = lastCurve.get(lastCurve.size()-1).y;
    }

    @Override
    public int minLineLength() {
        return minLineLength;
    }

    @Override
    public void onLuminanceTestSuccess(IPixelData pixels, int x, int y, int[] dest) {
        super.onLuminanceTestSuccess(pixels, x, y, dest);
        darkestAngle = testAngle;
    }

    @Override
    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust) {
        if(lastCurve != null){
            for(Vector2i point : lastCurve){
                task.getPathBuilder().addCatmullCurveVertex(point.x, point.y);
            }
            defaultColourTest.resetColourSamples(adjust);
            bresenham.plotCatmullRom(lastCurve, 90F, (xT, yT)-> defaultColourTest.testPixel(task.getPixelData(), xT, yT));
            int argb = defaultColourTest.getCurrentAverage();

            alpha += ImageTools.alpha(argb);
            red += ImageTools.red(argb);
            green += ImageTools.green(argb);
            blue += ImageTools.blue(argb);
            count ++;
        }
    }

    public int getCurveARGB(){
        int argb = ImageTools.getARGB((int)((float)alpha/count), (int)((float)red/count), (int)((float)green/count), (int)((float)blue/count));
        red = green = blue = alpha = count = 0;
        return argb;
    }
}
*/