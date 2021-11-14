package drawingbot.pfm.wip;

import drawingbot.pfm.AbstractDarkestPFM;
import drawingbot.geom.tsp.TSPAlgorithmGenetic;

import java.util.ArrayList;
import java.util.List;

public class PFMContinuousCurve extends AbstractDarkestPFM {

    @Override
    public void doProcess() {

        for(int range = 0; range < 16; range++){
            List<int[]> points = new ArrayList<>();
            int minLum = (range * 16);
            int maxLum = 16 + (range*16);

            for(int x = 0; x < task.getPixelData().getWidth(); x ++){
                for(int y = 0; y < task.getPixelData().getHeight(); y ++){
                    int c = task.getPixelData().getLuminance(x, y);
                    if(c >= minLum && c < maxLum && c != 255) {
                        points.add(new int[]{x, y});
                    }
                }
            }

            TSPAlgorithmGenetic ga = new TSPAlgorithmGenetic();
            int[] best = ga.tsp(TSPAlgorithmGenetic.getDist(points));

            int n = 0;
            while (n++ < 4) {
                best = ga.nextGeneration();
            }

            task.getPathBuilder().startCatmullCurve();

            for(int i = 0; i < best.length; i++){
                int[] pixel = points.get(best[i]);
                task.getPathBuilder().addCatmullCurveVertex(pixel[0], pixel[1]);
            }

            task.getPathBuilder().endCatmullCurve();
            if(task.isFinished()){
                task.finishProcess();
                return;
            }
        }

        task.finishProcess();

    }



}
