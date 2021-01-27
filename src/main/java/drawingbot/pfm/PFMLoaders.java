package drawingbot.pfm;

import drawingbot.pfm.legacy.LegacyPFMLoaders;
import drawingbot.plotting.PlottingTask;

import java.util.function.Function;

public enum PFMLoaders {

    SKETCH("Sketch PFM", PFMSketch::new),
    SQUARES("Squares PFM", PFMSquares::new),
    SPIRAL("Spiral PFM", PFMSpiral::new),
    SKETCH_LEGACY("Sketch PFM (Legacy)", LegacyPFMLoaders.pfmSketchLegacy),
    SQUARES_LEGACY("Squares PFM (Legacy)", LegacyPFMLoaders.pfmSquaresLegacy),
    SPIRAL_LEGACY("Spiral PFM (Legacy)", LegacyPFMLoaders.pfmSpiralLegacy);

    private final String name;
    private final Function<PlottingTask, IPFM> create;

    PFMLoaders(String name, Function<PlottingTask, IPFM> create) {
        this.name = name;
        this.create = create;
    }

    public IPFM createNewPFM(PlottingTask task){
        return create.apply(task);
    }

    public String getName(){
        return name;
    }
    @Override
    public String toString() {
        return getName();
    }

}
