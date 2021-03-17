package drawingbot.pfm;

import drawingbot.api.IPlottingTask;
import drawingbot.image.ImageTools;

public class PFMSketchCurves extends PFMSketchLines {

    public float tension = 0.4F;

    ///rgba samples
    private long red, green, blue, alpha;
    private int count;

    @Override
    public void preProcess(IPlottingTask task) {
        super.preProcess(task);
        task.getPathBuilder().setCurveTension(tension);
    }

    @Override
    public void doProcess(IPlottingTask task) {
        super.doProcess(task);
        if(shouldLiftPen){
            task.getPathBuilder().endCurve();
            task.getLastGeometry().setCustomRGBA(getCurveARGB());
        }
    }

    @Override
    public void postProcess(IPlottingTask task) {
        task.getPathBuilder().endCurve();
        task.getLastGeometry().setCustomRGBA(getCurveARGB());
    }

    @Override
    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust) {
        int argb = adjustLuminanceLine(task, task.getPixelData(), x1, y1, x2, y2, adjust);
        alpha += ImageTools.alpha(argb);
        red += ImageTools.red(argb);
        green += ImageTools.green(argb);
        blue += ImageTools.blue(argb);
        count ++;
        task.getPathBuilder().addCurveVertex(x2, y2);
    }

    public int getCurveARGB(){
        int argb = ImageTools.getARGB((int)((float)alpha/count), (int)((float)red/count), (int)((float)green/count), (int)((float)blue/count));
        red = green = blue = alpha = count = 0;
        return argb;
    }
}
