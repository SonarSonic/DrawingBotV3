package drawingbot.image;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTools;

/**
 * Essentially a fast implementation of {@link drawingbot.pfm.AbstractDarkestPFM#findDarkestArea(IPixelData, int[])}
 * It splits the image into tiles and keeps track of the total darkness of each tile so it can provide the darkest pixel in that tile as fast as possible.
 */
public class PixelTargetDarkestArea extends PixelTargetCache implements RawData.IDataListener {

    public IPlottingTools tools;
    public IPixelData data;

    private final int sampleWidth = 10;
    private final int sampleHeight = 10;

    private int totalSamplesX;
    private int totalSamplesY;

    private double[][] tileSamples;
    private int[][] pixelCounts;
    private int[] darkestPixelCache;

    public PixelTargetDarkestArea(IPlottingTools tools, IPixelData data){
        this.tools = tools;
        this.data = data;
    }

    private void createTiles(){
        totalSamplesX = Math.max(1, getWidth()/getSampleWidth());
        totalSamplesY = Math.max(1, getHeight()/getSampleHeight());

        tileSamples = new double[totalSamplesX][totalSamplesY];
        pixelCounts = new int[totalSamplesX][totalSamplesY];

        for(int sampleX = 0; sampleX < totalSamplesX; sampleX++) {
            for (int sampleY = 0; sampleY < totalSamplesY; sampleY++) {

                int startX = sampleX * getSampleWidth();
                int endX = startX + getSampleWidth();

                int startY = sampleY*getSampleHeight();
                int endY = startY + getSampleHeight();

                if(sampleX == getWidth()/getSampleWidth()-1){
                    endX = getWidth();
                }

                if(sampleY == getHeight()/getSampleHeight()-1){
                    endY = getHeight();
                }

                double tileSample = 0;
                int pixelCount = 0;

                for(int x = startX; x < endX; x ++){
                    for(int y = startY; y < endY; y ++){
                        if(tools.withinPlottableArea(x, y)){
                            int c = data.getLuminance(x, y);
                            tileSample += c;
                            pixelCount++;
                        }
                    }
                }
                tileSamples[sampleX][sampleY] = tileSample;
                pixelCounts[sampleX][sampleY] = pixelCount;
            }
        }

        this.data.attachLuminanceDataListener(this);
    }

    @Override
    public void onChange(int x, int y, int oldValue, int newValue) {
        if(tileSamples == null){
            return;
        }
        if(tools.withinPlottableArea(x, y)) {
            int tileX = Math.min(totalSamplesX - 1, getTileX(x, y));
            int tileY = Math.min(totalSamplesY - 1, getTileY(x, y));
            tileSamples[tileX][tileY] += newValue - oldValue;
            if (darkestPixelCache != null && x == darkestPixelCache[0] && y == darkestPixelCache[1]) {
                darkestPixelCache = null;
            }
        }
    }

    public int[] getNextDarkestPixel(boolean remove) {

        // recover the cached darkest pixel, primarily for use with ColourMatch, as the getNextDarkestPixel() method is frequently called without changing the underlying data
        if(darkestPixelCache != null){
            return darkestPixelCache;
        }

        if(tileSamples == null){
            createTiles();
        }

        //// 1) FIND THE DARKEST TILE \\\\

        double darkestSample = Double.MAX_VALUE;
        int darkestSampleX = -1;
        int darkestSampleY = -1;

        for(int sampleX = 0; sampleX < totalSamplesX; sampleX++) {
            for (int sampleY = 0; sampleY < totalSamplesY; sampleY++) {
                double sample = tileSamples[sampleX][sampleY];
                int pixelCount = pixelCounts[sampleX][sampleY];

                // When using Soft Clip, it's possible that no pixels are being observed in a given area.
                if(pixelCount == 0){
                    continue;
                }

                double average = (int) (sample/pixelCount);
                if((darkestSampleX == -1 || average < darkestSample)){
                    darkestSample = average;
                    darkestSampleX = sampleX;
                    darkestSampleY = sampleY;
                }
            }
        }

        //// 2) FIND THE DARKEST PIXEL IN THAT TILE \\\\

        int startX = darkestSampleX * getSampleWidth();
        int endX = startX + getSampleWidth();

        int startY = darkestSampleY*getSampleHeight();
        int endY = startY + getSampleHeight();

        if(darkestSampleX == getWidth()/getSampleWidth()-1){
            endX = getWidth();
        }

        if(darkestSampleY == getHeight()/getSampleHeight()-1){
            endY = getHeight();
        }

        int darkestPixel = Integer.MAX_VALUE;
        int darkestPixelX = -1;
        int darkestPixelY = -1;

        for(int x = startX; x < endX; x ++){
            for(int y = startY; y < endY; y ++){
                if(tools.withinPlottableArea(x, y)) {
                    int luminance = data.getLuminance(x, y);
                    if (darkestPixelX == -1 || luminance < darkestPixel) {
                        darkestPixel = luminance;
                        darkestPixelX = x;
                        darkestPixelY = y;
                    }
                }
            }
        }
        return darkestPixelCache = new int[]{darkestPixelX, darkestPixelY, darkestPixel};
    }

    public int getWidth(){
        return data.getWidth();
    }

    public int getHeight(){
        return data.getHeight();
    }

    public int getSampleWidth(){
        return Math.min(getWidth(), sampleWidth);
    }

    public int getSampleHeight(){
        return Math.min(getHeight(), sampleHeight);
    }

    public int getTileWidth(){
        return (getWidth()/getSampleWidth());
    }

    public int getTileHeight(){
        return (getHeight()/sampleHeight);
    }

    public int getTileX(int x, int y){
        return (x/sampleWidth);
    }

    public int getTileY(int x, int y){
        return (y/sampleHeight);
    }
}
