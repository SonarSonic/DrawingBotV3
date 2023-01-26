package drawingbot.pfm.helpers;

import drawingbot.api.IPixelData;

import java.awt.*;

public class LuminanceTestLine extends LuminanceTest {

    public int[] darkestDst;
    public int sampleIndex;
    public int minPixelCount;
    public int maxPixelCount;
    public boolean stopPrematurely;

    public LuminanceTestLine(){
        setup(new int[2], 0, Integer.MAX_VALUE, false, null);
    }

    public LuminanceTestLine(int[] darkestDst, int minPixelCount, int maxPixelCount, boolean stopPrematurely){
        setup(darkestDst, minPixelCount, maxPixelCount, stopPrematurely, null);
    }

    public LuminanceTestLine(int[] darkestDst, int minPixelCount, int maxPixelCount, boolean stopPrematurely, Shape softClip){
        setup(darkestDst, minPixelCount, maxPixelCount, stopPrematurely, softClip);
    }

    public void setup(int[] darkestDst, int minPixelCount, int maxPixelCount, boolean stopPrematurely, Shape softClip){
        this.darkestDst = darkestDst;
        this.minPixelCount = minPixelCount;
        this.maxPixelCount = maxPixelCount;
        this.stopPrematurely = stopPrematurely;
        this.sampleIndex = 0;
        this.softClip = softClip;
    }

    @Override
    public void addSample(IPixelData pixels, int x, int y){

        if(isPixelInvalid(pixels, x, y)){
            //if we're off the image we'll take the last result and stop there
            if(stopPrematurely && darkestSample == -1 && pixelCount > minPixelCount){
                darkestDst[0] = lastTestX;
                darkestDst[1] = lastTestY;
                darkestSample = getCurrentSample();
                sampleIndex = pixelCount;
            }
            return;
        }

        super.addSample(pixels, x, y);

        if((darkestSample == -1 || getCurrentSample() < darkestSample) && pixelCount > minPixelCount){
            darkestDst[0] = x;
            darkestDst[1] = y;
            darkestSample = getCurrentSample();
            sampleIndex = pixelCount;
        }
    }

    public float getSampleIndex(){
        return (float)sampleIndex / pixelCount;
    }

}
