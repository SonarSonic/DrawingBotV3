package drawingbot.pfm.helpers;

import drawingbot.api.IPixelData;
import drawingbot.image.ImageTools;

public class ColourSampleTest extends PixelTest {

    //colour samples
    protected int sum_red = 0;
    protected int sum_green = 0;
    protected int sum_blue = 0;
    protected int sum_alpha = 0;
    protected int total_pixels = 0;

    public int adjustLum = 0;

    public int getSampleCount(){
        return total_pixels;
    }

    public void resetColourSamples(int adjust){
        sum_red = 0;
        sum_green = 0;
        sum_blue = 0;
        sum_alpha = 0;
        total_pixels = 0;
        adjustLum = adjust;
    }

    public int getCurrentAverage(){
        if(total_pixels == 0){
            return 0;
        }
        return ImageTools.getARGB(sum_alpha / total_pixels, sum_red /total_pixels, sum_green / total_pixels, sum_blue / total_pixels);
    }

    public int getAndResetColourSamples(){
        int average = getCurrentAverage();
        resetColourSamples(0);
        return average;
    }

    public void addSample(int argb){
        sum_alpha += ImageTools.alpha(argb);
        sum_red += ImageTools.red(argb);
        sum_green += ImageTools.green(argb);
        sum_blue += ImageTools.blue(argb);
        total_pixels++;
    }

    @Override
    public void addSample(IPixelData pixels, int x, int y) {
        if(x < 0 || x >= pixels.getWidth() || y < 0 || y >= pixels.getHeight()){
            return;
        }
        sum_alpha += pixels.getAlpha(x, y);
        sum_red += pixels.getRed(x, y);
        sum_green += pixels.getGreen(x, y);
        sum_blue += pixels.getBlue(x, y);
        if(adjustLum != 0){
            pixels.adjustRed(x, y, adjustLum);
            pixels.adjustGreen(x, y, adjustLum);
            pixels.adjustBlue(x, y, adjustLum);
        }
        total_pixels++;
    }

}
