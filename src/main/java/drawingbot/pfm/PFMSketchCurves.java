package drawingbot.pfm;

import drawingbot.api.IPlottingTask;
import drawingbot.image.ImageTools;

public class PFMSketchCurves extends PFMSketchLines {

    public float tension = 0.4F;

    ///rgba samples
    private long red, green, blue, alpha;
    private int count;

    @Override
    public void preProcess() {
        super.preProcess();
        task.getPathBuilder().setCatmullCurveTension(tension);
    }

    @Override
    public void doProcess() {
        super.doProcess();
        if(shouldLiftPen){
            task.getPathBuilder().endCatmullCurve();
            task.getLastGeometry().setCustomRGBA(getCurveARGB());
        }
    }

    @Override
    public void postProcess() {
        task.getPathBuilder().endCatmullCurve();
        task.getLastGeometry().setCustomRGBA(getCurveARGB());
    }

    @Override
    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust) {
        if(task.getPathBuilder().hasCurvePoints()){
            resetColourSamples();
            bresenham.plotCatmullRom(task.getPathBuilder().getCatmullP0(), task.getPathBuilder().getCatmullP1(), task.getPathBuilder().getCatmullP2(), new float[]{x2, y2}, task.getPathBuilder().getCatmullTension(), (x, y) -> adjustLuminanceColour(task.getPixelData(), x, y, adjust));
            int argb = getColourTestAverage();

            alpha += ImageTools.alpha(argb);
            red += ImageTools.red(argb);
            green += ImageTools.green(argb);
            blue += ImageTools.blue(argb);
            count ++;
        }
        task.getPathBuilder().addCatmullCurveVertex(x2, y2);
    }

    public int getCurveARGB(){
        int argb = ImageTools.getARGB((int)((float)alpha/count), (int)((float)red/count), (int)((float)green/count), (int)((float)blue/count));
        red = green = blue = alpha = count = 0;
        return argb;
    }

    @Override
    public int minLineLength() {
        return minLineLength;
    }
}
