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

    @Override
    public String getPluginName() {
        return "Special Pen Plugin";
    }

    @Override
    public void registerDrawingTools() {

        //// ORIGINAL COLOURS \\\\

        ORIGINAL_COLOUR_PEN = new CustomPen(DBConstants.DRAWING_TYPE_SPECIAL, "Original Colour", -1){
            @Override
            public int getCustomARGB(int pfmARGB) {
                return pfmARGB;
            }
        };
        MasterRegistry.INSTANCE.registerDrawingPen(ORIGINAL_COLOUR_PEN);

        ORIGINAL_GRAYSCALE_PEN = new CustomPen(DBConstants.DRAWING_TYPE_SPECIAL, "Original Grayscale", -1){
            @Override
            public int getCustomARGB(int pfmARGB) {
                return ImageTools.grayscaleFilter(pfmARGB);
            }
        };
        MasterRegistry.INSTANCE.registerDrawingPen(ORIGINAL_GRAYSCALE_PEN);

        ORIGINAL_RED_PEN = new CustomPen(DBConstants.DRAWING_TYPE_SPECIAL, "Original Red", ImageTools.getARGB(255, 255, 0, 0)){
            @Override
            public int getCustomARGB(int pfmARGB) {
                int red = ImageTools.red(pfmARGB);
                return ImageTools.getARGB(255, red, 0, 0);
            }
        };
        MasterRegistry.INSTANCE.registerDrawingPen(ORIGINAL_RED_PEN);

        ORIGINAL_GREEN_PEN = new CustomPen(DBConstants.DRAWING_TYPE_SPECIAL, "Original Green", ImageTools.getARGB(255, 0, 255, 0)){
            @Override
            public int getCustomARGB(int pfmARGB) {
                int green = ImageTools.green(pfmARGB);
                return ImageTools.getARGB(255, 0, green, 0);
            }
        };
        MasterRegistry.INSTANCE.registerDrawingPen(ORIGINAL_GREEN_PEN);

        ORIGINAL_BLUE_PEN = new CustomPen(DBConstants.DRAWING_TYPE_SPECIAL, "Original Blue", ImageTools.getARGB(255, 0, 0, 255)){
            @Override
            public int getCustomARGB(int pfmARGB) {
                int blue = ImageTools.blue(pfmARGB);
                return ImageTools.getARGB(255, 0, 0, blue);
            }
        };
        MasterRegistry.INSTANCE.registerDrawingPen(ORIGINAL_BLUE_PEN);

        ORIGINAL_COLOUR_SET = new DrawingSet(DBConstants.DRAWING_TYPE_SPECIAL,"Original Colour", List.of(ORIGINAL_COLOUR_PEN));
        MasterRegistry.INSTANCE.registerDrawingSet(ORIGINAL_COLOUR_SET);

        ORIGINAL_GRAYSCALE_SET = new DrawingSet(DBConstants.DRAWING_TYPE_SPECIAL,"Original Grayscale", List.of(ORIGINAL_GRAYSCALE_PEN));
        MasterRegistry.INSTANCE.registerDrawingSet(ORIGINAL_GRAYSCALE_SET);
    }

}
