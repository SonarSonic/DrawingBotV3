package drawingbot.plugins;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.image.ImageTools;
import drawingbot.registry.MasterRegistry;

public class BicPensPlugin extends AbstractPenPlugin {

    public static final BicPensPlugin INSTANCE = new BicPensPlugin();

    private BicPensPlugin() {}

    @Override
    public String getPluginName() {
        return "Bic Plugin";
    }

    @Override
    public void registerPens() {
        registerPenWithCode("1", new DrawingPen("Cristal Ballpoint", "Black", -230146496));
        registerPenWithCode("2", new DrawingPen("Cristal Ballpoint", "Blue", -230025291));
        registerPenWithCode("3", new DrawingPen("Cristal Ballpoint", "Purple", -228182670));
        registerPenWithCode("4", new DrawingPen("Cristal Ballpoint", "Red", -524020973));
        registerPenWithCode("5", new DrawingPen("Cristal Ballpoint", "Green", -450989747));
        registerPenWithCode("6", new DrawingPen("Cristal Ballpoint", "Dark Teal", -534942112));
        registerPenWithCode("7", new DrawingPen("Cristal Ballpoint", "Orange", -521650432));
        registerPenWithCode("8", new DrawingPen("Cristal Ballpoint", "Dark Pink", -524278397));
        registerPenWithCode("9", new DrawingPen("Cristal Ballpoint", "Pink", -957873963));
        registerPenWithCode("10", new DrawingPen("Cristal Ballpoint", "Fluorescent Orange", -1459652569));
        registerPenWithCode("11", new DrawingPen("Cristal Ballpoint", "Gold", -526090984));
        registerPenWithCode("12", new DrawingPen("Cristal Ballpoint", "Light Green", -581133534));
        registerPenWithCode("13", new DrawingPen("Cristal Ballpoint", "Light Blue", -855602212));
        registerPenWithCode("14", new DrawingPen("Cristal Ballpoint", "Fluorescent Yellow", -1442840832));

        registerPenWithCode("31", new DrawingPen("Cristal Intensity", "Yellow", ImageTools.getARGB(255, 210, 165, 14)));
        registerPenWithCode("32", new DrawingPen("Cristal Intensity", "Orange", ImageTools.getARGB(255, 251, 136, 119)));
        registerPenWithCode("33", new DrawingPen("Cristal Intensity", "Red", ImageTools.getARGB(255, 220, 32, 32)));
        registerPenWithCode("34", new DrawingPen("Cristal Intensity", "Pink", ImageTools.getARGB(255, 235, 175, 201)));
        registerPenWithCode("35", new DrawingPen("Cristal Intensity", "Purple", ImageTools.getARGB(255, 81, 51, 129)));
        registerPenWithCode("36", new DrawingPen("Cristal Intensity", "Light Blue", ImageTools.getARGB(255, 136, 205, 229)));
        registerPenWithCode("37", new DrawingPen("Cristal Intensity", "Blue", ImageTools.getARGB(255, 60, 78, 152)));
        registerPenWithCode("38", new DrawingPen("Cristal Intensity", "Light Green", ImageTools.getARGB(255, 133, 185, 38)));
        registerPenWithCode("39", new DrawingPen("Cristal Intensity", "Green", ImageTools.getARGB(255, 13, 155, 87)));
        registerPenWithCode("40", new DrawingPen("Cristal Intensity", "Black", ImageTools.getARGB(255, 20, 20, 20)));

        registerPenWithCode("41", new DrawingPen("Cristal Intensity Pastel", "Verdigris", ImageTools.getARGB(255, 56, 181, 173)));
        registerPenWithCode("42", new DrawingPen("Cristal Intensity Pastel", "Mustard", ImageTools.getARGB(255, 237, 187, 80)));
        registerPenWithCode("43", new DrawingPen("Cristal Intensity Pastel", "Mauve", ImageTools.getARGB(255, 171, 160, 202)));
        registerPenWithCode("44", new DrawingPen("Cristal Intensity Pastel", "Light Grey", ImageTools.getARGB(255, 210, 210, 210)));
        registerPenWithCode("45", new DrawingPen("Cristal Intensity Pastel", "Lime", ImageTools.getARGB(255, 159, 200, 81)));

        registerPenWithCode("46", new DrawingPen("Cristal Intensity", "Dark Red", ImageTools.getARGB(255, 150, 21, 25)));
        registerPenWithCode("47", new DrawingPen("Cristal Intensity", "Dark Grey", ImageTools.getARGB(255, 80, 71, 68)));
        registerPenWithCode("48", new DrawingPen("Cristal Intensity", "Dark Green", ImageTools.getARGB(255, 70, 87, 57)));
        registerPenWithCode("49", new DrawingPen("Cristal Intensity", "Brown", ImageTools.getARGB(255, 125, 65, 34)));
        registerPenWithCode("50", new DrawingPen("Cristal Intensity", "Dark Blue", ImageTools.getARGB(255, 36, 57, 130)));
    }

    @Override
    public void registerPenSets() {
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Bic", "Cristal Ballpoint", getDrawingPensFromCodes(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Bic", "Grey Blues", getDrawingPensFromCodes(40, 47, 37, 44, 36)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Bic", "Intensity Fineliner Pastel", getDrawingPensFromCodes(41, 42, 34, 43, 44, 45)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Bic", "Intensity Fineliner 10", getDrawingPensFromCodes(31, 32, 33, 34, 35, 36, 37, 38, 39, 40)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Bic", "Intensity Fineliner 20", getDrawingPensFromCodes(31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50)));
    }
}
