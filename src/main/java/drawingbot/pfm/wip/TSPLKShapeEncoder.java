package drawingbot.pfm.wip;

import drawingbot.DrawingBotV3;
import drawingbot.api.IPixelData;
import drawingbot.geom.basic.GEllipse;
import drawingbot.geom.tsp.TSPAlgorithmAbstract;
import drawingbot.geom.tsp.TSPAlgorithmLK;
import drawingbot.geom.tsp.TSPNode;
import drawingbot.pfm.PFMModular;
import drawingbot.pfm.modules.PositionEncoder;
import drawingbot.pfm.modules.ShapeEncoder;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;
/**
 * A TSP Shape Encoder using an classic Lin-Kernighan algorithm
 * W.I.P
 */
public class TSPLKShapeEncoder extends ShapeEncoder {

    @Override
    public void doProcess(IPixelData data, PositionEncoder positionEncoder) {
        if(pfmModular.currentIteration != pfmModular.targetIteration-1){

            List<Coordinate> coordinateList = positionEncoder.getCoordinates();
            for(Coordinate coord : coordinateList){
                pfmModular.task.addGeometry(new GEllipse.Filled((float)coord.x - 1/2F, (float)coord.y - 1/2F, 1, 1));
            }
            return;
        }
        List<Coordinate> coordinates = positionEncoder.getCoordinates();
        TSPAlgorithmLK linKernighan = new TSPAlgorithmLK(coordinates);
        linKernighan.init();

        linKernighan.progressCallback = (f) -> {
            pfmModular.updateShapeEncoderProgess(f, 1);
            return null;
        };

        while(linKernighan.runNextImprovement()){
            renderLinKernighan(pfmModular, linKernighan);
            pfmModular.task.updateMessage("Iteration: " + linKernighan.currentIteration + " / " + linKernighan.targetIterations+ " Improve: " + linKernighan.currentImprovement + " / " + coordinates.size());
        }

        renderLinKernighan(pfmModular, linKernighan);

        pfmModular.task.updateMessage("Best Solution was Found");
    }




    public static void renderLinKernighan(PFMModular pfmModular, TSPAlgorithmAbstract linKernighan){

        pfmModular.task.getPathBuilder().startPath();
        for (TSPNode node : linKernighan.tour) {
            Coordinate coordinate = linKernighan.coordinates.get(node.id);
            pfmModular.task.getPathBuilder().lineTo((float)coordinate.x, (float)coordinate.y);
        }

        pfmModular.task.getPathBuilder().closePath();

        //due to the speed at which changes are made, this is placed in a synchronized loop to avoid clashes
        synchronized (pfmModular.task.plottedDrawing.geometries){
            pfmModular.task.plottedDrawing.clearGeometries();
            DrawingBotV3.RENDERER.clearProcessRendering();

            //note the path is also ended in this loop, so only now is the geometry actually added, the geometry code above is just construction
            pfmModular.task.getPathBuilder().endPath();
        }

        //TODO
        //String iterations = linKernighan.targetIterations == -1 ? "" + (linKernighan.currentIteration + 1) : (linKernighan.currentIteration + 1) + " / " + linKernighan.targetIterations;
        //pfmModular.task.updateMessage("Iteration: " + iterations + " -  Node: " + linKernighan.currentImprovement + " / " + linKernighan.orderedNodes.size() + " - Improvements: " + linKernighan.currentChanges + " - Reduction: " + ((int)linKernighan.totalGain) + " px");

    }
}
