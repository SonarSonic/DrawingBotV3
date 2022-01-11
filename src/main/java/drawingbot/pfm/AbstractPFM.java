package drawingbot.pfm;

import drawingbot.api.IPathFindingModule;
import drawingbot.api.IPlottingTask;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.Utils;

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
    public int randomSeed(int a, int b){
        return a == b ? a : a > b ? randomSeed.nextInt(a-b) + b : randomSeed.nextInt(b-a) + a;
    }

    /**produces random results which will be consistent every time the PFM is run*/
    public float randomSeedF(float a, float b){
        return a == b ? a : a > b ? randomSeed.nextFloat()*(a-b) + b : randomSeed.nextFloat()*(b-a) + a;
    }

    public int clampX(int x){
        return Utils.clamp(x, 0, task.getPixelData().getWidth()-1);
    }

    public int clampY(int y){
        return Utils.clamp(y, 0, task.getPixelData().getHeight()-1);
    }

    public boolean withinX(int x){
        return Utils.within(x, 0, task.getPixelData().getWidth()-1);
    }

    public boolean withinY(int y){
        return Utils.within(y, 0, task.getPixelData().getHeight()-1);
    }

    public boolean withinXY(int x, int y){
        return withinX(x) && withinY(y);
    }

}
