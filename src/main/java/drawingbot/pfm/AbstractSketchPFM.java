package drawingbot.pfm;

import drawingbot.helpers.RawLuminanceData;
import drawingbot.plotting.PlottingTask;

public abstract class AbstractSketchPFM extends AbstractDarkestPFM {

    public int squiggle_length;         // How often to lift the pen
    public int adjustbrightness;        // How fast it moves from dark to light, over-draw
    public float desired_brightness;    // How long to process.  You can always stop early with "s" key

    public int tests;                   // Reasonable values:  13 for development, 720 for final
    public int minLineLength;
    public int maxLineLength;

    public boolean shouldLiftPen;

    protected int squiggle_count;

    protected float initialProgress;
    protected float progress;
    protected RawLuminanceData rawBrightnessData;

    public AbstractSketchPFM(PlottingTask task) {
        super(task);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void init() {
        super.init();
        if(maxLineLength < minLineLength){
            int value = minLineLength;
            minLineLength = maxLineLength;
            maxLineLength = value;
        }
    }

    @Override
    public float progress() {
        return progress;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void findPath() {
        int x, y;
        findDarkestArea(rawBrightnessData);

        x = darkest_x;
        y = darkest_y;
        squiggle_count++;

        findDarkestNeighbour(x, y);

        task.moveAbs(0, darkest_x, darkest_y);
        task.penDown();

        for (int s = 0; s < squiggle_length; s++) {
            findDarkestNeighbour(x, y);
            bresenhamLighten(rawBrightnessData, x, y, darkest_x, darkest_y, adjustbrightness);
            task.moveAbs(0, darkest_x, darkest_y);
            x = darkest_x;
            y = darkest_y;


            float avgBrightness = rawBrightnessData.getAverageBrightness();
            progress = (avgBrightness-initialProgress) / (desired_brightness-initialProgress);
            if(avgBrightness > desired_brightness || task.isCancelled() || finished()){
                finish();
                break;
            }

        }
        if(shouldLiftPen){
            task.penUp();
        }

    }

    protected abstract void findDarkestNeighbour(int x, int y);
}
