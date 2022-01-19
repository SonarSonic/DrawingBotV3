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

    @Override
    public String getPluginName() {
        return "Special Pen Plugin";
    }

    @Override
    public void registerDrawingTools() {

        //// ORIGINAL COLOURS \\\\

        DrawingPen originalColourPen = new CustomPen(DBConstants.DRAWING_TYPE_SPECIAL, "Original Colour", -1){
            @Override
            public int getCustomARGB(int pfmARGB) {
                return pfmARGB;
            }
        };
        MasterRegistry.INSTANCE.registerDrawingPen(originalColourPen);

        DrawingPen originalGrayscalePen = new CustomPen(DBConstants.DRAWING_TYPE_SPECIAL, "Original Grayscale", -1){
            @Override
            public int getCustomARGB(int pfmARGB) {
                return ImageTools.grayscaleFilter(pfmARGB);
            }
        };
        MasterRegistry.INSTANCE.registerDrawingPen(originalGrayscalePen);

        DrawingPen originalRedPen = new CustomPen(DBConstants.DRAWING_TYPE_SPECIAL, "Original Red", ImageTools.getARGB(255, 255, 0, 0)){
            @Override
            public int getCustomARGB(int pfmARGB) {
                int red = ImageTools.red(pfmARGB);
                return ImageTools.getARGB(255, red, 0, 0);
            }
        };
        MasterRegistry.INSTANCE.registerDrawingPen(originalRedPen);

        DrawingPen originalGreenPen = new CustomPen(DBConstants.DRAWING_TYPE_SPECIAL, "Original Green", ImageTools.getARGB(255, 0, 255, 0)){
            @Override
            public int getCustomARGB(int pfmARGB) {
                int green = ImageTools.green(pfmARGB);
                return ImageTools.getARGB(255, 0, green, 0);
            }
        };
        MasterRegistry.INSTANCE.registerDrawingPen(originalGreenPen);

        DrawingPen originalBluePen = new CustomPen(DBConstants.DRAWING_TYPE_SPECIAL, "Original Blue", ImageTools.getARGB(255, 0, 0, 255)){
            @Override
            public int getCustomARGB(int pfmARGB) {
                int blue = ImageTools.blue(pfmARGB);
                return ImageTools.getARGB(255, 0, 0, blue);
            }
        };
        MasterRegistry.INSTANCE.registerDrawingPen(originalBluePen);

        DrawingSet originalColourSet = new DrawingSet(DBConstants.DRAWING_TYPE_SPECIAL,"Original Colour", List.of(originalColourPen));
        MasterRegistry.INSTANCE.registerDrawingSet(originalColourSet);

        DrawingSet originalGrayscaleSet = new DrawingSet(DBConstants.DRAWING_TYPE_SPECIAL,"Original Grayscale", List.of(originalGrayscalePen));
        MasterRegistry.INSTANCE.registerDrawingSet(originalGrayscaleSet);
    }

}
