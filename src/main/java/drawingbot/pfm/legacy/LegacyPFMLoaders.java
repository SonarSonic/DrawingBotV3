package drawingbot.pfm.legacy;

import drawingbot.pfm.IPFM;
import drawingbot.plotting.PlottingTask;

import java.util.function.Function;
import java.util.function.Supplier;

/**as the legacy classes are not longer public, this is a way to still create instances, but without the classes being referenced elsewhere by mistake*/
public class LegacyPFMLoaders {

    public static final Supplier<IPFM> pfmSketchLegacy = PFMSketchLegacy::new;
    public static final Supplier<IPFM> pfmSquaresLegacy = PFMSquaresLegacy::new;
    public static final Supplier<IPFM> pfmSpiralLegacy = PFMSpiralLegacy::new;


    public static final Class<? extends IPFM> pfmSketchLegacyClass = PFMSketchLegacy.class;
    public static final Class<? extends IPFM> pfmSquaresLegacyClass = PFMSquaresLegacy.class;
    public static final Class<? extends IPFM> pfmSpiralLegacyClass = PFMSpiralLegacy.class;

}
