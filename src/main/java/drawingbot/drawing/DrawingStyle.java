package drawingbot.drawing;

import com.google.gson.JsonElement;
import drawingbot.api.IDrawingStyle;
import drawingbot.pfm.PFMFactory;
import drawingbot.registry.MasterRegistry;

import java.util.HashMap;

public class DrawingStyle implements IDrawingStyle {

    public boolean enabled;
    public String name;
    public String pfm;
    public HashMap<String, JsonElement> settings;
    public int weight;

    public DrawingStyle(){}

    public DrawingStyle(boolean enabled, String name, String pfm, int weight, HashMap<String, JsonElement> settings){
        update(enabled, name, pfm, weight, settings);
    }

    public DrawingStyle(IDrawingStyle style){
        update(style.isEnabled(), style.getName(), style.getPFMName(), style.getDistributionWeight(), style.getSaveableSettings());
    }

    public void update(boolean enabled, String name, String pfm, int weight, HashMap<String, JsonElement> settings){
        this.enabled = enabled;
        this.name = name;
        this.pfm = pfm;
        this.settings = settings;
        this.weight = weight;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPFMName() {
        return pfm;
    }

    @Override
    public PFMFactory<?> getFactory() {
        return MasterRegistry.INSTANCE.getPFMFactory(getPFMName());
    }

    @Override
    public HashMap<String, JsonElement> getSaveableSettings() {
        return settings;
    }

    @Override
    public int getDistributionWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return getName();
    }
}
