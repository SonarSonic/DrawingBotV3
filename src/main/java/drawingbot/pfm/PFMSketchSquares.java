package drawingbot.pfm;

import drawingbot.api.IPixelData;

public class PFMSketchSquares extends AbstractSketchPFM {

    public float startAngle;

    public PFMSketchSquares(){
        super();
    }

    @Override
    public void findDarkestNeighbour(IPixelData pixels, int start_x, int start_y) {
        float angle = startAngle + (float)Math.toDegrees((Math.sin(Math.toRadians((start_x/9D))) + Math.cos(Math.toRadians((start_y/9D)+26D))));
        float deltaAngle = 360.0F / (float) lineTests;

        int nextLineLength = randomSeed(minLineLength, maxLineLength);
        resetLuminanceTest();
        for (int d = 0; d < lineTests; d ++) {
            luminanceTestAngledLine(pixels, start_x, start_y, nextLineLength, (deltaAngle * d) + angle);
        }
    }
}