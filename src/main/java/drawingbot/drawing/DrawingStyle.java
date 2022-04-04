package drawingbot.drawing;

import com.google.gson.JsonElement;
import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingStyle;
import drawingbot.image.ImageTools;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.pfm.PFMFactory;
import drawingbot.registry.MasterRegistry;
import javafx.scene.paint.Color;

import java.util.HashMap;

public class DrawingStyle implements IDrawingStyle {

    public boolean enabled;
    public String name;
    public String pfm;
    public HashMap<String, JsonElement> settings;
    public int weight;
    public int argb = -1;
    public int drawingSetSlot = 0;

    public transient Color maskColor = null; // could be null

    public DrawingStyle(){}

    public DrawingStyle(boolean enabled, String name, String pfm, int weight, int drawingSetSlot, Color maskColor, HashMap<String, JsonElement> settings){
        update(enabled, name, pfm, weight, drawingSetSlot, maskColor, settings);
    }

    public DrawingStyle(IDrawingStyle style){
        update(style.isEnabled(), style.getName(), style.getPFMName(), style.getDistributionWeight(), style.getDrawingSetSlot(), style.getMaskColor(), style.getSaveableSettings());
    }

    public void update(boolean enabled, String name, String pfm, int weight, int drawingSetSlot, Color maskColor, HashMap<String, JsonElement> settings){
        this.enabled = enabled;
        this.name = name;
        this.pfm = pfm;
        this.settings = settings;
        this.weight = weight;
        this.drawingSetSlot = drawingSetSlot;
        this.maskColor = maskColor;
        this.argb = ImageTools.getARGBFromColor(maskColor);
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
    public Color getMaskColor() {
        if(maskColor == null){
            maskColor = ImageTools.getColorFromARGB(argb);
        }
        return maskColor;
    }

    @Override
    public ObservableDrawingSet getDrawingSet() {
        ObservableDrawingSet drawingSet = null;
        if(DrawingBotV3.INSTANCE.drawingSetSlots.get().size() > drawingSetSlot){
            drawingSet = DrawingBotV3.INSTANCE.drawingSetSlots.get().get(drawingSetSlot);
        }
        return drawingSet == null ? DrawingBotV3.INSTANCE.activeDrawingSet.get() : drawingSet;
    }

    @Override
    public int getDrawingSetSlot() {
        return drawingSetSlot;
    }

    @Override
    public String toString() {
        return getName();
    }
}
