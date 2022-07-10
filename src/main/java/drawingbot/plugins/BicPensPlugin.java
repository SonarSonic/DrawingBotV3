package drawingbot.plugins;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.image.ImageTools;
import drawingbot.registry.MasterRegistry;

public class BicPensPlugin extends AbstractPenPlugin {

    @Override
    public String getPluginName() {
        return "Bic Plugin";
    }

    @Override
    public void registerPens() {
      registerPenWithCode("10", new DrawingPen("Cristal Ballpoint",  "Black", ImageTools.getARGB(255, 44, 42, 50)));
      registerPenWithCode("5", new DrawingPen("Cristal Ballpoint",  "Purple", ImageTools.getARGB(255, 77, 34, 138)));
      registerPenWithCode("7", new DrawingPen("Cristal Ballpoint",  "Blue", ImageTools.getARGB(255, 30, 21, 137)));
      registerPenWithCode("8", new DrawingPen("Cristal Ballpoint",  "Red", ImageTools.getARGB(255, 207, 57, 74)));
      registerPenWithCode("9", new DrawingPen("Cristal Ballpoint",  "Green", ImageTools.getARGB(255, 33, 89, 76)));
      registerPenWithCode("4", new DrawingPen("Cristal Ballpoint",  "Pink", ImageTools.getARGB(255, 218, 38, 129)));
      registerPenWithCode("3", new DrawingPen("Cristal Ballpoint",  "Light Green", ImageTools.getARGB(255, 94, 135, 71)));
      registerPenWithCode("6", new DrawingPen("Cristal Ballpoint",  "Light Blue", ImageTools.getARGB(255, 41, 149, 220)));
      registerPenWithCode("1", new DrawingPen("Cristal Ballpoint",  "Fluorescent Yellow", ImageTools.getARGB(255, 251, 255, 106)));
      registerPenWithCode("2", new DrawingPen("Cristal Ballpoint",  "Fluorescent Orange", ImageTools.getARGB(255, 255, 145, 90)));

      registerPenWithCode("21", new DrawingPen("Cristal Intensity Pastel",  "Verdigris", ImageTools.getARGB(255, 56, 181, 173)));
      registerPenWithCode("22", new DrawingPen("Cristal Intensity Pastel",  "Mustard", ImageTools.getARGB(255, 237, 187, 80)));
      registerPenWithCode("23", new DrawingPen("Cristal Intensity Pastel",  "Mauve", ImageTools.getARGB(255, 171, 160, 202)));
      registerPenWithCode("24", new DrawingPen("Cristal Intensity Pastel",  "Light Grey", ImageTools.getARGB(255, 210, 210, 210)));
      registerPenWithCode("25", new DrawingPen("Cristal Intensity Pastel",  "Lime", ImageTools.getARGB(255, 159, 200, 81)));

      registerPenWithCode("26", new DrawingPen("Cristal Intensity",  "Dark Red", ImageTools.getARGB(255, 150, 21, 25)));
      registerPenWithCode("27", new DrawingPen("Cristal Intensity",  "Dark Grey", ImageTools.getARGB(255, 80, 71, 68)));
      registerPenWithCode("28", new DrawingPen("Cristal Intensity",  "Dark Green", ImageTools.getARGB(255, 70, 87, 57)));
      registerPenWithCode("29", new DrawingPen("Cristal Intensity",  "Brown", ImageTools.getARGB(255, 125, 65, 34)));
      registerPenWithCode("30", new DrawingPen("Cristal Intensity",  "Dark Blue", ImageTools.getARGB(255, 36, 57, 130)));
    }

    @Override
    public void registerPenSets() {
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Bic", "Cristal Ballpoint", getDrawingPensFromCodes(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Bic", "Intensity Fineliner 10", getDrawingPensFromCodes(11, 12, 13, 14, 15, 16, 17, 18, 19, 20)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Bic", "Intensity Fineliner 20", getDrawingPensFromCodes(11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Bic", "Intensity Fineliner Pastel", getDrawingPensFromCodes(21, 22, 14, 23, 24, 25)));
    }
}

