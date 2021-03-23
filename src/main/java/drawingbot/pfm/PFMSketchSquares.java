package drawingbot.pfm;

import drawingbot.api.IPixelData;

public class PFMSketchSquares extends AbstractSketchPFM {

    public PFMSketchSquares(){
        super();
        squiggle_length = 1000;
        adjustbrightness = 9;
        tests = 4;
        minLineLength = 30;
        maxLineLength = 30;
    }

    @Override
    public void findDarkestNeighbour(IPixelData pixels, int start_x, int start_y) {
        float start_angle;
        float delta_angle;

        start_angle = 36F + (float)(Math.toDegrees((Math.sin(Math.toRadians(start_x/9F+46F)) + Math.cos(Math.toRadians(start_y/26F+26F)))));
        delta_angle = 360.0F / (float)tests;

        int nextLineLength = randomSeed(minLineLength, maxLineLength);

        resetLuminanceTest();
        for (int d = 0; d < tests; d ++) {
            luminanceTestAngledLine(pixels, start_x, start_y, nextLineLength, (delta_angle * d) + start_angle);
        }
    }
}