package drawingbot.pfm;

import drawingbot.helpers.AlgorithmHelper;
import drawingbot.helpers.RawBrightnessData;
import drawingbot.plotting.PlottingTask;

import static processing.core.PApplet.*;

public abstract class AbstractSketchPFM extends AbstractPFM {

    public int squiggle_length;         // How often to lift the pen
    public int adjustbrightness;        // How fast it moves from dark to light, over-draw
    public float desired_brightness;    // How long to process.  You can always stop early with "s" key

    public int tests;                   // Reasonable values:  13 for development, 720 for final
    public int line_length;             // Reasonable values:  3 through 100 - Impacts the amount of lines drawn

    public int squiggle_count;
    public int darkest_x;
    public int darkest_y;
    public float darkest_value;
    public float darkest_neighbor;

    protected float initialProgress;
    protected float progress;

    ///bresenham calculations
    private int sum_brightness = 0;
    private int count_brightness = 0;

    public RawBrightnessData rawBrightnessData;

    public AbstractSketchPFM(PlottingTask task) {
        super(task);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public float progress() {
        return progress;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void findPath() {
        int x, y;
        findDarkestArea();

        x = darkest_x;
        y = darkest_y;
        squiggle_count++;

        findDarkestNeighbour(x, y);

        task.moveAbs(0, darkest_x, darkest_y);
        task.penDown();

        for (int s = 0; s < squiggle_length; s++) {
            findDarkestNeighbour(x, y);
            bresenhamLighten(x, y, darkest_x, darkest_y, adjustbrightness);
            task.moveAbs(0, darkest_x, darkest_y);
            x = darkest_x;
            y = darkest_y;
        }
        task.penUp();

        float avgBrightness = rawBrightnessData.getAverageBrightness();
        progress = (avgBrightness-initialProgress) / (desired_brightness-initialProgress);
        if(avgBrightness > desired_brightness){
            finish();
        }

    }

    protected abstract void findDarkestNeighbour(int x, int y);

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void findDarkestArea() {
        int sampleWidth = 10;
        int sampleHeight = 10;

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

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void bresenhamAvgBrightness(int x0, int y0, float distance, float degree) {
        sum_brightness = 0;
        count_brightness = 0;
        int x1, y1;

        x1 = (int)(cos(radians(degree))*distance) + x0;
        y1 = (int)(sin(radians(degree))*distance) + y0;
        x0 = constrain(x0, 0, task.getPlottingImage().width-1);
        y0 = constrain(y0, 0, task.getPlottingImage().height-1);
        x1 = constrain(x1, 0, task.getPlottingImage().width-1);
        y1 = constrain(y1, 0, task.getPlottingImage().height-1);

        AlgorithmHelper.bresenham(x0, y0, x1, y1, this::bresenhamTest);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void bresenhamTest(int x, int y){
        sum_brightness += rawBrightnessData.getBrightness(x, y);
        count_brightness++;
        if ((float)sum_brightness / (float)count_brightness < darkest_neighbor) {
            darkest_x = x;
            darkest_y = y;
            darkest_neighbor = (float)sum_brightness / (float)count_brightness;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void bresenhamLighten(int x0, int y0, int x1, int y1, int adjustbrightness) {
        AlgorithmHelper.bresenham(x0, y0, x1, y1, (x, y) -> rawBrightnessData.brightenPixel(x, y, adjustbrightness * 5));
    }
}
