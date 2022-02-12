package drawingbot.pfm.helpers;

import drawingbot.api.IPixelData;

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
    
}
