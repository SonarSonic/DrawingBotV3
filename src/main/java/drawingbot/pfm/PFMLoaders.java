package drawingbot.pfm;

import drawingbot.tasks.PlottingTask;

import java.util.function.Function;

public enum PFMLoaders {

    ORIGINAL("Original PFM", PFMOriginal::new),
    SPIRAL("Spiral PFM", PFMSpiral::new),
    SQUARES("Squares PFM", PFMSquares::new);

    private String name;
    private Function<PlottingTask, IPFM> create;

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
