package drawingbot.pfm;

import drawingbot.api.IPFM;
import drawingbot.api.IPlottingTools;
import drawingbot.plotting.PlottingTools;

public abstract class AbstractPFM implements IPFM {

    public float pfmResolution = 1;

    public PlottingTools tools;

    @Override
    public final void setPlottingTools(IPlottingTools tools) {
        this.tools = (PlottingTools)tools;
    }

    @Override
    public float getPlottingResolution() {
        return pfmResolution;
    }

    @Override
    public void onStopped() {
        IPFM.super.onStopped();
    }
}
