package drawingbot.plugins;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.image.ImageTools;
import drawingbot.registry.MasterRegistry;

import java.util.List;

public class GaahlerPenPlugin extends AbstractPenPlugin {

    public static final GaahlerPenPlugin INSTANCE = new GaahlerPenPlugin();
    public static final String VERSION = "1.0.0";

    private GaahlerPenPlugin() {}

    public String getPenManufacturer() {
        return "Gaahler";
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void registerPens() {
        registerPenWithCode("1", new DrawingPen("Gaahler Metallic Markers", "GM-01 Aluminium", ImageTools.getARGB(255, 200, 200, 200)));
        registerPenWithCode("2", new DrawingPen("Gaahler Metallic Markers", "GM-02 Stainless Steel", ImageTools.getARGB(255, 160, 160, 158)));
        registerPenWithCode("3", new DrawingPen("Gaahler Metallic Markers", "GM-03 Dark Steel", ImageTools.getARGB(255, 105, 105, 100)));
        registerPenWithCode("4", new DrawingPen("Gaahler Metallic Markers", "GM-04 Dark Iron", ImageTools.getARGB(255, 72, 72, 65)));
        registerPenWithCode("5", new DrawingPen("Gaahler Metallic Markers", "GM-05 Gun Metal", ImageTools.getARGB(255, 85, 85, 78)));
        registerPenWithCode("6", new DrawingPen("Gaahler Metallic Markers", "GM-06 Zirconium Black", ImageTools.getARGB(255, 48, 48, 45)));
        registerPenWithCode("7", new DrawingPen("Gaahler Metallic Markers", "GM-07 Red Copper", ImageTools.getARGB(255, 180, 130, 110)));
        registerPenWithCode("8", new DrawingPen("Gaahler Metallic Markers", "GM-08 Dark Gold", ImageTools.getARGB(255, 130, 110, 80)));
        registerPenWithCode("9", new DrawingPen("Gaahler Metallic Markers", "GM-09 Scorched Gold", ImageTools.getARGB(255, 145, 135, 75)));
        registerPenWithCode("10", new DrawingPen("Gaahler Metallic Markers", "GM-10 Flashy Gold", ImageTools.getARGB(255, 195, 175, 85)));
    }

    @Override
    public void registerPenSets() {
        // Full set
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Gaahler", "Metallic Markers", getDrawingPensFromCodes(manufacturerCodes.keySet().toArray(new Object[0]))));

        // Silver range: dark to bright
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Gaahler", "Silver Gradient", getDrawingPensFromCodes("6", "4", "5", "3", "2", "1")));

        // Gold range: dark to bright
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Gaahler", "Gold Gradient", getDrawingPensFromCodes("8", "9", "7", "10")));

        // Cool silver through dark midpoint into warm gold
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Gaahler", "Silver to Gold", getDrawingPensFromCodes("1", "2", "5", "6", "8", "10")));

        // Steel base with copper highlight, weighted distribution
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Gaahler", "Copper Accent on Steel", List.of(
                new DrawingPen("Gaahler Metallic Markers", "GM-06 Zirconium Black", ImageTools.getARGB(255, 48, 48, 45), 120, 1.0F),
                new DrawingPen("Gaahler Metallic Markers", "GM-03 Dark Steel", ImageTools.getARGB(255, 105, 105, 100), 100, 1.0F),
                new DrawingPen("Gaahler Metallic Markers", "GM-01 Aluminium", ImageTools.getARGB(255, 200, 200, 200), 80, 1.0F),
                new DrawingPen("Gaahler Metallic Markers", "GM-07 Red Copper", ImageTools.getARGB(255, 180, 130, 110), 60, 1.0F)
        )));

        // Three darkest pens for dramatic plots
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Gaahler", "Dark Monochrome", getDrawingPensFromCodes("6", "4", "5")));

        // Maximum contrast: darkest, brightest, gold
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Gaahler", "Bright Contrast", getDrawingPensFromCodes("6", "1", "10")));
    }
}
