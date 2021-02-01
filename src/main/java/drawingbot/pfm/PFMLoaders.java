package drawingbot.pfm;

import drawingbot.pfm.legacy.LegacyPFMLoaders;
import drawingbot.plotting.PlottingTask;

import java.util.function.Function;

public enum PFMLoaders {

    SKETCH("Sketch PFM", PFMSketch.class, PFMSketch::new),
    SQUARES("Squares PFM", PFMSquares.class, PFMSquares::new),
    SPIRAL("Spiral PFM", PFMSpiral.class, PFMSpiral::new),
    SINGLE_LINE("Single Line PFM (Experimental)", PFMSingleLine.class, PFMSingleLine::new),
    LINES("Lines PFM (Experimental)", PFMLines.class, PFMLines::new),
    SKETCH_LEGACY("Sketch PFM (Legacy)", LegacyPFMLoaders.pfmSketchLegacyClass, LegacyPFMLoaders.pfmSketchLegacy),
    SQUARES_LEGACY("Squares PFM (Legacy)", LegacyPFMLoaders.pfmSquaresLegacyClass, LegacyPFMLoaders.pfmSquaresLegacy),
    SPIRAL_LEGACY("Spiral PFM (Legacy)", LegacyPFMLoaders.pfmSpiralLegacyClass, LegacyPFMLoaders.pfmSpiralLegacy);

    private final String name;
    private final Class<? extends IPFM> pfmClass;
    private final Function<PlottingTask, IPFM> create;

    PFMLoaders(String name, Class<? extends IPFM> pfmClass, Function<PlottingTask, IPFM> create) {
        this.name = name;
        this.pfmClass = pfmClass;
        this.create = create;
    }

    public Class<? extends IPFM> getPFMClass(){
        return pfmClass;
    }

    public IPFM createNewPFM(PlottingTask task){
        IPFM pfm = create.apply(task);
        PFMSettingsRegistry.applySettings(pfm);
        return pfm;
    }

    public String getName(){
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

}
