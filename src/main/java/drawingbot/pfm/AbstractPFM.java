package drawingbot.pfm;

import drawingbot.DrawingBotV3;
import drawingbot.plotting.PlottingTask;

import java.util.Random;

public abstract class AbstractPFM implements IPFM {

    public static DrawingBotV3 app = DrawingBotV3.INSTANCE;
    public PlottingTask task;
    public boolean finished = false;

    protected float plottingResolution = 1;

    protected long seed = 0;
    protected Random randomSeed;

    @Override
    public void init(PlottingTask task) {
        this.task = task;
        randomSeed = new Random(seed);
    }

    /**produces random results which will be consistent every time the PFM is run*/
    public int randomSeed(int low, int high){
        return randomSeed.nextInt(high-low) + low;
    }

    @Override
    public void finish(){
        finished = true;
    }

    @Override
    public boolean finished(){
        return finished;
    }
}
