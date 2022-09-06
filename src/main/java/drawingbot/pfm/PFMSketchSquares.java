package drawingbot.pfm;

import drawingbot.api.IPixelData;

public class PFMSketchSquares extends AbstractSketchPFM {

    public float startAngle;

    @Override
    protected float findDarkestNeighbour(IPixelData pixels, int[] lastPoint, int[] currentPoint, int[] darkestDst) {
        float angle = startAngle + (float)Math.toDegrees((Math.sin(Math.toRadians((currentPoint[0]/9D))) + Math.cos(Math.toRadians((currentPoint[1]/9D)+26D))));
        int nextLineLength = tools.randomInt(minLineLength, maxLineLength);
        return tools.findDarkestLine(pixels, currentPoint[0], currentPoint[1], minLineLength, nextLineLength, lineTests, angle, 360.0F, false, darkestDst);
    }
}