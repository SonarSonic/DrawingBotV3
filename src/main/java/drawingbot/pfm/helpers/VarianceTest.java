package drawingbot.pfm.helpers;

import drawingbot.api.IPixelData;

import java.util.function.Consumer;

public class VarianceTest extends PixelTest{

    public int max = 0;
    public int min = 255;

    public void resetTest(){
        max = 0;
        min = 255;
    }

    /**
     * @return lower the variance the better the match
     */
    public int getVariance(){
        return Math.abs(max-min);
    }

    public int getMedianLuminance(){
        return Math.abs(max-min) / 2;
    }

    public float getWeightedVariance(){
        float actualVariance = (getVariance() + min)/(255F + min);
        return (actualVariance*0.2F) + 0.8F;
    }

    @Override
    public void addSample(IPixelData pixels, int x, int y) {
        if(!isPixelInvalid(pixels, x, y)) {
            addSample(pixels.getLuminance(x, y));
        }
    }

    public void addSample(int luminance) {
        max = Math.max(max, luminance);
        min = Math.min(min, luminance);
    }

    public int runVarianceTest(IPixelData data, Consumer<BresenhamHelper.IPixelSetter> testConsumer){
        resetTest();
        testConsumer.accept((xT, yT) -> addSample(data, xT, yT));
        return getVariance();
    }
}
