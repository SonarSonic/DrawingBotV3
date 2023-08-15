package drawingbot.image;

import java.awt.*;

/**
 * an implementation of {@link drawingbot.api.IPixelData} optimised for quick access to both ARGB + Luminance (Y) values, and also provides cached averages for each
 */
public class PixelDataARGBY extends PixelDataARGB implements IPixelListenable {

    public RawData luminance;
    public IPixelListener preListener;
    public IPixelListener listener;

    public PixelDataARGBY(int width, int height) {
        super(width, height);
        this.luminance = new RawData(width, height);
    }

    @Override
    public void setChannel(int channel, int x, int y, int value) {
        if(preListener != null) {
            preChangeRGB(x, y, red.getData(x, y), green.getData(x, y), blue.getData(x, y));
        }
        super.setChannel(channel, x, y, value);
        onChangeRGB(x, y, red.getData(x, y), green.getData(x, y), blue.getData(x, y));
    }

    @Override
    public void adjustChannel(int channel, int x, int y, int value) {
        if(preListener != null){
            preChangeRGB(x, y, red.getData(x, y), green.getData(x, y), blue.getData(x, y));
        }
        super.adjustChannel(channel, x, y, value);
        onChangeRGB(x, y, red.getData(x, y), green.getData(x, y), blue.getData(x, y));
    }

    protected void preChangeRGB(int x, int y, int r, int g, int b){}

    protected void onChangeRGB(int x, int y, int r, int g, int b){
        luminance.setData(x, y, ImageTools.getPerceivedLuminanceFromRGB(r, g, b));
        if(listener != null){
            listener.onPixelChanged(x, y);
        }
    }

    @Override
    public void setARGB(int x, int y, int a, int r, int g, int b) {
        if(preListener != null){
            preChangeRGB(x, y, r, g, b);
        }

        super.setARGB(x, y, a, r, g, b);
        onChangeRGB(x, y, r, g, b);
    }

    @Override
    public int getLuminance(int x, int y) {
        return luminance.getData(x, y);
    }

    @Override
    public double getAverageLuminance() {
        return luminance.getAverage();
    }

    @Override
    public void addListener(IPixelListener listener) {
        this.listener = listener;
    }

    @Override
    public void removeListener(IPixelListener listener) {
        this.listener = null;
    }

    @Override
    public RawData getRawLuminanceData() {
        return luminance;
    }

    @Override
    public void setSoftClip(Shape softClip) {
        super.setSoftClip(softClip);
        luminance.setSoftClip(softClip);
    }

    @Override
    public void destroy() {
        super.destroy();
        luminance.destroy();
    }
}
