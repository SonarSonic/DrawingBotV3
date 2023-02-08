package drawingbot.plugins;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.image.ImageTools;
import drawingbot.registry.MasterRegistry;

public class StaedtlerPenPlugin extends AbstractPenPlugin {

    @Override
    public String getPluginName() {
        return "Staedtler Pen Plugin";
    }

    @Override
    public void registerPens() {

        registerPenWithCode("1", new DrawingPen("Staedtler Fineliner", "1 Yellow", ImageTools.getARGB(255, 255, 238, 0)));
        registerPenWithCode("10", new DrawingPen("Staedtler Fineliner", "10 Light Yellow", ImageTools.getARGB(255, 255, 217, 0)));
        registerPenWithCode("101", new DrawingPen("Staedtler Fineliner", "101 Neon Yellow", ImageTools.getARGB(255, 255, 238, 0)));
        registerPenWithCode("110", new DrawingPen("Staedtler Fineliner", "110 Bright Yellow", ImageTools.getARGB(255, 255, 196, 0)));
        registerPenWithCode("13", new DrawingPen("Staedtler Fineliner", "13 Vanilla", ImageTools.getARGB(255, 255, 208, 151)));
        registerPenWithCode("14", new DrawingPen("Staedtler Fineliner", "14 Mustard", ImageTools.getARGB(255, 208, 177, 16)));
        registerPenWithCode("16", new DrawingPen("Staedtler Fineliner", "16 Golden Ochre", ImageTools.getARGB(255, 236, 159, 3)));

        registerPenWithCode("2", new DrawingPen("Staedtler Fineliner", "2 Red", ImageTools.getARGB(255, 217, 0, 5)));
        registerPenWithCode("20", new DrawingPen("Staedtler Fineliner", "20 Magenta", ImageTools.getARGB(255, 254, 0, 146)));
        registerPenWithCode("201", new DrawingPen("Staedtler Fineliner", "201 Neon Red", ImageTools.getARGB(255, 217, 0, 5)));
        registerPenWithCode("21", new DrawingPen("Staedtler Fineliner", "21 Light Rose", ImageTools.getARGB(255, 253, 190, 214)));
        registerPenWithCode("221", new DrawingPen("Staedtler Fineliner", "221 Neon Pink", ImageTools.getARGB(255, 253, 190, 214)));
        registerPenWithCode("222", new DrawingPen("Staedtler Fineliner", "222 Salmon", ImageTools.getARGB(255, 255, 180, 153)));
        registerPenWithCode("23", new DrawingPen("Staedtler Fineliner", "23 Bordeaux", ImageTools.getARGB(255, 204, 0, 72)));
        registerPenWithCode("24", new DrawingPen("Staedtler Fineliner", "24 Scarlet Red", ImageTools.getARGB(255, 230, 84, 56)));
        registerPenWithCode("26", new DrawingPen("Staedtler Fineliner", "26 Antique Pink", ImageTools.getARGB(255, 195, 156, 146)));
        registerPenWithCode("260", new DrawingPen("Staedtler Fineliner", "260 Mauve", ImageTools.getARGB(255, 152, 107, 141)));
        registerPenWithCode("28", new DrawingPen("Staedtler Fineliner", "28 Mahogany", ImageTools.getARGB(255, 115, 37, 59)));
        registerPenWithCode("29", new DrawingPen("Staedtler Fineliner", "29 Carmine Red", ImageTools.getARGB(255, 170, 32, 44)));

        registerPenWithCode("3", new DrawingPen("Staedtler Fineliner", "3 Blue", ImageTools.getARGB(255, 0, 69, 159)));
        registerPenWithCode("30", new DrawingPen("Staedtler Fineliner", "30 Pale Blue", ImageTools.getARGB(255, 0, 153, 206)));
        registerPenWithCode("301", new DrawingPen("Staedtler Fineliner", "301 Neon Blue", ImageTools.getARGB(255, 0, 188, 251)));
        registerPenWithCode("305", new DrawingPen("Staedtler Fineliner", "305 Sky Blue", ImageTools.getARGB(255, 146, 206, 230)));
        registerPenWithCode("32", new DrawingPen("Staedtler Fineliner", "32 Azur", ImageTools.getARGB(255, 191, 224, 236)));
        registerPenWithCode("34", new DrawingPen("Staedtler Fineliner", "34 Aqua Blue", ImageTools.getARGB(255, 140, 203, 234)));
        registerPenWithCode("36", new DrawingPen("Staedtler Fineliner", "36 Indigo Blue", ImageTools.getARGB(255, 55, 71, 102)));
        registerPenWithCode("37", new DrawingPen("Staedtler Fineliner", "37 Ultramarine Blue", ImageTools.getARGB(255, 0, 149, 207)));
        registerPenWithCode("38", new DrawingPen("Staedtler Fineliner", "38 Sea Green", ImageTools.getARGB(255, 0, 164, 166)));

        registerPenWithCode("4", new DrawingPen("Staedtler Fineliner", "4 Orange", ImageTools.getARGB(255, 255, 114, 2)));
        registerPenWithCode("401", new DrawingPen("Staedtler Fineliner", "401 Neon Orange", ImageTools.getARGB(255, 255, 178, 1)));
        registerPenWithCode("43", new DrawingPen("Staedtler Fineliner", "43 Light Orange", ImageTools.getARGB(255, 255, 205, 144)));
        registerPenWithCode("430", new DrawingPen("Staedtler Fineliner", "430 Peach", ImageTools.getARGB(255, 255, 205, 144)));
        registerPenWithCode("450", new DrawingPen("Staedtler Fineliner", "450 Sand", ImageTools.getARGB(255, 200, 162, 141)));
        registerPenWithCode("48", new DrawingPen("Staedtler Fineliner", "48 Kalahari Orange", ImageTools.getARGB(255, 200, 67, 19)));

        registerPenWithCode("5", new DrawingPen("Staedtler Fineliner", "5 Green", ImageTools.getARGB(255, 0, 120, 62)));
        registerPenWithCode("501", new DrawingPen("Staedtler Fineliner", "501 Neon Green", ImageTools.getARGB(255, 69, 167, 70)));
        registerPenWithCode("51", new DrawingPen("Staedtler Fineliner", "51 Willow Green", ImageTools.getARGB(255, 215, 235, 124)));
        registerPenWithCode("52", new DrawingPen("Staedtler Fineliner", "52 Sap Green", ImageTools.getARGB(255, 40, 141, 79)));
        registerPenWithCode("53", new DrawingPen("Staedtler Fineliner", "53 Lime Green", ImageTools.getARGB(255, 193, 213, 92)));
        registerPenWithCode("54", new DrawingPen("Staedtler Fineliner", "54 Turquoise", ImageTools.getARGB(255, 38, 183, 180)));
        registerPenWithCode("55", new DrawingPen("Staedtler Fineliner", "55 Green Earth", ImageTools.getARGB(255, 0, 70, 41)));
        registerPenWithCode("550", new DrawingPen("Staedtler Fineliner", "550 Pale Green", ImageTools.getARGB(255, 64, 197, 123)));
        registerPenWithCode("57", new DrawingPen("Staedtler Fineliner", "57 Olive Green", ImageTools.getARGB(255, 83, 81, 38)));
        registerPenWithCode("59", new DrawingPen("Staedtler Fineliner", "59 Opal Green", ImageTools.getARGB(255, 0, 77, 83)));

        registerPenWithCode("6", new DrawingPen("Staedtler Fineliner", "6 Violet", ImageTools.getARGB(255, 94, 51, 106)));
        registerPenWithCode("61", new DrawingPen("Staedtler Fineliner", "61 Red Violet", ImageTools.getARGB(255, 181, 6, 129)));
        registerPenWithCode("62", new DrawingPen("Staedtler Fineliner", "62 Lavender", ImageTools.getARGB(255, 192, 139, 209)));
        registerPenWithCode("63", new DrawingPen("Staedtler Fineliner", "63 Delft Blue", ImageTools.getARGB(255, 116, 161, 203)));
        registerPenWithCode("68", new DrawingPen("Staedtler Fineliner", "68 Lilac", ImageTools.getARGB(255, 132, 98, 152)));
        registerPenWithCode("69", new DrawingPen("Staedtler Fineliner", "69 Dark Mauve", ImageTools.getARGB(255, 70, 48, 99)));

        registerPenWithCode("7", new DrawingPen("Staedtler Fineliner", "7 Light Brown", ImageTools.getARGB(255, 213, 105, 0)));
        registerPenWithCode("73", new DrawingPen("Staedtler Fineliner", "73 Burnt Sienna", ImageTools.getARGB(255, 188, 74, 33)));
        registerPenWithCode("75", new DrawingPen("Staedtler Fineliner", "75 Chocolate Brown", ImageTools.getARGB(255, 87, 41, 41)));
        registerPenWithCode("76", new DrawingPen("Staedtler Fineliner", "76 Van Dyke Brown", ImageTools.getARGB(255, 104, 36, 13)));
        registerPenWithCode("77", new DrawingPen("Staedtler Fineliner", "77 Tobacco Brown", ImageTools.getARGB(255, 99, 82, 76)));

        registerPenWithCode("8", new DrawingPen("Staedtler Fineliner", "8 Grey", ImageTools.getARGB(255, 99, 93, 100)));
        registerPenWithCode("82", new DrawingPen("Staedtler Fineliner", "82 Light Grey", ImageTools.getARGB(255, 176, 180, 185)));
        registerPenWithCode("83", new DrawingPen("Staedtler Fineliner", "83 Warm Grey", ImageTools.getARGB(255, 108, 129, 124)));
        registerPenWithCode("85", new DrawingPen("Staedtler Fineliner", "85 Dove Grey", ImageTools.getARGB(255, 131, 133, 137)));
        registerPenWithCode("86", new DrawingPen("Staedtler Fineliner", "86 Stone Grey", ImageTools.getARGB(255, 165, 161, 160)));

        registerPenWithCode("9", new DrawingPen("Staedtler Fineliner", "9 Black", ImageTools.getARGB(255, 1, 1, 1)));

    }

