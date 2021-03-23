package drawingbot.pfm.wip;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.geom.basic.GEllipse;
import drawingbot.geom.basic.GRectangle;
import drawingbot.image.ImageTools;
import drawingbot.pfm.AbstractSketchPFM;
import drawingbot.utils.EnumSketchShapes;

public class PFMSketchShapes  extends AbstractSketchPFM {

    public EnumSketchShapes shapes;
    public int startAngleMin;
    public int startAngleMax;
    public float drawingDeltaAngle;

    @Override
    public void init(IPlottingTask task) {
        super.init(task);
        if(startAngleMax < startAngleMin){
            int value = startAngleMin;
            startAngleMin = startAngleMax;
            startAngleMax = value;
        }
    }

    @Override
    public void findDarkestNeighbour(IPixelData pixels, int start_x, int start_y) {
        float start_angle = randomSeedF(startAngleMin, startAngleMax);
        float delta_angle = drawingDeltaAngle / (float)tests;

        resetLuminanceTest();
        for (int d = 0; d < tests; d ++) {
            int nextLineLength = randomSeed(minLineLength, maxLineLength);

            this.luminanceTestAngledLine(pixels, start_x, start_y, nextLineLength, (delta_angle * d) + start_angle);
        }
    }

    @Override
    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust) {
        int rgba = adjustLuminanceLine(task, task.getPixelData(), x1, y1, x2, y2, adjust);
        switch (shapes){
            case RECTANGLES:
                task.addGeometry(new GRectangle(x1, y1, x2-x1, y2-y1), null, rgba);
                break;
            case ELLIPSES:
                task.addGeometry(new GEllipse(x1, y1, x2-x1, y2-y1), null, rgba);
                break;
            default:
                break;
        }
    }

    public int adjustLuminanceRectangle(IPlottingTask task, IPixelData pixels, int startX, int startY, int endX, int endY, int adjustLum) {
        sum_red = 0;
        sum_green = 0;
        sum_blue = 0;
        sum_alpha = 0;
        total_pixels = 0;
        bresenham.rectangle(startX, startY, endX, endY, (x, y) -> adjustLuminanceColour(pixels, x, y, adjustLum));
        return ImageTools.getARGB(sum_alpha / total_pixels, sum_red /total_pixels, sum_green / total_pixels, sum_blue / total_pixels);
    }
}
