package drawingbot.pfm.helpers;

import drawingbot.api.IPixelData;
import drawingbot.image.ImageTools;

public class ColourSampleTestAlpha extends ColourSampleTest {
    
    @Override
    public void addSample(IPixelData pixels, int x, int y) {
        if(x < 0 || x >= pixels.getWidth() || y < 0 || y >= pixels.getHeight()){
            return;
        }
        sum_alpha += pixels.getAlpha(x, y);
        pixels.adjustAlpha(x, y, -adjustLum);
        total_pixels++;
    }

    @Override
    public int getCurrentAverage(){
        if(total_pixels == 0){
            return 0;
        }
        return ImageTools.getARGB(sum_alpha / total_pixels, sum_red /total_pixels, sum_green / total_pixels, sum_blue / total_pixels);
    }    
    
}
