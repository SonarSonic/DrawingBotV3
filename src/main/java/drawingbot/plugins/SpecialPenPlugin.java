package drawingbot.plugins;

import drawingbot.api.IPlugin;
import drawingbot.drawing.CustomPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.image.ImageTools;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.DBConstants;

import java.util.List;

public class SpecialPenPlugin implements IPlugin {

    public static DrawingPen ORIGINAL_COLOUR_PEN;
    public static DrawingPen ORIGINAL_GRAYSCALE_PEN;
    public static DrawingPen ORIGINAL_RED_PEN;
    public static DrawingPen ORIGINAL_GREEN_PEN;
    public static DrawingPen ORIGINAL_BLUE_PEN;

    public static DrawingSet ORIGINAL_COLOUR_SET;
    public static DrawingSet ORIGINAL_GRAYSCALE_SET;

    public static DrawingPen INVERTED_COLOUR_PEN;
    public static DrawingPen INVERTED_GRAYSCALE_PEN;

    public static DrawingSet INVERTED_COLOUR_SET;
    public static DrawingSet INVERTED_GRAYSCALE_SET;

    @Override
    public String getPluginName() {
        return "Special Pen Plugin";
    }

    @Override
    public void registerDrawingTools() {

        //// ORIGINAL COLOURS \\\\

        ORIGINAL_COLOUR_PEN = new CustomPen(DBConstants.DRAWING_TYPE_SPECIAL, "Original Colour", -1){
            final int black = ImageTools.getARGB(255, 0, 0, 0);
            @Override
            public int getCustomARGB(int pfmARGB) {
                if(pfmARGB == -1){
                    return black;
                }
                return pfmARGB;
            }
        };
        MasterRegistry.INSTANCE.registerDrawingPen(ORIGINAL_COLOUR_PEN);

        ORIGINAL_GRAYSCALE_PEN = new CustomPen(DBConstants.DRAWING_TYPE_SPECIAL, "Original Grayscale", -1){
            final int grey = ImageTools.getARGB(255, 25, 25, 25);
            @Override
            public int getCustomARGB(int pfmARGB) {
                if(pfmARGB == -1){
                    return grey;
                }
                return ImageTools.grayscaleFilter(pfmARGB);
            }
        };
        MasterRegistry.INSTANCE.registerDrawingPen(ORIGINAL_GRAYSCALE_PEN);

        ORIGINAL_RED_PEN = new CustomPen(DBConstants.DRAWING_TYPE_SPECIAL, "Original Red", ImageTools.getARGB(255, 255, 0, 0)){
            final int red = ImageTools.getARGB(255, 255, 0, 0);
            @Override
            public int getCustomARGB(int pfmARGB) {
                if(pfmARGB == -1){
                    return red;
                }
                int red = ImageTools.red(pfmARGB);
                return ImageTools.getARGB(255, red, 0, 0);
            }
        };
        MasterRegistry.INSTANCE.registerDrawingPen(ORIGINAL_RED_PEN);

        ORIGINAL_GREEN_PEN = new CustomPen(DBConstants.DRAWING_TYPE_SPECIAL, "Original Green", ImageTools.getARGB(255, 0, 255, 0)){
            final int green = ImageTools.getARGB(255, 0, 255, 0);
            @Override
            public int getCustomARGB(int pfmARGB) {
                if(pfmARGB == -1){
                    return green;
                }
                int green = ImageTools.green(pfmARGB);
                return ImageTools.getARGB(255, 0, green, 0);
            }
        };
        MasterRegistry.INSTANCE.registerDrawingPen(ORIGINAL_GREEN_PEN);

        ORIGINAL_BLUE_PEN = new CustomPen(DBConstants.DRAWING_TYPE_SPECIAL, "Original Blue", ImageTools.getARGB(255, 0, 0, 255)){
            final int blue = ImageTools.getARGB(255, 0, 0, 255);
            @Override
            public int getCustomARGB(int pfmARGB) {
                if(pfmARGB == -1){
                    return blue;
                }
                int blue = ImageTools.blue(pfmARGB);
                return ImageTools.getARGB(255, 0, 0, blue);
            }
        };
        MasterRegistry.INSTANCE.registerDrawingPen(ORIGINAL_BLUE_PEN);

        ORIGINAL_COLOUR_SET = new DrawingSet(DBConstants.DRAWING_TYPE_SPECIAL,"Original Colour", List.of(ORIGINAL_COLOUR_PEN));
        MasterRegistry.INSTANCE.registerDrawingSet(ORIGINAL_COLOUR_SET);

        ORIGINAL_GRAYSCALE_SET = new DrawingSet(DBConstants.DRAWING_TYPE_SPECIAL,"Original Grayscale", List.of(ORIGINAL_GRAYSCALE_PEN));
        MasterRegistry.INSTANCE.registerDrawingSet(ORIGINAL_GRAYSCALE_SET);

        //// INVERTED COLOURS \\\\

        INVERTED_COLOUR_PEN = new CustomPen(DBConstants.DRAWING_TYPE_SPECIAL, "Original Colour (Inverted)", -1){
            final int white = ImageTools.getARGB(255, 255, 255, 255);
            @Override
            public int getCustomARGB(int pfmARGB) {
                if(pfmARGB == -1){
                    return white;
                }
                int a = pfmARGB & 0xff000000;
                return a | (~pfmARGB & 0x00ffffff);
            }
        };
        MasterRegistry.INSTANCE.registerDrawingPen(INVERTED_COLOUR_PEN);

        INVERTED_GRAYSCALE_PEN = new CustomPen(DBConstants.DRAWING_TYPE_SPECIAL, "Original Grayscale (Inverted)", -1){
            final int softWhite = ImageTools.getARGB(255, 230, 230, 230);
            @Override
            public int getCustomARGB(int pfmARGB) {
                if(pfmARGB == -1){
                    return softWhite;
                }
                int a = pfmARGB & 0xff000000;
                return ImageTools.grayscaleFilter(a | (~pfmARGB & 0x00ffffff));
            }
        };
        MasterRegistry.INSTANCE.registerDrawingPen(INVERTED_GRAYSCALE_PEN);

        INVERTED_COLOUR_SET = new DrawingSet(DBConstants.DRAWING_TYPE_SPECIAL,"Original Colour (Inverted)", List.of(INVERTED_COLOUR_PEN));
        MasterRegistry.INSTANCE.registerDrawingSet(INVERTED_COLOUR_SET);

        INVERTED_GRAYSCALE_SET = new DrawingSet(DBConstants.DRAWING_TYPE_SPECIAL,"Original Grayscale (Inverted)", List.of(INVERTED_GRAYSCALE_PEN));
        MasterRegistry.INSTANCE.registerDrawingSet(INVERTED_GRAYSCALE_SET);
    }

}
