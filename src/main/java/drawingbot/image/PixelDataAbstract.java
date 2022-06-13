package drawingbot.image;

import drawingbot.api.IPixelData;
import drawingbot.utils.Utils;

import java.awt.*;

public abstract class PixelDataAbstract implements IPixelData {

    public int width, height;
    public int transparentARGB = -1;

    public PixelDataAbstract(int width, int height) {
        this.width = width;
        this.height = height;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int getTransparentARGB() {
        return transparentARGB;
    }

    @Override
    public void setTransparentARGB(int argb) {
        transparentARGB = argb;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int getLuminance(int x, int y) {
        return ImageTools.getPerceivedLuminanceFromRGB(getARGB(x, y));
    }

    @Override
    public void setLuminance(int x, int y, int luminance) {
        setARGB(x, y, getAlpha(x, y), luminance, luminance, luminance);
    }

    @Override
    public void adjustLuminance(int x, int y, int luminance) {
        setLuminance(x, y, Utils.clamp(getLuminance(x, y) + luminance,0,255));
    }

    @Override
    public double getAverageLuminance() {
        double r = getAverageRed();
        double g = getAverageGreen();
        double b = getAverageBlue();
        return ImageTools.getAverageLuminanceFromRGB((float)r, (float)g, (float)b);
    }

    public int[][] createARGBData(){
        int[][] data = new int[width][height];
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                data[x][y] = getARGB(x, y);
            }
        }
        return data;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    protected Integer prevRed, prevGreen, prevBlue;
    protected Float prevHue, prevSat, prevBri;
    protected float[] RGBtoHSBCache = new float[3];
    protected int[] HSBtoRGBCache = new int[4];

    protected Integer prevARGB;
    protected int[] cacheARGB = new int[4];

    public float[] copyHSB(float[] hsb){
        float[] copy = new float[3];
        copy[0] = hsb[0];
        copy[1] = hsb[1];
        copy[2] = hsb[2];
        return copy;
    }

    protected float[] updateRGBtoHSBCache(int r, int g, int b){
        if(prevRed == null || r != prevRed || g != prevGreen || b != prevBlue){
            Color.RGBtoHSB(r, g, b, RGBtoHSBCache);
            prevRed = r;
            prevGreen = g;
            prevBlue = b;
        }
        return RGBtoHSBCache;
    }

    protected void flushRGBtoHSBCache(){
        prevRed = null;
        prevGreen = null;
        prevBlue = null;
        RGBtoHSBCache = new float[3];
    }

    public int[] copyARGB(int[] argb){
        int[] copy = new int[4];
        copy[0] = HSBtoRGBCache[0];
        copy[1] = HSBtoRGBCache[1];
        copy[2] = HSBtoRGBCache[2];
        copy[3] = HSBtoRGBCache[3];
        return copy;
    }

    protected int[] updateHSBtoRGBCache(float h, float s, float b){
        if(prevHue == null || h != prevHue || s != prevSat || b != prevBri){
            int rgb = Color.HSBtoRGB(h, s, b);
            prevHue = h;
            prevSat = s;
            prevBri = b;
            ImageTools.getColourIntsFromARGB(rgb, HSBtoRGBCache);
        }
        return HSBtoRGBCache;
    }

    protected void flushHSBtoRGBCache(){
        prevHue = null;
        prevSat = null;
        prevBri = null;
        HSBtoRGBCache = new int[4];
    }

    protected int[] updateARGBCache(int argb){
        if(prevARGB == null || argb != prevARGB){
            prevARGB = argb;
            ImageTools.getColourIntsFromARGB(argb, cacheARGB);
        }
        return HSBtoRGBCache;
    }

    protected void flushARGBCache(){
        prevARGB = null;
        cacheARGB = new int[4];
    }
}

