package drawingbot.image;

import drawingbot.api.IPixelData;

public class PixelDataGray implements IPixelData {

    public int width, height;
    public RawData gray;

    private float[] hsbCache = new float[3];
    private int[] argbCache = new int[4];

    public PixelDataGray(int width, int height) {
        this.width = width;
        this.height = height;
        this.gray = new RawData(width, height);
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
    public int getARGB(int x, int y) {
        int g = gray.getData(x, y);
        return ImageTools.getARGB(255, g, g, g);
    }

    @Override
    public void setARGB(int x, int y, int argb) {
        int lum = (77*(argb>>16&0xff) + 151*(argb>>8&0xff) + 28*(argb&0xff))>>8;
        gray.setData(x, y, lum);
    }

    @Override
    public void setARGB(int x, int y, int a, int r, int g, int b) {
        int lum = (77*r + 151*g + 28*b)>>8;
        gray.setData(x, y, lum);
    }

    @Override
    public int getChannel(int channel, int x, int y) {
        return gray.getData(x, y);
    }

    @Override
    public void setChannel(int channel, int x, int y, int value) {
        gray.setData(x, y, value);
    }

    @Override
    public void adjustChannel(int channel, int x, int y, int value) {
        gray.adjustData(x, y, value);
    }

    @Override
    public float getAverageChannel(int channel) {
        return gray.getAverage();
    }

    @Override
    public int getHSB(int type, int x, int y) {
        return type == 2 ? gray.getData(x, y) : 0;
    }

    @Override
    public void setHSB(int type, int x, int y, int value) {
        if(type == 2){
            gray.setData(x, y, value);
        }
    }

    @Override
    public void adjustHSB(int type, int x, int y, int value) {
        if(type == 2){
            gray.adjustData(x, y, value);
        }
    }

    @Override
    public float getAverageHSB(int type) {
        return type == 2 ? gray.getAverage() : 0;
    }
}
