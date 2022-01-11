package drawingbot.pfm.helpers;

import drawingbot.api.IPixelData;

public class LuminanceTestLine extends LuminanceTest {

    public final int[] darkestDst;
    public final int minPixelCount;
    public final int maxPixelCount;
    public final boolean stopPrematurely;

    public LuminanceTestLine(){
        this.darkestDst = new int[2];
        this.minPixelCount = 0;
        this.maxPixelCount = Integer.MAX_VALUE;
        this.stopPrematurely = false;
    }

    public LuminanceTestLine(int[] darkestDst, int minPixelCount, int maxPixelCount, boolean stopPrematurely){
        this.darkestDst = darkestDst;
        this.minPixelCount = minPixelCount;
        this.maxPixelCount = maxPixelCount;
        this.stopPrematurely = stopPrematurely;
    }

    @Override
    public void addSample(IPixelData pixels, int x, int y){

        if(isPixelInvalid(pixels, x, y)){
            //if we're off the image we'll take the last result and stop there
            if(stopPrematurely && darkestSample == -1 && pixelCount > minPixelCount){
                darkestDst[0] = lastTestX;
                darkestDst[1] = lastTestY;
                darkestSample = getCurrentSample();
            }
            return;
        }

        super.addSample(pixels, x, y);

        if((darkestSample == -1 || getCurrentSample() < darkestSample) && pixelCount > minPixelCount){
            darkestDst[0] = x;
            darkestDst[1] = y;
            darkestSample = getCurrentSample();
        }
    }

}
