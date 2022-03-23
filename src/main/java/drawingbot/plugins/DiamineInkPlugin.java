package drawingbot.plugins;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.image.ImageTools;
import drawingbot.registry.MasterRegistry;

public class DiamineInkPlugin extends AbstractPenPlugin {

    @Override
    public String getPluginName() {
      return "Diamine Ink";
    }

    @Override
    public void registerPens() {
      registerPenWithCode("1", new DrawingPen("Diamine Ink", "1 Graphite", ImageTools.getARGB(255, 99, 99, 99)));
      registerPenWithCode("2", new DrawingPen("Diamine Ink", "2 Earl Grey", ImageTools.getARGB(255, 104, 104, 114)));
      registerPenWithCode("3", new DrawingPen("Diamine Ink", "3 Grey", ImageTools.getARGB(255, 83, 76, 76)));
      registerPenWithCode("4", new DrawingPen("Diamine Ink", "4 Eclipse", ImageTools.getARGB(255, 48, 35, 36)));
      registerPenWithCode("5", new DrawingPen("Diamine Ink", "5 Damson", ImageTools.getARGB(255, 55, 34, 47)));
      registerPenWithCode("6", new DrawingPen("Diamine Ink", "6 Grape", ImageTools.getARGB(255, 68, 57, 45)));
      registerPenWithCode("7", new DrawingPen("Diamine Ink", "7 Imperial Purple", ImageTools.getARGB(255, 103, 61, 89)));
      registerPenWithCode("8", new DrawingPen("Diamine Ink", "8 Tyrian Purple", ImageTools.getARGB(255, 135, 70, 96)));
      registerPenWithCode("9", new DrawingPen("Diamine Ink", "9 Amazing Amethyst", ImageTools.getARGB(255, 96, 67, 108)));
      registerPenWithCode("10", new DrawingPen("Diamine Ink", "10 Majestic Purple", ImageTools.getARGB(255, 101, 45, 139)));
      registerPenWithCode("11", new DrawingPen("Diamine Ink", "11 Lavender", ImageTools.getARGB(255, 89, 62, 152)));
      registerPenWithCode("12", new DrawingPen("Diamine Ink", "12 Violet", ImageTools.getARGB(255, 110, 70, 139)));
      registerPenWithCode("13", new DrawingPen("Diamine Ink", "13 Claret", ImageTools.getARGB(255, 194, 68, 129)));
      registerPenWithCode("14", new DrawingPen("Diamine Ink", "14 Amaranth Pink", ImageTools.getARGB(255, 168, 62, 94)));
      registerPenWithCode("15", new DrawingPen("Diamine Ink", "15 Flamingo Pink", ImageTools.getARGB(255, 236, 85, 59)));
      registerPenWithCode("16", new DrawingPen("Diamine Ink", "16 Pink", ImageTools.getARGB(255, 241, 84, 63)));
      registerPenWithCode("17", new DrawingPen("Diamine Ink", "17 Cerise", ImageTools.getARGB(255, 240, 55, 78)));
      registerPenWithCode("18", new DrawingPen("Diamine Ink", "18 Hope Pink", ImageTools.getARGB(255, 233, 52, 84)));
      registerPenWithCode("19", new DrawingPen("Diamine Ink", "19 Deep Magenta", ImageTools.getARGB(255, 192, 37, 131)));
      registerPenWithCode("20", new DrawingPen("Diamine Ink", "20 Coral", ImageTools.getARGB(255, 232, 70, 30)));
      registerPenWithCode("21", new DrawingPen("Diamine Ink", "21 Sunshine Yellow", ImageTools.getARGB(255, 248, 167, 0)));
      registerPenWithCode("22", new DrawingPen("Diamine Ink", "22 Yellow", ImageTools.getARGB(255, 245, 193, 0)));
      registerPenWithCode("23", new DrawingPen("Diamine Ink", "23 Merlot", ImageTools.getARGB(255, 109, 57, 71)));
      registerPenWithCode("24", new DrawingPen("Diamine Ink", "24 Maroon", ImageTools.getARGB(255, 198, 58, 65)));
      registerPenWithCode("25", new DrawingPen("Diamine Ink", "25 Monaco Red", ImageTools.getARGB(255, 162, 59, 51)));
      registerPenWithCode("26", new DrawingPen("Diamine Ink", "26 Syrah", ImageTools.getARGB(255, 133, 32, 60)));
      registerPenWithCode("27", new DrawingPen("Diamine Ink", "27 Crimson", ImageTools.getARGB(255, 140, 37, 40)));
      registerPenWithCode("28", new DrawingPen("Diamine Ink", "28 Matador", ImageTools.getARGB(255, 192, 27, 29)));
      registerPenWithCode("29", new DrawingPen("Diamine Ink", "29 Red Dragon", ImageTools.getARGB(255, 173, 26, 46)));
      registerPenWithCode("30", new DrawingPen("Diamine Ink", "30 Ruby", ImageTools.getARGB(255, 202, 31, 43)));
      registerPenWithCode("31", new DrawingPen("Diamine Ink", "31 Passion Red", ImageTools.getARGB(255, 220, 65, 65)));
      registerPenWithCode("32", new DrawingPen("Diamine Ink", "32 Scarlet", ImageTools.getARGB(255, 210, 30, 35)));
      registerPenWithCode("33", new DrawingPen("Diamine Ink", "33 Poppy Red", ImageTools.getARGB(255, 229, 34, 40)));
      registerPenWithCode("34", new DrawingPen("Diamine Ink", "34 Classic Red", ImageTools.getARGB(255, 212, 50, 65)));
      registerPenWithCode("35", new DrawingPen("Diamine Ink", "35 Wild Strawberry", ImageTools.getARGB(255, 225, 35, 36)));
      registerPenWithCode("36", new DrawingPen("Diamine Ink", "36 Brilliant Red", ImageTools.getARGB(255, 234, 78, 62)));
      registerPenWithCode("37", new DrawingPen("Diamine Ink", "37 Vermillion", ImageTools.getARGB(255, 227, 71, 45)));
      registerPenWithCode("38", new DrawingPen("Diamine Ink", "38 Oxblood", ImageTools.getARGB(255, 126, 41, 45)));
      registerPenWithCode("39", new DrawingPen("Diamine Ink", "39 Ancient Copper", ImageTools.getARGB(255, 153, 51, 34)));
      registerPenWithCode("40", new DrawingPen("Diamine Ink", "40 Peach Haze", ImageTools.getARGB(255, 240, 94, 26)));
      registerPenWithCode("41", new DrawingPen("Diamine Ink", "41 Orange", ImageTools.getARGB(255, 249, 94, 14)));
      registerPenWithCode("42", new DrawingPen("Diamine Ink", "42 Pumpkin", ImageTools.getARGB(255, 239, 58, 31)));
      registerPenWithCode("43", new DrawingPen("Diamine Ink", "43 Sunset Orange", ImageTools.getARGB(255, 233, 63, 23)));
      registerPenWithCode("44", new DrawingPen("Diamine Ink", "44 Blaze Orange", ImageTools.getARGB(255, 240, 101, 26)));
      registerPenWithCode("45", new DrawingPen("Diamine Ink", "45 Amber", ImageTools.getARGB(255, 241, 149, 0)));
      registerPenWithCode("46", new DrawingPen("Diamine Ink", "46 Autumn Oak", ImageTools.getARGB(255, 212, 90, 28)));
      registerPenWithCode("47", new DrawingPen("Diamine Ink", "47 Macasser", ImageTools.getARGB(255, 60, 35, 26)));
      registerPenWithCode("48", new DrawingPen("Diamine Ink", "48 Chocolate Brown", ImageTools.getARGB(255, 88, 41, 30)));
      registerPenWithCode("49", new DrawingPen("Diamine Ink", "49 Saddle Brown", ImageTools.getARGB(255, 97, 58, 49)));
      registerPenWithCode("50", new DrawingPen("Diamine Ink", "50 Rustic Brown.", ImageTools.getARGB(255, 117, 49, 50)));
      registerPenWithCode("51", new DrawingPen("Diamine Ink", "51 Burnt Sienna", ImageTools.getARGB(255, 122, 54, 27)));
      registerPenWithCode("52", new DrawingPen("Diamine Ink", "52 Ochre", ImageTools.getARGB(255, 124, 55, 12)));
      registerPenWithCode("53", new DrawingPen("Diamine Ink", "53 Raw Sienna.", ImageTools.getARGB(255, 142, 90, 70)));
      registerPenWithCode("54", new DrawingPen("Diamine Ink", "54 Warm Brown", ImageTools.getARGB(255, 170, 84, 49)));
      registerPenWithCode("55", new DrawingPen("Diamine Ink", "55 Sepia Brown", ImageTools.getARGB(255, 198, 110, 33)));
      registerPenWithCode("56", new DrawingPen("Diamine Ink", "56 Golden Brown", ImageTools.getARGB(255, 170, 112, 56)));
      registerPenWithCode("57", new DrawingPen("Diamine Ink", "57 Green-Black", ImageTools.getARGB(255, 25, 42, 26)));
      registerPenWithCode("58", new DrawingPen("Diamine Ink", "58 Salamander", ImageTools.getARGB(255, 70, 76, 65)));
      registerPenWithCode("59", new DrawingPen("Diamine Ink", "59 Classic Green", ImageTools.getARGB(255, 62, 61, 52)));
      registerPenWithCode("60", new DrawingPen("Diamine Ink", "60 Evergreen", ImageTools.getARGB(255, 51, 77, 53)));
      registerPenWithCode("61", new DrawingPen("Diamine Ink", "61 Umber", ImageTools.getARGB(255, 66, 104, 78)));
      registerPenWithCode("62", new DrawingPen("Diamine Ink", "62 Emerald Green", ImageTools.getARGB(255, 56, 108, 71)));
      registerPenWithCode("63", new DrawingPen("Diamine Ink", "63 Woodland Green", ImageTools.getARGB(255, 0, 129, 81)));
      registerPenWithCode("64", new DrawingPen("Diamine Ink", "64 Sherwood Green", ImageTools.getARGB(255, 0, 104, 63)));
      registerPenWithCode("65", new DrawingPen("Diamine Ink", "65 Delamere Green", ImageTools.getARGB(255, 0, 96, 75)));
      registerPenWithCode("66", new DrawingPen("Diamine Ink", "66 Cool Green", ImageTools.getARGB(255, 0, 129, 115)));
      registerPenWithCode("67", new DrawingPen("Diamine Ink", "67 Soft Mint", ImageTools.getARGB(255, 0, 171, 160)));
      registerPenWithCode("68", new DrawingPen("Diamine Ink", "68 Apple Glory", ImageTools.getARGB(255, 0, 179, 80)));
      registerPenWithCode("69", new DrawingPen("Diamine Ink", "69 Ultra Green", ImageTools.getARGB(255, 0, 147, 67)));
      registerPenWithCode("70", new DrawingPen("Diamine Ink", "70 Meadow.", ImageTools.getARGB(255, 116, 163, 2)));
      registerPenWithCode("71", new DrawingPen("Diamine Ink", "71 Kelly Green", ImageTools.getARGB(255, 107, 155, 0)));
      registerPenWithCode("72", new DrawingPen("Diamine Ink", "72 Spring Green", ImageTools.getARGB(255, 160, 166, 61)));
      registerPenWithCode("73", new DrawingPen("Diamine Ink", "73 Jade Green", ImageTools.getARGB(255, 158, 180, 0)));
      registerPenWithCode("74", new DrawingPen("Diamine Ink", "74 Midnight", ImageTools.getARGB(255, 1, 50, 92)));
      registerPenWithCode("75", new DrawingPen("Diamine Ink", "75 Blue-Black", ImageTools.getARGB(255, 26, 57, 72)));
      registerPenWithCode("76", new DrawingPen("Diamine Ink", "76 Twilight", ImageTools.getARGB(255, 20, 54, 69)));
      registerPenWithCode("77", new DrawingPen("Diamine Ink", "77 Denim", ImageTools.getARGB(255, 31, 55, 89)));
      registerPenWithCode("78", new DrawingPen("Diamine Ink", "78 Bilberry", ImageTools.getARGB(255, 43, 39, 112)));
      registerPenWithCode("79", new DrawingPen("Diamine Ink", "79 Oxford Blue", ImageTools.getARGB(255, 11, 59, 116)));
      registerPenWithCode("80", new DrawingPen("Diamine Ink", "80 Indigo", ImageTools.getARGB(255, 35, 16, 106)));
      registerPenWithCode("81", new DrawingPen("Diamine Ink", "81 Majestic Blue", ImageTools.getARGB(255, 0, 74, 134)));
      registerPenWithCode("82", new DrawingPen("Diamine Ink", "82 WES Imperial Blue", ImageTools.getARGB(255, 63, 66, 148)));
      registerPenWithCode("83", new DrawingPen("Diamine Ink", "83 Sargasso Sea", ImageTools.getARGB(255, 0, 66, 152)));
      registerPenWithCode("84", new DrawingPen("Diamine Ink", "84 Sapphire Blue", ImageTools.getARGB(255, 63, 80, 171)));
      registerPenWithCode("85", new DrawingPen("Diamine Ink", "85 Teal", ImageTools.getARGB(255, 16, 75, 83)));
      registerPenWithCode("86", new DrawingPen("Diamine Ink", "86 Prussian Blue", ImageTools.getARGB(255, 56, 94, 119)));
      registerPenWithCode("87", new DrawingPen("Diamine Ink", "87 Eau De Nil", ImageTools.getARGB(255, 0, 92, 108)));
      registerPenWithCode("88", new DrawingPen("Diamine Ink", "88 Florida Blue", ImageTools.getARGB(255, 57, 87, 125)));
      registerPenWithCode("89", new DrawingPen("Diamine Ink", "89 Asa Blue", ImageTools.getARGB(255, 0, 96, 151)));
      registerPenWithCode("90", new DrawingPen("Diamine Ink", "90 Kensington Blue", ImageTools.getARGB(255, 28, 99, 160)));
      registerPenWithCode("91", new DrawingPen("Diamine Ink", "91 Presidential Blue", ImageTools.getARGB(255, 0, 104, 166)));
      registerPenWithCode("92", new DrawingPen("Diamine Ink", "92 Royal Blue", ImageTools.getARGB(255, 0, 96, 184)));
      registerPenWithCode("93", new DrawingPen("Diamine Ink", "93 China Blue", ImageTools.getARGB(255, 60, 119, 179)));
      registerPenWithCode("94", new DrawingPen("Diamine Ink", "94 Misty Blue", ImageTools.getARGB(255, 0, 121, 167)));
      registerPenWithCode("95", new DrawingPen("Diamine Ink", "95 Washable Blue", ImageTools.getARGB(255, 0, 121, 186)));
      registerPenWithCode("96", new DrawingPen("Diamine Ink", "96 Mediterranean Blue", ImageTools.getARGB(255, 0, 134, 193)));
      registerPenWithCode("97", new DrawingPen("Diamine Ink", "97 Havasu Turquoise", ImageTools.getARGB(255, 0, 143, 189)));
      registerPenWithCode("98", new DrawingPen("Diamine Ink", "98 Aqua Blue", ImageTools.getARGB(255, 0, 165, 203)));
      registerPenWithCode("99", new DrawingPen("Diamine Ink", "99 Aqua Lagoon", ImageTools.getARGB(255, 0, 165, 188)));
      registerPenWithCode("100", new DrawingPen("Diamine Ink", "100 Turquoise", ImageTools.getARGB(255, 0, 168, 206)));
      registerPenWithCode("101", new DrawingPen("Diamine Ink", "101 Beau Blue", ImageTools.getARGB(255, 86, 185, 210)));
      registerPenWithCode("102", new DrawingPen("Diamine Ink", "102 Steel Blue", ImageTools.getARGB(255, 0, 130, 131)));
      registerPenWithCode("103", new DrawingPen("Diamine Ink", "103 Marine", ImageTools.getARGB(255, 0, 137, 146)));
      registerPenWithCode("104", new DrawingPen("Diamine Ink", "104 Onyx Black", ImageTools.getARGB(255, 35, 20, 16)));
      registerPenWithCode("105", new DrawingPen("Diamine Ink", "105 Jet Black", ImageTools.getARGB(255, 58, 52, 47)));
      registerPenWithCode("106", new DrawingPen("Diamine Ink", "106 Quartz Black", ImageTools.getARGB(255, 78, 57, 52)));
    }

    @Override
    public void registerPenSets(){
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Diamine", "Inks", getDrawingPensFromCodes(manufacturerCodes.keySet().toArray(new Object[0]))));
    }
}
