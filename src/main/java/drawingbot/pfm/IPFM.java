package drawingbot.pfm;

import drawingbot.plotting.PlottingTask;

public interface IPFM {

    boolean finished();

    void finish();

    float progress();

    void init(PlottingTask task);

    void preProcess();

    void doProcess();

    void postProcess();

}
