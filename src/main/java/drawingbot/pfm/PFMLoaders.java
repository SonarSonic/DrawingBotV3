package drawingbot.pfm;

import java.util.function.Supplier;

public enum PFMLoaders {

    ORIGINAL("Original PFM", PFMOriginal::new),
    SPIRAL("Spiral PFM", PFMSpiral::new),
    SQUARES("Squares PFM", PFMSquares::new);

    private String name;
    private Supplier<IPFM> create;

    PFMLoaders(String name, Supplier<IPFM> create) {
        this.name = name;
        this.create = create;
    }

    public String getName(){
        return name;
    }

    public IPFM createNewPFM(){
        return create.get();
    }
}
