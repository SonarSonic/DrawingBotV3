package drawingbot.pfm.helpers;

import drawingbot.api.IPixelData;

import java.awt.*;

public abstract class PixelTest {

    public Shape softClip = null;

    public abstract void addSample(IPixelData pixels, int x, int y);

    public boolean isPixelInvalid(IPixelData pixels, int x, int y){
        if(x < 0 || x >= pixels.getWidth() || y < 0 || y >= pixels.getHeight()){
            return true;
        }
        return softClip != null && !softClip.contains(x, y);
    }
}
