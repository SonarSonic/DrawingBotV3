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
    protected float findDarkestNeighbour(IPixelData pixels, int[] lastPoint, int[] current, int[] darkestDst) {
        boolean shading = enableShading && shadingThreshold < lumProgress;
        float deltaAngle = shading ? shadingDeltaAngle : drawingDeltaAngle;
        float startAngle = tools.randomFloat(startAngleMin, startAngleMax) + 0.5F;
        int nextLineLength = tools.randomInt(minLineLength, maxLineLength);

        float lastAngle = startAngle;

        if(lastPoint[0] != -1 && current[0] != -1){
            lastAngle = (float) getAngle(lastPoint[0], lastPoint[1], current[0], current[0]);
        }

        return AbstractDarkestPFM.findDarkestLineWithVariance(tools, tools.bresenham, pixels, tools.getReferencePixelData(), current[0], current[1], minLineLength, nextLineLength, unlimitedTests ? -1 : lineTests, startAngle, lastAngle, deltaAngle, shading, directionality, distortion, angularity, darkestDst);
    }

}