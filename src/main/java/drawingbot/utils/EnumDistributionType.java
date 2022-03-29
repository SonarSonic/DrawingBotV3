package drawingbot.utils;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.DistributionSet;
import drawingbot.plotting.PlottedDrawing;

import java.util.function.Consumer;

public enum EnumDistributionType {
    EVEN(group -> PlottedDrawing.updateEvenDistribution(group,false, false)),
    EVEN_WEIGHTED(group -> PlottedDrawing.updateEvenDistribution(group,true, false)),
    RANDOM(group -> PlottedDrawing.updateEvenDistribution(group,false, true)),
    RANDOM_WEIGHTED(group -> PlottedDrawing.updateEvenDistribution(group, true, true)),
    SINGLE_PEN(PlottedDrawing::updateSinglePenDistribution),
    PRECONFIGURED(PlottedDrawing::updatePreConfiguredPenDistribution);

    public final Consumer<DistributionSet> distribute;

    EnumDistributionType(Consumer<DistributionSet> distribute){
        this.distribute = distribute;
    }

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }

    public static EnumDistributionType getRecommendedType(){
        return getRecommendedType(DrawingBotV3.INSTANCE.activeDrawingSet.get(), DrawingBotV3.INSTANCE.pfmFactory.get());
    }

    public static EnumDistributionType getRecommendedType(ObservableDrawingSet drawingSet, PFMFactory<?> factory){
        if(drawingSet != null && drawingSet.colourSeperator.get().getDistributionType() != null){
            return drawingSet.colourSeperator.get().getDistributionType();
        }
        if(factory != null && factory.getDistributionType() != null){
            return factory.getDistributionType();
        }
        return EnumDistributionType.EVEN_WEIGHTED;
    }
}
