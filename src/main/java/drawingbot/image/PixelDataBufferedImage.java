package drawingbot.image;

import drawingbot.utils.Utils;

import java.awt.image.BufferedImage;

public class PixelDataBufferedImage extends PixelDataAbstract {

    public BufferedImage image;
    public RawData data;

    public PixelDataBufferedImage(BufferedImage image){
        super(image.getWidth(), image.getHeight());
        this.image = image;
        this.data = new RawData(width, height);
        this.loadData();
    }

    private void loadData(){
        for(int x = 0; x < getWidth(); x++){
            for(int y = 0; y < getHeight(); y++){
                data.setData(x, y, ImageTools.getPerceivedLuminanceFromRGB(image.getRGB(x, y)));
            }
        }
    }

    @Override
    public int getARGB(int x, int y) {
        return image.getRGB(x, y);
    }

    @Override
    public void setARGB(int x, int y, int argb) {
        image.setRGB(x, y, argb);
        data.setData(x, y, ImageTools.getPerceivedLuminanceFromRGB(argb));
    }

    @Override
    public void setARGB(int x, int y, int a, int r, int g, int b) {
        image.setRGB(x, y, ImageTools.getARGB(a, r, g, b));
        data.setData(x, y, ImageTools.getPerceivedLuminanceFromRGB(r, g, b));
    }

    @Override
    public int getChannel(int channel, int x, int y) {
        return image.getRGB(x, y);
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
    public int getHSB(int type, int x, int y) {
        return 0; //TODO
    }

    @Override
    public void setHSB(int type, int x, int y, int value) {
        //TODO
    }

    @Override
    public void adjustHSB(int type, int x, int y, int value) {
        //TODO
    }

    @Override
    public double getAverageHSB(int type) {
        return 0; //TODO
    }

    @Override
    public int getLuminance(int x, int y) {
        return data.getData(x, y);
    }

    @Override
    public void setLuminance(int x, int y, int luminance) {
        //TODO
    }

    @Override
    public void adjustLuminance(int x, int y, int luminance) {
        //TODO
    }

    @Override
    public double getAverageLuminance() {
        return data.getAverage();
    }
}
