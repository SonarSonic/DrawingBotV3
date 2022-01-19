package drawingbot.pfm;

import drawingbot.api.IPixelData;

public class PFMSketchSquares extends AbstractSketchPFM {

    public float startAngle;

    @Override
    protected float findDarkestNeighbour(IPixelData pixels, int[] point, int[] darkestDst) {
        float angle = startAngle + (float)Math.toDegrees((Math.sin(Math.toRadians((point[0]/9D))) + Math.cos(Math.toRadians((point[1]/9D)+26D))));
        int nextLineLength = randomSeed(minLineLength, maxLineLength);
        return findDarkestLine(pixels, point[0], point[1], minLineLength, nextLineLength, lineTests, angle, 360.0F, false, darkestDst);
    }
}