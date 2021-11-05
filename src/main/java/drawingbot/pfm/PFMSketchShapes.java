package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.geom.basic.GEllipse;
import drawingbot.geom.basic.GRectangle;
import drawingbot.pfm.helpers.LuminanceTestLine;
import drawingbot.utils.EnumSketchShapes;

public class PFMSketchShapes extends AbstractSketchPFM {

    private LuminanceTestLine luminanceTest;

    public EnumSketchShapes shapes;
    public int startAngleMin;
    public int startAngleMax;
    public float drawingDeltaAngle;

    @Override
    public void init(IPlottingTask task) {
        super.init(task);
        luminanceTest = new LuminanceTestLine(darkest, minLineLength, maxLineLength, true);
        if(startAngleMax < startAngleMin){
            int value = startAngleMin;
            startAngleMin = startAngleMax;
            startAngleMax = value;
        }
    }

    @Override
    protected boolean findDarkestNeighbour(IPixelData pixels, int[] point, int[] darkestDst) {
        float start_angle = randomSeedF(startAngleMin, startAngleMax);
        float delta_angle = drawingDeltaAngle / (float) lineTests;

        luminanceTest.resetTest();
        for (int d = 0; d < lineTests; d ++) {
            int nextLineLength = randomSeed(minLineLength, maxLineLength);
            luminanceTest.resetSamples();
            bresenham.plotAngledLine(point[0], point[1], nextLineLength, (delta_angle * d) + start_angle, (x, y) -> luminanceTest.addSample(pixels, x, y));
        }
        return luminanceTest.getDarkestSample() < pixels.getAverageLuminance();
    }

    @Override
    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust) {
        int shapeX = Math.min(x1, x2);
        int shapeY = Math.min(y1, y2);
        int shapeWidth = Math.abs(x2-x1);
        int shapeHeight = Math.abs(y2-y1);
        switch (shapes){
            case RECTANGLES:
                defaultColourTest.resetColourSamples(adjust);
                GRectangle rectangle = new GRectangle(shapeX, shapeY, shapeWidth, shapeHeight);
                bresenham.plotShape(rectangle, (x, y) -> defaultColourTest.addSample(task.getPixelData(), x, y));
                task.addGeometry(rectangle, null, defaultColourTest.getCurrentAverage());
                break;
            case ELLIPSES:
                defaultColourTest.resetColourSamples(adjust);
                GEllipse ellipse = new GEllipse(shapeX, shapeY, shapeWidth, shapeHeight);
                bresenham.plotShape(ellipse, (x, y) -> defaultColourTest.addSample(task.getPixelData(), x, y));
                task.addGeometry(ellipse, null, defaultColourTest.getCurrentAverage());
                break;
            default:
                break;
        }
    }
}