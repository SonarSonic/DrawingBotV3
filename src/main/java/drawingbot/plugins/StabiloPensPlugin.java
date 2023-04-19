package drawingbot.plugins;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.registry.MasterRegistry;

public class StabiloPensPlugin extends AbstractPenPlugin {

  @Override
  public String getPluginName() {
    return "Stabilo Pens Plugin";
  }

  @Override
  public void registerPens() {
    registerPenWithCode("88/024", new DrawingPen("Stabilo",  "88/024 Neon Yellow", -919497));
    registerPenWithCode("88/031", new DrawingPen("Stabilo",  "88/031 Neon Blue", -8399886));
    registerPenWithCode("88/033", new DrawingPen("Stabilo",  "88/033 Neon Green", -6435265));
    registerPenWithCode("88/040", new DrawingPen("Stabilo",  "88/040 Neon Red", -1405021));
    registerPenWithCode("88/054", new DrawingPen("Stabilo",  "88/054 Neon Orange", -1325758));
    registerPenWithCode("88/056", new DrawingPen("Stabilo",  "88/056 Neon Pink", -1477453));
    registerPenWithCode("88/11", new DrawingPen("Stabilo",  "88/11 Ice Blue", -3872007));
    registerPenWithCode("88/13", new DrawingPen("Stabilo",  "88/13 Ice Green", -9779020));
    registerPenWithCode("88/16", new DrawingPen("Stabilo",  "88/16 Light Emerald", -6498653));
    registerPenWithCode("88/17", new DrawingPen("Stabilo",  "88/17 Heliotrope", -1859378));
    registerPenWithCode("88/19", new DrawingPen("Stabilo",  "88/19 Purple", -4247205));
    registerPenWithCode("88/22", new DrawingPen("Stabilo",  "88/22 Nightblue", -13620124));
    registerPenWithCode("88/24", new DrawingPen("Stabilo",  "88/24 Lemon Yellow", -1182375));
    registerPenWithCode("88/26", new DrawingPen("Stabilo",  "88/26 Apricot", -1197404));
    registerPenWithCode("88/29", new DrawingPen("Stabilo",  "88/29 Light Pink", -1338193));
    registerPenWithCode("88/30", new DrawingPen("Stabilo",  "88/30 Pale Vermillion", -1480150));
    registerPenWithCode("88/31", new DrawingPen("Stabilo",  "88/31 Light Blue", -11099178));
    registerPenWithCode("88/32", new DrawingPen("Stabilo",  "88/32 Ultramarine", -11363888));
    registerPenWithCode("88/33", new DrawingPen("Stabilo",  "88/33 Apple Green", -4531653));
    registerPenWithCode("88/36", new DrawingPen("Stabilo",  "88/36 Green", -12409769));
    registerPenWithCode("88/38", new DrawingPen("Stabilo",  "88/38 Sanguine", -3324872));
    registerPenWithCode("88/40", new DrawingPen("Stabilo",  "88/40 Red", -1627356));
    registerPenWithCode("88/41", new DrawingPen("Stabilo",  "88/41 Blue", -13088620));
    registerPenWithCode("88/43", new DrawingPen("Stabilo",  "88/43 Light Green", -7485375));
    registerPenWithCode("88/44", new DrawingPen("Stabilo",  "88/44 Yellow", -992205));
    registerPenWithCode("88/45", new DrawingPen("Stabilo",  "88/45 Brown", -8237001));
    registerPenWithCode("88/46", new DrawingPen("Stabilo",  "88/46 Black", -15263982));
    registerPenWithCode("88/48", new DrawingPen("Stabilo",  "88/48 Light Red", -1551303));
    registerPenWithCode("88/50", new DrawingPen("Stabilo",  "88/50 Crimson", -2740664));
    registerPenWithCode("88/51", new DrawingPen("Stabilo",  "88/51 Turqouise", -11161410));
    registerPenWithCode("88/53", new DrawingPen("Stabilo",  "88/53 Pine Green", -13471915));
    registerPenWithCode("88/54", new DrawingPen("Stabilo",  "88/54 Orange", -1535188));
    registerPenWithCode("88/55", new DrawingPen("Stabilo",  "88/55 Violet", -10211190));
    registerPenWithCode("88/56", new DrawingPen("Stabilo",  "88/56 Pink", -1624173));
    registerPenWithCode("88/57", new DrawingPen("Stabilo",  "88/57 Azure", -9449243));
    registerPenWithCode("88/58", new DrawingPen("Stabilo",  "88/58 Lilac", -5295217));
    registerPenWithCode("88/59", new DrawingPen("Stabilo",  "88/59 Light Lilac", -4152366));
    registerPenWithCode("88/63", new DrawingPen("Stabilo",  "88/63 Olive Green", -11176894));
    registerPenWithCode("88/65", new DrawingPen("Stabilo",  "88/65 Umber", -8625076));
    registerPenWithCode("88/75", new DrawingPen("Stabilo",  "88/75 Sienna", -6923198));
    registerPenWithCode("88/88", new DrawingPen("Stabilo",  "88/88 Light Ochre", -2906788));
    registerPenWithCode("88/89", new DrawingPen("Stabilo",  "88/89 Dark Ochre", -3380178));
    registerPenWithCode("88/94", new DrawingPen("Stabilo",  "88/94 Light Grey", -3680539));
    registerPenWithCode("88/95", new DrawingPen("Stabilo",  "88/95 Medium Cold Grey", -5722954));
    registerPenWithCode("88/96", new DrawingPen("Stabilo",  "88/96 Dark Grey", -8747626));
    registerPenWithCode("88/97", new DrawingPen("Stabilo",  "88/97 Deep Cold Grey", -10591894));
    registerPenWithCode("88/98", new DrawingPen("Stabilo",  "88/98 Payne's Grey", -13415050));
  }

