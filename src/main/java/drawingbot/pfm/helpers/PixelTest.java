package drawingbot.pfm.helpers;

import drawingbot.api.IPixelData;

public abstract class PixelTest {

    public abstract void addSample(IPixelData pixels, int x, int y);

    public static boolean isPixelInvalid(IPixelData pixels, int x, int y){
        return x < 0 || x >= pixels.getWidth() || y < 0 || y >= pixels.getHeight();
    }
}
