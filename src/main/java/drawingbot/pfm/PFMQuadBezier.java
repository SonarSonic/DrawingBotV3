package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.image.ImageTools;
import org.joml.Vector2d;
import org.joml.Vector2i;

public class PFMQuadBezier extends PFMSketchLines {

    ///rgba samples
    private long red, green, blue, alpha;
    private int count;

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
        if(shouldLiftPen && count != 0){
            task.getPathBuilder().endPath();
            task.getLastGeometry().setCustomRGBA(getCurveARGB());
        }
    }

    @Override
    public void postProcess() {
        if(count != 0){
            task.getPathBuilder().endPath();
            task.getLastGeometry().setCustomRGBA(getCurveARGB());
        }
    }

    @Override
    public void findDarkestNeighbour(IPixelData pixels, int start_x, int start_y) {
        float delta_angle;
        float start_angle = randomSeedF(startAngleMin, startAngleMax) + 0.5F;

        if (!enableShading || shadingThreshold > lumProgress) {
            delta_angle = drawingDeltaAngle / (float) lineTests;
        } else {
            delta_angle = shadingDeltaAngle;
        }

        resetLuminanceTest();
        for (int d = 0; d < lineTests; d ++) {
            int nextLineLength = randomSeed(minLineLength, maxLineLength);
            float degree = (delta_angle * d) + start_angle;
            int endPointX = (int)(Math.cos(Math.toRadians(degree))*nextLineLength) + start_x;
            int endPointY = (int)(Math.sin(Math.toRadians(degree))*nextLineLength) + start_y;

            int[] edge = bresenham.findEdge(start_x, start_y, endPointX, endPointY, pixels.getWidth(), pixels.getHeight());
            endPointX = edge[0];
            endPointY = edge[1];

            if(endPointX == start_x && endPointY == start_y){
                continue;
            }

            Vector2i startPoint = new Vector2i(start_x, start_y);
            Vector2i endPoint = new Vector2i(endPointX, endPointY);
            Vector2i midPoint = new Vector2i((startPoint.x+endPoint.x)/2, (startPoint.y+endPoint.y)/2);

            Vector2i q = new Vector2i(endPoint.x - startPoint.x, endPoint.y - startPoint.y);
            double magnitude = Math.sqrt(q.x * q.x + q.y * q.y);
            Vector2d n = new Vector2d(q.y / magnitude, -q.x /magnitude);

            for(int t = 0; t < curveTests; t++){
                int offsetTest = (curveVariation / curveTests) - curveVariation / 2;
                int controlPointX = (int) (midPoint.x + n.x*offsetTest);
                int controlPointY = (int) (midPoint.y + n.y*offsetTest);

                resetLuminanceSamples();
                bresenham.plotQuadBezier(start_x, start_y, controlPointX, controlPointY, endPointX, endPointY, (x, y) -> luminanceTally(pixels, x, y));

                if ((test_luminance == -1 || getLuminanceTestAverage() < test_luminance)) {
                    darkest_x = endPointX;
                    darkest_y = endPointY;
                    darkest_control_x = controlPointX;
                    darkest_control_y= controlPointY;
                    test_luminance = getLuminanceTestAverage();
                }
            }
        }
    }

    @Override
    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust) {
        task.getPathBuilder().quadTo(darkest_control_x, darkest_control_y, x2, y2);

        resetColourSamples();
        bresenham.plotQuadBezier(x1, y1, darkest_control_x, darkest_control_y, x2, y2, (x, y) -> adjustLuminanceColour(task.getPixelData(), x, y, adjust));
        int argb = getColourTestAverage();

        alpha += ImageTools.alpha(argb);
        red += ImageTools.red(argb);
        green += ImageTools.green(argb);
        blue += ImageTools.blue(argb);
        count ++;
    }

    public int getCurveARGB(){
        int argb = ImageTools.getARGB((int)((float)alpha/count), (int)((float)red/count), (int)((float)green/count), (int)((float)blue/count));
        red = green = blue = alpha = count = 0;
        return argb;
    }

    @Override
    public int minLineLength() {
        return minLineLength;
    }
}
