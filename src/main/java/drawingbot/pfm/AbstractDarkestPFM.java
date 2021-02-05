package drawingbot.pfm;

import drawingbot.utils.AlgorithmHelper;
import drawingbot.image.RawLuminanceData;
import drawingbot.plotting.PlottingTask;

import java.util.ArrayList;
import java.util.List;

import static processing.core.PApplet.*;
import static processing.core.PApplet.constrain;

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

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void findDarkestArea(RawLuminanceData rawBrightnessData) {
        int totalSamplesX = rawBrightnessData.getWidth()/sampleWidth;
        int totalSamplesY = rawBrightnessData.getHeight()/sampleHeight;

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
                        sampleBrightness += rawBrightnessData.getBrightness(x, y);
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
    public void findDarkestPixel(RawLuminanceData rawBrightnessData){
        List<Integer> pixelLocs = new ArrayList<>();
        int brightness = rawBrightnessData.data[0];

        for(int i = 0; i < rawBrightnessData.data.length; i ++) {
            int c = rawBrightnessData.data[i];
            if(c == brightness) {
                pixelLocs.add(i);
            }
            if(c < brightness) {
                pixelLocs.clear();
                pixelLocs.add(i);
                brightness = c;
            }
        }

        int point = pixelLocs.get(randomSeed.nextInt(pixelLocs.size()));
        darkest_x = point % rawBrightnessData.width;
        darkest_y = (point - darkest_x) / rawBrightnessData.width;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void bresenhamAvgBrightness(RawLuminanceData rawBrightnessData, int x0, int y0, float distance, float degree) {
        sum_brightness = 0;
        count_brightness = 0;
        int x1, y1;
        x1 = (int)(cos(radians(degree))*distance) + x0;
        y1 = (int)(sin(radians(degree))*distance) + y0;
        x0 = constrain(x0, 0, rawBrightnessData.width-1);
        y0 = constrain(y0, 0, rawBrightnessData.height-1);
        x1 = constrain(x1, 0, rawBrightnessData.width-1);
        y1 = constrain(y1, 0, rawBrightnessData.height-1);

        AlgorithmHelper.bresenham(x0, y0, x1, y1, (x, y) -> bresenhamTest(rawBrightnessData, x, y));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void bresenhamTest(RawLuminanceData rawBrightnessData, int x, int y){
        sum_brightness += rawBrightnessData.getBrightness(x, y);
        count_brightness++;
        if ((float)sum_brightness / (float)count_brightness < darkest_neighbor) {
            darkest_x = x;
            darkest_y = y;
            darkest_neighbor = (float)sum_brightness / (float)count_brightness;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void bresenhamLighten(RawLuminanceData rawBrightnessData, int x0, int y0, int x1, int y1, int adjustbrightness) {
        AlgorithmHelper.bresenham(x0, y0, x1, y1, (x, y) -> rawBrightnessData.brightenPixel(x, y, adjustbrightness));
    }
}
