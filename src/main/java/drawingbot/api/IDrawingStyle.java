package drawingbot.api;

import com.google.gson.JsonElement;
import drawingbot.drawing.DrawingSets;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.pfm.PFMFactory;
import javafx.scene.paint.Color;

import java.util.HashMap;

//TODO - if this is actually API, it shouldn't have internal dependencies
public interface IDrawingStyle {

    boolean isEnabled();

    String getName();

    String getPFMName();

    PFMFactory<?> getFactory();

    HashMap<String, JsonElement> getSaveableSettings();

    int getDistributionWeight();

    Color getMaskColor();

    ObservableDrawingSet getDrawingSet(DrawingSets drawingSets);

    int getDrawingSetSlot();
}
