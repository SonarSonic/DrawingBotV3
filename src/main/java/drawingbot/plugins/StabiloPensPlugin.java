package drawingbot.plugins;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.registry.MasterRegistry;

public class StabiloPensPlugin extends AbstractPenPlugin {

    public static final StabiloPensPlugin INSTANCE = new StabiloPensPlugin();
    public static final String VERSION = "1.0.0";

    private StabiloPensPlugin() {}

    public String getPenManufacturer() {
        return "Stabilo";
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void registerPens() {
        registerPenWithCode("024", new DrawingPen("Stabilo Point 88", "024 Neon Yellow", -919497));
        registerPenWithCode("031", new DrawingPen("Stabilo Point 88", "031 Neon Blue", -8399886));
        registerPenWithCode("033", new DrawingPen("Stabilo Point 88", "033 Neon Green", -6435265));
        registerPenWithCode("040", new DrawingPen("Stabilo Point 88", "040 Neon Red", -1405021));
        registerPenWithCode("054", new DrawingPen("Stabilo Point 88", "054 Neon Orange", -1325758));
        registerPenWithCode("056", new DrawingPen("Stabilo Point 88", "056 Neon Pink", -1477453));
        registerPenWithCode("11", new DrawingPen("Stabilo Point 88", "11 Ice Blue", -3872007));
        registerPenWithCode("13", new DrawingPen("Stabilo Point 88", "13 Ice Green", -9779020));
        registerPenWithCode("16", new DrawingPen("Stabilo Point 88", "16 Light Emerald", -6498653));
        registerPenWithCode("17", new DrawingPen("Stabilo Point 88", "17 Heliotrope", -1859378));
        registerPenWithCode("19", new DrawingPen("Stabilo Point 88", "19 Purple", -4247205));
        registerPenWithCode("22", new DrawingPen("Stabilo Point 88", "22 Nightblue", -13620124));
        registerPenWithCode("24", new DrawingPen("Stabilo Point 88", "24 Lemon Yellow", -1182375));
        registerPenWithCode("26", new DrawingPen("Stabilo Point 88", "26 Apricot", -1197404));
        registerPenWithCode("29", new DrawingPen("Stabilo Point 88", "29 Light Pink", -1338193));
        registerPenWithCode("30", new DrawingPen("Stabilo Point 88", "30 Pale Vermillion", -1480150));
        registerPenWithCode("31", new DrawingPen("Stabilo Point 88", "31 Light Blue", -11099178));
        registerPenWithCode("32", new DrawingPen("Stabilo Point 88", "32 Ultramarine", -11363888));
        registerPenWithCode("33", new DrawingPen("Stabilo Point 88", "33 Apple Green", -4531653));
        registerPenWithCode("36", new DrawingPen("Stabilo Point 88", "36 Green", -12409769));
        registerPenWithCode("38", new DrawingPen("Stabilo Point 88", "38 Sanguine", -3324872));
        registerPenWithCode("40", new DrawingPen("Stabilo Point 88", "40 Red", -1627356));
        registerPenWithCode("41", new DrawingPen("Stabilo Point 88", "41 Blue", -13088620));
        registerPenWithCode("43", new DrawingPen("Stabilo Point 88", "43 Light Green", -7485375));
        registerPenWithCode("44", new DrawingPen("Stabilo Point 88", "44 Yellow", -992205));
        registerPenWithCode("45", new DrawingPen("Stabilo Point 88", "45 Brown", -8237001));
        registerPenWithCode("46", new DrawingPen("Stabilo Point 88", "46 Black", -15263982));
        registerPenWithCode("48", new DrawingPen("Stabilo Point 88", "48 Light Red", -1551303));
        registerPenWithCode("50", new DrawingPen("Stabilo Point 88", "50 Crimson", -2740664));
        registerPenWithCode("51", new DrawingPen("Stabilo Point 88", "51 Turqouise", -11161410));
        registerPenWithCode("53", new DrawingPen("Stabilo Point 88", "53 Pine Green", -13471915));
        registerPenWithCode("54", new DrawingPen("Stabilo Point 88", "54 Orange", -1535188));
        registerPenWithCode("55", new DrawingPen("Stabilo Point 88", "55 Violet", -10211190));
        registerPenWithCode("56", new DrawingPen("Stabilo Point 88", "56 Pink", -1624173));
        registerPenWithCode("57", new DrawingPen("Stabilo Point 88", "57 Azure", -9449243));
        registerPenWithCode("58", new DrawingPen("Stabilo Point 88", "58 Lilac", -5295217));
        registerPenWithCode("59", new DrawingPen("Stabilo Point 88", "59 Light Lilac", -4152366));
        registerPenWithCode("63", new DrawingPen("Stabilo Point 88", "63 Olive Green", -11176894));
        registerPenWithCode("65", new DrawingPen("Stabilo Point 88", "65 Umber", -8625076));
        registerPenWithCode("75", new DrawingPen("Stabilo Point 88", "75 Sienna", -6923198));
        registerPenWithCode("88", new DrawingPen("Stabilo Point 88", "88 Light Ochre", -2906788));
        registerPenWithCode("89", new DrawingPen("Stabilo Point 88", "89 Dark Ochre", -3380178));
        registerPenWithCode("94", new DrawingPen("Stabilo Point 88", "94 Light Grey", -3680539));
        registerPenWithCode("95", new DrawingPen("Stabilo Point 88", "95 Medium Cold Grey", -5722954));
        registerPenWithCode("96", new DrawingPen("Stabilo Point 88", "96 Dark Grey", -8747626));
        registerPenWithCode("97", new DrawingPen("Stabilo Point 88", "97 Deep Cold Grey", -10591894));
        registerPenWithCode("98", new DrawingPen("Stabilo Point 88", "98 Payne's Grey", -13415050));
    }

    @Override
    public void registerPenSets() {
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "Point 88 - Greys", getDrawingPensFromCodes("46", "98", "97", "96", "95", "94")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "Point 88 - Blues / Greys", getDrawingPensFromCodes("46", "96", "41", "32", "11", "95", "94")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "Point 88 - Pastel", getDrawingPensFromCodes("96", "13", "16", "11", "59", "29", "26", "24")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "Point 88 - Neon", getDrawingPensFromCodes("031", "033", "024", "054", "040", "056")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "Point 88 - Basics", getDrawingPensFromCodes("46", "36", "41", "55", "58", "40")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "Point 88 - Blues", getDrawingPensFromCodes("22", "98", "41", "31", "32", "51", "57", "11")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "Point 88 - Reds", getDrawingPensFromCodes("50", "19", "29", "30", "40", "48", "56", "040")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "Point 88 - 6 Pack - Assorted", getDrawingPensFromCodes("36", "40", "41", "46", "55", "58")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "Point 88 - 10 Pack A", getDrawingPensFromCodes("46", "45", "36", "43", "41", "55", "56", "40", "54", "44")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "Point 88 - 10 Pack B", getDrawingPensFromCodes("63", "51", "13", "57", "55", "58", "56", "50", "40", "44")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "Point 88 - 20 Pack - Colour Parade", getDrawingPensFromCodes("22", "26", "32", "33", "36", "40", "41", "44", "45", "46", "50", "51", "53", "54", "55", "56", "57", "58", "89", "96")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "Point 88 - 20 Pack - Assorted", getDrawingPensFromCodes("13", "32", "36", "40", "41", "43", "44", "45", "46", "50", "51", "54", "55", "56", "57", "58", "63", "89", "94", "96")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "Point 88 - 48 Pack", getDrawingPensFromCodes("024", "031", "033", "040", "054", "056", "11", "13", "16", "17", "19", "22", "24", "26", "29", "30", "31", "32", "33", "36", "38", "40", "41", "43", "44", "45", "46", "48", "50", "51", "53", "54", "55", "56", "57", "58", "59", "63", "65", "75", "88", "89", "94", "95", "96", "97", "98")));

    }
}
