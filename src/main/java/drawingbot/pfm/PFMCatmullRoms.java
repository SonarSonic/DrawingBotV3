package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.image.ImageTools;
import drawingbot.image.PixelDataARGB;

public class PFMCatmullRoms extends PFMSketchLines {

    public float tension = 0.5F;

    private IPixelData brightenedLineData;

    ///rgba samples
    private long red, green, blue, alpha;
    private int count;

    @Override
    public void preProcess() {
        super.preProcess();
        task.getPathBuilder().setCatmullCurveTension(tension);
    }

    @Override
    public void postProcess() {
        if(count != 0){
            task.getPathBuilder().endCatmullCurve();
            task.getLastGeometry().setCustomRGBA(getCurveARGB());
        }
    }

    public void resetLineData(){
        brightenedLineData = ImageTools.copy(task.getPixelData(), new PixelDataARGB(task.getPixelData().getWidth(), task.getPixelData().getHeight()));
    }

    @Override
    public void doProcess() {

        task.getPathBuilder().startCatmullCurve();
        findDarkestArea(task.getPixelData());

        x = darkest_x;
        y = darkest_y;
        squiggle_count++;

        //we add the first point twice, so it acts as it's own control point, completing the line.
        task.getPathBuilder().addCatmullCurveVertex(x, y);
        task.getPathBuilder().addCatmullCurveVertex(x, y);

        for (int s = 0; s < squiggle_length; s++) {
            findDarkestNeighbour(task.getPixelData(), x, y);
            addGeometry(task, x, y, darkest_x, darkest_y, adjustbrightness);
            x = darkest_x;
            y = darkest_y;
            if(updateProgress(task) || task.isFinished()){
                task.getPathBuilder().endCatmullCurve();
                task.finishProcess();
                return;
            }
        }
        if(shouldLiftPen){
            task.getPathBuilder().endCatmullCurve();
            task.getLastGeometry().setCustomRGBA(getCurveARGB());
        }
    }


    @Override
    public void findDarkestNeighbour(IPixelData pixels, int start_x, int start_y) {
        float delta_angle = drawingDeltaAngle / (float) lineTests;
        float start_angle = randomSeedF(startAngleMin, startAngleMax) + 0.5F;

        float[] p1 = task.getPathBuilder().getCatmullP1();
        float[] p2 = task.getPathBuilder().getCatmullP2();
        float[] p3Darkest = null;
        float[] p4Darkest = null;

        float darkestCurveTest = -1;

        int nextLineLength = randomSeed(minLineLength, maxLineLength);
        resetLuminanceTest();
        for (int test1 = 0; test1 < lineTests; test1 ++) {
            luminanceTestAngledLine(pixels, start_x, start_y, nextLineLength, (delta_angle * test1) + start_angle);

            float[] p3 = new float[]{darkest_x, darkest_y};

            resetLuminanceTest();
            for (int test2 = 0; test2 < lineTests; test2 ++) {
                luminanceTestAngledLine(pixels, (int)p3[0], (int)p3[1], nextLineLength, (delta_angle * test2) + start_angle);

                float[] p4 = new float[]{darkest_x, darkest_y};
                resetLuminanceSamples();
                bresenham.plotCatmullRom(p1, p2, p3, p4, tension, (x,y)->luminanceTally(pixels, x, y));
                float luminanceTest = getLuminanceTestAverage();

                if(darkestCurveTest == -1 || luminanceTest < darkestCurveTest){
                    p3Darkest = p3;
                    p4Darkest = p4;
                    darkestCurveTest = luminanceTest;
                }
            }
        }
        x = (int) p3Darkest[0];
        y = (int) p3Darkest[1];
        darkest_x = (int) p4Darkest[0];
        darkest_y = (int) p4Darkest[1];
    }

    @Override
    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust) {
        resetColourSamples();
        bresenham.plotCatmullRom(task.getPathBuilder().getCatmullP1(), task.getPathBuilder().getCatmullP2(), new float[]{x1, y1}, new float[]{x2, y2}, task.getPathBuilder().getCatmullTension(), (x, y) -> adjustLuminanceColour(task.getPixelData(), x, y, adjust));
        int argb = getColourTestAverage();

        alpha += ImageTools.alpha(argb);
        red += ImageTools.red(argb);
        green += ImageTools.green(argb);
        blue += ImageTools.blue(argb);
        count ++;

        task.getPathBuilder().addCatmullCurveVertex(x1, y1);
        task.getPathBuilder().addCatmullCurveVertex(x2, y2);
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
