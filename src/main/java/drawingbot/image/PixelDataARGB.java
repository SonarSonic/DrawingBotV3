package drawingbot.image;

import drawingbot.api.IPixelDataHSB;

import java.awt.*;

/**
 * an implementation of {@link drawingbot.api.IPixelData} optimised for quick access to RGB values and their cached averages
 */
public class PixelDataARGB extends PixelDataAbstract implements IPixelDataHSB {

    public RawData alpha;
    public RawData red;
    public RawData green;
    public RawData blue;

    private final int[] cacheARGBtoARGB = new int[4];

    public PixelDataARGB(int width, int height) {
        super(width, height);
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
    public int getAlpha(int x, int y) {
        return alpha.getData(x, y);
    }

    @Override
    public int getRed(int x, int y) {
        return red.getData(x, y);
    }

    @Override
    public int getGreen(int x, int y) {
        return green.getData(x, y);
    }

    @Override
    public int getBlue(int x, int y) {
        return blue.getData(x, y);
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
    public double getAverageChannel(int channel) {
        return getRawData(channel).getAverage();
    }

    @Override
    public int getHSB(int type, int x, int y) {
        int r = red.getData(x, y);
        int g = green.getData(x, y);
        int b = blue.getData(x, y);
        return (int)(updateRGBtoHSBCache(r, g, b)[type]*255);
    }

    @Override
    public void setHSB(int type, int x, int y, int value) {
        float[] hsb = copyHSB(updateRGBtoHSBCache(red.getData(x, y), green.getData(x, y), blue.getData(x, y)));
        hsb[type] = value/255F; //set value
        onChangedHSB(x, y, hsb[0], hsb[1], hsb[2]);
    }

    @Override
    public void adjustHSB(int type, int x, int y, int value) {
        float[] hsb = copyHSB(updateRGBtoHSBCache(red.getData(x, y), green.getData(x, y), blue.getData(x, y)));
        hsb[type] = Math.max(0, Math.min((hsb[type]*255) + value, 255))/255F; //adjust value
        onChangedHSB(x, y, hsb[0], hsb[1], hsb[2]);
    }

    /**update the RGB cache*/
    protected void onChangedHSB(int x, int y, float hue, float saturation, float brightness){
        int[] rgb = updateHSBtoRGBCache(hue, saturation, brightness);
        red.setData(x, y, rgb[1]);
        green.setData(x, y, rgb[2]);
        blue.setData(x, y, rgb[3]);
    }

    @Override
    public double getAverageHSB(int type) {
        int r = (int)red.getAverage();
        int g = (int)green.getAverage();
        int b = (int)blue.getAverage();
        return (updateRGBtoHSBCache(r, g, b)[type]*255);
    }

    @Override
    public int getARGB(int x, int y) {
        return ImageTools.getARGB(getAlpha(x, y), getRed(x, y), getGreen(x, y), getBlue(x, y));
    }

    @Override
    public void setARGB(int x, int y, int argb) {
        ImageTools.getColourIntsFromARGB(argb, cacheARGBtoARGB);
        setARGB(x, y, cacheARGBtoARGB[0], cacheARGBtoARGB[1], cacheARGBtoARGB[2], cacheARGBtoARGB[3]);
    }

    @Override
    public void setARGB(int x, int y, int a, int r, int g, int b) {
        alpha.setData(x, y, a);
        red.setData(x, y, r);
        green.setData(x, y, g);
        blue.setData(x, y, b);
    }

    @Override
    public void setSoftClip(Shape softClip) {
        super.setSoftClip(softClip);
        alpha.setSoftClip(softClip);
        red.setSoftClip(softClip);
        green.setSoftClip(softClip);
        blue.setSoftClip(softClip);
    }

    @Override
    public void destroy() {
        alpha.destroy();
        red.destroy();
        green.destroy();
        blue.destroy();
    }
}
