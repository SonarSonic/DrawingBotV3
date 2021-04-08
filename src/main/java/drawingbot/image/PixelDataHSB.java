package drawingbot.image;

import java.awt.*;

/**
 * an implementation of {@link drawingbot.api.IPixelData} optimised for quick access to HSB values and their cached averages
 */
public class PixelDataHSB extends PixelDataAbstract {

    public RawData hue;
    public RawData saturation;
    public RawData brightness;

    private final float[] hsbCache = new float[3];
    private final int[] argbCache = new int[4];

    public PixelDataHSB(int width, int height) {
        super(width, height);
        this.height = height;
        this.hue = new RawData(width, height);
        this.saturation = new RawData(width, height);
        this.brightness = new RawData(width, height);
    }

    public RawData getRawData(int type) {
        switch (type){
            case 0:
                return hue;
            case 1:
                return saturation;
            case 2:
                return brightness;
        }
        return null;
    }

    @Override
    public int getARGB(int x, int y) {
        int h = hue.getData(x, y);
        int s = saturation.getData(x, y);
        int b = brightness.getData(x, y);
        return Color.HSBtoRGB(h/255F, s/255F, b/255F);
    }

    @Override
    public void setARGB(int x, int y, int argb) {
        ImageTools.getColourIntsFromARGB(argb, argbCache);
        setARGB(x, y, argbCache[0], argbCache[1], argbCache[2], argbCache[3]);
    }

    @Override
    public void setARGB(int x, int y, int a, int r, int g, int b) {
        Color.RGBtoHSB(r, g, b, hsbCache);
        hue.setData(x, y, (int)(hsbCache[0] * 255));
        saturation.setData(x, y, (int)(hsbCache[1] * 255));
        brightness.setData(x, y, (int)(hsbCache[2] * 255));
    }

    @Override
    public int getChannel(int channel, int x, int y) {
        int h = hue.getData(x, y);
        int s = saturation.getData(x, y);
        int b = brightness.getData(x, y);
        return ImageTools.getColourIntsFromARGB(Color.HSBtoRGB(h/255F, s/255F, b/255F), argbCache)[channel];
    }

    @Override
    public void setChannel(int channel, int x, int y, int value) {
        int h = hue.getData(x, y);
        int s = saturation.getData(x, y);
        int b = brightness.getData(x, y);
        ImageTools.getColourIntsFromARGB(Color.HSBtoRGB(h, s, b), argbCache);
        argbCache[channel] = value; //set value
        Color.RGBtoHSB(argbCache[1], argbCache[2], argbCache[3], hsbCache);
        hue.setData(x, y, (int)(hsbCache[0] * 255));
        saturation.setData(x, y, (int)(hsbCache[1] * 255));
        brightness.setData(x, y, (int)(hsbCache[2] * 255));
    }

    @Override
    public void adjustChannel(int channel, int x, int y, int value) {
        int h = hue.getData(x, y);
        int s = saturation.getData(x, y);
        int b = brightness.getData(x, y);
        ImageTools.getColourIntsFromARGB(Color.HSBtoRGB(h, s, b), argbCache);
        argbCache[channel] = Math.max(0, Math.min((argbCache[channel]) + value, 255)); //set value
        Color.RGBtoHSB(argbCache[1], argbCache[2], argbCache[3], hsbCache);
        hue.setData(x, y, (int)(hsbCache[0] * 255));
        saturation.setData(x, y, (int)(hsbCache[1] * 255));
        brightness.setData(x, y, (int)(hsbCache[2] * 255));
    }

    @Override
    public double getAverageChannel(int channel) {
        int h = (int)hue.getAverage();
        int s = (int)saturation.getAverage();
        int b = (int)brightness.getAverage();
        return ImageTools.getColourIntsFromARGB(Color.HSBtoRGB(h/255F, s/255F, b/255F), argbCache)[channel];
    }

    @Override
    public int getHSB(int type, int x, int y) {
        return getRawData(type).getData(x, y);
    }

    @Override
    public void setHSB(int type, int x, int y, int value) {
        getRawData(type).setData(x, y, value);
    }

    @Override
    public void adjustHSB(int type, int x, int y, int value) {
        getRawData(type).adjustData(x, y, value);
    }

    @Override
    public double getAverageHSB(int type) {
        return getRawData(type).getAverage();
    }
}
