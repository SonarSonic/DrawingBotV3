package drawingbot.plugins;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.image.ImageTools;
import drawingbot.registry.MasterRegistry;

public class WinsorNewtonPenPlugin extends AbstractPenPlugin {

    public static final WinsorNewtonPenPlugin INSTANCE = new WinsorNewtonPenPlugin();

    private WinsorNewtonPenPlugin() {}

    @Override
    public String getPluginName() {
        return "Winsor & Newton Plugin";
    }

    @Override
    public void registerPens() {
        registerPenWithCode("1", new DrawingPen("Winsor & Newton ProMarker", "Lemon Chiffon", ImageTools.getARGB(255, 246, 217, 193)));
        registerPenWithCode("2", new DrawingPen("Winsor & Newton ProMarker", "Pale Cream", ImageTools.getARGB(255, 237, 215, 194)));
        registerPenWithCode("3", new DrawingPen("Winsor & Newton ProMarker", "Bisque", ImageTools.getARGB(255, 251, 216, 183)));
        registerPenWithCode("4", new DrawingPen("Winsor & Newton ProMarker", "Pink Camellia", ImageTools.getARGB(255, 250, 205, 196)));
        registerPenWithCode("5", new DrawingPen("Winsor & Newton ProMarker", "Misty Rose", ImageTools.getARGB(255, 255, 194, 185)));
        registerPenWithCode("6", new DrawingPen("Winsor & Newton ProMarker", "Linen", ImageTools.getARGB(255, 239, 215, 218)));
        registerPenWithCode("7", new DrawingPen("Winsor & Newton ProMarker", "Antique White", ImageTools.getARGB(255, 241, 205, 192)));
        registerPenWithCode("8", new DrawingPen("Winsor & Newton ProMarker", "Light Pecan", ImageTools.getARGB(255, 204, 172, 148)));
        registerPenWithCode("9", new DrawingPen("Winsor & Newton ProMarker", "Wheat", ImageTools.getARGB(255, 239, 202, 190)));
        registerPenWithCode("10", new DrawingPen("Winsor & Newton ProMarker", "Muted Pink", ImageTools.getARGB(255, 237, 198, 183)));
        registerPenWithCode("11", new DrawingPen("Winsor & Newton ProMarker", "Burnt Mahogany", ImageTools.getARGB(255, 148, 77, 67)));
        registerPenWithCode("12", new DrawingPen("Winsor & Newton ProMarker", "Chocolate", ImageTools.getARGB(255, 143, 92, 65)));
        registerPenWithCode("13", new DrawingPen("Winsor & Newton ProMarker", "Deep Mocha", ImageTools.getARGB(255, 143, 96, 69)));
        registerPenWithCode("14", new DrawingPen("Winsor & Newton ProMarker", "Hazelnut", ImageTools.getARGB(255, 175, 125, 72)));
        registerPenWithCode("15", new DrawingPen("Winsor & Newton ProMarker", "Coffee", ImageTools.getARGB(255, 175, 120, 61)));
        registerPenWithCode("16", new DrawingPen("Winsor & Newton ProMarker", "Cedar Brown", ImageTools.getARGB(255, 174, 87, 56)));
        registerPenWithCode("17", new DrawingPen("Winsor & Newton ProMarker", "Primrose", ImageTools.getARGB(255, 249, 249, 220)));
        registerPenWithCode("18", new DrawingPen("Winsor & Newton ProMarker", "Soft Lime", ImageTools.getARGB(255, 251, 246, 194)));
        registerPenWithCode("19", new DrawingPen("Winsor & Newton ProMarker", "Buttercup", ImageTools.getARGB(255, 254, 251, 210)));
        registerPenWithCode("20", new DrawingPen("Winsor & Newton ProMarker", "Lemon", ImageTools.getARGB(255, 255, 243, 101)));
        registerPenWithCode("21", new DrawingPen("Winsor & Newton ProMarker", "Yellow", ImageTools.getARGB(255, 252, 240, 4)));
        registerPenWithCode("22", new DrawingPen("Winsor & Newton ProMarker", "Canary", ImageTools.getARGB(255, 255, 221, 0)));
        registerPenWithCode("23", new DrawingPen("Winsor & Newton ProMarker", "Tulip Yellow", ImageTools.getARGB(255, 251, 239, 120)));
        registerPenWithCode("24", new DrawingPen("Winsor & Newton ProMarker", "Sunflower", ImageTools.getARGB(255, 255, 209, 43)));
        registerPenWithCode("25", new DrawingPen("Winsor & Newton ProMarker", "Mustard", ImageTools.getARGB(255, 249, 202, 131)));
        registerPenWithCode("26", new DrawingPen("Winsor & Newton ProMarker", "Pastel Yellow", ImageTools.getARGB(255, 251, 217, 156)));
        registerPenWithCode("27", new DrawingPen("Winsor & Newton ProMarker", "Vanilla", ImageTools.getARGB(255, 254, 225, 185)));
        registerPenWithCode("28", new DrawingPen("Winsor & Newton ProMarker", "Oatmeal", ImageTools.getARGB(255, 250, 204, 165)));
        registerPenWithCode("29", new DrawingPen("Winsor & Newton ProMarker", "Saffron", ImageTools.getARGB(255, 255, 216, 174)));
        registerPenWithCode("30", new DrawingPen("Winsor & Newton ProMarker", "Apricot", ImageTools.getARGB(255, 252, 189, 126)));
        registerPenWithCode("31", new DrawingPen("Winsor & Newton ProMarker", "Honeycomb", ImageTools.getARGB(255, 252, 175, 78)));
        registerPenWithCode("32", new DrawingPen("Winsor & Newton ProMarker", "Gold", ImageTools.getARGB(255, 252, 185, 67)));
        registerPenWithCode("33", new DrawingPen("Winsor & Newton ProMarker", "Amber", ImageTools.getARGB(255, 249, 165, 55)));
        registerPenWithCode("34", new DrawingPen("Winsor & Newton ProMarker", "Pumpkin", ImageTools.getARGB(255, 250, 152, 39)));
        registerPenWithCode("35", new DrawingPen("Winsor & Newton ProMarker", "Mandarin", ImageTools.getARGB(255, 247, 123, 45)));
        registerPenWithCode("36", new DrawingPen("Winsor & Newton ProMarker", "Bright Orange", ImageTools.getARGB(255, 244, 108, 53)));
        registerPenWithCode("37", new DrawingPen("Winsor & Newton ProMarker", "Orange", ImageTools.getARGB(255, 247, 124, 85)));
        registerPenWithCode("38", new DrawingPen("Winsor & Newton ProMarker", "Mango", ImageTools.getARGB(255, 249, 155, 116)));
        registerPenWithCode("39", new DrawingPen("Winsor & Newton ProMarker", "Peach", ImageTools.getARGB(255, 250, 159, 124)));
        registerPenWithCode("40", new DrawingPen("Winsor & Newton ProMarker", "Ginger", ImageTools.getARGB(255, 242, 169, 108)));
        registerPenWithCode("41", new DrawingPen("Winsor & Newton ProMarker", "Soft Peach", ImageTools.getARGB(255, 250, 202, 189)));
        registerPenWithCode("42", new DrawingPen("Winsor & Newton ProMarker", "Sunkissed Pink", ImageTools.getARGB(255, 253, 202, 183)));
        registerPenWithCode("43", new DrawingPen("Winsor & Newton ProMarker", "Lipstick Red", ImageTools.getARGB(255, 245, 98, 106)));
        registerPenWithCode("44", new DrawingPen("Winsor & Newton ProMarker", "Ruby", ImageTools.getARGB(255, 219, 70, 97)));
        registerPenWithCode("45", new DrawingPen("Winsor & Newton ProMarker", "Red", ImageTools.getARGB(255, 235, 71, 91)));
        registerPenWithCode("46", new DrawingPen("Winsor & Newton ProMarker", "Poppy", ImageTools.getARGB(255, 221, 66, 91)));
        registerPenWithCode("47", new DrawingPen("Winsor & Newton ProMarker", "Berry Red", ImageTools.getARGB(255, 219, 67, 88)));
        registerPenWithCode("48", new DrawingPen("Winsor & Newton ProMarker", "Crimson", ImageTools.getARGB(255, 202, 66, 89)));
        registerPenWithCode("49", new DrawingPen("Winsor & Newton ProMarker", "Cardinal Red", ImageTools.getARGB(255, 198, 35, 94)));
        registerPenWithCode("50", new DrawingPen("Winsor & Newton ProMarker", "Antique Pink", ImageTools.getARGB(255, 229, 133, 152)));
        registerPenWithCode("51", new DrawingPen("Winsor & Newton ProMarker", "Hot Pink", ImageTools.getARGB(255, 225, 58, 128)));
        registerPenWithCode("52", new DrawingPen("Winsor & Newton ProMarker", "Magenta", ImageTools.getARGB(255, 237, 44, 131)));
        registerPenWithCode("53", new DrawingPen("Winsor & Newton ProMarker", "Mulberry", ImageTools.getARGB(255, 195, 73, 163)));
        registerPenWithCode("54", new DrawingPen("Winsor & Newton ProMarker", "Maroon", ImageTools.getARGB(255, 168, 37, 108)));
        registerPenWithCode("55", new DrawingPen("Winsor & Newton ProMarker", "Cerise", ImageTools.getARGB(255, 246, 139, 187)));
        registerPenWithCode("56", new DrawingPen("Winsor & Newton ProMarker", "Carmine", ImageTools.getARGB(255, 241, 88, 143)));
        registerPenWithCode("57", new DrawingPen("Winsor & Newton ProMarker", "Salmon Pink", ImageTools.getARGB(255, 249, 171, 165)));
        registerPenWithCode("58", new DrawingPen("Winsor & Newton ProMarker", "Cocktail Pink", ImageTools.getARGB(255, 249, 188, 187)));
        registerPenWithCode("59", new DrawingPen("Winsor & Newton ProMarker", "Dusky Rose", ImageTools.getARGB(255, 215, 154, 176)));
        registerPenWithCode("60", new DrawingPen("Winsor & Newton ProMarker", "Pastel Pink", ImageTools.getARGB(255, 252, 205, 196)));
        registerPenWithCode("61", new DrawingPen("Winsor & Newton ProMarker", "Baby Pink", ImageTools.getARGB(255, 251, 203, 212)));
        registerPenWithCode("62", new DrawingPen("Winsor & Newton ProMarker", "Pale Pink", ImageTools.getARGB(255, 252, 220, 227)));
        registerPenWithCode("63", new DrawingPen("Winsor & Newton ProMarker", "Pale Blossom", ImageTools.getARGB(255, 253, 231, 240)));
        registerPenWithCode("64", new DrawingPen("Winsor & Newton ProMarker", "Pink Carnation", ImageTools.getARGB(255, 247, 198, 219)));
        registerPenWithCode("65", new DrawingPen("Winsor & Newton ProMarker", "Rose Pink", ImageTools.getARGB(255, 245, 172, 199)));
        registerPenWithCode("66", new DrawingPen("Winsor & Newton ProMarker", "Blossom", ImageTools.getARGB(255, 249, 214, 229)));
        registerPenWithCode("67", new DrawingPen("Winsor & Newton ProMarker", "Fuchsia Pink", ImageTools.getARGB(255, 224, 167, 202)));
        registerPenWithCode("68", new DrawingPen("Winsor & Newton ProMarker", "Orchid", ImageTools.getARGB(255, 214, 188, 219)));
        registerPenWithCode("69", new DrawingPen("Winsor & Newton ProMarker", "Lavender", ImageTools.getARGB(255, 222, 210, 230)));
        registerPenWithCode("70", new DrawingPen("Winsor & Newton ProMarker", "Lilac", ImageTools.getARGB(255, 191, 178, 210)));
        registerPenWithCode("71", new DrawingPen("Winsor & Newton ProMarker", "Bluebell", ImageTools.getARGB(255, 169, 178, 219)));
        registerPenWithCode("72", new DrawingPen("Winsor & Newton ProMarker", "Blue Pearl", ImageTools.getARGB(255, 198, 212, 237)));
        registerPenWithCode("73", new DrawingPen("Winsor & Newton ProMarker", "Cornflower", ImageTools.getARGB(255, 170, 190, 221)));
        registerPenWithCode("74", new DrawingPen("Winsor & Newton ProMarker", "Plum", ImageTools.getARGB(255, 146, 59, 138)));
        registerPenWithCode("75", new DrawingPen("Winsor & Newton ProMarker", "Amethyst", ImageTools.getARGB(255, 191, 147, 195)));
        registerPenWithCode("76", new DrawingPen("Winsor & Newton ProMarker", "Purple", ImageTools.getARGB(255, 178, 109, 176)));
        registerPenWithCode("77", new DrawingPen("Winsor & Newton ProMarker", "Aubergine", ImageTools.getARGB(255, 90, 41, 95)));
        registerPenWithCode("78", new DrawingPen("Winsor & Newton ProMarker", "Prussian", ImageTools.getARGB(255, 100, 84, 169)));
        registerPenWithCode("79", new DrawingPen("Winsor & Newton ProMarker", "Indigo Blue", ImageTools.getARGB(255, 1, 58, 108)));
        registerPenWithCode("80", new DrawingPen("Winsor & Newton ProMarker", "Royal Blue", ImageTools.getARGB(255, 27, 96, 166)));
        registerPenWithCode("81", new DrawingPen("Winsor & Newton ProMarker", "Violet", ImageTools.getARGB(255, 114, 121, 187)));
        registerPenWithCode("82", new DrawingPen("Winsor & Newton ProMarker", "Cobalt Blue", ImageTools.getARGB(255, 134, 169, 215)));
        registerPenWithCode("83", new DrawingPen("Winsor & Newton ProMarker", "Azure", ImageTools.getARGB(255, 0, 153, 210)));
        registerPenWithCode("84", new DrawingPen("Winsor & Newton ProMarker", "True Blue", ImageTools.getARGB(255, 0, 133, 197)));
        registerPenWithCode("85", new DrawingPen("Winsor & Newton ProMarker", "French Navy", ImageTools.getARGB(255, 0, 130, 184)));
        registerPenWithCode("86", new DrawingPen("Winsor & Newton ProMarker", "Midnight Blue", ImageTools.getARGB(255, 99, 116, 146)));
        registerPenWithCode("87", new DrawingPen("Winsor & Newton ProMarker", "Storm Cloud", ImageTools.getARGB(255, 114, 123, 145)));
        registerPenWithCode("88", new DrawingPen("Winsor & Newton ProMarker", "Sky Blue", ImageTools.getARGB(255, 101, 202, 239)));
        registerPenWithCode("89", new DrawingPen("Winsor & Newton ProMarker", "Cyan", ImageTools.getARGB(255, 0, 188, 242)));
        registerPenWithCode("90", new DrawingPen("Winsor & Newton ProMarker", "Aegean", ImageTools.getARGB(255, 0, 158, 196)));
        registerPenWithCode("91", new DrawingPen("Winsor & Newton ProMarker", "Cool Aqua", ImageTools.getARGB(255, 212, 240, 247)));
        registerPenWithCode("92", new DrawingPen("Winsor & Newton ProMarker", "Duck Egg", ImageTools.getARGB(255, 137, 209, 219)));
        registerPenWithCode("93", new DrawingPen("Winsor & Newton ProMarker", "Arctic Blue", ImageTools.getARGB(255, 171, 222, 239)));
        registerPenWithCode("94", new DrawingPen("Winsor & Newton ProMarker", "China Blue", ImageTools.getARGB(255, 121, 153, 202)));
        registerPenWithCode("95", new DrawingPen("Winsor & Newton ProMarker", "Powder Blue", ImageTools.getARGB(255, 209, 227, 244)));
        registerPenWithCode("96", new DrawingPen("Winsor & Newton ProMarker", "Cadet Blue", ImageTools.getARGB(255, 154, 197, 232)));
        registerPenWithCode("97", new DrawingPen("Winsor & Newton ProMarker", "Denim Blue", ImageTools.getARGB(255, 148, 196, 214)));
        registerPenWithCode("98", new DrawingPen("Winsor & Newton ProMarker", "Cloud Blue", ImageTools.getARGB(255, 175, 215, 241)));
        registerPenWithCode("99", new DrawingPen("Winsor & Newton ProMarker", "Grey Green", ImageTools.getARGB(255, 157, 176, 177)));
        registerPenWithCode("100", new DrawingPen("Winsor & Newton ProMarker", "Pebble Blue", ImageTools.getARGB(255, 217, 230, 222)));
        registerPenWithCode("101", new DrawingPen("Winsor & Newton ProMarker", "Pastel Blue", ImageTools.getARGB(255, 221, 237, 246)));
        registerPenWithCode("102", new DrawingPen("Winsor & Newton ProMarker", "Tea Green", ImageTools.getARGB(255, 222, 229, 225)));
        registerPenWithCode("103", new DrawingPen("Winsor & Newton ProMarker", "Pastel Green", ImageTools.getARGB(255, 203, 233, 228)));
        registerPenWithCode("104", new DrawingPen("Winsor & Newton ProMarker", "Verdigris", ImageTools.getARGB(255, 130, 185, 203)));
        registerPenWithCode("105", new DrawingPen("Winsor & Newton ProMarker", "Petrol Blue", ImageTools.getARGB(255, 23, 123, 142)));
        registerPenWithCode("106", new DrawingPen("Winsor & Newton ProMarker", "Marine", ImageTools.getARGB(255, 48, 157, 159)));
        registerPenWithCode("107", new DrawingPen("Winsor & Newton ProMarker", "Turquoise", ImageTools.getARGB(255, 15, 183, 191)));
        registerPenWithCode("108", new DrawingPen("Winsor & Newton ProMarker", "Green", ImageTools.getARGB(255, 0, 180, 151)));
        registerPenWithCode("109", new DrawingPen("Winsor & Newton ProMarker", "Holly", ImageTools.getARGB(255, 0, 110, 100)));
        registerPenWithCode("110", new DrawingPen("Winsor & Newton ProMarker", "Pine", ImageTools.getARGB(255, 71, 135, 92)));
        registerPenWithCode("111", new DrawingPen("Winsor & Newton ProMarker", "Lush Green", ImageTools.getARGB(255, 0, 146, 92)));
        registerPenWithCode("112", new DrawingPen("Winsor & Newton ProMarker", "Emerald", ImageTools.getARGB(255, 35, 186, 139)));
        registerPenWithCode("113", new DrawingPen("Winsor & Newton ProMarker", "Soft Green", ImageTools.getARGB(255, 153, 209, 201)));
        registerPenWithCode("114", new DrawingPen("Winsor & Newton ProMarker", "Mint Green", ImageTools.getARGB(255, 116, 203, 170)));
        registerPenWithCode("115", new DrawingPen("Winsor & Newton ProMarker", "Grass", ImageTools.getARGB(255, 124, 200, 125)));
        registerPenWithCode("116", new DrawingPen("Winsor & Newton ProMarker", "Forest Green", ImageTools.getARGB(255, 99, 163, 66)));
        registerPenWithCode("117", new DrawingPen("Winsor & Newton ProMarker", "Bright Green", ImageTools.getARGB(255, 139, 202, 83)));
        registerPenWithCode("118", new DrawingPen("Winsor & Newton ProMarker", "Leaf Green", ImageTools.getARGB(255, 169, 210, 110)));
        registerPenWithCode("119", new DrawingPen("Winsor & Newton ProMarker", "Apple", ImageTools.getARGB(255, 171, 213, 142)));
        registerPenWithCode("120", new DrawingPen("Winsor & Newton ProMarker", "Meadow Green", ImageTools.getARGB(255, 212, 233, 203)));
        registerPenWithCode("121", new DrawingPen("Winsor & Newton ProMarker", "Lime Zest", ImageTools.getARGB(255, 233, 237, 160)));
        registerPenWithCode("122", new DrawingPen("Winsor & Newton ProMarker", "Lime Green", ImageTools.getARGB(255, 214, 224, 71)));
        registerPenWithCode("123", new DrawingPen("Winsor & Newton ProMarker", "Pear Green", ImageTools.getARGB(255, 200, 202, 57)));
        registerPenWithCode("124", new DrawingPen("Winsor & Newton ProMarker", "Moss", ImageTools.getARGB(255, 195, 182, 0)));
        registerPenWithCode("125", new DrawingPen("Winsor & Newton ProMarker", "Herb Green", ImageTools.getARGB(255, 173, 165, 36)));
        registerPenWithCode("126", new DrawingPen("Winsor & Newton ProMarker", "Olive Green", ImageTools.getARGB(255, 163, 158, 96)));
        registerPenWithCode("188", new DrawingPen("Winsor & Newton ProMarker", "Pesto", ImageTools.getARGB(255, 87, 102, 61)));
        registerPenWithCode("127", new DrawingPen("Winsor & Newton ProMarker", "Marsh Green", ImageTools.getARGB(255, 159, 168, 92)));
        registerPenWithCode("128", new DrawingPen("Winsor & Newton ProMarker", "Khaki", ImageTools.getARGB(255, 208, 204, 178)));
        registerPenWithCode("129", new DrawingPen("Winsor & Newton ProMarker", "Pastel Beige", ImageTools.getARGB(255, 237, 234, 208)));
        registerPenWithCode("130", new DrawingPen("Winsor & Newton ProMarker", "Caramel", ImageTools.getARGB(255, 207, 173, 134)));
        registerPenWithCode("131", new DrawingPen("Winsor & Newton ProMarker", "Praline", ImageTools.getARGB(255, 221, 188, 148)));
        registerPenWithCode("132", new DrawingPen("Winsor & Newton ProMarker", "Sandstone", ImageTools.getARGB(255, 227, 204, 166)));
        registerPenWithCode("133", new DrawingPen("Winsor & Newton ProMarker", "Champagne", ImageTools.getARGB(255, 243, 231, 208)));
        registerPenWithCode("134", new DrawingPen("Winsor & Newton ProMarker", "Ivory", ImageTools.getARGB(255, 254, 248, 234)));
        registerPenWithCode("135", new DrawingPen("Winsor & Newton ProMarker", "Satin", ImageTools.getARGB(255, 250, 227, 215)));
        registerPenWithCode("136", new DrawingPen("Winsor & Newton ProMarker", "Almond", ImageTools.getARGB(255, 250, 236, 223)));
        registerPenWithCode("137", new DrawingPen("Winsor & Newton ProMarker", "Blush", ImageTools.getARGB(255, 251, 228, 203)));
        registerPenWithCode("138", new DrawingPen("Winsor & Newton ProMarker", "Dusky Pink", ImageTools.getARGB(255, 252, 220, 201)));
        registerPenWithCode("139", new DrawingPen("Winsor & Newton ProMarker", "Putty", ImageTools.getARGB(255, 247, 217, 203)));
        registerPenWithCode("140", new DrawingPen("Winsor & Newton ProMarker", "Coral", ImageTools.getARGB(255, 228, 165, 148)));
        registerPenWithCode("141", new DrawingPen("Winsor & Newton ProMarker", "Cinnamon", ImageTools.getARGB(255, 219, 176, 139)));
        registerPenWithCode("142", new DrawingPen("Winsor & Newton ProMarker", "Tan", ImageTools.getARGB(255, 235, 191, 171)));
        registerPenWithCode("143", new DrawingPen("Winsor & Newton ProMarker", "Umber", ImageTools.getARGB(255, 134, 116, 92)));
        registerPenWithCode("144", new DrawingPen("Winsor & Newton ProMarker", "Burnt Sienna", ImageTools.getARGB(255, 140, 88, 44)));
        registerPenWithCode("145", new DrawingPen("Winsor & Newton ProMarker", "Raw Sienna", ImageTools.getARGB(255, 215, 149, 59)));
        registerPenWithCode("146", new DrawingPen("Winsor & Newton ProMarker", "Spice", ImageTools.getARGB(255, 228, 129, 57)));
        registerPenWithCode("147", new DrawingPen("Winsor & Newton ProMarker", "Saddle Brown", ImageTools.getARGB(255, 201, 128, 79)));
        registerPenWithCode("148", new DrawingPen("Winsor & Newton ProMarker", "Terracotta", ImageTools.getARGB(255, 191, 103, 51)));
        registerPenWithCode("149", new DrawingPen("Winsor & Newton ProMarker", "Burnt Orange", ImageTools.getARGB(255, 211, 103, 80)));
        registerPenWithCode("150", new DrawingPen("Winsor & Newton ProMarker", "Cocoa", ImageTools.getARGB(255, 185, 138, 88)));
        registerPenWithCode("151", new DrawingPen("Winsor & Newton ProMarker", "Henna", ImageTools.getARGB(255, 138, 69, 25)));
        registerPenWithCode("152", new DrawingPen("Winsor & Newton ProMarker", "Walnut", ImageTools.getARGB(255, 131, 67, 38)));
        registerPenWithCode("153", new DrawingPen("Winsor & Newton ProMarker", "Firebrick", ImageTools.getARGB(255, 178, 57, 65)));
        registerPenWithCode("154", new DrawingPen("Winsor & Newton ProMarker", "Chestnut", ImageTools.getARGB(255, 170, 88, 64)));
        registerPenWithCode("155", new DrawingPen("Winsor & Newton ProMarker", "Burnt Umber", ImageTools.getARGB(255, 183, 106, 80)));
        registerPenWithCode("156", new DrawingPen("Winsor & Newton ProMarker", "Burgundy", ImageTools.getARGB(255, 168, 47, 89)));
        registerPenWithCode("157", new DrawingPen("Winsor & Newton ProMarker", "Shale", ImageTools.getARGB(255, 153, 119, 122)));
        registerPenWithCode("158", new DrawingPen("Winsor & Newton ProMarker", "Slate", ImageTools.getARGB(255, 118, 93, 124)));
        registerPenWithCode("159", new DrawingPen("Winsor & Newton ProMarker", "Warm Grey 00", ImageTools.getARGB(255, 248, 248, 244)));
        registerPenWithCode("160", new DrawingPen("Winsor & Newton ProMarker", "Warm Grey 0", ImageTools.getARGB(255, 238, 239, 234)));
        registerPenWithCode("161", new DrawingPen("Winsor & Newton ProMarker", "Warm Grey 1", ImageTools.getARGB(255, 230, 221, 216)));
        registerPenWithCode("162", new DrawingPen("Winsor & Newton ProMarker", "Warm Grey 2", ImageTools.getARGB(255, 201, 186, 179)));
        registerPenWithCode("163", new DrawingPen("Winsor & Newton ProMarker", "Warm Grey 3", ImageTools.getARGB(255, 186, 170, 163)));
        registerPenWithCode("164", new DrawingPen("Winsor & Newton ProMarker", "Warm Grey 4", ImageTools.getARGB(255, 165, 149, 143)));
        registerPenWithCode("165", new DrawingPen("Winsor & Newton ProMarker", "Warm Grey 5", ImageTools.getARGB(255, 116, 102, 93)));
        registerPenWithCode("166", new DrawingPen("Winsor & Newton ProMarker", "Warm Grey 6", ImageTools.getARGB(255, 130, 114, 89)));
        registerPenWithCode("167", new DrawingPen("Winsor & Newton ProMarker", "Warm Grey 7", ImageTools.getARGB(255, 98, 83, 65)));
        registerPenWithCode("168", new DrawingPen("Winsor & Newton ProMarker", "Cool Grey 00", ImageTools.getARGB(255, 249, 249, 249)));
        registerPenWithCode("169", new DrawingPen("Winsor & Newton ProMarker", "Cool Grey 0", ImageTools.getARGB(255, 246, 246, 246)));
        registerPenWithCode("170", new DrawingPen("Winsor & Newton ProMarker", "Cool Grey 1", ImageTools.getARGB(255, 245, 241, 239)));
        registerPenWithCode("171", new DrawingPen("Winsor & Newton ProMarker", "Cool Grey 2", ImageTools.getARGB(255, 223, 223, 219)));
        registerPenWithCode("172", new DrawingPen("Winsor & Newton ProMarker", "Cool Grey 3", ImageTools.getARGB(255, 215, 214, 213)));
        registerPenWithCode("173", new DrawingPen("Winsor & Newton ProMarker", "Cool Grey 4", ImageTools.getARGB(255, 138, 139, 143)));
        registerPenWithCode("174", new DrawingPen("Winsor & Newton ProMarker", "Cool Grey 5", ImageTools.getARGB(255, 112, 111, 109)));
        registerPenWithCode("175", new DrawingPen("Winsor & Newton ProMarker", "Cool Grey 6", ImageTools.getARGB(255, 119, 129, 128)));
        registerPenWithCode("176", new DrawingPen("Winsor & Newton ProMarker", "Cool Grey 7", ImageTools.getARGB(255, 81, 91, 88)));
        registerPenWithCode("177", new DrawingPen("Winsor & Newton ProMarker", "Ice Grey 00", ImageTools.getARGB(255, 250, 250, 250)));
        registerPenWithCode("178", new DrawingPen("Winsor & Newton ProMarker", "Ice Grey 0", ImageTools.getARGB(255, 246, 245, 245)));
        registerPenWithCode("179", new DrawingPen("Winsor & Newton ProMarker", "Ice Grey 1", ImageTools.getARGB(255, 229, 229, 229)));
        registerPenWithCode("180", new DrawingPen("Winsor & Newton ProMarker", "Ice Grey 2", ImageTools.getARGB(255, 206, 209, 212)));
        registerPenWithCode("181", new DrawingPen("Winsor & Newton ProMarker", "Ice Grey 3", ImageTools.getARGB(255, 192, 193, 197)));
        registerPenWithCode("182", new DrawingPen("Winsor & Newton ProMarker", "Ice Grey 4", ImageTools.getARGB(255, 151, 168, 180)));
        registerPenWithCode("183", new DrawingPen("Winsor & Newton ProMarker", "Ice Grey 5", ImageTools.getARGB(255, 149, 154, 150)));
        registerPenWithCode("184", new DrawingPen("Winsor & Newton ProMarker", "Ice Grey 6", ImageTools.getARGB(255, 128, 130, 125)));
        registerPenWithCode("185", new DrawingPen("Winsor & Newton ProMarker", "Ice Grey 7", ImageTools.getARGB(255, 105, 106, 101)));
        registerPenWithCode("186", new DrawingPen("Winsor & Newton ProMarker", "Black", ImageTools.getARGB(255, 62, 58, 66)));
        registerPenWithCode("187", new DrawingPen("Winsor & Newton ProMarker", "Blue Black", ImageTools.getARGB(255, 0, 3, 36)));
    }

