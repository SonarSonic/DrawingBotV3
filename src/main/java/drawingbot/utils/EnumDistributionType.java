package drawingbot.utils;

import drawingbot.plotting.PlottedDrawing;

import java.util.function.Consumer;

public enum EnumDistributionType {
    EVEN(drawing -> drawing.updateEvenDistribution(false, false)),
    EVEN_WEIGHTED(drawing -> drawing.updateEvenDistribution(true, false)),
    RANDOM(drawing -> drawing.updateEvenDistribution(false, true)),
    RANDOM_WEIGHTED(drawing -> drawing.updateEvenDistribution(true, true)),
    SINGLE_PEN(PlottedDrawing::updateSinglePenDistribution),
    PRECONFIGURED(PlottedDrawing::updatePreConfiguredPenDistribution);

    public final Consumer<PlottedDrawing> distribute;

    EnumDistributionType(Consumer<PlottedDrawing> distribute){
        this.distribute = distribute;
    }

    @Override
    public String toString() {
        if(this == EVEN_WEIGHTED){
            return Utils.capitalize(name()) + " " + "(Default)";
        }
        return Utils.capitalize(name());
    }
}
