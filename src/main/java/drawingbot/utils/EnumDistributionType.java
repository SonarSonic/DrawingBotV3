package drawingbot.utils;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.DistributionSet;
import drawingbot.plotting.PlottedDrawing;

import java.util.function.Consumer;

public enum EnumDistributionType {
    //EVEN("Even", false, group -> PlottedDrawing.updateEvenDistribution(group,false)),
    EVEN_WEIGHTED("Even Weighted", group -> PlottedDrawing.updateEvenDistribution(group,true)),
    //RANDOM("Random", false, group -> PlottedDrawing.updateRandomDistribution(group,false)),
    RANDOM_WEIGHTED("Random Weighted", group -> PlottedDrawing.updateRandomDistribution(group, true)),
    //RANDOM_SQUIGGLES("Random Squiggles", false, group -> PlottedDrawing.updateRandomSquiggleDistribution(group,false)),
    RANDOM_SQUIGGLES_WEIGHTED("Random Squiggles Weighted", group -> PlottedDrawing.updateRandomSquiggleDistribution(group, true)),
    //LUMINANCE("Luminance", false, group -> PlottedDrawing.updateLuminanceDistribution(group,true)),
    LUMINANCE_WEIGHTED("Luminance Weighted", group -> PlottedDrawing.updateLuminanceDistribution(group,true)),
    SINGLE_PEN("Single Pen", PlottedDrawing::updateSinglePenDistribution),
    PRECONFIGURED("Preconfigured", PlottedDrawing::updatePreConfiguredPenDistribution);

    public String displayName;
    public final Consumer<DistributionSet> distribute;

    EnumDistributionType(String displayName, Consumer<DistributionSet> distribute){
        this.displayName = displayName;
        this.distribute = distribute;
    }

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }

    public static EnumDistributionType getRecommendedType(){
        return getRecommendedType(DrawingBotV3.context());
    }

    public static EnumDistributionType getRecommendedType(DBTaskContext context){
        return getRecommendedType(context.project.getDrawingSets().getActiveDrawingSet(), context.project.getPFMSettings().getPFMFactory());
    }

    public static EnumDistributionType getRecommendedType(ObservableDrawingSet drawingSet, PFMFactory<?> factory){
        if(drawingSet != null && drawingSet.colorHandler.get().getDistributionType() != null){
            return drawingSet.colorHandler.get().getDistributionType();
        }
        if(factory != null && factory.getDistributionType() != null){
            return factory.getDistributionType();
        }
        return EnumDistributionType.EVEN_WEIGHTED;
    }
}
