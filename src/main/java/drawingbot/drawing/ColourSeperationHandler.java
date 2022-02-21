package drawingbot.drawing;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.DrawingBotV3;
import drawingbot.files.presets.JsonAdapterColourSplitter;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.DBConstants;
import drawingbot.utils.EnumDistributionType;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@JsonAdapter(JsonAdapterColourSplitter.class)
public class ColourSeperationHandler {

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

    public void applySettings(){
        DrawingBotV3.INSTANCE.nextDistributionType = EnumDistributionType.getRecommendedType();
    }

    public void resetSettings(){
        DrawingBotV3.INSTANCE.nextDistributionType = EnumDistributionType.getRecommendedType();
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
