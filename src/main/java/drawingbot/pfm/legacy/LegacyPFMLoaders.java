package drawingbot.pfm.legacy;

import drawingbot.pfm.IPFM;
import drawingbot.plotting.PlottingTask;

import java.util.function.Function;

/**as the legacy classes are not longer public, this is a way to still create instances, but without the classes being referenced elsewhere by mistake*/
public class LegacyPFMLoaders {

    public static final Function<PlottingTask, IPFM> pfmSketchLegacy = PFMSketchLegacy::new;
    public static final Function<PlottingTask, IPFM> pfmSquaresLegacy = PFMSquaresLegacy::new;
    public static final Function<PlottingTask, IPFM> pfmSpiralLegacy = PFMSpiralLegacy::new;

}
