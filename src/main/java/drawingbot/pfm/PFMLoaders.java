package drawingbot.pfm;

import drawingbot.pfm.legacy.LegacyPFMLoaders;
import drawingbot.plotting.PlottingTask;

import java.util.function.Function;

public enum PFMLoaders {

    SKETCH("Sketch PFM", PFMSketch.class, PFMSketch::new, false),
    SQUARES("Squares PFM", PFMSquares.class, PFMSquares::new, false),
    SPIRAL("Spiral PFM", PFMSpiral.class, PFMSpiral::new, false),
    LINES("Lines PFM (Experimental)", PFMLines.class, PFMLines::new, true),
    SKETCH_LEGACY("Sketch PFM (Legacy)", LegacyPFMLoaders.pfmSketchLegacyClass, LegacyPFMLoaders.pfmSketchLegacy, true),
    SQUARES_LEGACY("Squares PFM (Legacy)", LegacyPFMLoaders.pfmSquaresLegacyClass, LegacyPFMLoaders.pfmSquaresLegacy, true),
    SPIRAL_LEGACY("Spiral PFM (Legacy)", LegacyPFMLoaders.pfmSpiralLegacyClass, LegacyPFMLoaders.pfmSpiralLegacy, true);

    private final String name;
    private final Class<? extends IPFM> pfmClass;
    private final Function<PlottingTask, IPFM> create;
    private final boolean isHidden;

    PFMLoaders(String name, Class<? extends IPFM> pfmClass, Function<PlottingTask, IPFM> create, boolean isHidden) {
        this.name = name;
        this.pfmClass = pfmClass;
        this.create = create;
        this.isHidden = isHidden;
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

    public boolean isHidden() {
        return isHidden;
    }

    @Override
    public String toString() {
        return getName();
    }

}
