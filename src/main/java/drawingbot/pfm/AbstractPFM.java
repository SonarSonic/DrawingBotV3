package drawingbot.pfm;

import drawingbot.DrawingBotV3;
import drawingbot.plotting.PlottingTask;

import java.util.Random;

public abstract class AbstractPFM implements IPFM {

    public static DrawingBotV3 app = DrawingBotV3.INSTANCE;
    public final PlottingTask task;
    public boolean finished = false;

    protected long seed = 0;
    protected Random randomSeed;

    public AbstractPFM(PlottingTask task){
        this.task = task;
    }

    @Override
    public void init() {
        randomSeed = new Random(seed);
    }

    /**produces random results which will be consistent every time the PFM is run*/
    public int randomSeed(int low, int high){
        return randomSeed.nextInt(high-low) + low;
    }

    protected void finish(){
        finished = true;
    }

    @Override
    public boolean finished(){
        return finished;
    }
}
