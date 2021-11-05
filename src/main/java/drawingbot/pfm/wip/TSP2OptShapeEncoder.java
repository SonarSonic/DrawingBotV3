package drawingbot.pfm.wip;

import drawingbot.DrawingBotV3;
import drawingbot.api.IPixelData;
import drawingbot.geom.basic.GEllipse;
import drawingbot.geom.tsp.TSPAlgorithm2Opt;
import drawingbot.pfm.modules.PositionEncoder;
import drawingbot.pfm.modules.ShapeEncoder;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A TSP Shape Encoder using a basic 2-opt algorithm.
 * W.I.P
 */
public class TSP2OptShapeEncoder extends ShapeEncoder {

    public boolean hasImproved = false;

    @Override
    public void doProcess(IPixelData data, PositionEncoder positionEncoder) {
        if(pfmModular.currentIteration != pfmModular.targetIteration-1){
            List<Coordinate> coordinateList = positionEncoder.getCoordinates();
            for(Coordinate coord : coordinateList){
                pfmModular.task.addGeometry(new GEllipse.Filled((float)coord.x - 1/2F, (float)coord.y - 1/2F, 1, 1));
            }
            return;
        }
        pfmModular.task.updateMessage("Starting 2-Opt Algorithm");

        //setup the TSP using Lin-Kernighan Heuristic
        TSPAlgorithm2Opt linKernighan = new TSPAlgorithm2Opt(positionEncoder.getCoordinates());
        linKernighan.progressCallback = f -> pfmModular.updateShapeEncoderProgess(f, 1);
        linKernighan.improvementCallback = l -> hasImproved = true;
        linKernighan.cancelCallback = () -> pfmModular.task.isFinished();

        //Start a seperate thread for the tsp, so we can seperate it from the rendering
        ExecutorService service = Executors.newFixedThreadPool(2,  r -> {
            Thread t = new Thread(r, "DrawingBotV3 - TSP Service");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(DrawingBotV3.INSTANCE.exceptionHandler);
            return t ;
        });

        final CountDownLatch latch = new CountDownLatch(1);

        service.submit(() -> {
            linKernighan.run();
            latch.countDown();
            hasImproved = true;
        });

        ///Await the latch, and render improvements as they occur
        while(hasImproved || (latch.getCount() != 0 && !pfmModular.task.isFinished())){
            if(hasImproved){
                TSPLKShapeEncoder.renderLinKernighan(pfmModular, linKernighan);
                hasImproved = false;
            }
        }
        service.shutdown();
    }
}
