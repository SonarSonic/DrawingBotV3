package drawingbot.pfm.wip;
/*
import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.geom.basic.GEllipse;
import drawingbot.geom.basic.GRectangle;
import drawingbot.image.ImageTools;
import drawingbot.pfm.AbstractSketchPFM;
import drawingbot.utils.EnumSketchShapes;

/*
import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.geom.basic.GEllipse;
import drawingbot.geom.basic.GRectangle;
import drawingbot.image.ImageTools;
import drawingbot.pfm.AbstractSketchPFM;
import drawingbot.utils.EnumSketchShapes;

/**
 * An attempt at a PFM which uses rectangles instead of lines & importantly finds the next darkest rectangle.
 * If doesn't work however as the next darkest rectangle will pretty much always be the one which is one pixel over from another...so maybe they have to be randomly rotated??

public class PFMSketchShapesAware extends AbstractSketchPFM {

    public EnumSketchShapes shapes;

    public int darkestRectStartX = 0;
    public int darkestRectStartY = 0;
    public int darkestRectEndX = 0;
    public int darkestRectEndY = 0;

    @Override
    protected void findDarkestNeighbour(IPixelData pixels, int[] point, int[] darkestDst) {

        resetLuminanceTest();
        for (int d = 0; d < lineTests; d ++) {
            int width = randomSeed(minLineLength, maxLineLength);
            int height = randomSeed(minLineLength, maxLineLength);
            for (int flip = 0; flip < 2; flip ++) {
                int startX = point[0] = clampX(flip == 0 ? point[0] : point[0]-width, pixels.getWidth());
                int startY = point[1] = clampY(flip == 0 ? point[1] : point[1]-height, pixels.getHeight());
                int endX = clampX(flip == 0 ? point[0]+width : point[0], pixels.getWidth());
                int endY = clampY(flip == 0 ? point[1]+height : point[1], pixels.getHeight());

                this.luminanceTestRectangle(pixels, startX, startY, endX, endY);
                if(test_luminance == -1 || getLuminanceTestAverage() < test_luminance){
                    test_luminance = getLuminanceTestAverage();

                    darkestRectStartX = startX;
                    darkestRectStartY = startY;
                    darkestRectEndX = endX;
                    darkestRectEndY = endY;

                    darkestDst[0] = flip == 0 ? endX : startX;
                    darkestDst[1] = flip == 0 ? endY : startY;
                }
            }
        }

    }

    @Override
    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust) {
        int rgba = adjustLuminanceRectangle(task, task.getPixelData(), darkestRectStartX, darkestRectStartY, darkestRectEndX, darkestRectEndY, adjust);
        switch (shapes){
            case RECTANGLES:
                task.addGeometry(new GRectangle(darkestRectStartX, darkestRectStartY, darkestRectEndX - darkestRectStartX, darkestRectEndY - darkestRectStartY), null, rgba);
                break;
            case ELLIPSES:
                task.addGeometry(new GEllipse(darkestRectStartX, darkestRectStartY, darkestRectEndX - darkestRectStartX, darkestRectEndY - darkestRectStartY), null, rgba);
                break;
            default:
                break;
        }
    }


    protected void luminanceTestRectangle(IPixelData pixels, int startX, int startY, int endX, int endY){
        resetLuminanceSamples();
        //bresenham.rectangle(startX, startY, endX, endY, (x, y) -> luminanceTest(pixels, x, y));
    }

    public int adjustLuminanceRectangle(IPlottingTask task, IPixelData pixels, int startX, int startY, int endX, int endY, int adjustLum) {
        sum_red = 0;
        sum_green = 0;
        sum_blue = 0;
        sum_alpha = 0;
        total_pixels = 0;
        //bresenham.rectangle(startX, startY, endX, endY, (x, y) -> adjustLuminanceColour(pixels, x, y, adjustLum));
        return ImageTools.getARGB(sum_alpha / total_pixels, sum_red /total_pixels, sum_green / total_pixels, sum_blue / total_pixels);
    }

    @Override
    protected boolean luminanceTest(IPixelData pixels, int x, int y, int[] dest){
        if(x < 0 || x >= pixels.getWidth() || y < 0 || y >= pixels.getHeight()){
            return true;
        }
        sum_luminance += pixels.getLuminance(x, y);
        count_pixels++;
        ///doesn't test individual pixels.
        return false;
    }
}
*/