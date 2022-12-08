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
      registerPenWithCode("1", new DrawingPen("Cristal Ballpoint",  "Fluorescent Yellow", ImageTools.getARGB(255, 253, 236, 45)));
      registerPenWithCode("2", new DrawingPen("Cristal Ballpoint",  "Fluorescent Orange", ImageTools.getARGB(255, 237, 98, 34)));
      registerPenWithCode("3", new DrawingPen("Cristal Ballpoint",  "Light Green", ImageTools.getARGB(255, 166, 199, 50)));
      registerPenWithCode("4", new DrawingPen("Cristal Ballpoint",  "Pink", ImageTools.getARGB(255, 238, 115, 169)));
      registerPenWithCode("5", new DrawingPen("Cristal Ballpoint",  "Purple", ImageTools.getARGB(255, 144, 103, 169)));
      registerPenWithCode("6", new DrawingPen("Cristal Ballpoint",  "Light Blue", ImageTools.getARGB(255, 69, 192, 237)));
      registerPenWithCode("7", new DrawingPen("Cristal Ballpoint",  "Blue", ImageTools.getARGB(255, 28, 98, 169)));
      registerPenWithCode("8", new DrawingPen("Cristal Ballpoint",  "Red", ImageTools.getARGB(255, 225, 33, 38)));
      registerPenWithCode("9", new DrawingPen("Cristal Ballpoint",  "Green", ImageTools.getARGB(255, 24, 151, 72)));
      registerPenWithCode("10", new DrawingPen("Cristal Ballpoint",  "Black", ImageTools.getARGB(255, 43, 43, 43)));

      registerPenWithCode("11", new DrawingPen("Cristal Intensity",  "Yellow", ImageTools.getARGB(255, 47, 190, 9)));
      registerPenWithCode("12", new DrawingPen("Cristal Intensity",  "Orange", ImageTools.getARGB(255, 251, 136, 119)));
      registerPenWithCode("13", new DrawingPen("Cristal Intensity",  "Red", ImageTools.getARGB(255, 220, 32, 32)));
      registerPenWithCode("14", new DrawingPen("Cristal Intensity",  "Pink", ImageTools.getARGB(255, 235, 175, 201)));
      registerPenWithCode("15", new DrawingPen("Cristal Intensity",  "Purple", ImageTools.getARGB(255, 81, 51, 129)));
      registerPenWithCode("16", new DrawingPen("Cristal Intensity",  "Light Blue", ImageTools.getARGB(255, 136, 205, 229)));
      registerPenWithCode("17", new DrawingPen("Cristal Intensity",  "Blue", ImageTools.getARGB(255, 60, 78, 152)));
      registerPenWithCode("18", new DrawingPen("Cristal Intensity",  "Light Green", ImageTools.getARGB(255, 133, 185, 38)));
      registerPenWithCode("19", new DrawingPen("Cristal Intensity",  "Green", ImageTools.getARGB(255, 13, 155, 87)));
      registerPenWithCode("20", new DrawingPen("Cristal Intensity",  "Black", ImageTools.getARGB(255, 20, 20, 20)));

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
