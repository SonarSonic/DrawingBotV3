package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.geom.basic.GLine;
import drawingbot.plotting.PlottingTask;

public abstract class AbstractSketchPFM extends AbstractDarkestPFM {

    public int squiggle_length;         // How often to lift the pen
    public int adjustbrightness;        // How fast it moves from dark to light, over-draw
    public float desired_brightness;    // How long to process.  You can always stop early with "s" key

    public int tests;                   // Reasonable values:  13 for development, 720 for final
    public int minLineLength;
    public int maxLineLength;
    public int maxLines;

    public boolean shouldLiftPen;

    protected int squiggle_count;

    protected double initialProgress;

    protected int x = -1;
    protected int y = -1;

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

        if(!shouldLiftPen && x != -1){
            addGeometry(task, x, y, darkest_x, darkest_y, adjustbrightness);
        }

        x = darkest_x;
        y = darkest_y;
        squiggle_count++;

        for (int s = 0; s < squiggle_length; s++) {
            findDarkestNeighbour(task.getPixelData(), x, y);
            addGeometry(task, x, y, darkest_x, darkest_y, adjustbrightness);
            x = darkest_x;
            y = darkest_y;
            if(updateProgress(task) || task.isFinished()){
                task.finishProcess();
                return;
            }
        }
    }

    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust){
        int rgba = adjustLuminanceLine(task, task.getPixelData(), x1, y1, x2, y2, adjust);
        task.addGeometry(new GLine(x1, y1, x2, y2), null, rgba);
    }

    protected boolean updateProgress(IPlottingTask task){
        PlottingTask plottingTask = (PlottingTask) task;
        double avgLuminance = task.getPixelData().getAverageLuminance();
        double lineProgress = maxLines == -1 ? 0 : (double)plottingTask.plottedDrawing.geometries.size() / maxLines;
        double lumProgress = avgLuminance >= desired_brightness ? 1 : (avgLuminance-initialProgress) / (desired_brightness-initialProgress);
        double progress = Math.max(lineProgress, lumProgress);

        task.updatePlottingProgress(progress, 1D);
        return progress >= 1;

    }

    protected abstract void findDarkestNeighbour(IPixelData pixels, int x, int y);
}
