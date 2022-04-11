package drawingbot.drawing;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.DrawingBotV3;
import drawingbot.files.json.adapters.JsonAdapterColourSplitter;
import drawingbot.registry.Register;
import drawingbot.utils.EnumDistributionType;
import drawingbot.utils.INamedSetting;

@JsonAdapter(JsonAdapterColourSplitter.class)
public class ColourSeperationHandler implements INamedSetting {

    public final String name;
    public boolean applied;

    public ColourSeperationHandler(String name){
        this.name = name;
    }

    public final boolean isDefault(){
        return Register.DEFAULT_COLOUR_SPLITTER == this;
    }

    /**
     * The colour seperators distribution preference, can be null
     */
    public EnumDistributionType getDistributionType(){
        return null;
    }

    public void applySettings(DrawingSets drawingSets){
        if(drawingSets == DrawingBotV3.INSTANCE.drawingSets){
            DrawingBotV3.INSTANCE.pfmSettings.nextDistributionType.set(EnumDistributionType.getRecommendedType(drawingSets.activeDrawingSet.get(), DrawingBotV3.INSTANCE.pfmSettings.factory.get()));
        }
    }

    public void resetSettings(DrawingSets drawingSets){
        if(drawingSets == DrawingBotV3.INSTANCE.drawingSets) {
            DrawingBotV3.INSTANCE.pfmSettings.nextDistributionType.set(EnumDistributionType.getRecommendedType(drawingSets.activeDrawingSet.get(), DrawingBotV3.INSTANCE.pfmSettings.factory.get()));
        }
    }

    /**
     * @return true if the default settings should be applied
     */
    public boolean onUserSelected() {
        return true;
    }

    /**
     * @return true if this colour seperator has an additional UI panel to control it
     */
    public boolean canUserConfigure(){
        return false;
    }

    public void onUserConfigure(){}

    public boolean wasApplied(){
        return applied;
    }

    public void setApplied(boolean appliedSettings){
        this.applied = appliedSettings;
    }

    @Override
    public String toString() {
        return name;
    }
}
