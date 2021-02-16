package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.image.ImageTools;
import drawingbot.utils.AlgorithmHelper;
import drawingbot.utils.Utils;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDarkestPFM extends AbstractPFM {

    protected int sampleWidth = 10;
    protected int sampleHeight = 10;

    protected int darkest_x;
    protected int darkest_y;
    protected float darkest_value;
    protected float darkest_neighbor;

    ///bresenham calculations
    protected int sum_brightness = 0;
    protected int count_brightness = 0;

    @Override
    public int getColourMode() {
        return 2;
    }

    @Override
    public int getTransparentARGB() {
        return ImageTools.getARGB(0, 255, 255, 255);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void findDarkestArea(IPixelData pixels) {
        int totalSamplesX = pixels.getWidth()/sampleWidth;
        int totalSamplesY = pixels.getHeight()/sampleHeight;

        int darkestSampleX = 0;
        int darkestSampleY = 0;

        darkest_value = 1000;

        for(int sampleX = 0; sampleX < totalSamplesX; sampleX++){
            for(int sampleY = 0; sampleY < totalSamplesY; sampleY++){
                int startX = sampleX*sampleWidth;
                int endX = startX + sampleWidth;

                int startY = sampleY*sampleHeight;
                int endY = startY + sampleHeight;
                float sampleBrightness = 0;
                for(int x = startX; x < endX; x++){
                    for(int y = startY; y < endY; y++){
                        sampleBrightness += getBrightnessAlphaTest(pixels, x, y);
                    }
                }
                float avgBrightness = sampleBrightness / (sampleWidth*sampleHeight);
                if (avgBrightness < darkest_value) {
                    darkest_value = avgBrightness;
                    darkestSampleX = startX;
                    darkestSampleY = startY;
                }
            }
        }

        darkest_x = darkestSampleX + randomSeed.nextInt(sampleWidth);
        darkest_y = darkestSampleY + randomSeed.nextInt(sampleHeight);
    }

    /**returns a random pixel of the darkest pixels found*/
    public void findDarkestPixel(IPixelData pixels){
        List<Pair<Integer, Integer>> points = new ArrayList<>();
        int brightness = getBrightnessAlphaTest(pixels, 0,0);


        for(int x = 0; x < pixels.getWidth(); x ++){
            for(int y = 0; y < pixels.getHeight(); y ++){
                int c = getBrightnessAlphaTest(pixels, x, y);
                if(c == brightness) {
                    points.add(new Pair<>(x, y));
                }
                if(c < brightness) {
                    points.clear();
                    points.add(new Pair<>(x, y));
                    brightness = c;
                }
            }
        }

        Pair<Integer, Integer> point = points.get(randomSeed.nextInt(points.size()));
        darkest_x = point.getKey();
        darkest_y = point.getValue();
    }

    public int[] getFullLine(IPixelData pixels, int x0, int y0, float degree){ //TODO FIX TENDENCY FOR PFMS TO GO TO CORNERS WITH LONG LINES
        double minX, minY, maxX, maxY;

        double maxWidth = pixels.getWidth()-1;
        double maxHeight = pixels.getHeight()-1;

        double slope = Math.tan(degree);

        double leftYIntercept = y0 - slope*x0;
        if(leftYIntercept >= maxHeight){
            minY = maxHeight;
            minX = ((maxHeight-leftYIntercept)/slope);
        }else if(leftYIntercept < 0){
            minY = 0;
            minX = ((-leftYIntercept)/slope);
        }else{
            minY = leftYIntercept;
            minX = 0;
        }

        double rightYIntercept = y0 - slope*(x0-maxWidth);

        if(rightYIntercept >= maxHeight){
            maxY = maxHeight-1;
            maxX = ((maxHeight-rightYIntercept)/slope)  + maxWidth+1;
        }else if(rightYIntercept < 0){
            maxY = 0;
            maxX = ((-rightYIntercept)/slope) + maxWidth+1;
        }else{
            maxY = rightYIntercept;
            maxX = maxWidth;
        }

        return new int[]{(int)Math.floor(minX), (int)Math.floor(minY), (int)Math.floor(maxX), (int)Math.floor(maxY)};
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void bresenhamAvgBrightness(IPixelData pixels, int x0, int y0, float distance, float degree) {
        sum_brightness = 0;
        count_brightness = 0;

        int x1, y1;
        x1 = (int)(Math.cos(Math.toRadians(degree))*distance) + x0;
        y1 = (int)(Math.sin(Math.toRadians(degree))*distance) + y0;
        x0 = Utils.clamp(x0, 0, pixels.getWidth()-1);
        y0 = Utils.clamp(y0, 0, pixels.getHeight()-1);
        x1 = Utils.clamp(x1, 0, pixels.getWidth()-1);
        y1 = Utils.clamp(y1, 0, pixels.getHeight()-1);

        AlgorithmHelper.bresenham(x0, y0, x1, y1, (x, y) -> bresenhamTest(pixels, x, y));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void bresenhamTest(IPixelData pixels, int x, int y){
        sum_brightness += getBrightnessAlphaTest(pixels, x, y);
        count_brightness++;
        if ((float)sum_brightness / (float)count_brightness < darkest_neighbor) {
            darkest_x = x;
            darkest_y = y;
            darkest_neighbor = (float)sum_brightness / (float)count_brightness;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void bresenhamLighten(IPixelData pixels, int x0, int y0, int x1, int y1, int adjustbrightness) {
        AlgorithmHelper.bresenham(x0, y0, x1, y1, (x, y) -> pixels.adjustBrightness(x, y, adjustbrightness));
    }

    public int getBrightnessAlphaTest(IPixelData data, int x, int y){
        return data.getBrightness(x, y);
    }
}