    @Override
    public void registerPenSets() {
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Greys", getDrawingPensFromCodes(9, 8, 85, 86, 82)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Grey Blues", getDrawingPensFromCodes(9, 8, 85, 86, 82, 36, 30, 34)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Grey Yellows", getDrawingPensFromCodes(9, 8, 85, 86, 82, 16, 13, 1)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - Neon - 6 x Pen Set", getDrawingPensFromCodes(101, 201, 221, 301, 401, 501)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - Pastel - 6 x Pen Set", getDrawingPensFromCodes(21, 43, 53, 62, 63, 82)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - Pastel Pink - 6 x Pen Set", getDrawingPensFromCodes(21, 305, 430, 505, 62, 82)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - Galaxy - 6 x Pen Set", getDrawingPensFromCodes(10, 30, 36, 54, 6, 61)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - Dino - 6 x Pen Set", getDrawingPensFromCodes(10, 24, 36, 38, 54, 550)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - Flamingo - 6 x Pen Set", getDrawingPensFromCodes(61, 20, 6, 23, 222, 21)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - Hygge - 6 x Pen Set", getDrawingPensFromCodes(260, 305, 430, 62, 63, 82)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - 3 x Pen Set", getDrawingPensFromCodes(2, 3, 9)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - 4 x Pen Set", getDrawingPensFromCodes(2, 3, 5, 9)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - 10 x Pen Set", getDrawingPensFromCodes(1, 2, 20, 3, 34, 4, 5, 51, 76, 9)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - 12 x Pen Set", getDrawingPensFromCodes(1, 2, 20, 3, 30, 4, 5, 51, 6, 76, 8, 9)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - 15 x Pen Set", getDrawingPensFromCodes(1, 3, 20, 3, 34, 4, 43, 52, 53, 54, 6, 61, 76, 8, 9)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - 20 x Pen Set", getDrawingPensFromCodes(1, 2, 20, 23, 3, 30, 34, 37, 4, 5, 51, 54, 57, 6, 61, 7, 76, 8, 82, 9)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - 24 x Pen Set", getDrawingPensFromCodes(1, 2, 20, 21, 23, 260, 29, 3, 30, 34, 37, 4, 43, 5, 51, 53, 54, 55, 57, 6, 61, 62, 63, 69, 7, 76, 77, 8, 82, 9)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - 30 x Pen Set", getDrawingPensFromCodes(1, 10, 16, 2, 20, 21, 222, 24, 29, 3, 30, 34, 36, 37, 38, 4, 43, 430, 51, 52, 53, 54, 550, 6, 61, 73, 76, 8, 82, 9)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - 36 x Pen Set", getDrawingPensFromCodes(1, 101, 2, 20, 201, 21, 221, 23, 260, 29, 3, 30, 301, 34, 37, 4, 401, 43, 5, 501, 51, 53, 54, 55, 57, 6, 61, 62, 63, 69, 7, 76, 77, 8, 82, 9)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - 40 x Pen Set", getDrawingPensFromCodes(1, 101, 110, 2, 20, 201, 21, 221, 222, 23, 24, 260, 29, 3, 30, 301, 34, 36, 37, 4, 401, 5, 501, 51, 53, 54, 55, 550, 57, 6, 61, 62, 63, 69, 7, 73, 76, 77, 8, 82, 9)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - 42 x Pen Set", getDrawingPensFromCodes(1, 101, 110, 2, 20, 201, 21, 221, 222, 23, 260, 29, 3, 30, 301, 34, 36, 37, 4, 401, 43, 48, 5, 501, 51, 53, 54, 55, 550, 57, 6, 61, 62, 63, 69, 7, 73, 76, 77, 8, 82, 9)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - 50 x Pen Set", getDrawingPensFromCodes(1, 10, 101, 110, 16, 2, 20 ,201, 21, 221, 222, 23, 24, 260, 29, 3, 30, 301, 34, 36, 37, 38, 4, 401, 43, 430, 48, 5, 501, 51, 52, 53, 54, 55, 550, 57, 6, 61, 62, 63, 69, 7, 73, 76, 77, 8, 82, 9)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Staedtler", "Fineliner 334 - 60 x Pen Set", getDrawingPensFromCodes(1, 10, 101, 110, 13, 14, 16, 2, 20, 201, 21, 221, 222, 23, 24, 26, 260, 28, 29, 3, 30, 301, 32, 34, 36, 37, 38, 4, 401, 43, 430, 450, 48, 5, 501, 51, 52, 53, 54, 55, 550, 57, 59, 6, 61, 62, 63, 68, 69, 7, 73, 75, 76, 77, 8, 82, 83, 85, 86, 9)));


    }
}
