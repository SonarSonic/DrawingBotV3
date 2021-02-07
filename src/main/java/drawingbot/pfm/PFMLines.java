package drawingbot.pfm;

import drawingbot.DrawingBotV3;
import drawingbot.api.IPlottingTask;
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
    public void doProcess(IPlottingTask task) {
        for(int i = 0; i < maxLines; i ++){
            findDarkestPixel(task.getPixelData());
            int startX = darkest_x;
            int startY = darkest_y;
            float darkestLineAvg = 0;
            int[] line = null;

            for (int d = 0; d < tests; d ++) {
                count_brightness = 0;
                sum_brightness = 0;
                int[] testLine = getFullLine(task.getPixelData(), startX, startY, randomSeed(0, 360));
                AlgorithmHelper.bresenham(testLine[0], testLine[1], testLine[2], testLine[3], (x,y) -> bresenhamTest(task.getPixelData(), x, y));
                float averageBrightness = (float)sum_brightness/(float)count_brightness;

                if(line == null || averageBrightness < darkestLineAvg){
                    darkestLineAvg = averageBrightness;
                    line = testLine;
                }
            }

            task.moveAbsolute(line[0], line[1]);
            task.movePenDown();
            task.moveAbsolute(line[2], line[3]);
            task.movePenUp();

            ((PlottingTask)task).plottedDrawing.plottedLines.get(((PlottingTask)task).plottedDrawing.plottedLines.size()-1).rgba = DrawingBotV3.INSTANCE.color(adjustbrightness, adjustbrightness, adjustbrightness, 50);
            bresenhamLighten(task.getPixelData(), line[0], line[1], line[2], line[3], adjustbrightness);
            progress = (float)i / maxLines;

            if(task.isFinished()){
                break;
            }
        }
        task.finishProcess();
    }

}
