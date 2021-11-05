package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.image.ImageTools;
import drawingbot.image.PixelDataARGB;
import drawingbot.pfm.helpers.ColourSampleTest;
import drawingbot.pfm.helpers.LuminanceTest;
import drawingbot.pfm.helpers.LuminanceTestLine;

public class PFMCatmullRoms extends PFMSketchLines {

    private final ColourSampleTest curveColourSamples = new ColourSampleTest();

    public float tension = 0.5F;

    private IPixelData brightenedLineData;

    @Override
    public void preProcess() {
        super.preProcess();
        task.getPathBuilder().setCatmullCurveTension(tension);
    }

    @Override
    public void postProcess() {
        if(curveColourSamples.getSampleCount() != 0){
            task.getPathBuilder().endCatmullCurve();
            task.getLastGeometry().setCustomRGBA(curveColourSamples.getAndResetColourSamples());
        }
    }

    public void resetLineData(){
        brightenedLineData = ImageTools.copy(task.getPixelData(), new PixelDataARGB(task.getPixelData().getWidth(), task.getPixelData().getHeight()));
    }

    @Override
    public void doProcess() {

        int[] current = new int[]{-1, -1};
        int[] darkest = new int[]{-1, -1};


        task.getPathBuilder().startCatmullCurve();
        while (!task.isFinished()) {
            findDarkestArea(task.getPixelData(), darkest);

            current[0] = darkest[0];
            current[1] = darkest[1];

            squiggle_count++;

            //we add the first point twice, so it acts as it's own control point, completing the line.
            task.getPathBuilder().addCatmullCurveVertex(current[0], current[1]);
            task.getPathBuilder().addCatmullCurveVertex(current[0], current[1]);

            for (int s = 0; s < squiggle_length; s++) {
                if(!findDarkestNeighbour(task.getPixelData(), current, darkest)){
                    task.getPathBuilder().endCatmullCurve();
                    return;
                }
                addGeometry(task, current[0], current[1], darkest[0], darkest[1], adjustbrightness);
                current[0] = darkest[0];
                current[1] = darkest[1];
                if(updateProgress(task) || task.isFinished()){
                    task.getPathBuilder().endCatmullCurve();
                    task.finishProcess();
                    return;
                }
            }
            if(shouldLiftPen && curveColourSamples.getSampleCount() != 0){
                task.getPathBuilder().endCatmullCurve();
                task.getLastGeometry().setCustomRGBA(curveColourSamples.getAndResetColourSamples());
            }
        }
    }


    @Override
    public boolean findDarkestNeighbour(IPixelData pixels, int[] point, int[] darkestDst){
        float delta_angle = drawingDeltaAngle / (float) lineTests;
        float start_angle = randomSeedF(startAngleMin, startAngleMax) + 0.5F;

        float[] p1 = task.getPathBuilder().getCatmullP1();
        float[] p2 = task.getPathBuilder().getCatmullP2();
        float[] p3Darkest = null;
        float[] p4Darkest = null;

        int nextLineLength = randomSeed(minLineLength, maxLineLength);

        LuminanceTest curveTest = new LuminanceTest();
        LuminanceTestLine lineTest = new LuminanceTestLine(darkestDst, minLineLength, maxLineLength, true);

        for (int test1 = 0; test1 < lineTests; test1 ++) {
            lineTest.resetSamples();
            bresenham.plotAngledLine(point[0], point[1], nextLineLength, (delta_angle * test1) + start_angle, (x, y) -> lineTest.addSample(pixels, x, y));

            float[] p3 = new float[]{darkestDst[0], darkestDst[1]};

            lineTest.resetTest();
            for (int test2 = 0; test2 < lineTests; test2 ++) {
                lineTest.resetSamples();
                bresenham.plotAngledLine((int)p3[0], (int)p3[1], nextLineLength, (delta_angle * test2) + start_angle, (x, y) -> lineTest.addSample(pixels, x, y));

                float[] p4 = new float[]{darkestDst[0], darkestDst[1]};
                curveTest.resetSamples();
                bresenham.plotCatmullRom(p1, p2, p3, p4, tension, (x, y) -> curveTest.addSample(pixels, x, y));

                if(curveTest.getAndSetTestResult()){
                    p3Darkest = p3;
                    p4Darkest = p4;
                }
            }
        }
        point[0] = (int) p3Darkest[0];
        point[1] = (int) p3Darkest[1];

        darkestDst[0] = (int) p4Darkest[0];
        darkestDst[1] = (int) p4Darkest[1];

        return curveTest.getDarkestSample() < pixels.getAverageLuminance();
    }

    @Override
    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust) {
        defaultColourTest.resetColourSamples(adjust);
        bresenham.plotCatmullRom(task.getPathBuilder().getCatmullP1(), task.getPathBuilder().getCatmullP2(), new float[]{x1, y1}, new float[]{x2, y2}, task.getPathBuilder().getCatmullTension(), (x, y) -> defaultColourTest.addSample(task.getPixelData(), x, y));
        curveColourSamples.addSample(defaultColourTest.getCurrentAverage());

        task.getPathBuilder().addCatmullCurveVertex(x1, y1);
        task.getPathBuilder().addCatmullCurveVertex(x2, y2);
    }

    @Override
    public int minLineLength() {
        return minLineLength;
    }
}
