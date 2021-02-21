package drawingbot.pfm;

import drawingbot.api.IPlottingTask;
import drawingbot.image.ImageTools;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.AlgorithmHelper;

public class PFMLines extends AbstractDarkestPFM{

    public int adjustbrightness;        // How fast it moves from dark to light, over-draw
    //public float desired_brightness;    // How long to process.  You can always stop early with "s" key

    public int tests;                   // Reasonable values:  13 for development, 720 for final

    protected int maxLines;

    protected float initialProgress;
    protected float progress;

    public PFMLines() {
        super();
        tests = 360;
        adjustbrightness = 30;
        sampleHeight = 1;
        sampleWidth = 1;
        maxLines = 5000;
    }

    @Override
    public void init(IPlottingTask task) {
        super.init(task);
        task.useCustomARGB(true);
    }

    @Override
    public void doProcess(IPlottingTask task) {
        for(int i = 0; i < maxLines; i ++){
            findDarkestPixel(task.getPixelData());
            int startX = darkest_x;
            int startY = darkest_y;
            float darkestLineAvg = 0;
            int[] line = null;

            for (int d = 0; d < tests; d ++) {
                count_pixels = 0;
                sum_luminance = 0;
                int[] testLine = getIntersectingLine(task.getPixelData(), startX, startY, randomSeed(0, 360));
                AlgorithmHelper.bresenham(testLine[0], testLine[1], testLine[2], testLine[3], (x,y) -> bresenhamTest(task.getPixelData(), x, y));
                float averageBrightness = (float) sum_luminance /(float) count_pixels;

                if(line == null || averageBrightness < darkestLineAvg){
                    darkestLineAvg = averageBrightness;
                    line = testLine;
                }
            }

            task.openPath();
            task.addToPath(line[0], line[1]);
            task.setCustomARGB(ImageTools.getARGB(adjustbrightness, adjustbrightness, adjustbrightness, 50));
            task.addToPath(line[2], line[3]);
            task.closePath();

            bresenhamLighten(task, task.getPixelData(), line[0], line[1], line[2], line[3], adjustbrightness);
            progress = (float)i / maxLines;

            if(task.isFinished()){
                break;
            }
        }
        task.finishProcess();
    }

}
