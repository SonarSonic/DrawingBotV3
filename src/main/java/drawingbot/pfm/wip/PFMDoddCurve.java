package drawingbot.pfm.wip;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.geom.basic.GPath;
import drawingbot.pfm.AbstractSketchPFM;

import java.util.ArrayList;
import java.util.List;

public class PFMDoddCurve extends AbstractSketchPFM {

    private List<int[]> lastCurve = null;

    @Override
    protected void findDarkestNeighbour(IPixelData pixels, int x, int y) {

        float initialAngleRange = 360F;
        float initialAngleTests = 16;

        float searchAngleRange = 90F;
        float searchAngleTests = 4;

        int pointPerCurve = 4;

        List<int[]> darkestCurve = null;
        float luminanceTest = -1F;

        for(int i = 0; i < initialAngleTests; i++){

            float testAngle = (initialAngleRange / initialAngleTests) * i;
            int nextLineLength = randomSeed(minLineLength, maxLineLength);

            List<int[]> curvePoints = new ArrayList<>();

            curvePoints.add(new int[]{clampX(x, pixels.getWidth()), clampY(y, pixels.getHeight())});

            //create an initial line which goes out at the given test angle.
            resetLuminanceTest();
            luminanceTestAngledLine(pixels, x, y, nextLineLength, testAngle);

            int searchStartX = darkest_x;
            int searchStartY = darkest_y;

            curvePoints.add(new int[]{clampX(searchStartX, pixels.getWidth()), clampY(searchStartY, pixels.getHeight())});

            for(int l = 0; l < pointPerCurve; l++){
                resetLuminanceTest();
                for (int d = 0; d < searchAngleTests; d ++) {
                    luminanceTestAngledLine(pixels, searchStartX, searchStartY, nextLineLength, testAngle - (searchAngleRange/2) +  (searchAngleRange/searchAngleTests));
                }
                curvePoints.add(new int[]{clampX(darkest_x, pixels.getWidth()), clampY(darkest_y, pixels.getHeight())});
                searchStartX = darkest_x;
                searchStartY = darkest_y;
            }

            resetLuminanceSamples();
            int[] xPoints = new int[curvePoints.size()];
            int[] yPoints = new int[curvePoints.size()];
            for(int p = 0; p < curvePoints.size(); p++){
                xPoints[p] = curvePoints.get(p)[0];
                yPoints[p] = curvePoints.get(p)[1];
            }
            //TODO MAKE ME WORK WITH EXTRA LENGTH ONES
            bresenham.plotCatmullRom(xPoints[0], yPoints[0], xPoints[1], yPoints[1], xPoints[2], yPoints[2], xPoints[3], yPoints[4], task.getPathBuilder().getCatmullTension(), (xT, yT)-> luminanceTest(pixels, xT, yT));

            float curveLuminance = getLuminanceTestAverage();
            if(luminanceTest == -1F || curveLuminance < luminanceTest){
                darkestCurve = curvePoints;
                luminanceTest = curveLuminance;
            }
        }

        ///set the end of the darkest curve as the next darkest point, for the next call to findDarkestNeighbour
        darkest_x = darkestCurve.get(pointPerCurve-1)[0];
        darkest_y = darkestCurve.get(pointPerCurve-1)[1];
        lastCurve = darkestCurve;

    }

    public void addGeometry(IPlottingTask task, int x1, int y1, int x2, int y2, int adjust){

        GPath newPath = new GPath();
        newPath.moveTo(lastCurve.get(0)[0], lastCurve.get(0)[1]);
        newPath.curveTo(lastCurve.get(1)[0], lastCurve.get(1)[1], lastCurve.get(2)[0], lastCurve.get(2)[1], lastCurve.get(3)[0], lastCurve.get(3)[1]);


        resetColourSamples();
        int[] xPoints = new int[lastCurve.size()];
        int[] yPoints = new int[lastCurve.size()];
        for(int p = 0; p < lastCurve.size(); p++){
            xPoints[p] = lastCurve.get(p)[0];
            yPoints[p] = lastCurve.get(p)[1];
        }

        //TODO WORK WITH EXTRA LENGTH
        bresenham.plotCatmullRom(xPoints[0], yPoints[0], xPoints[1], yPoints[1], xPoints[2], yPoints[2], xPoints[3], yPoints[4], task.getPathBuilder().getCatmullTension(), (xT, yT)-> adjustLuminanceColour(task.getPixelData(), xT, yT, adjust));
              //bresenham.plotCubicSpline(lastCurve.size(), xPoints, yPoints, (xT, yT) -> luminanceTest(task.getPixelData(), xT, yT));
        task.addGeometry(newPath, null, getColourTestAverage());
    }

}
