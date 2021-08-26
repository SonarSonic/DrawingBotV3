package drawingbot.pfm.wip;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.image.ImageTools;
import drawingbot.pfm.PFMSketchLines;

public class PFMSketchCurvesV3 extends PFMSketchLines {

    public float tension = 0.4F;

    ///rgba samples
    private long red, green, blue, alpha;
    private int count;

    @Override
    public int getColourMode() {
        return 0;
    }

    @Override
    public void preProcess() {
        super.preProcess();
        task.getPathBuilder().setCatmullCurveTension(tension);
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

    @Override
    public void findDarkestNeighbour(IPixelData pixels, int start_x, int start_y) {
        float delta_angle = 360 / (float) lineTests;
        float start_angle = randomSeedF(startAngleMin, startAngleMax) + 0.5F;
        float ignoreAngle = 90F;

        float[] catmullP0 = task.pathBuilder.getCatmullP0();
        float[] catmullP1 = task.pathBuilder.getCatmullP1();
        float[] catmullP2 = task.pathBuilder.getCatmullP2();

        float darkestValue = -1F;
        int bestX = 0;
        int bestY = 0;

        //find the angle of the last full line
        if(catmullP1 != null && catmullP2 != null){
            double deltaX = catmullP2[0] - catmullP1[0];
            double deltaY = catmullP1[1] - catmullP2[1];
            double angle = Math.atan2(deltaY, deltaX);
            double last = Math.toDegrees(angle) + 180;
            //start_angle = (float) last + (ignoreAngle / 2) + 0.5F;
            //delta_angle = (360 - ignoreAngle) / (float)tests;
        }


        resetLuminanceTest();
        for (int d = 0; d < lineTests; d ++) {
            float degree = (delta_angle * d) + start_angle;
            float distance = randomSeed(minLineLength, maxLineLength);

            int x1 = (int)(Math.cos(Math.toRadians(degree))*distance) + start_x;
            int y1 = (int)(Math.sin(Math.toRadians(degree))*distance) + start_y;

            ///TODO PREVENT CURVE GOING BACK ON ITSELF!

            resetLuminanceSamples();

            bresenham.plotLine(start_x, start_y, x1, y1, (x, y) -> luminanceTest(pixels, x, y));

            if(task.pathBuilder.hasCurvePoints()){
                bresenham.plotCatmullRom(catmullP0, catmullP1, catmullP2, new float[]{x1, y1}, task.getPathBuilder().getCatmullTension(), (x, y) -> luminanceTest(pixels, x, y));
                //TODO CHECK BEAUTY RATING OF CURVE??? and NOT brightness
            }

            //the test of both the line and the previous catmull rom
            float luminanceTest = getLuminanceTestAverage();
            if(darkestValue == -1F || luminanceTest < darkestValue){
                darkestValue = luminanceTest;
                bestX = darkest_x;
                bestY = darkest_y;
            }
        }
        darkest_x = bestX;
        darkest_y = bestY;
        //TODO MAKE VERSION WHERE CURVES CAN'T GO BACK ON THEMSELVES
    }

    @Override
    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust) {
         if(task.getPathBuilder().hasCurvePoints()){
             resetColourSamples();
             bresenham.plotCatmullRom(task.getPathBuilder().getCatmullP0(), task.getPathBuilder().getCatmullP1(), task.getPathBuilder().getCatmullP2(), new float[]{x2, y2}, task.getPathBuilder().getCatmullTension(), (x, y) -> adjustLuminanceColour(task.getPixelData(), x, y, adjust));

             int argb = getColourTestAverage();
             alpha += ImageTools.alpha(argb);
             red += ImageTools.red(argb);
             green += ImageTools.green(argb);
             blue += ImageTools.blue(argb);
             count ++;
        }
        task.getPathBuilder().addCatmullCurveVertex(x2, y2);
    }

    public int getCurveARGB(){
        int argb = ImageTools.getARGB((int)((float)alpha/count), (int)((float)red/count), (int)((float)green/count), (int)((float)blue/count));
        red = green = blue = alpha = count = 0;
        return argb;
    }
}
