package drawingbot.drawing;

/*Regex used in sublime to clean up html from:
Source data:  https://imaginationinternationalinc.com/copic/store/color-picker/
^.*color:
; cursor.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*<h5>
</h5>\n.*<p>
</p>\n.*clearfix.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n.*\n
^(.*?),(.*?),(.*)
sketch_color.put("$2"), color($1));  sketch_name.put("$2"), "$3"));
*/
import drawingbot.DrawingBotV3;

public class CopicPenPlugin {

    public static DrawingBotV3 app = DrawingBotV3.INSTANCE;

    public static void registerPens(DrawingRegistry registry) {
        //COPIC SKETCH PENS
        registry.registerDrawingPen(new DrawingPen("Copic Sketch 0 Colorless Blender", app.color(-1)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch 100 Black", app.color(-13554901)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch 110 Special Black", app.color(-16578808)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B0000 Pale Celestine", app.color(-984578)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B000 Pale Porcelain Blue", app.color(-1641227)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B00 Frost Blue", app.color(-2232076)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B01 Mint Blue", app.color(-2691342)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B02 Robin&#39;s Egg Blue", app.color(-4987919)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B04 Tahitian Blue", app.color(-9187354)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B05 Process Blue", app.color(-12532250)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B06 Peacok Blue", app.color(-16731162)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B12 Ice Blue", app.color(-3610896)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B14 Light Blue", app.color(-9318421)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B16 Cyanine Blue", app.color(-16728854)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B18 Lapis Lazuli", app.color(-14841141)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B21 Baby Blue", app.color(-2363911)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B23 Phthalo Blue", app.color(-7159064)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B24 Sky", app.color(-7680269)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B26 Cobalt Blue", app.color(-10112029)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B28 Royal Blue", app.color(-15110730)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B29 Ultramarine", app.color(-16681023)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B32 Pale Blue", app.color(-1904649)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B34 Manganese Blue", app.color(-8207379)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B37 Antwerp Blue", app.color(-15372380)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B39 Prussian Blue", app.color(-13933399)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B41 Powder Blue", app.color(-1904389)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B45 Smoky Blue", app.color(-9060118)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B52 Soft Greenish Blue", app.color(-5386788)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B60 Pale Blue Gray", app.color(-2432525)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B63 Light Hydrangea", app.color(-5784608)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B66 Clematis", app.color(-9926459)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B69 Stratospheric Blue", app.color(-14588498)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B79 Iris", app.color(-12892259)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B91 Pale Grayish Blue", app.color(-2759957)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B93 Light Crockery Blue", app.color(-6962726)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B95 Light Grayish Cobalt", app.color(-9132090)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B97 Night Blue", app.color(-12223846)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch B99 Agate", app.color(-15772546)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG0000 Snow Green", app.color(-1050381)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG000 Pale Aqua", app.color(-1706771)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG01 Aqua Blue", app.color(-3676422)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG02 New Blue", app.color(-3741462)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG05 Holiday Blue", app.color(-8138015)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG07 Petroleum Blue", app.color(-14829362)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG09 Blue Green", app.color(-16666167)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG10 Cool Shadow", app.color(-2297617)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG11 Moon White", app.color(-3216399)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG13 Mint Green", app.color(-3872791)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG15 Aqua", app.color(-6235694)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG18 Teal Blue", app.color(-13123408)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG23 Coral Sea", app.color(-4332067)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG32 Aqua Mint", app.color(-4398377)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG34 Horizon Green", app.color(-6038825)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG45 Nile Blue", app.color(-5251105)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG49 Duck Blue", app.color(-16730439)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG53 Ice Mint", app.color(-5451823)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG57 Sketch Jasper", app.color(-10174786)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG70 Ocean Mist", app.color(-2429714)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG72 Ice Ocean", app.color(-9127749)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG75 Abyss Green", app.color(-10907250)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG78 Bronze", app.color(-11964309)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG90 Sketch Gray Sky", app.color(-1511961)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG93 Green Gray", app.color(-4537927)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG96 Bush", app.color(-8281455)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BG99 Flagstone Blue", app.color(-9528441)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BV0000 Sketch Pale Thistle", app.color(-1382414)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BV000 Iridescent Mauve", app.color(-1382414)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BV00 Mauve Shadow", app.color(-2040595)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BV01 Viola", app.color(-3880474)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BV02 Prune", app.color(-5588773)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BV04 Blue Berry", app.color(-8611890)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BV08 Blue Violet", app.color(-6455623)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BV11 Soft Violet", app.color(-2829592)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BV13 Hydrangea Blue", app.color(-8089144)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BV17 Deep Reddish Blue", app.color(-9534275)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BV20 Dull Lavender", app.color(-3154959)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BV23 Grayish Lavender", app.color(-5127971)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BV25 Grayish Violet", app.color(-8289113)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BV29 Slate", app.color(-13089448)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BV31 Pale Lavender", app.color(-1382414)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch BV34 Sketch Bluebell", app.color(-6314052)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch C00 Cool Gray", app.color(-1511181)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch C0 Cool Gray", app.color(-2037779)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch C10 Cool Gray", app.color(-14669007)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch C1 Cool Gray No. 1", app.color(-2432024)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch C2 Cool Gray", app.color(-3352611)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch C3 Cool Gray No. 3", app.color(-4076334)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch C4 Cool Gray", app.color(-5851971)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch C5 Cool Gray No. 5", app.color(-7167829)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch C6 Cool Gray", app.color(-8680298)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch C7 Cool Gray No. 7", app.color(-10260359)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch C8 Cool Gray", app.color(-11313818)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch C9 Cool Gray", app.color(-12826803)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E0000 Floral White", app.color(-1292)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E000 Pale Fruit Pink", app.color(-68114)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E00 Cotton Pearl", app.color(-134166)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E01 Pink Flamingo", app.color(-4380)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E02 Fruit Pink", app.color(-70432)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E04 Lipstick Natural", app.color(-1786684)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E07 Light Mahogany", app.color(-3374742)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E08 Brown", app.color(-3513005)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E09 Burnt Sienna", app.color(-2528689)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E11 Barley Beige", app.color(-71210)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E13 Light Suntan", app.color(-1456721)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E15 Dark Suntan", app.color(-279667)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E17 Reddish Brass", app.color(-4694185)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E18 Copper", app.color(-7842995)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E19 Redwood", app.color(-3911112)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E21 Soft Sun", app.color(-138553)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E23 Hazelnut", app.color(-1258831)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E25 Caribe Cocoa", app.color(-2972542)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E27 Milk Chocolate", app.color(-6719901)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E29 Burnt Umber", app.color(-7846346)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E30 Bisque", app.color(-528170)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E31 Brick Beige", app.color(-858418)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E33 Sand", app.color(-798031)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E34 Toast", app.color(-996698)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E35 Chamois", app.color(-1653853)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E37 Sepia", app.color(-3370663)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E39 Leather", app.color(-3836865)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E40 Brick White", app.color(-857892)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E41 Pearl White", app.color(-69151)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E42 Sand White", app.color(-791847)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E43 Dull Ivory", app.color(-1516867)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E44 Clay", app.color(-3819095)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E47 Dark Brown", app.color(-7704999)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E49 Dark Bark", app.color(-10269636)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E50 Egg Shell", app.color(-726032)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E51 Milky White", app.color(-70442)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E53 Raw Silk", app.color(-792893)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E55 Light Camel", app.color(-925767)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E57 Light Walnut", app.color(-5143208)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E59 Walnut", app.color(-6652052)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E70 Ash Rose", app.color(-1053978)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E71 Champagne", app.color(-1910829)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E74 Cocoa Brown", app.color(-6192004)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E77 Maroon", app.color(-8429490)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E79 Cashew", app.color(-11916254)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E81 Ivory", app.color(-989502)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E84 - Sketch Khaki", app.color(-5333120)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E87 Fig", app.color(-9478067)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E89 - Sketch Pecan", app.color(-10860231)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E93 Tea Rose", app.color(-77127)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E95 Tea Orange", app.color(-213890)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E97 Deep Orange", app.color(-1205155)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch E99 Baked Clay", app.color(-4956108)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch FB2 Fluorescent Dull Blue", app.color(-16412720)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch FBG2 Fluorescent Dull Blue Green", app.color(-10302488)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch FRV1 Fluorescent Pink", app.color(-678969)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch FV2 Fluorescent Dull Violet", app.color(-8424266)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch FY1 Fluorescent Yellow Orange", app.color(-2409)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch FYG1 Fluorescent Yellow", app.color(-6369981)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch FYG2 Fluorescent Dull Yellow Green", app.color(-6369981)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch FYR1 Fluorescent Orange", app.color(-78695)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G0000 Crystal Opal", app.color(-919565)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G000 Pale Green", app.color(-1378835)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G00 Jade Green", app.color(-1838355)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G02 Spectrum Green", app.color(-3151661)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G03 Meadow Green", app.color(-4793700)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G05 Emerald Green", app.color(-9846661)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G07 Nile Green", app.color(-8665738)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G09 Veronese Green", app.color(-8731547)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G12 Sea Green", app.color(-2955068)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G14 Apple Green", app.color(-6828144)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G16 Malachite", app.color(-10436200)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G17 Forest Green", app.color(-15420547)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G19 Bright Parrot Green", app.color(-13780598)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G20 Wax White", app.color(-1181989)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G21 Lime Green", app.color(-3873587)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G24 Willow", app.color(-3940172)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G28 Ocean Green", app.color(-15625118)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G29 Pine Tree Green", app.color(-15106979)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G40 Dim Green", app.color(-1773089)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G43 Various Pistachio", app.color(-2627672)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G46 Sketch Mistletoe", app.color(-11035020)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G82 Spring Dim Green", app.color(-3351879)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G85 Verdigris", app.color(-6437974)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G94 Grayish Olive", app.color(-6772858)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch G99 Olive", app.color(-10518982)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch N0 Neutral Gray", app.color(-1249555)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch N10 Neutral Gray", app.color(-13553872)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch N1 Neutral Gray", app.color(-1907739)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch N2 Neutral Gray", app.color(-2434083)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch N3 Neutral Gray", app.color(-3026220)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch N4 Neutral Gray", app.color(-4407871)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch N5 Neutral Gray", app.color(-5723731)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch N6 Neutral Gray", app.color(-7039591)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch N7 Neutral Gray", app.color(-8947588)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch N8 Neutral Gray", app.color(-10263450)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch N9 Neutral Gray", app.color(-11776689)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R0000 Pink Beryl", app.color(-68625)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R000 Cherry White", app.color(-69401)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R00 Pinkish White", app.color(-70943)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R01 Pinkish Vanilla", app.color(-139048)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R02 Rose Salmon", app.color(-142393)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R05 Salmon Red", app.color(-617861)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R08 Vermilion", app.color(-891052)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R11 Pale Cherry Pink", app.color(-138795)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R12 Light Tea Rose", app.color(-207935)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R14 Light Rouge", app.color(-681070)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R17 Lipstick Orange", app.color(-752532)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R20 Blush", app.color(-206897)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R21 Sardonyx", app.color(-343626)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R22 Light Prawn", app.color(-477263)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R24 Prawn", app.color(-887431)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R27 Cadmium Red", app.color(-962462)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R29 Lipstick Red", app.color(-1239221)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R30 Pale Yellowish Pink", app.color(-203809)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R32 Peach", app.color(-343622)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R35 Coral", app.color(-888443)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R37 Carmine", app.color(-1545100)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R39 Garnet", app.color(-3454854)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R43 Bougainvillaea", app.color(-1145714)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R46 Strong Red", app.color(-2077335)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R56 Currant", app.color(-2982763)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R59 Cardinal", app.color(-4763792)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R81 Rose Pink", app.color(-931626)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R83 Rose Mist", app.color(-942919)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R85 Rose Red", app.color(-2921837)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch R89 Dark Red", app.color(-8574142)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV0000 Evening Primrose", app.color(-857355)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV000 Pale Purple", app.color(-728338)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV00 Water Lily", app.color(-926998)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV02 Sugared Almond Pink", app.color(-338458)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV04 Shock  Pink", app.color(-613441)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV06 Cerise", app.color(-817489)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV09 Fuchsia", app.color(-2002516)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV10 Pale Pink", app.color(-135948)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV11 Pink", app.color(-272675)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV13 Tender Pink", app.color(-407081)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV14 Begonia Pink", app.color(-748105)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV17 Deep Magenta", app.color(-2392397)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV19 Red Violet", app.color(-2987862)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV21 Light Pink", app.color(-136985)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV23 Pure Pink", app.color(-476471)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV25 Dog Rose Flower", app.color(-748610)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV29 Crimson", app.color(-1095552)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV32 Shadow Pink", app.color(-338994)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV34 Dark Pink", app.color(-413778)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV42 Salmon Pink", app.color(-476234)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV52 Various Cotton Candy", app.color(-406818)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV55 Hollyhock", app.color(-1464886)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV63 Begonia", app.color(-3105362)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV66 Raspberry", app.color(-4691324)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV69 Peony", app.color(-7645330)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV91 Garyish Cherry", app.color(-1649438)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV93 Smokey Purple", app.color(-1591604)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV95 Baby Blossoms", app.color(-4815711)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch RV99 Argyle Purple", app.color(-10860456)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch T0 Toner Gray", app.color(-1249555)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch T10 Toner Gray", app.color(-13488595)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch T1 Toner Gray", app.color(-1381656)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch T2 Toner Gray", app.color(-2039586)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch T3 Toner Gray", app.color(-3026228)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch T4 Toner Gray", app.color(-4408391)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch T5 Toner Gray", app.color(-5724253)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch T6 Toner Gray", app.color(-7039600)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch T7 Toner Gray", app.color(-8948108)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch T8 Toner Gray", app.color(-10263457)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch T9 Toner Gray", app.color(-11777207)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V0000 Rose Quartz", app.color(-987658)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V000 Pale Heath", app.color(-1448461)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V01 Heath", app.color(-1785383)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V04 Lilac", app.color(-1660210)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V05 Azalea", app.color(-1923382)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V06 Lavender", app.color(-3238462)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V09 Violet", app.color(-7908191)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V12 Pale Lilac", app.color(-1124375)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V15 Mallow", app.color(-2906419)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V17 Amethyst", app.color(-6253881)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V20 Wisteria", app.color(-1908499)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V22 Sketch Ash Lavender", app.color(-5066288)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V25 Pale Blackberry", app.color(-8028243)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V28 Sketch Eggplant", app.color(-9738610)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V91 Pale Grape", app.color(-1522480)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V93 Early Grape", app.color(-1719845)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V95 Light Grape", app.color(-4752216)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch V99 Aubergine", app.color(-11386024)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch W00 Warm Gray", app.color(-789525)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch W0 Warm Gray", app.color(-1250076)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch W10 Warm Gray", app.color(-13619413)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch W1 Warm Gray No. 1", app.color(-1579041)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch W2 Warm Gray", app.color(-2236971)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch W3 Warm Gray No. 3", app.color(-2960694)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch W4 Warm Gray", app.color(-4407881)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch W5 Warm Gray No. 5", app.color(-5723740)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch W6 Warm Gray", app.color(-7039601)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch W7 Warm Gray No. 7", app.color(-8947597)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch W8 Warm Gray", app.color(-10263457)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch W9 Warm Gray", app.color(-11776696)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y0000 Yellow Fluorite", app.color(-65804)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y000 Pale Lemon", app.color(-791)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y00 Barium Yellow", app.color(-66081)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y02 Canary Yellow", app.color(-593002)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y04 Acacia", app.color(-1186474)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y06 Yellow", app.color(-68244)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y08 Acid Yellow", app.color(-69120)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y11 Pale Yellow", app.color(-1076)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y13 Lemon Yellow", app.color(-264274)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y15 Cadmium Yellow", app.color(-71316)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y17 Golden Yellow", app.color(-7083)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y18 Lightning Yellow", app.color(-70315)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y19 Napoli Yellow", app.color(-5826)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y21 Buttercup Yellow", app.color(-4414)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y23 Yellowish Beige", app.color(-269389)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y26 Mustard", app.color(-991897)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y28 Lionet Gold", app.color(-3495831)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y32 Cashmere", app.color(-401728)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y35 Maize", app.color(-10119)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch Y38 Honey", app.color(-11404)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG0000 Lily White", app.color(-854048)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG00 Mimosa Yellow", app.color(-1644898)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG01 Green Bice", app.color(-1905742)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG03 Yellow Green", app.color(-2168150)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG05 Salad", app.color(-2693742)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG06 Yellowish Green", app.color(-3874926)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG07 Acid Green", app.color(-5910705)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG09 Lettuce Green", app.color(-8207002)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG11 Mignonette", app.color(-1707824)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG13 Chartreuse", app.color(-2824801)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG17 Grass Green", app.color(-9256618)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG21 Anise", app.color(-526658)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG23 New Leaf", app.color(-1643633)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG25 Celadon Green", app.color(-3088005)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG41 Pale Cobalt Green", app.color(-2757676)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG45 Cobalt Green", app.color(-4924233)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG61 Pale Moss", app.color(-2692650)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG63 Pea Green", app.color(-6239582)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG67 Moss", app.color(-8274036)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG91 Putty", app.color(-2435154)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG93 Grayish Yellow", app.color(-2960740)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG95 Pale Olive", app.color(-3422626)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG97 Spanish Olive", app.color(-6975741)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YG99 Marine Green", app.color(-11638251)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR0000 Pale Chiffon", app.color(-3099)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR000 Silk", app.color(-70440)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR00 Powder Pink", app.color(-76099)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR01 Peach Puff", app.color(-75070)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR02 Light Orange", app.color(-205627)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR04 Chrome Orange", app.color(-81047)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR07 Cadmium Orange", app.color(-889031)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR09 Chinese Orange", app.color(-961244)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR12 Loquat", app.color(-7514)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR14 Caramel", app.color(-79794)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR15 Pumpkin Yellow", app.color(-280444)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR16 Apricot", app.color(-84183)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR18 Sanguine", app.color(-890052)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR20 Yellowish Shade", app.color(-7745)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR21 Cream", app.color(-664143)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR23 Yellow Ochre", app.color(-1257589)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR24 Pale Sepia", app.color(-995484)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR27 Tuscan Orange", app.color(-2791880)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR30 Macadamia Nut", app.color(-68902)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR31 Light Reddish Yellow", app.color(-8536)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR61 Spring Orange", app.color(-140604)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR65 Atoll", app.color(-348576)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR68 Orange", app.color(-823262)));
        registry.registerDrawingPen(new DrawingPen("Copic Sketch YR82 Mellow Peach", app.color(-145779)));


        //COPIC ORIGINAL PENS
        registry.registerDrawingPen(new DrawingPen("Copic Original 0 Colorless Blender", app.color(-1)));
        registry.registerDrawingPen(new DrawingPen("Copic Original 100 Black", app.color(-13554901)));
        registry.registerDrawingPen(new DrawingPen("Copic Original 110 Special Black", app.color(-16578808)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B00 Frost Blue", app.color(-2232076)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B01 Mint Blue", app.color(-2691342)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B02 Robin's Egg Blue", app.color(-4987919)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B04 Tahitian Blue", app.color(-9187354)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B05 Process Blue", app.color(-12532250)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B06 Peacok Blue", app.color(-16731162)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B12 Ice Blue", app.color(-3610896)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B14 Light Blue", app.color(-9318421)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B16 Cyanine Blue", app.color(-16728854)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B18 Lapis Lazuli", app.color(-14841141)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B21 Baby Blue", app.color(-2363911)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B23 Phthalo Blue", app.color(-7159064)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B24 Sky", app.color(-7680269)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B26 Cobalt Blue", app.color(-10112029)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B29 Ultramarine", app.color(-16681023)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B32 Pale Blue", app.color(-1904649)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B34 Manganese Blue", app.color(-8207379)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B37 Antwerp Blue", app.color(-15372380)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B39 Prussian Blue", app.color(-13933399)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B41 Powder Blue", app.color(-1904389)));
        registry.registerDrawingPen(new DrawingPen("Copic Original B45 Smoky Blue", app.color(-9060118)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BG02 New Blue", app.color(-3741462)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BG05 Holiday Blue", app.color(-8138015)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BG09 Blue Green", app.color(-16666167)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BG10 Cool Shadow", app.color(-2297617)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BG11 Moon White", app.color(-3216399)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BG13 Mint Green", app.color(-3872791)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BG15 Aqua", app.color(-6235694)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BG18 Teal Blue", app.color(-13123408)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BG32 Aqua Mint", app.color(-4398377)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BG34 Horizon Green", app.color(-6038825)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BG45 Nile Blue", app.color(-5251105)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BG49 Duck Blue", app.color(-16730439)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BG99 Flagstone Blue", app.color(-9528441)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BV00 Mauve Shadow", app.color(-2040595)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BV04 Blue Berry", app.color(-8611890)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BV08 Blue Violet", app.color(-6455623)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BV23 Grayish Lavender", app.color(-5127971)));
        registry.registerDrawingPen(new DrawingPen("Copic Original BV31 Pale Lavender", app.color(-1382414)));
        registry.registerDrawingPen(new DrawingPen("Copic Original C0 Cool Gray", app.color(-2037779)));
        registry.registerDrawingPen(new DrawingPen("Copic Original C10 Cool Gray", app.color(-14669007)));
        registry.registerDrawingPen(new DrawingPen("Copic Original C1 Cool Gray No. 1", app.color(-2432024)));
        registry.registerDrawingPen(new DrawingPen("Copic Original C2 Cool Gray", app.color(-3352611)));
        registry.registerDrawingPen(new DrawingPen("Copic Original C3 Cool Gray No. 3", app.color(-4076334)));
        registry.registerDrawingPen(new DrawingPen("Copic Original C4 Cool Gray", app.color(-5851971)));
        registry.registerDrawingPen(new DrawingPen("Copic Original C5 Cool Gray No. 5", app.color(-7167829)));
        registry.registerDrawingPen(new DrawingPen("Copic Original C6 Cool Gray", app.color(-8680298)));
        registry.registerDrawingPen(new DrawingPen("Copic Original C7 Cool Gray No. 7", app.color(-10260359)));
        registry.registerDrawingPen(new DrawingPen("Copic Original C8 Cool Gray", app.color(-11313818)));
        registry.registerDrawingPen(new DrawingPen("Copic Original C9 Cool Gray", app.color(-12826803)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E00 Cotton Pearl", app.color(-134166)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E02 Fruit Pink", app.color(-70432)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E04 Lipstick Natural", app.color(-1786684)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E07 Light Mahogany", app.color(-3374742)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E09 Burnt Sienna", app.color(-2528689)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E11 Barley Beige", app.color(-71210)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E13 Light Suntan", app.color(-1456721)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E15 Dark Suntan", app.color(-279667)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E19 Redwood", app.color(-3911112)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E21 Soft Sun", app.color(-138553)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E25 Caribe Cocoa", app.color(-2972542)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E27 Milk Chocolate", app.color(-6719901)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E29 Burnt Umber", app.color(-7846346)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E31 Brick Beige", app.color(-858418)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E33 Sand", app.color(-798031)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E34 Toast", app.color(-996698)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E35 Chamois", app.color(-1653853)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E37 Sepia", app.color(-3370663)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E39 Leather", app.color(-3836865)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E40 Brick White", app.color(-857892)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E41 Pearl White", app.color(-69151)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E43 Dull Ivory", app.color(-1516867)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E44 Clay", app.color(-3819095)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E49 Dark Bark", app.color(-10269636)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E51 Milky White", app.color(-70442)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E53 Raw Silk", app.color(-792893)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E55 Light Camel", app.color(-925767)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E57 Light Walnut", app.color(-5143208)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E59 Walnut", app.color(-6652052)));
        registry.registerDrawingPen(new DrawingPen("Copic Original E77 Maroon", app.color(-8429490)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G00 Jade Green", app.color(-1838355)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G02 Spectrum Green", app.color(-3151661)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G05 Emerald Green", app.color(-9846661)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G07 Nile Green", app.color(-8665738)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G09 Veronese Green", app.color(-8731547)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G12 Sea Green", app.color(-2955068)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G14 Apple Green", app.color(-6828144)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G16 Malachite", app.color(-10436200)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G17 Forest Green", app.color(-15420547)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G19 Bright Parrot Green", app.color(-13780598)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G20 Wax White", app.color(-1181989)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G21 Lime Green", app.color(-3873587)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G24 Willow", app.color(-3940172)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G28 Ocean Green", app.color(-15625118)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G29 Pine Tree Green", app.color(-15106979)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G40 Dim Green", app.color(-1773089)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G82 Spring Dim Green", app.color(-3351879)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G85 Verdigris", app.color(-6437974)));
        registry.registerDrawingPen(new DrawingPen("Copic Original G99 Olive", app.color(-10518982)));
        registry.registerDrawingPen(new DrawingPen("Copic Original N0 Neutral Gray", app.color(-1249555)));
        registry.registerDrawingPen(new DrawingPen("Copic Original N10 Neutral Gray", app.color(-13553872)));
        registry.registerDrawingPen(new DrawingPen("Copic Original N1 Neutral Gray", app.color(-1907739)));
        registry.registerDrawingPen(new DrawingPen("Copic Original N2 Neutral Gray", app.color(-2434083)));
        registry.registerDrawingPen(new DrawingPen("Copic Original N3 Neutral Gray", app.color(-3026220)));
        registry.registerDrawingPen(new DrawingPen("Copic Original N4 Neutral Gray", app.color(-4407871)));
        registry.registerDrawingPen(new DrawingPen("Copic Original N5 Neutral Gray", app.color(-5723731)));
        registry.registerDrawingPen(new DrawingPen("Copic Original N6 Neutral Gray", app.color(-7039591)));
        registry.registerDrawingPen(new DrawingPen("Copic Original N7 Neutral Gray", app.color(-8947588)));
        registry.registerDrawingPen(new DrawingPen("Copic Original N8 Neutral Gray", app.color(-10263450)));
        registry.registerDrawingPen(new DrawingPen("Copic Original N9 Neutral Gray", app.color(-11776689)));
        registry.registerDrawingPen(new DrawingPen("Copic Original R00 Pinkish White", app.color(-70943)));
        registry.registerDrawingPen(new DrawingPen("Copic Original R02 Rose Salmon", app.color(-142393)));
        registry.registerDrawingPen(new DrawingPen("Copic Original R05 Salmon Red", app.color(-617861)));
        registry.registerDrawingPen(new DrawingPen("Copic Original R08 Vermilion", app.color(-891052)));
        registry.registerDrawingPen(new DrawingPen("Copic Original R11 Pale Cherry Pink", app.color(-138795)));
        registry.registerDrawingPen(new DrawingPen("Copic Original R17 Lipstick Orange", app.color(-752532)));
        registry.registerDrawingPen(new DrawingPen("Copic Original R20 Blush", app.color(-206897)));
        registry.registerDrawingPen(new DrawingPen("Copic Original R24 Prawn", app.color(-887431)));
        registry.registerDrawingPen(new DrawingPen("Copic Original R27 Cadmium Red", app.color(-962462)));
        registry.registerDrawingPen(new DrawingPen("Copic Original R29 Lipstick Red", app.color(-1239221)));
        registry.registerDrawingPen(new DrawingPen("Copic Original R32 Peach", app.color(-343622)));
        registry.registerDrawingPen(new DrawingPen("Copic Original R35 Coral", app.color(-888443)));
        registry.registerDrawingPen(new DrawingPen("Copic Original R37 Carmine", app.color(-1545100)));
        registry.registerDrawingPen(new DrawingPen("Copic Original R39 Garnet", app.color(-3454854)));
        registry.registerDrawingPen(new DrawingPen("Copic Original R59 Cardinal", app.color(-4763792)));
        registry.registerDrawingPen(new DrawingPen("Copic Original RV02 Sugared Almond Pink", app.color(-338458)));
        registry.registerDrawingPen(new DrawingPen("Copic Original RV04 Shock  Pink", app.color(-613441)));
        registry.registerDrawingPen(new DrawingPen("Copic Original RV06 Cerise", app.color(-817489)));
        registry.registerDrawingPen(new DrawingPen("Copic Original RV09 Fuchsia", app.color(-2002516)));
        registry.registerDrawingPen(new DrawingPen("Copic Original RV10 Pale Pink", app.color(-135948)));
        registry.registerDrawingPen(new DrawingPen("Copic Original RV11 Pink", app.color(-272675)));
        registry.registerDrawingPen(new DrawingPen("Copic Original RV13 Tender Pink", app.color(-407081)));
        registry.registerDrawingPen(new DrawingPen("Copic Original RV14 Begonia Pink", app.color(-748105)));
        registry.registerDrawingPen(new DrawingPen("Copic Original RV17 Deep Magenta", app.color(-2392397)));
        registry.registerDrawingPen(new DrawingPen("Copic Original RV19 Red Violet", app.color(-2987862)));
        registry.registerDrawingPen(new DrawingPen("Copic Original RV21 Light Pink", app.color(-136985)));
        registry.registerDrawingPen(new DrawingPen("Copic Original RV25 Dog Rose Flower", app.color(-748610)));
        registry.registerDrawingPen(new DrawingPen("Copic Original RV29 Crimson", app.color(-1095552)));
        registry.registerDrawingPen(new DrawingPen("Copic Original RV32 Shadow Pink", app.color(-338994)));
        registry.registerDrawingPen(new DrawingPen("Copic Original RV34 Dark Pink", app.color(-413778)));
        registry.registerDrawingPen(new DrawingPen("Copic Original T0 Toner Gray", app.color(-1249555)));
        registry.registerDrawingPen(new DrawingPen("Copic Original T10 Toner Gray", app.color(-13488595)));
        registry.registerDrawingPen(new DrawingPen("Copic Original T1 Toner Gray", app.color(-1381656)));
        registry.registerDrawingPen(new DrawingPen("Copic Original T2 Toner Gray", app.color(-2039586)));
        registry.registerDrawingPen(new DrawingPen("Copic Original T3 Toner Gray", app.color(-3026228)));
        registry.registerDrawingPen(new DrawingPen("Copic Original T4 Toner Gray", app.color(-4408391)));
        registry.registerDrawingPen(new DrawingPen("Copic Original T5 Toner Gray", app.color(-5724253)));
        registry.registerDrawingPen(new DrawingPen("Copic Original T6 Toner Gray", app.color(-7039600)));
        registry.registerDrawingPen(new DrawingPen("Copic Original T7 Toner Gray", app.color(-8948108)));
        registry.registerDrawingPen(new DrawingPen("Copic Original T8 Toner Gray", app.color(-10263457)));
        registry.registerDrawingPen(new DrawingPen("Copic Original T9 Toner Gray", app.color(-11777207)));
        registry.registerDrawingPen(new DrawingPen("Copic Original V04 Lilac", app.color(-1660210)));
        registry.registerDrawingPen(new DrawingPen("Copic Original V06 Lavender", app.color(-3238462)));
        registry.registerDrawingPen(new DrawingPen("Copic Original V09 Violet", app.color(-7908191)));
        registry.registerDrawingPen(new DrawingPen("Copic Original V12 Pale Lilac", app.color(-1124375)));
        registry.registerDrawingPen(new DrawingPen("Copic Original V15 Mallow", app.color(-2906419)));
        registry.registerDrawingPen(new DrawingPen("Copic Original V17 Amethyst", app.color(-6253881)));
        registry.registerDrawingPen(new DrawingPen("Copic Original W0 Warm Gray", app.color(-1250076)));
        registry.registerDrawingPen(new DrawingPen("Copic Original W10 Warm Gray", app.color(-13619413)));
        registry.registerDrawingPen(new DrawingPen("Copic Original W1 Warm Gray No. 1", app.color(-1579041)));
        registry.registerDrawingPen(new DrawingPen("Copic Original W2 Warm Gray", app.color(-2236971)));
        registry.registerDrawingPen(new DrawingPen("Copic Original W3 Warm Gray No. 3", app.color(-2960694)));
        registry.registerDrawingPen(new DrawingPen("Copic Original W4 Warm Gray", app.color(-4407881)));
        registry.registerDrawingPen(new DrawingPen("Copic Original W5 Warm Gray No. 5", app.color(-5723740)));
        registry.registerDrawingPen(new DrawingPen("Copic Original W6 Warm Gray", app.color(-7039601)));
        registry.registerDrawingPen(new DrawingPen("Copic Original W7 Warm Gray No. 7", app.color(-8947597)));
        registry.registerDrawingPen(new DrawingPen("Copic Original W8 Warm Gray", app.color(-10263457)));
        registry.registerDrawingPen(new DrawingPen("Copic Original W9 Warm Gray", app.color(-11776696)));
        registry.registerDrawingPen(new DrawingPen("Copic Original Y00 Barium Yellow", app.color(-66081)));
        registry.registerDrawingPen(new DrawingPen("Copic Original Y02 Canary Yellow", app.color(-593002)));
        registry.registerDrawingPen(new DrawingPen("Copic Original Y06 Yellow", app.color(-68244)));
        registry.registerDrawingPen(new DrawingPen("Copic Original Y08 Acid Yellow", app.color(-69120)));
        registry.registerDrawingPen(new DrawingPen("Copic Original Y11 Pale Yellow", app.color(-1076)));
        registry.registerDrawingPen(new DrawingPen("Copic Original Y13 Lemon Yellow", app.color(-264274)));
        registry.registerDrawingPen(new DrawingPen("Copic Original Y15 Cadmium Yellow", app.color(-71316)));
        registry.registerDrawingPen(new DrawingPen("Copic Original Y17 Golden Yellow", app.color(-7083)));
        registry.registerDrawingPen(new DrawingPen("Copic Original Y19 Napoli Yellow", app.color(-5826)));
        registry.registerDrawingPen(new DrawingPen("Copic Original Y21 Buttercup Yellow", app.color(-4414)));
        registry.registerDrawingPen(new DrawingPen("Copic Original Y23 Yellowish Beige", app.color(-269389)));
        registry.registerDrawingPen(new DrawingPen("Copic Original Y26 Mustard", app.color(-991897)));
        registry.registerDrawingPen(new DrawingPen("Copic Original Y38 Honey", app.color(-11404)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG01 Green Bice", app.color(-1905742)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG03 Yellow Green", app.color(-2168150)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG05 Salad", app.color(-2693742)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG07 Acid Green", app.color(-5910705)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG09 Lettuce Green", app.color(-8207002)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG11 Mignonette", app.color(-1707824)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG13 Chartreuse", app.color(-2824801)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG17 Grass Green", app.color(-9256618)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG21 Anise", app.color(-526658)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG23 New Leaf", app.color(-1643633)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG25 Celadon Green", app.color(-3088005)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG41 Pale Cobalt Green", app.color(-2757676)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG45 Cobalt Green", app.color(-4924233)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG63 Pea Green", app.color(-6239582)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG67 Moss", app.color(-8274036)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG91 Putty", app.color(-2435154)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG95 Pale Olive", app.color(-3422626)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG97 Spanish Olive", app.color(-6975741)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YG99 Marine Green", app.color(-11638251)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YR00 Powder Pink", app.color(-76099)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YR02 Light Orange", app.color(-205627)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YR04 Chrome Orange", app.color(-81047)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YR07 Cadmium Orange", app.color(-889031)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YR09 Chinese Orange", app.color(-961244)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YR14 Caramel", app.color(-79794)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YR16 Apricot", app.color(-84183)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YR18 Sanguine", app.color(-890052)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YR21 Cream", app.color(-664143)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YR23 Yellow Ochre", app.color(-1257589)));
        registry.registerDrawingPen(new DrawingPen("Copic Original YR24 Pale Sepia", app.color(-995484)));
    }

    public static void registerPenSets(DrawingRegistry registry) {
        registry.registerDrawingSet(new DrawingSet("Copic Dark Greys", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original N10", "Copic Original N8", "Copic Original N6", "Copic Original N4", "Copic Original N2"})));
        registry.registerDrawingSet(new DrawingSet("Copic Light Greys", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original 100", "Copic Original N7", "Copic Original N5", "Copic Original N3", "Copic Original N2"})));
        registry.registerDrawingSet(new DrawingSet("Copic Warm Greys", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original W10", "Copic Original W8", "Copic Original W6", "Copic Original W4", "Copic Original W2"})));
        registry.registerDrawingSet(new DrawingSet("Copic Cool Greys", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original C10", "Copic Original C8", "Copic Original C6", "Copic Original C4", "Copic Original C2"})));
        registry.registerDrawingSet(new DrawingSet("Copic Mixed Greys 1", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original 100", "Copic Original C7", "Copic Original W5", "Copic Original C3", "Copic Original W2"})));
        registry.registerDrawingSet(new DrawingSet("Copic Mixed Greys 2", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original 100", "Copic Original W7", "Copic Original C5", "Copic Original W3", "Copic Original C2"})));
        registry.registerDrawingSet(new DrawingSet("Copic Browns 1", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original 100", "Copic Original E49", "Copic Original E27", "Copic Original E13", "Copic Original E00"})));
        registry.registerDrawingSet(new DrawingSet("Copic Browns 2", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original 100", "Copic Original E49", "Copic Original E27", "Copic Original N4", "Copic Original N2"})));
        registry.registerDrawingSet(new DrawingSet("Copic Dark Grey Browns 2", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original 100", "Copic Original E49", "Copic Original N6", "Copic Original N4", "Copic Original N2"})));
        registry.registerDrawingSet(new DrawingSet("Copic Dark Grey Browns 1", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original 100", "Copic Original E49", "Copic Original E27", "Copic Original E13", "Copic Original N2"})));
        registry.registerDrawingSet(new DrawingSet("Copic Dark Grey Blues", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original 100", "Copic Original B37", "Copic Original N6", "Copic Original N4", "Copic Original N2"})));
        registry.registerDrawingSet(new DrawingSet("Copic Dark Grey Red", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original 100", "Copic Original R59", "Copic Original N6", "Copic Original N4", "Copic Original N2"})));
        registry.registerDrawingSet(new DrawingSet("Copic Dark Grey Violet", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original 100", "Copic Original G29", "Copic Original N6", "Copic Original N4", "Copic Original N2"})));
        registry.registerDrawingSet(new DrawingSet("Copic Dark Grey Orange", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original 100", "Copic Original YR09", "Copic Original N6", "Copic Original N4", "Copic Original N2"})));
        registry.registerDrawingSet(new DrawingSet("Copic Blue Green", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original 100", "Copic Original B39", "Copic Original G28", "Copic Original B26", "Copic Original G14"})));
        registry.registerDrawingSet(new DrawingSet("Copic Blue Purples", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original 100", "Copic Original B39", "Copic Original V09", "Copic Original B02", "Copic Original V04"})));
        registry.registerDrawingSet(new DrawingSet("Copic Reds", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original 100", "Copic Original R29", "Copic Original R27", "Copic Original R24", "Copic Original R20"})));
        registry.registerDrawingSet(new DrawingSet("Copic Yellow Green", registry.getDrawingPensFromCodes(new String[]{"Copic Original 100", "Copic Original E29", "Copic Original YG99", "Copic Original Y17", "Copic Original YG03", "Copic Original Y11"})));

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
            int c1 = app.color(i, app.mouseX, app.mouseY);
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