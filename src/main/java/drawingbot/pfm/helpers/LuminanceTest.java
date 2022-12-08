package drawingbot.pfm.helpers;

import drawingbot.api.IPixelData;

public class LuminanceTest extends PixelTest {

    protected int luminanceSum = 0;
    protected int pixelCount = 0;
    protected float darkestSample = -1;

    protected int lastTestX = -1;
    protected int lastTestY = -1;

    protected float lastResult = -1;

    public int getPixelCount(){
        return pixelCount;
    }

    public void resetTest(){
        darkestSample = -1;
    }

    public void resetSamples(){
        luminanceSum = 0;
        pixelCount = 0;
    }

    public float getCurrentSample(){
        return (float) luminanceSum / (float) pixelCount;
    }

    public float getDarkestSample(){
        return darkestSample;
    }

    /**
     * Tests the current sample against the darkest sample.
     * The darkest sample will be set to the darker of the two.
     *
     * @return true if the current sample was darker
     */
    public boolean getAndSetTestResult(){
        float average = getCurrentSample();
        boolean test = darkestSample == -1 || average < darkestSample;
        if(test){
            darkestSample = average;
        }
        return test;
    }

    public void addSample(int luminance){
        luminanceSum += luminance;
        pixelCount++;
    }

    @Override
    public void addSample(IPixelData pixels, int x, int y){
        if(!isPixelInvalid(pixels, x, y)){
            luminanceSum += pixels.getLuminance(x, y);
            pixelCount++;
            lastTestX = x;
            lastTestY = y;
        }
    }

}
