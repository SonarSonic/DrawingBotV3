package drawingbot.pfm.helpers;

import drawingbot.api.IPixelData;

public class LuminanceVarianceTestLine extends LuminanceTestLine{

    public VarianceTest varianceTest = new VarianceTest();

    public LuminanceVarianceTestLine() {
        super();
    }

    public LuminanceVarianceTestLine(int[] darkestDst, int minPixelCount, int maxPixelCount, boolean stopPrematurely) {
        super(darkestDst, minPixelCount, maxPixelCount, stopPrematurely);
    }

    @Override
    public float getCurrentSample() {
        return super.getCurrentSample() * varianceTest.getWeightedVariance();
    }

    public void addSampleWithVariance(IPixelData pixels, IPixelData reference, int x, int y) {
        varianceTest.addSample(reference, x, y);
        super.addSample(pixels, x, y);
    }

    @Override
    public void resetSamples() {
        super.resetSamples();
        varianceTest.resetTest();
    }
}
