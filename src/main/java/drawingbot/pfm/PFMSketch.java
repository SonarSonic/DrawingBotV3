package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;

public class PFMSketch extends AbstractSketchPFM {

    public boolean enableShading;
    public int squigglesTillShading;
    public int startAngleMin;
    public int startAngleMax;
    public float drawingDeltaAngle;
    public float shadingDeltaAngle;

    @Override
    public void init(IPlottingTask task) {
        super.init(task);
        if(startAngleMax < startAngleMin){
            int value = startAngleMin;
            startAngleMin = startAngleMax;
            startAngleMax = value;
        }
    }

    @Override
    public void findDarkestNeighbour(IPixelData pixels, int start_x, int start_y) {
        darkest_neighbor = 1000;
        float delta_angle;
        float start_angle = randomSeed(startAngleMin, startAngleMax);    // Spitfire;

        if (!enableShading || squiggle_count < squigglesTillShading) {
            delta_angle = drawingDeltaAngle / (float)tests;
        } else {
            delta_angle = shadingDeltaAngle;
        }

        int nextLineLength = randomSeed(minLineLength, maxLineLength);
        for (int d = 0; d < tests; d ++) {
            bresenhamAvgLuminance(pixels, start_x, start_y, nextLineLength, (delta_angle * d) + start_angle);
        }
    }

}