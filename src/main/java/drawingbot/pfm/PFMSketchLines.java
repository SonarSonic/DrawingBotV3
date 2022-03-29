package drawingbot.pfm;

import drawingbot.api.IPixelData;

public class PFMSketchLines extends AbstractSketchPFM {

    public int startAngleMin;
    public int startAngleMax;

    public boolean enableShading;
    public float shadingThreshold;
    public float drawingDeltaAngle;
    public float shadingDeltaAngle;
    public boolean unlimitedTests;

    @Override
    protected float findDarkestNeighbour(IPixelData pixels, int[] point, int[] darkestDst) {
        boolean shading = enableShading && shadingThreshold < lumProgress;
        float deltaAngle = shading ? shadingDeltaAngle : drawingDeltaAngle;
        float startAngle = tools.randomFloat(startAngleMin, startAngleMax) + 0.5F;
        int nextLineLength = tools.randomInt(minLineLength, maxLineLength);
        return tools.findDarkestLine(pixels, point[0], point[1], minLineLength, nextLineLength, unlimitedTests ? -1 : lineTests, startAngle, deltaAngle, shading, darkestDst);
    }

}