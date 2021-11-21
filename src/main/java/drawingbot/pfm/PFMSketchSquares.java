package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.pfm.helpers.LuminanceTestLine;

public class PFMSketchSquares extends AbstractSketchPFM {

    public float startAngle;

    public PFMSketchSquares(){
        super();
    }

    @Override
    protected boolean findDarkestNeighbour(IPixelData pixels, int[] point, int[] darkestDst) {
        float angle = startAngle + (float)Math.toDegrees((Math.sin(Math.toRadians((point[0]/9D))) + Math.cos(Math.toRadians((point[1]/9D)+26D))));
        float deltaAngle = 360.0F / (float) lineTests;

        int nextLineLength = randomSeed(minLineLength, maxLineLength);
        LuminanceTestLine luminanceTest = new LuminanceTestLine(darkestDst, minLineLength, maxLineLength, true);
        luminanceTest.resetTest();
        for (int d = 0; d < lineTests; d ++) {
            luminanceTest.resetSamples();
            bresenham.plotAngledLine(point[0], point[1], nextLineLength, (deltaAngle * d) + angle, (x, y) -> luminanceTest.addSample(pixels, x, y));
        }
        return luminanceTest.getDarkestSample() < pixels.getAverageLuminance();
    }
}