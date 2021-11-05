package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.pfm.helpers.LuminanceTestLine;

public class PFMSketchLines extends AbstractSketchPFM {

    private LuminanceTestLine luminanceTest;

    public int startAngleMin;
    public int startAngleMax;

    public boolean enableShading;
    public float shadingThreshold;
    public float drawingDeltaAngle;
    public float shadingDeltaAngle;

    @Override
    public void init(IPlottingTask task) {
        super.init(task);
        luminanceTest = new LuminanceTestLine(darkest, minLineLength, maxLineLength, true);
        if(startAngleMax < startAngleMin){
            int value = startAngleMin;
            startAngleMin = startAngleMax;
            startAngleMax = value;
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

        int nextLineLength = randomSeed(minLineLength, maxLineLength);

        LuminanceTestLine luminanceTest = new LuminanceTestLine(darkest, minLineLength, maxLineLength, true);
        for (int d = 0; d < lineTests; d ++) {
            luminanceTest.resetSamples();
            bresenham.plotAngledLine(point[0], point[1], nextLineLength, (delta_angle * d) + start_angle, (x, y) -> luminanceTest.addSample(pixels, x, y));
        }
        return luminanceTest.getDarkestSample() < pixels.getAverageLuminance();
    }

}