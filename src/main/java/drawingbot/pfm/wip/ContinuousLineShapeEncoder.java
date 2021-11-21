package drawingbot.pfm.wip;

import com.aparapi.Kernel;
import com.aparapi.Range;
import drawingbot.DrawingBotV3;
import drawingbot.api.IPixelData;
import drawingbot.geom.basic.GEllipse;
import drawingbot.image.PixelDataARGBY;
import drawingbot.pfm.helpers.BresenhamHelper;
import drawingbot.pfm.helpers.LuminanceTest;
import drawingbot.geom.tsp.TSPAlgorithmGenetic;
import drawingbot.pfm.modules.PositionEncoder;
import drawingbot.pfm.modules.ShapeEncoder;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;

//W.I.P CUDA Accelerated TSP Art.
public class ContinuousLineShapeEncoder extends ShapeEncoder {

    public BresenhamHelper bresenham = new BresenhamHelper();
    public LuminanceTest luminanceTest = new LuminanceTest();

    @Override
    public void doProcess(IPixelData data, PositionEncoder positionEncoder) {
        if(pfmModular.currentIteration != pfmModular.targetIteration-1){

            List<Coordinate> coordinateList = positionEncoder.getCoordinates();
            for(Coordinate coord : coordinateList){
                pfmModular.task.addGeometry(new GEllipse.Filled((float)coord.x - 1/2F, (float)coord.y - 1/2F, 1, 1));
            }
            return;
        }

        pfmModular.task.updateMessage("Comparing Lines");
        List<Coordinate> coordinateList = positionEncoder.getCoordinates();

        TSPAlgorithmGenetic tspHelper = TSPAlgorithmGenetic.getInstance();
        float[][] lineBrightnessGPU = getLineBrightnessFromCoordinatesAccelerated(coordinateList);
        int[] best = tspHelper.tsp(lineBrightnessGPU);

        int n = 0;
        while (n++ < 1000) {
            pfmModular.task.updateMessage("Calculating Best Path: Iteration " + n + " of " + 1000);
            pfmModular.task.plottedDrawing.clearGeometries();
            DrawingBotV3.RENDERER.clearProcessRendering();

            pfmModular.task.getPathBuilder().startPath();
            for (int index : best) {
                Coordinate coord = coordinateList.get(index);
                pfmModular.task.getPathBuilder().lineTo((float) coord.x, (float) coord.y);
            }
            pfmModular.task.getPathBuilder().endPath();
            pfmModular.task.updatePlottingProgress(n, 1000);
            best = tspHelper.nextGeneration();

            if(pfmModular.task.isFinished()){
                break;
            }
        }

        /*
        ///re-render the line as a curve.
        pfmModular.task.plottedDrawing.clearGeometries();
        DrawingBotV3.RENDERER.clearProcessRendering();

        pfmModular.task.getPathBuilder().setCatmullCurveTension(0.2F);
        pfmModular.task.getPathBuilder().startCatmullCurve();

        for (int i = 0; i < best.length; i++) {
            Coordinate coord = coordinateList.get(best[i]);
            pfmModular.task.getPathBuilder().addCatmullCurveVertex((float)coord.x, (float)coord.y);
            if(i == 0 || i == best.length-1){ //so we don't skip the first and last points
                pfmModular.task.getPathBuilder().addCatmullCurveVertex((float)coord.x, (float)coord.y);
            }
            pfmModular.updateShapeEncoderProgess(i, best.length);
        }
        pfmModular.task.getPathBuilder().endCatmullCurve();

         */

    }


    public float[][] getLineBrightnessFromCoordinates(List<Coordinate> points) {
        float[][] dist = new float[points.size()][points.size()];
        for (int i = 0; i < points.size(); i++) {
            for (int j = 0; j < points.size(); j++) {
                float distance = TSPAlgorithmGenetic.distanceFromCoordinates(points.get(i), points.get(j));
                luminanceTest.resetSamples();
                bresenham.plotLine((int)points.get(i).x, (int)points.get(i).y, (int)points.get(j).x, (int)points.get(j).y, (x, y) -> luminanceTest.addSample(pfmModular.task.getPixelData(), x, y));
                dist[i][j] = (distance*2) * luminanceTest.getCurrentSample();
            }
            pfmModular.updateShapeEncoderProgess(i, points.size());
        }
        return dist;
    }

    public float[][] getLineBrightnessFromCoordinatesAccelerated(List<Coordinate> points) {
        final int size = points.size();
        final float[][] output = new float[points.size()][points.size()];

        final int[][] coordinates = new int[points.size()][2];
        int index = 0;
        for(Coordinate coordinate : points){
            coordinates[index][0] = (int)coordinate.x;
            coordinates[index][1] = (int)coordinate.y;
            index++;
        }

        PixelDataARGBY pixelDataARGBY = (PixelDataARGBY) pfmModular.task.getPixelData();
        final int[][] luminance = pixelDataARGBY.luminance.data;

        //the following code will be executed on the GPU, note many typical JAVA operations won't be translated to OpenCL properly, so keep any changes simple or it will break, and either not compile, or run on the cpu instead.
        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int gid = getGlobalId();
                int coordIndexA = gid % size;
                int coordIndexB = gid / size;

                int[] coordA = coordinates[coordIndexA];
                int[] coordB = coordinates[coordIndexB];

                float sumLuminance = 0;
                int pixelCount = 0;

                int x0 = coordA[0];
                int x1 = coordB[0];

                int y0 = coordA[1];
                int y1 = coordB[1];

                int dx =  Math.abs(x1-x0), sx = x0<x1 ? 1 : -1;
                int dy = -Math.abs(y1-y0), sy = y0<y1 ? 1 : -1;
                int err = dx+dy, e2;                                  /* error value e_xy */

                boolean end = false;

                while (!end) {
                    sumLuminance+=luminance[x0][y0];
                    pixelCount++;
                    e2 = 2*err;
                    if (e2 >= dy) {                                       /* e_xy+e_x > 0 */
                        if (x0 == x1){
                            end = true;
                        }else{
                            err += dy; x0 += sx;
                        }
                    }
                    if (e2 <= dx) {                                       /* e_xy+e_y < 0 */
                        if (y0 == y1){
                            end = true;
                        }else{
                            err += dx; y0 += sy;
                        }
                    }
                }
                float distance = (float) Math.sqrt((coordA[0] - coordB[0]) * (coordA[0] - coordB[0]) + (coordA[1] - coordB[1]) * (coordA[1] - coordB[1]));
                output[coordIndexA][coordIndexB] = distance * (sumLuminance / (float) pixelCount);
            }
        };

        kernel.execute(Range.create(coordinates.length * coordinates.length));
        kernel.dispose();

        return output;

    }

}
