package drawingbot.image;

import drawingbot.api.IPixelData;

import java.awt.*;

public class PixelDataARGB implements IPixelData {

    public int width, height;
    public RawData alpha;
    public RawData red;
    public RawData green;
    public RawData blue;
    public int transparentARGB = -1;

    private float[] hsbCache = new float[3];
    private int[] argbCache = new int[4];

    public PixelDataARGB(int width, int height) {
        this.width = width;
        this.height = height;
        this.alpha = new RawData(width, height);
        this.red = new RawData(width, height);
        this.green = new RawData(width, height);
        this.blue = new RawData(width, height);
    }

    public RawData getRawData(int channel) {
        switch (channel){
            case 0:
                return alpha;
            case 1:
                return red;
            case 2:
                return green;
            case 3:
                return blue;
        }
        return null;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getChannel(int channel, int x, int y) {
        return getRawData(channel).getData(x, y);
    }

    @Override
    public void setChannel(int channel, int x, int y, int value) {
        getRawData(channel).setData(x, y, value);
    }

    @Override
    public void adjustChannel(int channel, int x, int y, int value) {
        getRawData(channel).adjustData(x, y, value);
    }

    @Override
    public float getAverageChannel(int channel) {
        return getRawData(channel).getAverage();
    }

    @Override
    public int getHSB(int type, int x, int y) {
        int r = red.getData(x, y);
        int g = green.getData(x, y);
        int b = blue.getData(x, y);
        return (int)(Color.RGBtoHSB(r, g, b, hsbCache)[type]*255);
    }

    @Override
    public void setHSB(int type, int x, int y, int value) {
        int r = red.getData(x, y);
        int g = green.getData(x, y);
        int b = blue.getData(x, y);
        Color.RGBtoHSB(r, g, b, hsbCache);
        hsbCache[type] = value/255F; //set value
        int rgb = Color.HSBtoRGB(hsbCache[0], hsbCache[1], hsbCache[2]);
        red.setData(x, y, (rgb>>16)&0xff);
        green.setData(x, y, (rgb>>8)&0xff);
        blue.setData(x, y, rgb&0xff);
    }

    @Override
    public void adjustHSB(int type, int x, int y, int value) {
        int r = red.getData(x, y);
        int g = green.getData(x, y);
        int b = blue.getData(x, y);
        Color.RGBtoHSB(r, g, b, hsbCache);
        hsbCache[type] = Math.max(0, Math.min((hsbCache[type]*255) + value, 255))/255F; //adjust value
        int rgb = Color.HSBtoRGB(hsbCache[0], hsbCache[1], hsbCache[2]);
        red.setData(x, y, (rgb>>16)&0xff);
        green.setData(x, y, (rgb>>8)&0xff);
        blue.setData(x, y, rgb&0xff);
    }

    @Override
    public float getAverageHSB(int type) {
        int r = (int)red.getAverage();
        int g = (int)green.getAverage();
        int b = (int)blue.getAverage();
        return (Color.RGBtoHSB(r, g, b, hsbCache)[type]*255);
    }

    @Override
    public int getARGB(int x, int y) {
        return ImageTools.getARGB(getAlpha(x, y), getRed(x, y), getGreen(x, y), getBlue(x, y));
    }

    @Override
    public void setARGB(int x, int y, int argb) {
        ImageTools.getColourIntsFromARGB(argb, argbCache);
        setAlpha(x, y, argbCache[0]);
        setRed(x, y, argbCache[1]);
        setGreen(x, y, argbCache[2]);
        setBlue(x, y, argbCache[3]);
    }

    @Override
    public void setARGB(int x, int y, int a, int r, int g, int b) {
        setAlpha(x, y, a);
        setRed(x, y, r);
        setGreen(x, y, g);
        setBlue(x, y, b);
    }

    @Override
    public int getTransparentARGB() {
        return transparentARGB;
    }

    @Override
    public void setTransparentARGB(int argb) {
        transparentARGB = argb;
    }
}