    @Override
    public void registerPenSets() {
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "5 Earth Tones", getDrawingPensFromCodes(128, 132, 142, 148, 152, 143)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "6 Green Tones", getDrawingPensFromCodes(120, 119, 114, 115, 112, 109)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "6 Landscape Tones 1", getDrawingPensFromCodes(133, 153, 125, 188, 131, 147)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "6 Mid Tones", getDrawingPensFromCodes(43, 150, 23, 115, 94, 64)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "6 Neutral Tones", getDrawingPensFromCodes(170, 171, 172, 173, 174, 186)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "6 Pastel Tones", getDrawingPensFromCodes(41, 19, 120, 91, 69, 62)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "6 Red Tones", getDrawingPensFromCodes(41, 57, 43, 47, 54, 156)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "6 Rich Tones", getDrawingPensFromCodes(49, 124, 106, 78, 74, 54)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "6 Skin Tones 1", getDrawingPensFromCodes(134, 135, 136, 139, 140, 41)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "6 Skin Tones 2", getDrawingPensFromCodes(144, 141, 142, 137, 138, 146)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "6 Skyscape Tones 1", getDrawingPensFromCodes(96, 98, 63, 100, 87, 104)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "6 Vibrant Tones", getDrawingPensFromCodes(46, 35, 89, 117, 21, 55)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "6 Yellow Tones", getDrawingPensFromCodes(19, 20, 22, 27, 25, 33)));

        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "12 Manga Romance", getDrawingPensFromCodes(17, 32, 51, 66, 69, 85, 106, 124, 141, 154, 161, 163)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "12 Skin Tones Set", getDrawingPensFromCodes(1, 5, 2, 3, 10, 7, 142, 8, 14, 11, 15, 13)));

        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "24 Architecture", getDrawingPensFromCodes(186, 178, 180, 182, 184, 160, 162, 164, 166, 133, 30, 130, 145, 110, 116, 119, 86, 82, 99)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "24 Arts & Illustration", getDrawingPensFromCodes(20, 34, 36, 45, 48, 51, 65, 68, 81, 92, 89, 84, 86, 107, 113, 124, 115, 108, 150, 29, 139, 171, 173, 186)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "24 Black & Greys", getDrawingPensFromCodes(161, 162, 163, 164, 165, 179, 180, 181, 182, 183, 170, 171, 172, 173, 174, 186)));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "24 In Wallet Set", getDrawingPensFromCodes(47, 156, 35, 128, 122, 123, 107, 97, 83, 53, 74, 54, 33, 21, 24, 117, 111, 114, 80, 79, 75, 56, 50, 58)));

        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "48 Essential Collection", getDrawingPensFromCodes(134, 129, 137, 29, 138, 142, 132, 141, 145, 150, 143, 144, 21, 24, 32, 35, 45, 46, 48, 54, 65, 52, 68, 76, 91, 95, 88, 89, 85, 107, 86, 120, 119, 122, 117, 116, 110, 126, 163, 165, 179, 181, 183, 170, 172, 174, 186)));

        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Winsor & Newton", "96 Extended Collection", getDrawingPensFromCodes(17, 20, 23, 21, 22, 24, 39, 33, 34, 35, 43, 45, 46, 48, 51, 61, 57, 56, 55, 64, 52, 54, 74, 77, 158, 70, 75, 76, 81, 78, 72, 73, 82, 89, 83, 90, 84, 80, 79, 85, 105, 106, 107, 108, 103, 110, 112, 115, 116, 119, 118, 117, 122, 124, 123, 128, 132, 130, 134, 136, 137, 139, 138, 140, 141, 142, 145, 149, 146, 148, 150, 151, 155, 156, 154, 144, 143, 161, 162, 163, 164, 165, 170, 171, 172, 173, 174, 179, 180, 181, 182, 183, 186)));
    }
}
