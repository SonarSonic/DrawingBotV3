package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.image.ImageTools;
import org.joml.Vector2d;
import org.joml.Vector2i;

public class PFMCubicBezier extends PFMSketchLines {

    ///rgba samples
    private long red, green, blue, alpha;
    private int count;

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
        if(shouldLiftPen){
            task.getPathBuilder().endPath();
            task.getLastGeometry().setCustomRGBA(getCurveARGB());
        }
    }

    @Override
    public void postProcess() {
        task.getPathBuilder().endPath();
        task.getLastGeometry().setCustomRGBA(getCurveARGB());
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

        float darkestCurve = -1F;

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

            Vector2i controlPoint1 = new Vector2i((startPoint.x + midPoint.x)/2, (startPoint.y + midPoint.y)/2);
            Vector2i controlPoint2 = new Vector2i((endPoint.x + midPoint.x)/2, (endPoint.y + midPoint.y)/2);

            Vector2i q = new Vector2i(endPoint.x - startPoint.x, endPoint.y - startPoint.y);
            double magnitude = Math.sqrt(q.x * q.x + q.y * q.y);
            Vector2d n = new Vector2d(q.y / magnitude, -q.x /magnitude);

            int darkestControlPoint1X = controlPoint1.x;
            int darkestControlPoint1Y = controlPoint1.y;

            resetLuminanceTest();
            for(int t = 0; t < curveTests; t++){
                int offsetTest = (curveVariation / curveTests) - curveVariation / 2;
                int controlPoint1X = (int) (controlPoint1.x + n.x*offsetTest);
                int controlPoint1Y = (int) (controlPoint1.y + n.y*offsetTest);

                resetLuminanceSamples();
                bresenham.plotCubicBezier(start_x, start_y, controlPoint1X, controlPoint1Y, controlPoint2.x, controlPoint2.y, endPointX, endPointY, (x, y) -> luminanceTally(pixels, x, y));

                if(testAndSetLuminanceTest()){
                    darkestControlPoint1X = controlPoint1X;
                    darkestControlPoint1Y = controlPoint1Y;
                }
            }

            controlPoint1 = new Vector2i(darkestControlPoint1X, darkestControlPoint1Y);

            int darkestControlPoint2X = controlPoint2.x;
            int darkestControlPoint2Y = controlPoint2.y;

            resetLuminanceTest();
            for(int t = 0; t < curveTests; t++){
                int offsetTest = (curveVariation / curveTests) - curveVariation / 2;
                int controlPoint2X = (int) (controlPoint2.x + n.x*offsetTest);
                int controlPoint2Y = (int) (controlPoint2.y + n.y*offsetTest);

                resetLuminanceSamples();
                bresenham.plotCubicBezier(start_x, start_y, controlPoint1.x, controlPoint1.y, controlPoint2X, controlPoint2Y, endPointX, endPointY, (x, y) -> luminanceTally(pixels, x, y));

                if(testAndSetLuminanceTest()){
                    darkestControlPoint2X = controlPoint2X;
                    darkestControlPoint2Y = controlPoint2Y;
                }
            }

            controlPoint1 = new Vector2i(darkestControlPoint2X, darkestControlPoint2Y);

            float average = getLuminanceTestAverage();
            if(darkestCurve == -1F || average < darkestCurve){
                darkestCurve = average;
                this.darkest_x = endPointX;
                this.darkest_y = endPointY;
                this.darkControlPoint1 = controlPoint1;
                this.darkControlPoint2 = controlPoint2;
            }
        }
    }

    @Override
    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust) {
        bresenham.plotCubicBezier(x1, y1, darkControlPoint1.x, darkControlPoint1.y, darkControlPoint2.x, darkControlPoint2.y, x2, y2, (x, y) -> adjustLuminanceColour(task.getPixelData(), x, y, adjust));

        if(!task.getPathBuilder().hasMoveTo){
            task.getPathBuilder().moveTo(x1, y1);
        }
        task.getPathBuilder().curveTo(darkControlPoint1.x, darkControlPoint1.y, darkControlPoint2.x, darkControlPoint2.y, x2, y2);
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
