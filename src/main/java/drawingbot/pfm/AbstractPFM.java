package drawingbot.pfm;

import drawingbot.api.IPathFindingModule;
import drawingbot.api.IPlottingTask;

import java.util.Random;

public abstract class AbstractPFM implements IPathFindingModule {

    protected float pfmResolution = 1;
    protected int transparentARGB = -1;

    protected int seed = 0;
    protected Random randomSeed;

    @Override
    public void init(IPlottingTask task) {
        randomSeed = new Random(seed);
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
        return randomSeed.nextInt(high-low) + low;
    }

}
