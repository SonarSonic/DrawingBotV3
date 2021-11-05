package drawingbot.pfm.wip;

import drawingbot.DrawingBotV3;
import drawingbot.api.IPixelData;
import drawingbot.geom.tsp.TSPAlgorithmLKH;
import drawingbot.pfm.modules.PositionEncoder;
import drawingbot.pfm.modules.shapes.TreeShapeEncoder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A TSP Shape Encoder using an the advanced Lin-Kernighan-Helsgaun algorithm
 * W.I.P
 */
public class TSPLKHShapeEncoder extends TreeShapeEncoder {

    public boolean hasImproved = false;

    {
        create1Tree = true;
    }

    @Override
    public void doProcess(IPixelData data, PositionEncoder positionEncoder) {
        super.doProcess(data, positionEncoder);
        if(mst == null){
            return;
        }
        pfmModular.task.updateMessage("Starting Lin-Kernighan Algorithm");

        //setup the TSP using Lin-Kernighan Heuristic
        TSPAlgorithmLKH linKernighan = new TSPAlgorithmLKH(mst);
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
            linKernighan.init();
            linKernighan.runFullTSP();
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
