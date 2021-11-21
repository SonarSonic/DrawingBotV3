package drawingbot.pfm.modules.shapes;

import drawingbot.DrawingBotV3;
import drawingbot.api.IPixelData;
import drawingbot.geom.basic.GEllipse;
import drawingbot.geom.tree.MinimumSpanningTree;
import drawingbot.geom.tsp.*;
import drawingbot.pfm.PFMModular;
import drawingbot.pfm.modules.PositionEncoder;
import drawingbot.pfm.modules.ShapeEncoder;
import drawingbot.utils.Utils;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class TSPShapeEncoder extends ShapeEncoder{

        public EnumTSPAlgorithm algorithmFactory;

        public boolean hasImproved = false;

        public List<Coordinate> coordinates;
        public MinimumSpanningTree mst;

        public enum EnumTSPAlgorithm {
            TWO_OPT("2-Opt", encoder -> new TSPAlgorithm2Opt(encoder.coordinates),false),
            LIN_KERNIGHAN("Lin-Kernighan", encoder -> new TSPAlgorithmLK(encoder.coordinates), false),
            LIN_KERNIGHAN_HELSGAUN("Lin-Kernighan-Helsgaun", encoder -> new TSPAlgorithmLKH(encoder.mst),true);

            public String name;
            public Function<TSPShapeEncoder, ? extends TSPAlgorithmAbstract> factory;
            public boolean create1Tree;

            <T extends TSPAlgorithmAbstract> EnumTSPAlgorithm(String name, Function<TSPShapeEncoder, T> factory, boolean create1Tree){
                this.name = name;
                this.factory = factory;
                this.create1Tree = create1Tree;
            }

            @Override
            public String toString() {
                return name;
            }
        }


        @Override
        public void doProcess(IPixelData data, PositionEncoder positionEncoder) {
            if(pfmModular.currentIteration != pfmModular.targetIteration-1){
                List<Coordinate> coordinateList = positionEncoder.getCoordinates();
                for(Coordinate coord : coordinateList){
                    pfmModular.task.addGeometry(new GEllipse.Filled((float)coord.x - 1/2F, (float)coord.y - 1/2F, 1, 1));
                }
                return;
            }
            pfmModular.task.updateMessage("Starting " + algorithmFactory.toString()  + " Algorithm");

            coordinates = positionEncoder.getCoordinates();

            if(algorithmFactory.create1Tree){
                TreeShapeEncoder encoder = new TreeShapeEncoder();
                encoder.setPFMModular(pfmModular);
                encoder.create1Tree = true;
                encoder.doProcess(data, positionEncoder);
                mst = encoder.mst;
            }

            //setup the TSP using 2-Opt Algorithm
            TSPAlgorithmAbstract tspAlgorithm = algorithmFactory.factory.apply(this);
            tspAlgorithm.progressCallback = f -> pfmModular.updateShapeEncoderProgess(f, 1);
            tspAlgorithm.improvementCallback = l -> hasImproved = true;
            tspAlgorithm.cancelCallback = () -> pfmModular.task.isFinished();

            //start a seperate thread for the tsp, so we can seperate it from the rendering
            ExecutorService service = Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "DrawingBotV3 - TSP Service");
                t.setDaemon(true);
                t.setUncaughtExceptionHandler(DrawingBotV3.INSTANCE.exceptionHandler);
                return t ;
            });

            final CountDownLatch latch = new CountDownLatch(1);

            service.submit(() -> {
                tspAlgorithm.run();
                latch.countDown();
                hasImproved = true;
            });

            ///Await the latch, and render improvements as they occur
            while(hasImproved || (latch.getCount() != 0 && !pfmModular.task.isFinished())){
                if(hasImproved){
                    renderTSPAlgorithm(pfmModular, tspAlgorithm);
                    hasImproved = false;
                }
            }
            service.shutdown();
        }




    public static void renderTSPAlgorithm(PFMModular pfmModular, TSPAlgorithmAbstract linKernighan){

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

