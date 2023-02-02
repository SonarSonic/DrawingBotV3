package drawingbot.drawing;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.files.json.adapters.JsonAdapterColourSplitter;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.observables.ObservableDrawingSet;
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

    public void applySettings(DBTaskContext context, ObservableDrawingSet activeSet){
        context.project().getPFMSettings().setNextDistributionType(EnumDistributionType.getRecommendedType(activeSet, context.project().getPFMSettings().getPFMFactory()));
    }

    public void resetSettings(DBTaskContext context, ObservableDrawingSet activeSet){
        context.project().getPFMSettings().setNextDistributionType(EnumDistributionType.getRecommendedType(activeSet, context.project().getPFMSettings().getPFMFactory()));
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
