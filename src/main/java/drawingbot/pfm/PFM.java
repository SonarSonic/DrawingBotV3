package drawingbot.pfm;

import drawingbot.DrawingBotV3;
import drawingbot.plotting.PlottingTask;

public abstract class PFM implements IPFM {

    public static DrawingBotV3 app = DrawingBotV3.INSTANCE;
    public final PlottingTask task;
    public boolean finished = false;

    public PFM(PlottingTask task){
        this.task = task;
    }

    protected void finish(){
        finished = true;
    }

    @Override
    public boolean finished(){
        return finished;
    }
}
