package drawingbot.pfm;

import drawingbot.api.IPlottingTask;
import drawingbot.image.ImageTools;
import drawingbot.pfm.helpers.ColourSampleTest;

import java.util.Comparator;

public class PFMSketchCurves extends PFMSketchLines {

    private final ColourSampleTest curveColourSamples = new ColourSampleTest();

    //user defined settings
    public float tension = 0.4F;

    @Override
    public void preProcess() {
        super.preProcess();
        task.getPathBuilder().setCatmullCurveTension(tension);
    }

    @Override
    public void doProcess() {
        super.doProcess();
        if(shouldLiftPen && curveColourSamples.getSampleCount() != 0){
            task.getPathBuilder().endCatmullCurve();
            task.getLastGeometry().setCustomRGBA(curveColourSamples.getAndResetColourSamples());
        }
    }

    @Override
    public void postProcess() {
        if(curveColourSamples.getSampleCount() != 0){
            task.getPathBuilder().endCatmullCurve();
            task.getLastGeometry().setCustomRGBA(curveColourSamples.getAndResetColourSamples());
        }
    }

    @Override
    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust) {
        if(task.getPathBuilder().hasCurvePoints()){
            defaultColourTest.resetColourSamples(adjust);
            bresenham.plotCatmullRom(task.getPathBuilder().getCatmullP0(), task.getPathBuilder().getCatmullP1(), task.getPathBuilder().getCatmullP2(), new float[]{x2, y2}, task.getPathBuilder().getCatmullTension(), (x, y) -> defaultColourTest.addSample(task.getPixelData(), x, y));
            curveColourSamples.addSample(defaultColourTest.getCurrentAverage());
        }
        task.getPathBuilder().addCatmullCurveVertex(x2, y2);
    }

    @Override
    public int minLineLength() {
        return minLineLength;
    }
}
