package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;

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

    /////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void init(IPlottingTask task) {
        super.init(task);
        if(maxLineLength < minLineLength){
            int value = minLineLength;
            minLineLength = maxLineLength;
            maxLineLength = value;
        }
        initialProgress = task.getPixelData().getAverageLuminance();
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void doProcess(IPlottingTask task) {
        findDarkestArea(task.getPixelData());

        int x = darkest_x;
        int y = darkest_y;
        squiggle_count++;

        findDarkestNeighbour(task.getPixelData(), x, y);

        task.moveAbsolute(darkest_x, darkest_y);
        task.movePenDown();

        for (int s = 0; s < squiggle_length; s++) {
            findDarkestNeighbour(task.getPixelData(), x, y);
            bresenhamLighten(task, task.getPixelData(), x, y, darkest_x, darkest_y, adjustbrightness);
            task.moveAbsolute(darkest_x, darkest_y);
            x = darkest_x;
            y = darkest_y;


            float avgLuminance = task.getPixelData().getAverageLuminance();
            task.updateProgess(avgLuminance-initialProgress, desired_brightness-initialProgress);
            if(avgLuminance > desired_brightness || task.isFinished()){
                task.finishProcess();
                return;
            }

        }
        if(shouldLiftPen){
            task.movePenUp();
        }
    }

    protected abstract void findDarkestNeighbour(IPixelData pixels, int x, int y);
}
