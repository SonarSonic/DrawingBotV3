package drawingbot.pfm;

import drawingbot.DrawingBotV3;
import drawingbot.api.IPlottingTask;
import drawingbot.pfm.modules.PositionEncoder;
import drawingbot.pfm.modules.ShapeEncoder;

/**
 * Designed following the principles outlined here
 * https://www.researchgate.net/publication/331407670_LinesLab_A_Flexible_Low-Cost_Approach_for_the_Generation_of_Physical_Monochrome_Art
 */
public class PFMModular extends AbstractPFM{

    public int currentIteration = 0;
    public int targetIteration = 0;

    public PositionEncoder positionalEncoder;
    public ShapeEncoder shapeEncoder;

    public PFMModular(PositionEncoder positionalEncoder, ShapeEncoder encoder){
        this.positionalEncoder = positionalEncoder;
        this.positionalEncoder.setPFMModular(this);

        this.shapeEncoder = encoder;
        this.shapeEncoder.setPFMModular(this);
    }

    @Override
    public void init(IPlottingTask task) {
        super.init(task);
    }

    @Override
    public void preProcess() {}

    @Override
    public void doProcess() {
        positionalEncoder.preProcess(task.getPixelData());

        currentIteration = 0;
        targetIteration = positionalEncoder.getIterations();
        while(currentIteration < targetIteration){
            if(task.isFinished()){
                break;
            }
            positionalEncoder.doProcess(task.getPixelData());

            //clear previous geometries & mark for re-render
            task.plottedDrawing.clearGeometries();
            DrawingBotV3.RENDERER.clearProcessRendering();

            shapeEncoder.doProcess(task.getPixelData(), positionalEncoder);
            currentIteration++;
        }

        task.finishProcess();
    }

    public void updatePositionEncoderProgess(double progress, double max){
        task.updatePlottingProgress(currentIteration + ((progress / max)/2), targetIteration);
    }

    public void updateShapeEncoderProgess(double progress, double max){
        task.updatePlottingProgress(currentIteration + 0.5D + ((progress / max)/2), targetIteration);
    }

}
