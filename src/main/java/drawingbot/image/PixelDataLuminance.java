package drawingbot.image;

/**
 * an implementation of {@link drawingbot.api.IPixelData} optimised for quick access to perceived luminance values and the cached average
 */
//TODO ISSUE WITH LUMINANCE VALUES?
public class PixelDataLuminance extends PixelDataAbstract {

    public RawData luminance;

    private float[] hsbCache = new float[3];
    private int[] argbCache = new int[4];

    public PixelDataLuminance(int width, int height) {
        super(width, height);
        this.luminance = new RawData(width, height);
    }

    @Override
    public int getARGB(int x, int y) {
        int g = luminance.getData(x, y);
        return ImageTools.getARGB(255, g, g, g);
    }

    @Override
    public void setARGB(int x, int y, int argb) {
        ImageTools.getColourIntsFromARGB(argb, argbCache);
        luminance.setData(x, y, ImageTools.getPerceivedLuminanceFromRGB(argbCache[1], argbCache[2], argbCache[3]));
    }

    @Override
    public void setARGB(int x, int y, int a, int r, int g, int b) {
        luminance.setData(x, y, ImageTools.getPerceivedLuminanceFromRGB(r, g, b));
    }

    @Override
    public int getChannel(int channel, int x, int y) {
        return luminance.getData(x, y);
    }

    @Override
    public void setChannel(int channel, int x, int y, int value) {
        luminance.setData(x, y, value);
    }

    @Override
    public void adjustChannel(int channel, int x, int y, int value) {
        luminance.adjustData(x, y, value);
    }

    @Override
    public float getAverageChannel(int channel) {
        return luminance.getAverage();
    }

    @Override
    public int getHSB(int type, int x, int y) {
        return 0;
    }

    @Override
    public void setHSB(int type, int x, int y, int value) {}

    @Override
    public void adjustHSB(int type, int x, int y, int value) {}

    @Override
    public float getAverageHSB(int type) {
        return 0;
    }

    @Override
    public int getLuminance(int x, int y) {
        return luminance.getData(x, y);
    }

    @Override
    public void setLuminance(int x, int y, int lum) {
        luminance.setData(x, y, lum);
    }

    @Override
    public void adjustLuminance(int x, int y, int lum) {
        luminance.adjustData(x, y, lum);
    }

    @Override
    public float getAverageLuminance() {
        return luminance.getAverage();
    }
}
