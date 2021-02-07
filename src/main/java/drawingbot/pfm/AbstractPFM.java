package drawingbot.pfm;

import drawingbot.api.IPathFindingModule;
import drawingbot.api.IPlottingTask;

import java.util.Random;

public abstract class AbstractPFM implements IPathFindingModule {

    protected float pfmResolution = 1;

    protected long seed = 0;
    protected Random randomSeed;

    @Override
    public void init(IPlottingTask task) {
        randomSeed = new Random(seed);
        task.setPlottingResolution(pfmResolution);
    }

    /**produces random results which will be consistent every time the PFM is run*/
    public int randomSeed(int low, int high){
        return randomSeed.nextInt(high-low) + low;
    }

}
