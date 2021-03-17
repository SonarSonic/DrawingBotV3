package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;

public class PFMSketchLines extends AbstractSketchPFM {

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
        float delta_angle;
        float start_angle = randomSeed(startAngleMin, startAngleMax);

        if (!enableShading || squiggle_count < squigglesTillShading) {
            delta_angle = drawingDeltaAngle / (float)tests;
        } else {
            delta_angle = shadingDeltaAngle;
        }

        int nextLineLength = randomSeed(minLineLength, maxLineLength);

        resetLuminanceTest();
        for (int d = 0; d < tests; d ++) {
            luminanceTestAngledLine(pixels, start_x, start_y, nextLineLength, (delta_angle * d) + start_angle);
        }
    }

}