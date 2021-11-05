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

    /**
     * luminance test for angled lines

    protected void luminanceTestAngledLine(IPixelData pixels, int originX, int originY, float distance, float degree){
        int x1 = (int)(Math.cos(Math.toRadians(degree))*distance) + originX;
        int y1 = (int)(Math.sin(Math.toRadians(degree))*distance) + originY;
        luminanceTestLine(pixels, originX, originY, x1, y1);
    }

    /**
     * luminance test for straight lines

    protected void luminanceTestLine(IPixelData pixels, int x0, int y0, int x1, int y1) {
        resetLuminanceSamples();

        //x0 = clampX(x0, pixels.getWidth());
        //y0 = clampY(y0, pixels.getHeight());

        bresenham.plotLine(x0, y0, x1, y1, (x, y) -> luminanceTest(pixels, x, y));
    }

    /**
     * luminance test for individual pixels and STOPS prematurely

    protected boolean luminanceTest(IPixelData pixels, int x, int y){
        if(x < 0 || x >= pixels.getWidth() || y < 0 || y >= pixels.getHeight()){
            //if we're off the image we'll take the last result and stop there
            if(test_luminance == -1 && count_pixels > minLineLength()){
                onLuminanceTestSuccess(pixels, x, y);
            }
            return true;
        }
        sum_luminance += pixels.getLuminance(x, y);
        count_pixels++;
        if ((test_luminance == -1 || getLuminanceTestAverage() < test_luminance) && count_pixels > minLineLength()) {
            onLuminanceTestSuccess(pixels, x, y);
        }
        return count_pixels > maxLineLength();
    }
    /*

    public void onLuminanceTestSuccess(IPixelData pixels, int x, int y){
        darkest_x = x;
        darkest_y = y;
        test_luminance = getLuminanceTestAverage();
    }

    protected void luminanceTally(IPixelData pixels, int x, int y){
        if(x < 0 || x >= pixels.getWidth() || y < 0 || y >= pixels.getHeight()){
            return;
        }
        sum_luminance += pixels.getLuminance(x, y);
        count_pixels++;
    }
*/



}
