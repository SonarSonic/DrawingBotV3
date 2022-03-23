package drawingbot.plugins;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.image.ImageTools;
import drawingbot.registry.MasterRegistry;

public class SakuraPenPlugin extends AbstractPenPlugin {

    @Override
    public String getPluginName() {
        return "Sakura Pen Plugin";
    }

    @Override
    public void registerPens() {
      registerPenWithCode("1", new DrawingPen("Sakura Pigma Micron",  "1 Yellow", ImageTools.getARGB(255,  233, 198, 93 )));
      registerPenWithCode("2", new DrawingPen("Sakura Pigma Micron",  "2 Fresh Green", ImageTools.getARGB(255,  0, 145, 70)));
      registerPenWithCode("3", new DrawingPen("Sakura Pigma Micron",  "3 Hunter Green", ImageTools.getARGB(255,  45, 80, 64)));
      registerPenWithCode("4", new DrawingPen("Sakura Pigma Micron",  "4 Royal Blue", ImageTools.getARGB(255,  0, 76, 148)));
      registerPenWithCode("5", new DrawingPen("Sakura Pigma Micron",  "5 Blue Black", ImageTools.getARGB(255,  37, 58, 95)));
      registerPenWithCode("6", new DrawingPen("Sakura Pigma Micron",  "6 Burgundy", ImageTools.getARGB(255,  115, 39, 41)));
      registerPenWithCode("7", new DrawingPen("Sakura Pigma Micron",  "7 Purple", ImageTools.getARGB(255,  84, 64, 125)));
      registerPenWithCode("8", new DrawingPen("Sakura Pigma Micron",  "8 Rose", ImageTools.getARGB(255,  240, 121, 172)));
      registerPenWithCode("9", new DrawingPen("Sakura Pigma Micron", "9 Sepia", ImageTools.getARGB(255,  82, 54, 55)));
      registerPenWithCode("10", new DrawingPen("Sakura Pigma Micron", "10 Brown", ImageTools.getARGB(255,  162, 75, 60)));
      registerPenWithCode("11", new DrawingPen("Sakura Pigma Micron", "11 Orange", ImageTools.getARGB(255,  235, 129, 1)));
      registerPenWithCode("12", new DrawingPen("Sakura Pigma Micron", "12 Green", ImageTools.getARGB(255,   0, 140, 121)));
      registerPenWithCode("13", new DrawingPen("Sakura Pigma Micron", "13 Blue", ImageTools.getARGB(255,   0, 110, 208)));
      registerPenWithCode("14", new DrawingPen("Sakura Pigma Micron", "14 Red", ImageTools.getARGB(255,   209, 52, 36)));
      registerPenWithCode("15", new DrawingPen("Sakura Pigma Micron", "15 Black", ImageTools.getARGB(255,  54, 47, 53)));
      registerPenWithCode("16", new DrawingPen("Sakura Pigma Micron", "16 Cool Gray", ImageTools.getARGB(255, 108, 105, 105)));
      registerPenWithCode("17", new DrawingPen("Sakura Pigma Micron", "17 Light Cool Gray", ImageTools.getARGB(255, 175, 176, 176)));
    }

    @Override
    public void registerPenSets() {
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Sakura", "Pigma Micron", getDrawingPensFromCodes(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17)));
    }
}
