package drawingbot.pfm;

import drawingbot.api.IPFM;
import drawingbot.api.IPlottingTools;
import drawingbot.plotting.PFMTask;
import drawingbot.plotting.PlottingTools;

public abstract class AbstractPFM implements IPFM {

    public PlottingTools tools;

    @Override
    public void init(IPlottingTools tools) {
        this.tools = (PlottingTools)tools;
    }

}
