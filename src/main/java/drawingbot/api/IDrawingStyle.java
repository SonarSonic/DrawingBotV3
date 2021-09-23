package drawingbot.api;

import com.google.gson.JsonElement;
import drawingbot.pfm.PFMFactory;

import java.util.HashMap;

public interface IDrawingStyle {

    boolean isEnabled();

    String getName();

    String getPFMName();

    PFMFactory<?> getFactory();

    HashMap<String, JsonElement> getSaveableSettings();

    int getDistributionWeight();

}
