package drawingbot.plugins;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.image.ImageTools;
import drawingbot.registry.MasterRegistry;

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
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Gaahler", "Metallic Silvers", getDrawingPensFromCodes("1", "2", "3", "4", "5", "6")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Gaahler", "Metallic Golds", getDrawingPensFromCodes("7", "8", "9", "10")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Gaahler", "Metallic Markers", getDrawingPensFromCodes(manufacturerCodes.keySet().toArray(new Object[0]))));
    }
}
