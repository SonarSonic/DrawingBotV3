package drawingbot.plugins;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.registry.MasterRegistry;

public class CopicPenPlugin extends AbstractPenPlugin {

    @Override
    public String getPluginName() {
        return "Copic Pen Plugin";
    }

    @Override
    public void registerPens() {

        //Copic Original Pens
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "0 Colorless Blender", -1));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "100 Black", -13554901));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "110 Special Black", -16578808));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B00 Frost Blue", -2232076));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B01 Mint Blue", -2691342));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B02 Robin's Egg Blue", -4987919));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B04 Tahitian Blue", -9187354));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B05 Process Blue", -12532250));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B06 Peacok Blue", -16731162));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B12 Ice Blue", -3610896));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B14 Light Blue", -9318421));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B16 Cyanine Blue", -16728854));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B18 Lapis Lazuli", -14841141));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B21 Baby Blue", -2363911));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B23 Phthalo Blue", -7159064));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B24 Sky", -7680269));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B26 Cobalt Blue", -10112029));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B29 Ultramarine", -16681023));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B32 Pale Blue", -1904649));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B34 Manganese Blue", -8207379));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B37 Antwerp Blue", -15372380));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B39 Prussian Blue", -13933399));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B41 Powder Blue", -1904389));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "B45 Smoky Blue", -9060118));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BG02 New Blue", -3741462));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BG05 Holiday Blue", -8138015));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BG09 Blue Green", -16666167));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BG10 Cool Shadow", -2297617));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BG11 Moon White", -3216399));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BG13 Mint Green", -3872791));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BG15 Aqua", -6235694));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BG18 Teal Blue", -13123408));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BG32 Aqua Mint", -4398377));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BG34 Horizon Green", -6038825));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BG45 Nile Blue", -5251105));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BG49 Duck Blue", -16730439));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BG99 Flagstone Blue", -9528441));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BV00 Mauve Shadow", -2040595));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BV04 Blue Berry", -8611890));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BV08 Blue Violet", -6455623));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BV23 Grayish Lavender", -5127971));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "BV31 Pale Lavender", -1382414));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "C0 Cool Gray", -2037779));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "C10 Cool Gray", -14669007));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "C1 Cool Gray", -2432024));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "C2 Cool Gray", -3352611));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "C3 Cool Gray", -4076334));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "C4 Cool Gray", -5851971));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "C5 Cool Gray", -7167829));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "C6 Cool Gray", -8680298));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "C7 Cool Gray", -10260359));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "C8 Cool Gray", -11313818));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "C9 Cool Gray", -12826803));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E00 Cotton Pearl", -134166));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E02 Fruit Pink", -70432));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E04 Lipstick Natural", -1786684));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E07 Light Mahogany", -3374742));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E09 Burnt Sienna", -2528689));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E11 Barley Beige", -71210));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E13 Light Suntan", -1456721));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E15 Dark Suntan", -279667));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E19 Redwood", -3911112));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E21 Soft Sun", -138553));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E25 Caribe Cocoa", -2972542));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E27 Milk Chocolate", -6719901));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E29 Burnt Umber", -7846346));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E31 Brick Beige", -858418));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E33 Sand", -798031));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E34 Toast", -996698));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E35 Chamois", -1653853));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E37 Sepia", -3370663));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E39 Leather", -3836865));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E40 Brick White", -857892));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E41 Pearl White", -69151));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E43 Dull Ivory", -1516867));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E44 Clay", -3819095));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E49 Dark Bark", -10269636));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E51 Milky White", -70442));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E53 Raw Silk", -792893));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E55 Light Camel", -925767));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E57 Light Walnut", -5143208));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E59 Walnut", -6652052));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "E77 Maroon", -8429490));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G00 Jade Green", -1838355));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G02 Spectrum Green", -3151661));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G05 Emerald Green", -9846661));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G07 Nile Green", -8665738));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G09 Veronese Green", -8731547));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G12 Sea Green", -2955068));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G14 Apple Green", -6828144));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G16 Malachite", -10436200));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G17 Forest Green", -15420547));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G19 Bright Parrot Green", -13780598));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G20 Wax White", -1181989));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G21 Lime Green", -3873587));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G24 Willow", -3940172));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G28 Ocean Green", -15625118));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G29 Pine Tree Green", -15106979));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G40 Dim Green", -1773089));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G82 Spring Dim Green", -3351879));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G85 Verdigris", -6437974));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "G99 Olive", -10518982));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "N0 Neutral Gray", -1249555));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "N10 Neutral Gray", -13553872));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "N1 Neutral Gray", -1907739));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "N2 Neutral Gray", -2434083));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "N3 Neutral Gray", -3026220));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "N4 Neutral Gray", -4407871));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "N5 Neutral Gray", -5723731));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "N6 Neutral Gray", -7039591));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "N7 Neutral Gray", -8947588));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "N8 Neutral Gray", -10263450));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "N9 Neutral Gray", -11776689));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "R00 Pinkish White", -70943));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "R02 Rose Salmon", -142393));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "R05 Salmon Red", -617861));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "R08 Vermilion", -891052));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "R11 Pale Cherry Pink", -138795));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "R17 Lipstick Orange", -752532));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "R20 Blush", -206897));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "R24 Prawn", -887431));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "R27 Cadmium Red", -962462));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "R29 Lipstick Red", -1239221));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "R32 Peach", -343622));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "R35 Coral", -888443));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "R37 Carmine", -1545100));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "R39 Garnet", -3454854));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "R59 Cardinal", -4763792));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "RV02 Sugared Almond Pink", -338458));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "RV04 Shock  Pink", -613441));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "RV06 Cerise", -817489));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "RV09 Fuchsia", -2002516));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "RV10 Pale Pink", -135948));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "RV11 Pink", -272675));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "RV13 Tender Pink", -407081));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "RV14 Begonia Pink", -748105));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "RV17 Deep Magenta", -2392397));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "RV19 Red Violet", -2987862));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "RV21 Light Pink", -136985));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "RV25 Dog Rose Flower", -748610));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "RV29 Crimson", -1095552));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "RV32 Shadow Pink", -338994));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "RV34 Dark Pink", -413778));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "T0 Toner Gray", -1249555));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "T10 Toner Gray", -13488595));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "T1 Toner Gray", -1381656));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "T2 Toner Gray", -2039586));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "T3 Toner Gray", -3026228));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "T4 Toner Gray", -4408391));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "T5 Toner Gray", -5724253));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "T6 Toner Gray", -7039600));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "T7 Toner Gray", -8948108));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "T8 Toner Gray", -10263457));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "T9 Toner Gray", -11777207));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "V04 Lilac", -1660210));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "V06 Lavender", -3238462));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "V09 Violet", -7908191));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "V12 Pale Lilac", -1124375));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "V15 Mallow", -2906419));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "V17 Amethyst", -6253881));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "W0 Warm Gray", -1250076));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "W10 Warm Gray", -13619413));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "W1 Warm Gray", -1579041));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "W2 Warm Gray", -2236971));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "W3 Warm Gray", -2960694));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "W4 Warm Gray", -4407881));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "W5 Warm Gray", -5723740));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "W6 Warm Gray", -7039601));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "W7 Warm Gray", -8947597));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "W8 Warm Gray", -10263457));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "W9 Warm Gray", -11776696));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "Y00 Barium Yellow", -66081));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "Y02 Canary Yellow", -593002));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "Y06 Yellow", -68244));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "Y08 Acid Yellow", -69120));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "Y11 Pale Yellow", -1076));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "Y13 Lemon Yellow", -264274));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "Y15 Cadmium Yellow", -71316));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "Y17 Golden Yellow", -7083));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "Y19 Napoli Yellow", -5826));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "Y21 Buttercup Yellow", -4414));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "Y23 Yellowish Beige", -269389));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "Y26 Mustard", -991897));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "Y38 Honey", -11404));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG01 Green Bice", -1905742));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG03 Yellow Green", -2168150));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG05 Salad", -2693742));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG07 Acid Green", -5910705));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG09 Lettuce Green", -8207002));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG11 Mignonette", -1707824));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG13 Chartreuse", -2824801));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG17 Grass Green", -9256618));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG21 Anise", -526658));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG23 New Leaf", -1643633));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG25 Celadon Green", -3088005));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG41 Pale Cobalt Green", -2757676));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG45 Cobalt Green", -4924233));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG63 Pea Green", -6239582));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG67 Moss", -8274036));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG91 Putty", -2435154));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG95 Pale Olive", -3422626));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG97 Spanish Olive", -6975741));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YG99 Marine Green", -11638251));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YR00 Powder Pink", -76099));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YR02 Light Orange", -205627));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YR04 Chrome Orange", -81047));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YR07 Cadmium Orange", -889031));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YR09 Chinese Orange", -961244));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YR14 Caramel", -79794));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YR16 Apricot", -84183));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YR18 Sanguine", -890052));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YR21 Cream", -664143));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YR23 Yellow Ochre", -1257589));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Original", "YR24 Pale Sepia", -995484));

        //COPIC SKETCH PENS
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "0 Colorless Blender", -1));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "100 Black", -13554901));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "110 Special Black", -16578808));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B0000 Pale Celestine", -984578));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B000 Pale Porcelain Blue", -1641227));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B00 Frost Blue", -2232076));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B01 Mint Blue", -2691342));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B02 Robin&#39;s Egg Blue", -4987919));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B04 Tahitian Blue", -9187354));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B05 Process Blue", -12532250));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B06 Peacok Blue", -16731162));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B12 Ice Blue", -3610896));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B14 Light Blue", -9318421));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B16 Cyanine Blue", -16728854));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B18 Lapis Lazuli", -14841141));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B21 Baby Blue", -2363911));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B23 Phthalo Blue", -7159064));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B24 Sky", -7680269));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B26 Cobalt Blue", -10112029));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B28 Royal Blue", -15110730));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B29 Ultramarine", -16681023));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B32 Pale Blue", -1904649));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B34 Manganese Blue", -8207379));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B37 Antwerp Blue", -15372380));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B39 Prussian Blue", -13933399));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B41 Powder Blue", -1904389));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B45 Smoky Blue", -9060118));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B52 Soft Greenish Blue", -5386788));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B60 Pale Blue Gray", -2432525));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B63 Light Hydrangea", -5784608));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B66 Clematis", -9926459));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B69 Stratospheric Blue", -14588498));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B79 Iris", -12892259));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B91 Pale Grayish Blue", -2759957));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B93 Light Crockery Blue", -6962726));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B95 Light Grayish Cobalt", -9132090));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B97 Night Blue", -12223846));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "B99 Agate", -15772546));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG0000 Snow Green", -1050381));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG000 Pale Aqua", -1706771));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG01 Aqua Blue", -3676422));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG02 New Blue", -3741462));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG05 Holiday Blue", -8138015));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG07 Petroleum Blue", -14829362));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG09 Blue Green", -16666167));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG10 Cool Shadow", -2297617));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG11 Moon White", -3216399));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG13 Mint Green", -3872791));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG15 Aqua", -6235694));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG18 Teal Blue", -13123408));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG23 Coral Sea", -4332067));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG32 Aqua Mint", -4398377));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG34 Horizon Green", -6038825));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG45 Nile Blue", -5251105));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG49 Duck Blue", -16730439));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG53 Ice Mint", -5451823));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG57 Sketch Jasper", -10174786));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG70 Ocean Mist", -2429714));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG72 Ice Ocean", -9127749));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG75 Abyss Green", -10907250));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG78 Bronze", -11964309));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG90 Sketch Gray Sky", -1511961));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG93 Green Gray", -4537927));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG96 Bush", -8281455));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BG99 Flagstone Blue", -9528441));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BV0000 Sketch Pale Thistle", -1382414));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BV000 Iridescent Mauve", -1382414));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BV00 Mauve Shadow", -2040595));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BV01 Viola", -3880474));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BV02 Prune", -5588773));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BV04 Blue Berry", -8611890));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BV08 Blue Violet", -6455623));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BV11 Soft Violet", -2829592));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BV13 Hydrangea Blue", -8089144));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BV17 Deep Reddish Blue", -9534275));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BV20 Dull Lavender", -3154959));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BV23 Grayish Lavender", -5127971));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BV25 Grayish Violet", -8289113));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BV29 Slate", -13089448));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BV31 Pale Lavender", -1382414));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "BV34 Sketch Bluebell", -6314052));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "C00 Cool Gray", -1511181));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "C0 Cool Gray", -2037779));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "C10 Cool Gray", -14669007));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "C1 Cool Gray", -2432024));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "C2 Cool Gray", -3352611));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "C3 Cool Gray", -4076334));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "C4 Cool Gray", -5851971));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "C5 Cool Gray", -7167829));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "C6 Cool Gray", -8680298));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "C7 Cool Gray", -10260359));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "C8 Cool Gray", -11313818));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "C9 Cool Gray", -12826803));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E0000 Floral White", -1292));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E000 Pale Fruit Pink", -68114));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E00 Cotton Pearl", -134166));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E01 Pink Flamingo", -4380));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E02 Fruit Pink", -70432));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E04 Lipstick Natural", -1786684));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E07 Light Mahogany", -3374742));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E08 Brown", -3513005));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E09 Burnt Sienna", -2528689));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E11 Barley Beige", -71210));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E13 Light Suntan", -1456721));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E15 Dark Suntan", -279667));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E17 Reddish Brass", -4694185));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E18 Copper", -7842995));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E19 Redwood", -3911112));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E21 Soft Sun", -138553));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E23 Hazelnut", -1258831));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E25 Caribe Cocoa", -2972542));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E27 Milk Chocolate", -6719901));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E29 Burnt Umber", -7846346));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E30 Bisque", -528170));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E31 Brick Beige", -858418));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E33 Sand", -798031));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E34 Toast", -996698));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E35 Chamois", -1653853));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E37 Sepia", -3370663));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E39 Leather", -3836865));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E40 Brick White", -857892));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E41 Pearl White", -69151));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E42 Sand White", -791847));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E43 Dull Ivory", -1516867));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E44 Clay", -3819095));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E47 Dark Brown", -7704999));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E49 Dark Bark", -10269636));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E50 Egg Shell", -726032));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E51 Milky White", -70442));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E53 Raw Silk", -792893));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E55 Light Camel", -925767));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E57 Light Walnut", -5143208));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E59 Walnut", -6652052));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E70 Ash Rose", -1053978));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E71 Champagne", -1910829));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E74 Cocoa Brown", -6192004));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E77 Maroon", -8429490));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E79 Cashew", -11916254));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E81 Ivory", -989502));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E84 - Sketch Khaki", -5333120));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E87 Fig", -9478067));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E89 - Sketch Pecan", -10860231));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E93 Tea Rose", -77127));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E95 Tea Orange", -213890));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E97 Deep Orange", -1205155));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "E99 Baked Clay", -4956108));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "FB2 Fluorescent Dull Blue", -16412720));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "FBG2 Fluorescent Dull Blue Green", -10302488));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "FRV1 Fluorescent Pink", -678969));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "FV2 Fluorescent Dull Violet", -8424266));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "FY1 Fluorescent Yellow Orange", -2409));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "FYG1 Fluorescent Yellow", -6369981));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "FYG2 Fluorescent Dull Yellow Green", -6369981));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "FYR1 Fluorescent Orange", -78695));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G0000 Crystal Opal", -919565));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G000 Pale Green", -1378835));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G00 Jade Green", -1838355));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G02 Spectrum Green", -3151661));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G03 Meadow Green", -4793700));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G05 Emerald Green", -9846661));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G07 Nile Green", -8665738));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G09 Veronese Green", -8731547));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G12 Sea Green", -2955068));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G14 Apple Green", -6828144));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G16 Malachite", -10436200));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G17 Forest Green", -15420547));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G19 Bright Parrot Green", -13780598));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G20 Wax White", -1181989));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G21 Lime Green", -3873587));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G24 Willow", -3940172));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G28 Ocean Green", -15625118));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G29 Pine Tree Green", -15106979));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G40 Dim Green", -1773089));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G43 Various Pistachio", -2627672));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G46 Sketch Mistletoe", -11035020));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G82 Spring Dim Green", -3351879));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G85 Verdigris", -6437974));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G94 Grayish Olive", -6772858));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "G99 Olive", -10518982));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "N0 Neutral Gray", -1249555));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "N10 Neutral Gray", -13553872));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "N1 Neutral Gray", -1907739));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "N2 Neutral Gray", -2434083));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "N3 Neutral Gray", -3026220));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "N4 Neutral Gray", -4407871));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "N5 Neutral Gray", -5723731));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "N6 Neutral Gray", -7039591));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "N7 Neutral Gray", -8947588));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "N8 Neutral Gray", -10263450));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "N9 Neutral Gray", -11776689));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R0000 Pink Beryl", -68625));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R000 Cherry White", -69401));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R00 Pinkish White", -70943));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R01 Pinkish Vanilla", -139048));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R02 Rose Salmon", -142393));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R05 Salmon Red", -617861));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R08 Vermilion", -891052));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R11 Pale Cherry Pink", -138795));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R12 Light Tea Rose", -207935));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R14 Light Rouge", -681070));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R17 Lipstick Orange", -752532));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R20 Blush", -206897));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R21 Sardonyx", -343626));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R22 Light Prawn", -477263));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R24 Prawn", -887431));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R27 Cadmium Red", -962462));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R29 Lipstick Red", -1239221));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R30 Pale Yellowish Pink", -203809));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R32 Peach", -343622));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R35 Coral", -888443));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R37 Carmine", -1545100));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R39 Garnet", -3454854));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R43 Bougainvillaea", -1145714));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R46 Strong Red", -2077335));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R56 Currant", -2982763));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R59 Cardinal", -4763792));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R81 Rose Pink", -931626));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R83 Rose Mist", -942919));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R85 Rose Red", -2921837));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "R89 Dark Red", -8574142));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV0000 Evening Primrose", -857355));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV000 Pale Purple", -728338));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV00 Water Lily", -926998));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV02 Sugared Almond Pink", -338458));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV04 Shock  Pink", -613441));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV06 Cerise", -817489));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV09 Fuchsia", -2002516));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV10 Pale Pink", -135948));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV11 Pink", -272675));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV13 Tender Pink", -407081));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV14 Begonia Pink", -748105));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV17 Deep Magenta", -2392397));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV19 Red Violet", -2987862));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV21 Light Pink", -136985));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV23 Pure Pink", -476471));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV25 Dog Rose Flower", -748610));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV29 Crimson", -1095552));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV32 Shadow Pink", -338994));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV34 Dark Pink", -413778));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV42 Salmon Pink", -476234));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV52 Various Cotton Candy", -406818));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV55 Hollyhock", -1464886));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV63 Begonia", -3105362));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV66 Raspberry", -4691324));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV69 Peony", -7645330));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV91 Garyish Cherry", -1649438));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV93 Smokey Purple", -1591604));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV95 Baby Blossoms", -4815711));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "RV99 Argyle Purple", -10860456));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "T0 Toner Gray", -1249555));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "T10 Toner Gray", -13488595));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "T1 Toner Gray", -1381656));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "T2 Toner Gray", -2039586));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "T3 Toner Gray", -3026228));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "T4 Toner Gray", -4408391));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "T5 Toner Gray", -5724253));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "T6 Toner Gray", -7039600));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "T7 Toner Gray", -8948108));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "T8 Toner Gray", -10263457));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "T9 Toner Gray", -11777207));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V0000 Rose Quartz", -987658));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V000 Pale Heath", -1448461));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V01 Heath", -1785383));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V04 Lilac", -1660210));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V05 Azalea", -1923382));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V06 Lavender", -3238462));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V09 Violet", -7908191));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V12 Pale Lilac", -1124375));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V15 Mallow", -2906419));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V17 Amethyst", -6253881));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V20 Wisteria", -1908499));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V22 Sketch Ash Lavender", -5066288));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V25 Pale Blackberry", -8028243));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V28 Sketch Eggplant", -9738610));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V91 Pale Grape", -1522480));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V93 Early Grape", -1719845));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V95 Light Grape", -4752216));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "V99 Aubergine", -11386024));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "W00 Warm Gray", -789525));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "W0 Warm Gray", -1250076));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "W10 Warm Gray", -13619413));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "W1 Warm Gray", -1579041));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "W2 Warm Gray", -2236971));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "W3 Warm Gray", -2960694));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "W4 Warm Gray", -4407881));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "W5 Warm Gray", -5723740));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "W6 Warm Gray", -7039601));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "W7 Warm Gray", -8947597));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "W8 Warm Gray", -10263457));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "W9 Warm Gray", -11776696));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y0000 Yellow Fluorite", -65804));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y000 Pale Lemon", -791));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y00 Barium Yellow", -66081));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y02 Canary Yellow", -593002));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y04 Acacia", -1186474));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y06 Yellow", -68244));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y08 Acid Yellow", -69120));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y11 Pale Yellow", -1076));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y13 Lemon Yellow", -264274));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y15 Cadmium Yellow", -71316));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y17 Golden Yellow", -7083));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y18 Lightning Yellow", -70315));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y19 Napoli Yellow", -5826));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y21 Buttercup Yellow", -4414));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y23 Yellowish Beige", -269389));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y26 Mustard", -991897));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y28 Lionet Gold", -3495831));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y32 Cashmere", -401728));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y35 Maize", -10119));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "Y38 Honey", -11404));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG0000 Lily White", -854048));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG00 Mimosa Yellow", -1644898));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG01 Green Bice", -1905742));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG03 Yellow Green", -2168150));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG05 Salad", -2693742));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG06 Yellowish Green", -3874926));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG07 Acid Green", -5910705));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG09 Lettuce Green", -8207002));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG11 Mignonette", -1707824));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG13 Chartreuse", -2824801));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG17 Grass Green", -9256618));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG21 Anise", -526658));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG23 New Leaf", -1643633));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG25 Celadon Green", -3088005));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG41 Pale Cobalt Green", -2757676));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG45 Cobalt Green", -4924233));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG61 Pale Moss", -2692650));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG63 Pea Green", -6239582));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG67 Moss", -8274036));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG91 Putty", -2435154));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG93 Grayish Yellow", -2960740));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG95 Pale Olive", -3422626));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG97 Spanish Olive", -6975741));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YG99 Marine Green", -11638251));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR0000 Pale Chiffon", -3099));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR000 Silk", -70440));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR00 Powder Pink", -76099));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR01 Peach Puff", -75070));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR02 Light Orange", -205627));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR04 Chrome Orange", -81047));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR07 Cadmium Orange", -889031));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR09 Chinese Orange", -961244));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR12 Loquat", -7514));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR14 Caramel", -79794));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR15 Pumpkin Yellow", -280444));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR16 Apricot", -84183));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR18 Sanguine", -890052));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR20 Yellowish Shade", -7745));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR21 Cream", -664143));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR23 Yellow Ochre", -1257589));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR24 Pale Sepia", -995484));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR27 Tuscan Orange", -2791880));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR30 Macadamia Nut", -68902));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR31 Light Reddish Yellow", -8536));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR61 Spring Orange", -140604));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR65 Atoll", -348576));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR68 Orange", -823262));
        MasterRegistry.INSTANCE.registerDrawingPen(new DrawingPen("Copic Sketch", "YR82 Mellow Peach", -145779));
    }

    @Override
    public void registerPenSets() {
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Dark Greys", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:N10 Neutral Gray", "Copic Original:N8 Neutral Gray", "Copic Original:N6 Neutral Gray", "Copic Original:N4 Neutral Gray", "Copic Original:N2 Neutral Gray"})));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Light Greys", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:N7 Neutral Gray", "Copic Original:N5 Neutral Gray", "Copic Original:N3 Neutral Gray", "Copic Original:N2 Neutral Gray"})));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Warm Greys", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:W10 Warm Gray", "Copic Original:W8 Warm Gray", "Copic Original:W6 Warm Gray", "Copic Original:W4 Warm Gray", "Copic Original:W2 Warm Gray"})));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Cool Greys", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:C10 Cool Gray", "Copic Original:C8 Cool Gray", "Copic Original:C6 Cool Gray", "Copic Original:C4 Cool Gray", "Copic Original:C2 Cool Gray"})));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Mixed Greys 1", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:C7 Cool Gray", "Copic Original:W5 Warm Gray", "Copic Original:C3 Cool Gray", "Copic Original:W2 Warm Gray"})));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Mixed Greys 2", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:W7 Warm Gray", "Copic Original:C5 Cool Gray", "Copic Original:W3 Warm Gray", "Copic Original:C2 Cool Gray"})));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Browns 1", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:E49 Dark Bark", "Copic Original:E27 Milk Chocolate", "Copic Original:E13 Light Suntan", "Copic Original:E00 Cotton Pearl"})));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Browns 2", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:E49 Dark Bark", "Copic Original:E27 Milk Chocolate", "Copic Original:N4 Neutral Gray", "Copic Original:N2 Neutral Gray"})));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Dark Grey Browns 1", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:E49 Dark Bark", "Copic Original:E27 Milk Chocolate", "Copic Original:E13 Light Suntan", "Copic Original:N2 Neutral Gray"})));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Dark Grey Browns 2", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:E49 Dark Bark", "Copic Original:N6 Neutral Gray", "Copic Original:N4 Neutral Gray", "Copic Original:N2 Neutral Gray"})));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Dark Grey Blues", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:B37 Antwerp Blue", "Copic Original:N6 Neutral Gray", "Copic Original:N4 Neutral Gray", "Copic Original:N2 Neutral Gray"})));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Dark Grey Red", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:R59 Cardinal", "Copic Original:N6 Neutral Gray", "Copic Original:N4 Neutral Gray", "Copic Original:N2 Neutral Gray"})));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Dark Grey Violet", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:G29 Pine Tree Green", "Copic Original:N6 Neutral Gray", "Copic Original:N4 Neutral Gray", "Copic Original:N2 Neutral Gray"})));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Dark Grey Orange", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:YR09 Chinese Orange", "Copic Original:N6 Neutral Gray", "Copic Original:N4 Neutral Gray", "Copic Original:N2 Neutral Gray"})));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Blue Green", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:B39 Prussian Blue", "Copic Original:G28 Ocean Green", "Copic Original:B26 Cobalt Blue", "Copic Original:G14 Apple Green"})));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Blue Purples", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:B39 Prussian Blue", "Copic Original:V09 Violet", "Copic Original:B02 Robin's Egg Blue", "Copic Original:V04 Lilac"})));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Reds", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:R29 Lipstick Red", "Copic Original:R27 Cadmium Red", "Copic Original:R24 Prawn", "Copic Original:R20 Blush"})));
        MasterRegistry.INSTANCE.registerDrawingSet(new DrawingSet("Copic", "Yellow Green", MasterRegistry.INSTANCE.getDrawingPensFromRegistryNames(new String[]{"Copic Original:100 Black", "Copic Original:E29 Burnt Umber", "Copic Original:YG99 Marine Green", "Copic Original:Y17 Golden Yellow", "Copic Original:YG03 Yellow Green", "Copic Original:Y11 Pale Yellow"})));

    }

    /*
    public int get_sketch_color(String pen) {
        return sketch_color.get(pen);
    }

    public int get_original_color(String pen) {
        return original_color.get(pen);
    }

    public String get_sketch_name(String pen) {
        return sketch_name.get(pen);
    }

    public String get_original_name(String pen) {
        return original_name.get(pen);
    }

    public String get_closest_original(int c1) {
        //http://stackoverflow.com/questions/1847092/given-an-rgb-value-what-would-be-the-best-way-to-find-the-closest-match-in-the-d
        //https://en.wikipedia.org/wiki/Color_difference

        float r1 = app.red(c1);
        float g1 = app.green(c1);
        float b1 = app.blue(c1);

        float closest_value = 99999999999999999999999999.0F;
        String closest_pen = "";

        for (Map.Entry me : original_color.entrySet()) {
            //println(me.getKey() + " is " + me.getValue());

            int c2 = (int)me.getValue();
            float r2 = app.red(c2);
            float g2 = app.green(c2);
            float b2 = app.blue(c2);

            float d = sq((r2-r1)*0.30F) + sq((g2-g1)*0.59F) + sq((b2-b1)*0.11F);
            if (d<closest_value) {
                closest_value = d;
                closest_pen = (String)me.getKey();
            }
        }
        return closest_pen;
    }

    public String get_closest_sketch(int c1) {
        //http://stackoverflow.com/questions/1847092/given-an-rgb-value-what-would-be-the-best-way-to-find-the-closest-match-in-the-d
        //https://en.wikipedia.org/wiki/Color_difference

        float r1 = app.red(c1);
        float g1 = app.green(c1);
        float b1 = app.blue(c1);

        float closest_value = 99999999999999999999999999.0F;
        String closest_pen = "";

        for (Map.Entry me : sketch_color.entrySet()) {
            //println(me.getKey() + " is " + me.getValue());

            int c2 = (int)me.getValue();
            float r2 = app.red(c2);
            float g2 = app.green(c2);
            float b2 = app.blue(c2);

            float d = sq(abs((r2-r1))*0.30F) + sq(abs((g2-g1))*0.59F) + sq(abs((b2-b1))*0.11F);
            //float d = sq((r2-r1)*0.30) + sq((g2-g1)*0.59) + sq((b2-b1)*0.11);
            if (d<closest_value) {
                closest_value = d;
                closest_pen = (String)me.getKey();
            }
        }
        return closest_pen;
    }



    public void copic_alpha_simulator() {
        int[] p = new int[5];
        p[0] = app.copic.get_original_color("N1");
        p[1] = app.copic.get_original_color("N3");
        p[2] = app.copic.get_original_color("N5");
        p[3] = app.copic.get_original_color("N7");
        p[4] = app.copic.get_original_color("100");

        int alpha = 210;
        int pen_off=200;
        int off=30;

        for (int pen=0; pen<5; pen++) {
            for (int x=0; x<5; x++) {
                //fill(p[pen], alpha);  rect(pen*150+10, pen*off+x*pen_off, 500, 80);
                app.stroke(p[pen], alpha);
                app.strokeWeight(50);
                app.fill(p[4], 50);
                app.line(pen*150+10, pen*off+x*pen_off, pen*150+10+500, pen*off+x*pen_off);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void test_draw_closest_copic_color() {
        app.noStroke();

        for (int i = 0; i < 255; i++) {
            app.colorMode(HSB, 255);
            int c1 = i, app.mouseX, app.mouseY);
            //color c1 = color(mouseX, i, mouseY);
            //color c1 = color(mouseX, mouseY, i);
            app.fill(c1);
            app.rect(i*5, 0, 5, 100);

            String p = app.copic.get_closest_original(c1);
            //println(p + "   " + c.get_original_name(p));
            int c2 = app.copic.get_original_color(p);
            app.fill(c2);
            app.rect(i*5, 105, 5, 100);
        }
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////
    */
}
