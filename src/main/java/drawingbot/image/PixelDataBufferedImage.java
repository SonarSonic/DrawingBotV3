package drawingbot.image;

import drawingbot.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PixelDataBufferedImage extends PixelDataAbstract implements IPixelListener, IPixelListenable {

    public RawData data;
    public BufferedImage image;
    public IPixelListener listener;

    public PixelDataBufferedImage(int width, int height){
        super(width, height);
        this.data = new RawData(width, height);
        this.image = ObservableWritableRaster.createObservableBufferedImage(width, height, this);
    }

    @Override
    public void onPixelChanged(int x, int y) {
        int argb = image.getRGB(x, y);
        data.setData(x, y, ImageTools.alpha(argb) == 0 ? 255 : ImageTools.getPerceivedLuminanceFromRGB(ImageTools.red(argb), ImageTools.green(argb), ImageTools.blue(argb)));
        if(listener != null){
            listener.onPixelChanged(x, y);
        }
    }

    @Override
    public int getARGB(int x, int y) {
        return image.getRGB(x, y);
    }

    @Override
    public void setARGB(int x, int y, int argb) {
        image.setRGB(x, y, argb);
    }

    @Override
    public void setARGB(int x, int y, int a, int r, int g, int b) {
        image.setRGB(x, y, ImageTools.getARGB(a, r, g, b));
    }

    @Override
    public int getChannel(int channel, int x, int y) {
        int argb = image.getRGB(x, y);
        switch (channel){
            case 0:
                return ImageTools.alpha(argb);
            case 1:
                return ImageTools.red(argb);
            case 2:
                return ImageTools.green(argb);
            case 3:
                return ImageTools.blue(argb);
        }
        return 0;
    }

    @Override
    public void setChannel(int channel, int x, int y, int value) {
        updateARGBCache(getARGB(x, y));
        switch (channel){
            case 0:
                setARGB(x, y, value, cacheARGB[1], cacheARGB[2], cacheARGB[3]);
                break;
            case 1:
                setARGB(x, y, cacheARGB[0], value, cacheARGB[2], cacheARGB[3]);
                break;
            case 2:
                setARGB(x, y, cacheARGB[0], cacheARGB[1], value, cacheARGB[3]);
                break;
            case 3:
                setARGB(x, y, cacheARGB[0], cacheARGB[1], cacheARGB[2], value);
                break;
        }
    }

    @Override
    public void adjustChannel(int channel, int x, int y, int value) {
        updateARGBCache(getARGB(x, y));
        int adjusted = Utils.clamp(cacheARGB[channel] + value, 0, 255);
        switch (channel){
            case 0:
                setARGB(x, y, adjusted, cacheARGB[1], cacheARGB[2], cacheARGB[3]);
                break;
            case 1:
                setARGB(x, y, cacheARGB[0], adjusted, cacheARGB[2], cacheARGB[3]);
                break;
            case 2:
                setARGB(x, y, cacheARGB[0], cacheARGB[1], adjusted, cacheARGB[3]);
                break;
            case 3:
                setARGB(x, y, cacheARGB[0], cacheARGB[1], cacheARGB[2], adjusted);
                break;
        }

    }

    @Override
    public double getAverageChannel(int channel) {
        return 0; //TODO
    }


    @Override
    public int getLuminance(int x, int y) {
        return data.getData(x, y);
    }

    /*
    @Override
    public void setLuminance(int x, int y, int luminance) {
        setARGB(x, y, getAlpha(x, y), luminance, luminance, luminance);
    }

    @Override
    public void adjustLuminance(int x, int y, int luminance) {
        setARGB(x, y, getAlpha(x, y), luminance, luminance, luminance);
    }
    */

    @Override
    public double getAverageLuminance() {
        return data.getAverage();
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
        return data;
    }

    @Override
    public void setSoftClip(Shape softClip) {
        super.setSoftClip(softClip);
        data.setSoftClip(softClip);
    }

    @Override
    public BufferedImage asBufferedImage() {
        return image;
    }

    @Override
    public void destroy() {
        data.destroy();
        image.flush();
        image = null;
    }
}
