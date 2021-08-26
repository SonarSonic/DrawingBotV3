package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.geom.basic.GEllipse;
import drawingbot.geom.basic.GRectangle;
import drawingbot.utils.EnumSketchShapes;

public class PFMSketchShapes extends AbstractSketchPFM {

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
        float delta_angle = drawingDeltaAngle / (float) lineTests;

        resetLuminanceTest();
        for (int d = 0; d < lineTests; d ++) {
            int nextLineLength = randomSeed(minLineLength, maxLineLength);
            this.luminanceTestAngledLine(pixels, start_x, start_y, nextLineLength, (delta_angle * d) + start_angle);
        }
    }

    @Override
    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust) {
        int shapeX = Math.min(x1, x2);
        int shapeY = Math.min(y1, y2);
        int shapeWidth = Math.abs(x2-x1);
        int shapeHeight = Math.abs(y2-y1);
        switch (shapes){
            case RECTANGLES:
                resetColourSamples();
                GRectangle rectangle = new GRectangle(shapeX, shapeY, shapeWidth, shapeHeight);
                bresenham.plotShape(rectangle, (x, y) -> adjustLuminanceColour(task.getPixelData(), x, y, adjust));
                task.addGeometry(rectangle, null, getColourTestAverage());
                break;
            case ELLIPSES:
                resetColourSamples();
                GEllipse ellipse = new GEllipse(shapeX, shapeY, shapeWidth, shapeHeight);
                bresenham.plotShape(ellipse, (x, y) -> adjustLuminanceColour(task.getPixelData(), x, y, adjust));
                task.addGeometry(ellipse, null, getColourTestAverage());
                break;
            default:
                break;
        }
    }
}