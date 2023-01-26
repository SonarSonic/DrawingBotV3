package drawingbot.image;

import java.awt.*;

/**
 * an implementation of {@link drawingbot.api.IPixelData} optimised for quick access of both ARGB + HSB values, and also provides cached averages for each
 */
public class PixelDataHybrid extends PixelDataARGB {

    public RawData hue;
    public RawData saturation;
    public RawData brightness;
    public RawData luminance;

    public PixelDataHybrid(int width, int height) {
        super(width, height);
        this.hue = new RawData(width, height);
        this.saturation = new RawData(width, height);
        this.brightness = new RawData(width, height);
        this.luminance = new RawData(width, height);
    }

    public RawData getRawDataHSB(int type) {
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
    public void setChannel(int channel, int x, int y, int value) {
        super.setChannel(channel, x, y, value);
        onChangedRGB(x, y, red.getData(x, y), green.getData(x, y), blue.getData(x, y));
    }

    @Override
    public void adjustChannel(int channel, int x, int y, int value) {
        super.adjustChannel(channel, x, y, value);
        onChangedRGB(x, y, red.getData(x, y), green.getData(x, y), blue.getData(x, y));
    }

    @Override
    public int getHSB(int type, int x, int y) {
        return getRawDataHSB(type).getData(x, y);
    }

    @Override
    public void setHSB(int type, int x, int y, int value) {
        getRawDataHSB(type).setData(x, y, value);
        onChangedHSB(x, y, hue.getData(x, y), saturation.getData(x, y), brightness.getData(x, y));
    }

    @Override
    public void adjustHSB(int type, int x, int y, int value) {
        getRawDataHSB(type).adjustData(x, y, value);
        onChangedHSB(x, y, hue.getData(x, y), saturation.getData(x, y), brightness.getData(x, y));
    }

    protected void onChangedRGB(int x, int y, int r, int g, int b){
        updateRGBtoHSBCache(r, g, b);
        hue.setData(x, y, (int)(RGBtoHSBCache[0]*255));
        saturation.setData(x, y, (int)(RGBtoHSBCache[1]*255));
        brightness.setData(x, y, (int)(RGBtoHSBCache[2]*255));
        luminance.setData(x, y, ImageTools.getPerceivedLuminanceFromRGB(r, g, b));
    }

    @Override
    public double getAverageHSB(int type) {
        return getRawDataHSB(type).getAverage();
    }

    @Override
    public void setARGB(int x, int y, int a, int r, int g, int b) {
        super.setARGB(x, y, a, r, g, b);
        onChangedRGB(x, y, r, g, b);
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
    public RawData getRawLuminanceData() {
        return luminance;
    }

    @Override
    public void setSoftClip(Shape softClip) {
        super.setSoftClip(softClip);
        hue.setSoftClip(softClip);
        saturation.setSoftClip(softClip);
        brightness.setSoftClip(softClip);
        luminance.setSoftClip(softClip);
    }
}
