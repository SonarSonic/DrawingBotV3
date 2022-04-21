package drawingbot.pfm;

import drawingbot.api.IPFMImage;
import drawingbot.api.IPixelData;
import drawingbot.image.PixelDataARGB;
import drawingbot.image.PixelDataARGBY;
import drawingbot.utils.Utils;

public abstract class AbstractPFMImage extends AbstractPFM implements IPFMImage {

    public int transparentARGB = -1;

    @Override
    public IPixelData createPixelData(int width, int height) {
        return new PixelDataARGBY(width, height);
    }

    @Override
    public int getTransparentARGB() {
        return transparentARGB;
    }

    public int clampX(int x){
        return Utils.clamp(x, 0, tools.getPixelData().getWidth()-1);
    }

    public int clampY(int y){
        return Utils.clamp(y, 0, tools.getPixelData().getHeight()-1);
    }

    public boolean withinX(int x){
        return Utils.within(x, 0, tools.getPixelData().getWidth()-1);
    }

    public boolean withinY(int y){
        return Utils.within(y, 0, tools.getPixelData().getHeight()-1);
    }

    public boolean withinXY(int x, int y){
        return withinX(x) && withinY(y);
    }
}
