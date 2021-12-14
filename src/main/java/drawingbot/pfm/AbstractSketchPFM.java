package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.geom.basic.GLine;
import drawingbot.plotting.PlottingTask;

public abstract class AbstractSketchPFM extends AbstractDarkestPFM {

    //user settings
    public int squiggle_length;
    public int adjustbrightness;
    public float lineDensity;

    public int lineTests;
    public int minLineLength;
    public int maxLineLength;
    public int maxLines;

    public boolean shouldLiftPen;

    //process specific
    protected double initialLuminance;

    protected int squiggle_count;

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


    int[] current = new int[]{-1, -1};
    int[] darkest = new int[]{-1, -1};
    int neighbourFails = 0;
    int maxNeighbourFails = 20;

    @Override
    public void doProcess() {
        findDarkestArea(task.getPixelData(), darkest);

        if(!shouldLiftPen && current[0] != -1){
            addGeometry(task, current[0], current[1], darkest[0], darkest[1], adjustbrightness);
        }

        current[0] = darkest[0];
        current[1] = darkest[1];

        squiggle_count++;

        for (int s = 0; s < squiggle_length; s++) {
            if(!findDarkestNeighbour(task.getPixelData(), current, darkest) && neighbourFails < maxNeighbourFails){
                neighbourFails++;
                return;
            }
            neighbourFails = 0;
            addGeometry(task, current[0], current[1], darkest[0], darkest[1], adjustbrightness);

            current[0] = darkest[0];
            current[1] = darkest[1];

            if(updateProgress(task) || task.isFinished()){
                task.finishProcess();

/*
                //TODO DELETE ME //FIXME
                task.addGeometry(new GLine(-10 + task.getPixelData().getWidth()/2F, task.getPixelData().getHeight()/2F, 10 + task.getPixelData().getWidth()/2F, task.getPixelData().getHeight()/2F));

                task.addGeometry(new GLine(task.getPixelData().getWidth()/2F, -10 + task.getPixelData().getHeight()/2F, task.getPixelData().getWidth()/2F, 10 + task.getPixelData().getHeight()/2F));
*/
                return;
            }
        }
    }

    //TODO CHANGE ORDER OF ADDED GEOMETRIES!!!
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

    /**
     * @return returns false if the only line available isn't dark enough
     */
    protected abstract boolean findDarkestNeighbour(IPixelData pixels, int[] point, int[] darkestDst);

    @Override
    public int minLineLength() {
        return minLineLength;
    }

    @Override
    public int maxLineLength() {
        return maxLineLength;
    }
}
