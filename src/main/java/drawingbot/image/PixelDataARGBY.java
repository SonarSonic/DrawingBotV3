package drawingbot.image;

/**
 * an implementation of {@link drawingbot.api.IPixelData} optimised for quick access to both ARGB + Luminance (Y) values, and also provides cached averages for each
 */
public class PixelDataARGBY extends PixelDataARGB {

    public RawData luminance;

    public PixelDataARGBY(int width, int height) {
        super(width, height);
        this.luminance = new RawData(width, height);
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

    protected void onChangedRGB(int x, int y, int r, int g, int b){
        luminance.setData(x, y, ImageTools.getPerceivedLuminanceFromRGB(r, g, b));
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
}