    @Override
    public void registerPenSets() {
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "Greys", getDrawingPensFromCodes( "88/46", "88/98", "88/97", "88/96", "88/95", "88/94")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "Blues", getDrawingPensFromCodes( "88/46", "88/96", "88/41", "88/32", "88/11", "88/95", "88/94")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "88:48 Pack", getDrawingPensFromCodes( "88/024", "88/031", "88/033", "88/040", "88/054", "88/056", "88/11", "88/13", "88/16", "88/17", "88/19", "88/22", "88/24", "88/26", "88/29", "88/30", "88/31", "88/32", "88/33", "88/36", "88/38", "88/40", "88/41", "88/43", "88/44", "88/45", "88/46", "88/48", "88/50", "88/51", "88/53", "88/54", "88/55", "88/56", "88/57", "88/58", "88/59", "88/63", "88/65", "88/75", "88/88", "88/89", "88/94", "88/95", "88/96", "88/97", "88/98")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "88:Pastel", getDrawingPensFromCodes( "88/96", "88/13", "88/16", "88/11", "88/59", "88/29", "88/26", "88/24")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "88:Neon", getDrawingPensFromCodes( "88/031", "88/033", "88/024", "88/054", "88/040", "88/056")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "88:Basics", getDrawingPensFromCodes( "88/46", "88/36", "88/41", "88/55", "88/58", "88/40")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "88:Blues", getDrawingPensFromCodes( "88/22", "88/98", "88/41", "88/31", "88/32", "88/51", "88/57", "88/11")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "88:Reds", getDrawingPensFromCodes( "88/50", "88/19", "88/29", "88/30", "88/40", "88/48", "88/56", "88/040")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "88:20 Pack - Colour Parade", getDrawingPensFromCodes( "88/22", "88/26", "88/32", "88/33", "88/36", "88/40", "88/41", "88/44", "88/45", "88/46", "88/50", "88/51", "88/53", "88/54", "88/55", "88/56", "88/57", "88/58", "88/89", "88/96")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "88:20 Pack - Assorted", getDrawingPensFromCodes( "88/13", "88/32", "88/36", "88/40", "88/41", "88/43", "88/44", "88/45", "88/46", "88/50", "88/51", "88/54", "88/55", "88/56", "88/57", "88/58", "88/63", "88/89", "88/94", "88/96")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "88:10 Pack A", getDrawingPensFromCodes( "88/46", "88/45", "88/36", "88/43", "88/41", "88/55", "88/56", "88/40", "88/54", "88/44")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "88:10 Pack B", getDrawingPensFromCodes( "88/63", "88/51", "88/13", "88/57", "88/55", "88/58", "88/56", "88/50", "88/40", "88/44")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "88:6 Pack - Assorted", getDrawingPensFromCodes( "88/36", "88/40", "88/41", "88/46", "88/55", "88/58")));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Stabilo", "88:Greys", getDrawingPensFromCodes( "88/46", "88/98", "88/97", "88/96", "88/95", "88/94")));
    }
}
