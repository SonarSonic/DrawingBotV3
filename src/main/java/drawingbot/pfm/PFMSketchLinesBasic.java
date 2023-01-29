package drawingbot.pfm;

import drawingbot.api.IPixelData;

public class PFMSketchLinesBasic extends AbstractSketchPFM {

    public int startAngleMin;
    public int startAngleMax;

    public boolean enableShading;
    public float shadingThreshold;
    public float drawingDeltaAngle;
    public float shadingDeltaAngle;
    public boolean unlimitedTests;

    @Override
    public void nextPathFindingResult(PathFindingContext context, IPixelData pixels) {
        boolean shading = enableShading && shadingThreshold < lumProgress;
        float deltaAngle = shading ? shadingDeltaAngle : drawingDeltaAngle;
        float startAngle = tools.randomFloat(startAngleMin, startAngleMax) + 0.5F;
        int nextLineLength = tools.randomInt(minLineLength, maxLineLength);
        float avgDarkness = tools.findDarkestLine(pixels, tools.getSoftClipPixelMask(), context.getX(), context.getY(), minLineLength, nextLineLength, unlimitedTests ? -1 : lineTests, startAngle, deltaAngle, shading, context.getDstPosition());

        context.setResult(context.getDstPosition(), avgDarkness);
    }
}