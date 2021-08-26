package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.geom.basic.GLine;
import drawingbot.plotting.PlottingTask;

public abstract class AbstractSketchPFM extends AbstractDarkestPFM {

    public int squiggle_length;
    public int adjustbrightness;
    public float lineDensity;

    public int lineTests;
    public int minLineLength;
    public int maxLineLength;
    public int maxLines;

    public boolean shouldLiftPen;

    protected int squiggle_count;

    protected double initialLuminance;

    protected int x = -1;
    protected int y = -1;

    public final float desiredLuminance = 253.5F;

    //latest progress
    protected double lineProgress = 0;
    protected double lumProgress = 0;
    protected double actualProgress = 0;

    /////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void init(IPlottingTask task) {
        super.init(task);
        if(maxLineLength < minLineLength){
            int value = minLineLength;
            minLineLength = maxLineLength;
            maxLineLength = value;
        }
        initialLuminance = this.task.getPixelData().getAverageLuminance();
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void doProcess() {
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
        lineProgress = maxLines == -1 ? 0 : (double)plottingTask.plottedDrawing.geometries.size() / maxLines;
        lumProgress = avgLuminance >= desiredLuminance ? 1 : (avgLuminance - initialLuminance) / ((desiredLuminance - initialLuminance)*lineDensity);
        actualProgress = Math.max(lineProgress, lumProgress);

        task.updatePlottingProgress(actualProgress, 1D);
        return actualProgress >= 1;
    }

    protected abstract void findDarkestNeighbour(IPixelData pixels, int x, int y);

    @Override
    public int minLineLength() {
        return minLineLength;
    }

    @Override
    public int maxLineLength() {
        return maxLineLength;
    }
}
