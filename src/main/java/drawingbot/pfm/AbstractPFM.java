package drawingbot.pfm;

import drawingbot.api.IPathFindingModule;
import drawingbot.api.IPlottingTask;
import drawingbot.plotting.PlottingTask;

import java.util.Random;

public abstract class AbstractPFM implements IPathFindingModule {

    public PlottingTask task;
    public float pfmResolution = 1;
    public int transparentARGB = -1;

    public int seed = 0;
    public Random randomSeed;

    @Override
    public void init(IPlottingTask task) {
        this.task = (PlottingTask) task;
        this.randomSeed = new Random(seed);
    }

    @Override
    public float getPlottingResolution() {
        return pfmResolution;
    }

    @Override
    public int getTransparentARGB() {
        return transparentARGB;
    }

    /**produces random results which will be consistent every time the PFM is run*/
    public int randomSeed(int low, int high){
        return low == high ? low : randomSeed.nextInt(high-low) + low;
    }

    /**produces random results which will be consistent every time the PFM is run*/
    public float randomSeedF(float low, float high){
        return low == high ? low : (randomSeed.nextFloat()*(high-low)) + low;
    }

}
